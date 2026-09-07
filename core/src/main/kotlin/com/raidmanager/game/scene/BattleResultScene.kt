package com.raidmanager.game.scene

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.BattleResult
import com.raidmanager.game.model.DungeonDefinition
import com.raidmanager.game.model.RaidFormation
import java.util.ArrayDeque

class BattleResultScene(
    private val assets: GameAssets,
    private val formation: RaidFormation,
    private val dungeon: DungeonDefinition,
    private val result: BattleResult,
    private val onRetry: (RaidFormation, DungeonDefinition) -> Unit,
    private val onChangeRaid: (RaidFormation) -> Unit,
) : InputAdapter(), Scene {
    private enum class ResultCommand {
        RETRY,
        CHANGE_RAID,
    }

    private val pendingCommands = ArrayDeque<ResultCommand>()

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val retry = retryBounds()
        if (Ui.contains(screenX, screenY, retry.x, retry.y, retry.width, retry.height, Gdx.graphics.height)) {
            pendingCommands.addLast(ResultCommand.RETRY)
            return true
        }

        val change = changeBounds()
        if (Ui.contains(screenX, screenY, change.x, change.y, change.width, change.height, Gdx.graphics.height)) {
            pendingCommands.addLast(ResultCommand.CHANGE_RAID)
            return true
        }
        return false
    }

    override fun updateGame(delta: Float) {
        while (pendingCommands.isNotEmpty()) {
            when (pendingCommands.removeFirst()) {
                ResultCommand.RETRY -> onRetry(formation, dungeon)
                ResultCommand.CHANGE_RAID -> onChangeRaid(formation)
            }
        }
    }

    override fun renderGame(batch: SpriteBatch) {
        val margin = Gdx.graphics.width * 0.07f
        val outcomeColor = if (result.victory) Color(0.4f, 0.9f, 0.55f, 1f) else Color(1f, 0.42f, 0.35f, 1f)
        val outcome = if (result.victory) "RAID VICTORY" else "RAID DEFEAT"
        Ui.text(assets, batch, outcome, margin, Gdx.graphics.height - 44f, 1.4f, outcomeColor)
        Ui.text(
            assets,
            batch,
            "${dungeon.name}  /  ${"%.1f".format(result.elapsedTime)} SEC",
            margin,
            Gdx.graphics.height - 78f,
            0.72f,
            Color.LIGHT_GRAY,
        )

        Ui.text(assets, batch, "COMBAT STATISTICS", margin, Gdx.graphics.height - 130f, 0.82f, Color(0.55f, 0.78f, 1f, 1f))
        result.members.forEachIndexed { index, member ->
            val y = Gdx.graphics.height - 172f - index * 54f
            val status = if (member.survived) "ALIVE" else "DOWN"
            Ui.text(assets, batch, member.character.name, margin, y, 0.82f)
            Ui.text(
                assets,
                batch,
                "DMG ${member.damageDealt.toInt()}   HEAL ${member.healingDone.toInt()}   " +
                    "TAKEN ${member.damageTaken.toInt()}   SKILLS ${member.skillUses}   $status",
                margin + 120f,
                y,
                0.66f,
                if (member.survived) Color.LIGHT_GRAY else Color.SALMON,
            )
        }

        Ui.text(assets, batch, "ANALYSIS", margin, 248f, 0.82f, Color(1f, 0.78f, 0.38f, 1f))
        Ui.text(assets, batch, result.analysis, margin, 218f, 0.7f, Color.LIGHT_GRAY)

        val retry = retryBounds()
        Ui.button(assets, batch, "RETRY", retry.x, retry.y, retry.width, retry.height)
        val change = changeBounds()
        Ui.button(assets, batch, "CHANGE RAID", change.x, change.y, change.width, change.height, selected = true)
    }

    private fun retryBounds(): Bounds {
        val margin = Gdx.graphics.width * 0.07f
        val gap = 14f
        val width = (Gdx.graphics.width - margin * 2f - gap) / 2f
        return Bounds(margin, 64f, width, 58f)
    }

    private fun changeBounds(): Bounds {
        val retry = retryBounds()
        return Bounds(retry.x + retry.width + 14f, retry.y, retry.width, retry.height)
    }

    private data class Bounds(val x: Float, val y: Float, val width: Float, val height: Float)
}
