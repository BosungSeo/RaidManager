package com.raidmanager.game.scene

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
    private val pendingCommands = ArrayDeque<Int>()

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        dungeons.indices.firstOrNull { index ->
            val bounds = dungeonBounds(index)
            Ui.contains(screenX, screenY, bounds.x, bounds.y, bounds.width, bounds.height, Gdx.graphics.height)
        }?.let {
            pendingCommands.addLast(it)
            return true
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode != Input.Keys.ESCAPE) return false
        onBack(formation)
        return true
    }

    override fun updateGame(delta: Float) {
        while (pendingCommands.isNotEmpty()) {
            onStart(formation, dungeons[pendingCommands.removeFirst()])
        }
    }

    override fun renderGame(batch: SpriteBatch) {
        val margin = Gdx.graphics.width * 0.07f
        Ui.text(assets, batch, "SELECT DUNGEON", margin, Gdx.graphics.height - 42f, 1.35f)
        Ui.text(
            assets,
            batch,
            "RAID: ${formation.members.joinToString(" / ") { it.name }}   MONSTERS: ${formation.monsterCount}",
            margin,
            Gdx.graphics.height - 76f,
            0.72f,
            Color.LIGHT_GRAY,
        )

        dungeons.forEachIndexed { index, dungeon ->
            val bounds = dungeonBounds(index)
            Ui.button(assets, batch, dungeon.name, bounds.x, bounds.y, bounds.width, bounds.height)
            Ui.text(assets, batch, dungeon.description, bounds.x + 16f, bounds.y + 24f, 0.68f, Color.LIGHT_GRAY)
        }
        Ui.text(assets, batch, "ESC: CHANGE RAID", margin, 28f, 0.65f, Color.GRAY)
    }

    private fun dungeonBounds(index: Int): Bounds {
        val margin = Gdx.graphics.width * 0.07f
        val width = Gdx.graphics.width - margin * 2f
        val height = 96f
        return Bounds(margin, Gdx.graphics.height - 190f - index * 116f, width, height)
    }

    private data class Bounds(val x: Float, val y: Float, val width: Float, val height: Float)
}
