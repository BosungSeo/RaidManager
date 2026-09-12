package com.raidmanager.game.scene

import com.raidmanager.game.scene.SceneStyle.DungeonSelect as Style
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.DungeonDefinition
import com.raidmanager.game.model.RaidFormation
import java.util.ArrayDeque

class DungeonSelectScene(
    private val assets: GameAssets,
    private val formation: RaidFormation,
    private val dungeons: List<DungeonDefinition>,
    private val onStart: (RaidFormation, DungeonDefinition) -> Unit,
    private val onBack: (RaidFormation) -> Unit,
) : InputAdapter(), Scene {
    private sealed interface Command {
        data class Select(val index: Int) : Command
        data object Back : Command
    }

    private val pendingCommands = ArrayDeque<Command>()

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        dungeons.indices.firstOrNull { index ->
            val bounds = dungeonBounds(index)
            Ui.contains(screenX, screenY, bounds.x, bounds.y, bounds.width, bounds.height, Gdx.graphics.height)
        }?.let {
            pendingCommands.addLast(Command.Select(it))
            return true
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode != Input.Keys.ESCAPE) return false
        pendingCommands.addLast(Command.Back)
        return true
    }

    override fun updateGame(delta: Float) {
        while (pendingCommands.isNotEmpty()) {
            when (val command = pendingCommands.removeFirst()) {
                Command.Back -> onBack(formation)
                is Command.Select -> onStart(formation, dungeons[command.index])
            }
            pendingCommands.clear()
        }
    }

    override fun renderGame(batch: SpriteBatch) {
        val margin = Gdx.graphics.width * Style.MARGIN_RATIO
        Ui.text(assets, batch, "SELECT DUNGEON", margin, Gdx.graphics.height - Style.TITLE_TOP, Style.TITLE_SCALE)
        Ui.text(
            assets,
            batch,
            "RAID: ${formation.members.joinToString(" / ") { it.name }}   MONSTERS: ${formation.monsterCount}",
            margin,
            Gdx.graphics.height - Style.SUMMARY_TOP,
            Style.SUMMARY_SCALE,
            Color.LIGHT_GRAY,
        )

        dungeons.forEachIndexed { index, dungeon ->
            val bounds = dungeonBounds(index)
            Ui.button(assets, batch, dungeon.name, bounds.x, bounds.y, bounds.width, bounds.height)
            Ui.text(
                assets,
                batch,
                dungeon.description,
                bounds.x + Style.DESCRIPTION_LEFT,
                bounds.y + Style.DESCRIPTION_BOTTOM,
                Style.DESCRIPTION_SCALE,
                Color.LIGHT_GRAY,
            )
        }
        Ui.text(assets, batch, "ESC: CHANGE RAID", margin, Style.FOOTER_Y, Style.FOOTER_SCALE, Color.GRAY)
    }

    private fun dungeonBounds(index: Int): Bounds {
        val margin = Gdx.graphics.width * Style.MARGIN_RATIO
        val width = Gdx.graphics.width - margin * 2f
        val height = Style.CARD_HEIGHT
        return Bounds(margin, Gdx.graphics.height - Style.CARD_TOP - index * Style.CARD_STEP, width, height)
    }

}
