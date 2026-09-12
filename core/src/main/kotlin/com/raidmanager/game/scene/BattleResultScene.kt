package com.raidmanager.game.scene

import com.raidmanager.game.scene.SceneStyle.BattleResult as Style
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
        val margin = Gdx.graphics.width * Style.MARGIN_RATIO
        val outcomeColor = if (result.victory) Style.VICTORY_COLOR else Style.DEFEAT_COLOR
        val outcome = if (result.victory) "RAID VICTORY" else "RAID DEFEAT"
        Ui.text(assets, batch, outcome, margin, Gdx.graphics.height - Style.TITLE_TOP, Style.TITLE_SCALE, outcomeColor)
        Ui.text(
            assets,
            batch,
            "${dungeon.name}  /  ${"%.1f".format(result.elapsedTime)} SEC",
            margin,
            Gdx.graphics.height - Style.SUMMARY_TOP,
            Style.SUMMARY_SCALE,
            Color.LIGHT_GRAY,
        )

        Ui.text(assets, batch, "COMBAT STATISTICS", margin, Gdx.graphics.height - Style.STATS_TOP, Style.LABEL_SCALE, Style.STATS_COLOR)
        result.members.forEachIndexed { index, member ->
            val y = Gdx.graphics.height - Style.MEMBER_TOP - index * Style.MEMBER_STEP
            val status = if (member.survived) "ALIVE" else "DOWN"
            Ui.text(assets, batch, member.character.name, margin, y, Style.LABEL_SCALE)
            Ui.text(
                assets,
                batch,
                "DMG ${member.damageDealt.toInt()}   HEAL ${member.healingDone.toInt()}   " +
                    "TAKEN ${member.damageTaken.toInt()}   SKILLS ${member.skillUses}   $status",
                margin + Style.STATS_OFFSET,
                y,
                Style.STATS_SCALE,
                if (member.survived) Color.LIGHT_GRAY else Color.SALMON,
            )
        }

        Ui.text(assets, batch, "ANALYSIS", margin, Style.ANALYSIS_TITLE_Y, Style.LABEL_SCALE, Style.ANALYSIS_COLOR)
        Ui.text(assets, batch, result.analysis, margin, Style.ANALYSIS_Y, Style.ANALYSIS_SCALE, Color.LIGHT_GRAY)

        val retry = retryBounds()
        Ui.button(assets, batch, "RETRY", retry.x, retry.y, retry.width, retry.height)
        val change = changeBounds()
        Ui.button(assets, batch, "CHANGE RAID", change.x, change.y, change.width, change.height, selected = true)
    }

    private fun retryBounds(): Bounds {
        val margin = Gdx.graphics.width * Style.MARGIN_RATIO
        val gap = Style.BUTTON_GAP
        val width = (Gdx.graphics.width - margin * 2f - gap) / 2f
        return Bounds(margin, Style.BUTTON_Y, width, Style.BUTTON_HEIGHT)
    }

    private fun changeBounds(): Bounds {
        val retry = retryBounds()
        return Bounds(retry.x + retry.width + Style.BUTTON_GAP, retry.y, retry.width, retry.height)
    }

}
