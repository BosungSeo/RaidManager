package com.raidmanager.game.scene

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.CharacterDefinition
import com.raidmanager.game.model.RaidFormation
import java.util.ArrayDeque

class RaidSetupScene(
    private val assets: GameAssets,
    private val roster: List<CharacterDefinition>,
    initialFormation: RaidFormation?,
    private val onContinue: (RaidFormation) -> Unit,
    private val onBack: () -> Unit,
) : InputAdapter(), Scene {
    private sealed interface SetupCommand {
        data class Toggle(val index: Int) : SetupCommand
        data class MonsterCount(val delta: Int) : SetupCommand
        data object Continue : SetupCommand
        data object Back : SetupCommand
    }

    private val selectedIds = initialFormation?.members?.mapTo(mutableSetOf()) { it.id } ?: mutableSetOf()
    private var monsterCount = initialFormation?.monsterCount ?: RaidFormation.MIN_MONSTER_COUNT
    private val pendingCommands = ArrayDeque<SetupCommand>()
    private var message = "Select exactly ${RaidFormation.PARTY_SIZE} members"

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        roster.indices.firstOrNull { index ->
            val bounds = cardBounds(index)
            Ui.contains(screenX, screenY, bounds.x, bounds.y, bounds.width, bounds.height, Gdx.graphics.height)
        }?.let {
            pendingCommands.addLast(SetupCommand.Toggle(it))
            return true
        }

        val continueBounds = continueBounds()
        if (Ui.contains(
                screenX,
                screenY,
                continueBounds.x,
                continueBounds.y,
                continueBounds.width,
                continueBounds.height,
                Gdx.graphics.height,
            )
        ) {
            pendingCommands.addLast(SetupCommand.Continue)
            return true
        }
        val minus = monsterCountBounds(-1)
        val plus = monsterCountBounds(1)
        if (Ui.contains(screenX, screenY, minus.x, minus.y, minus.width, minus.height, Gdx.graphics.height)) {
            pendingCommands.addLast(SetupCommand.MonsterCount(-1))
            return true
        }
        if (Ui.contains(screenX, screenY, plus.x, plus.y, plus.width, plus.height, Gdx.graphics.height)) {
            pendingCommands.addLast(SetupCommand.MonsterCount(1))
            return true
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE -> pendingCommands.addLast(SetupCommand.Back)
            Input.Keys.ENTER -> pendingCommands.addLast(SetupCommand.Continue)
            else -> return false
        }
        return true
    }

    override fun updateGame(delta: Float) {
        while (pendingCommands.isNotEmpty()) {
            when (val command = pendingCommands.removeFirst()) {
                SetupCommand.Back -> onBack()
                SetupCommand.Continue -> continueToDungeon()
                is SetupCommand.Toggle -> toggle(command.index)
                is SetupCommand.MonsterCount -> adjustMonsterCount(command.delta)
            }
        }
    }

    override fun renderGame(batch: SpriteBatch) {
        Ui.text(assets, batch, "BUILD YOUR RAID", margin(), Gdx.graphics.height - 42f, 1.35f)
        Ui.text(assets, batch, message, margin(), Gdx.graphics.height - 74f, 0.78f, Color.LIGHT_GRAY)
        Ui.text(assets, batch, "MONSTERS", margin(), 126f, 0.78f, Color.LIGHT_GRAY)
        val minus = monsterCountBounds(-1)
        val plus = monsterCountBounds(1)
        Ui.button(assets, batch, "-", minus.x, minus.y, minus.width, minus.height, monsterCount > RaidFormation.MIN_MONSTER_COUNT)
        Ui.text(assets, batch, "$monsterCount", margin() + 86f, 143f, 0.95f, Color.WHITE)
        Ui.button(assets, batch, "+", plus.x, plus.y, plus.width, plus.height, monsterCount < RaidFormation.MAX_MONSTER_COUNT)

        roster.forEachIndexed { index, character ->
            val bounds = cardBounds(index)
            val selected = character.id in selectedIds
            Ui.button(assets, batch, "${character.name}  [${character.role}]", bounds.x, bounds.y, bounds.width, bounds.height, selected)
            Ui.text(
                assets,
                batch,
                "HP ${character.maxHp.toInt()}  ATK ${character.attackPower.toInt()}  ${character.skillName}",
                bounds.x + 12f,
                bounds.y + 16f,
                0.63f,
                Color.LIGHT_GRAY,
            )
        }

        val bounds = continueBounds()
        Ui.button(
            assets,
            batch,
            "CHOOSE DUNGEON",
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height,
            enabled = selectedIds.size == RaidFormation.PARTY_SIZE,
        )
        Ui.text(assets, batch, "ESC: MAIN MENU", margin(), 24f, 0.65f, Color.GRAY)
    }

    private fun toggle(index: Int) {
        val id = roster[index].id
        if (!selectedIds.remove(id)) {
            if (selectedIds.size >= RaidFormation.PARTY_SIZE) {
                message = "Raid is full. Deselect one member first."
                return
            }
            selectedIds += id
        }
        message = "${selectedIds.size} / ${RaidFormation.PARTY_SIZE} selected"
    }

    private fun continueToDungeon() {
        val members = roster.filter { it.id in selectedIds }
        if (members.size != RaidFormation.PARTY_SIZE) {
            message = "Select exactly ${RaidFormation.PARTY_SIZE} members"
            return
        }
        onContinue(RaidFormation(members, monsterCount))
    }

    private fun adjustMonsterCount(delta: Int) {
        monsterCount = (monsterCount + delta).coerceIn(RaidFormation.MIN_MONSTER_COUNT, RaidFormation.MAX_MONSTER_COUNT)
        message = "${selectedIds.size} / ${RaidFormation.PARTY_SIZE} selected · $monsterCount monster(s)"
    }

    private fun margin(): Float = Gdx.graphics.width * 0.07f

    private fun cardBounds(index: Int): Bounds {
        val gap = 12f
        val width = (Gdx.graphics.width - margin() * 2f - gap) / 2f
        val height = 74f
        val column = index % 2
        val row = index / 2
        val x = margin() + column * (width + gap)
        val y = Gdx.graphics.height - 170f - row * (height + gap)
        return Bounds(x, y, width, height)
    }

    private fun continueBounds(): Bounds {
        val width = 240f
        return Bounds(Gdx.graphics.width - margin() - width, 44f, width, 58f)
    }

    private fun monsterCountBounds(delta: Int): Bounds {
        val size = 42f
        val x = if (delta < 0) margin() else margin() + 130f
        return Bounds(x, 132f, size, 34f)
    }

    private data class Bounds(val x: Float, val y: Float, val width: Float, val height: Float)
}
