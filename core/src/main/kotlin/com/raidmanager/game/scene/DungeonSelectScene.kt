package com.raidmanager.game.scene

import com.raidmanager.game.scene.SceneStyle.DungeonSelect as Style
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.DungeonTimeSelection
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
        data class Time(val value: Int, val relative: Boolean) : Command
    }

    private val time = DungeonTimeSelection()
    private val timeButtons = listOf("-10s" to -10, "+10s" to 10, "1 MIN" to 60,
        "3 MIN" to 180, "5 MIN" to 300, "10 MIN" to 600)
    private fun timeBounds(index: Int): Bounds = Bounds(
        Gdx.graphics.width * Style.MARGIN_RATIO + index * 108f, 96f, 98f, 42f,
    )

    private val pendingCommands = ArrayDeque<Command>()

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        timeButtons.indices.firstOrNull { index ->
            val b = timeBounds(index)
            Ui.contains(screenX, screenY, b.x, b.y, b.width, b.height, Gdx.graphics.height)
        }?.let {
            pendingCommands.addLast(Command.Time(timeButtons[it].second, it < 2))
            return true
        }
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
                Command.Back -> { pendingCommands.clear(); onBack(formation); return }
                is Command.Select -> {
                    pendingCommands.clear()
                    onStart(formation, time.applyTo(dungeons[command.index]))
                    return
                }
                is Command.Time -> if (command.relative) time.adjust(command.value) else time.select(command.value)
            }
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
        Ui.text(assets, batch, "TIME LIMIT  ${time.label}  /  MAX 10:00", margin, 161f, 0.85f)
        timeButtons.forEachIndexed { index, (label, value) ->
            val b = timeBounds(index)
            val enabled = when (index) {
                0 -> time.seconds > DungeonTimeSelection.MIN_SECONDS
                1 -> time.seconds < DungeonTimeSelection.MAX_SECONDS
                else -> true
            }
            Ui.button(assets, batch, label, b.x, b.y, b.width, b.height,
                selected = index >= 2 && time.seconds == value, enabled = enabled)
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
