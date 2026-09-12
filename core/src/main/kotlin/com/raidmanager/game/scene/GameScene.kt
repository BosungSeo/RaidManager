package com.raidmanager.game.scene

import com.raidmanager.game.scene.SceneStyle.Game as Style
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.BattleResult
import com.raidmanager.game.model.BattleSimulator
import com.raidmanager.game.model.DungeonDefinition
import com.raidmanager.game.model.RaidFormation
import java.util.ArrayDeque

class GameScene(
    private val assets: GameAssets,
    formation: RaidFormation,
    private val dungeon: DungeonDefinition,
    private val onFinished: (RaidFormation, DungeonDefinition, BattleResult) -> Unit,
    private val onExit: (RaidFormation) -> Unit,
) : InputAdapter(), Scene {
    private enum class BattleCommand {
        EXIT,
        RESULTS,
    }

    private val formation = formation
    private val simulator = BattleSimulator(formation, dungeon)
    private val stage = BattleStage(assets, formation)
    private val audio = BattleAudio(assets, formation)
    private val stageProjection = Matrix4().setToOrtho2D(0f, 0f, Style.WIDTH, Style.HEIGHT)
    private val savedProjection = Matrix4()
    private val pendingCommands = ArrayDeque<BattleCommand>()

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE -> pendingCommands.addLast(BattleCommand.EXIT)
            Input.Keys.ENTER, Input.Keys.SPACE -> pendingCommands.addLast(BattleCommand.RESULTS)
            else -> return false
        }
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val bounds = resultButtonBounds()
        val canOpenResults = simulator.snapshot().finished && Ui.contains(
            screenX,
            screenY,
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height,
            Gdx.graphics.height,
        )
        if (!canOpenResults) return false

        pendingCommands.addLast(BattleCommand.RESULTS)
        return true
    }

    override fun updateGame(delta: Float) {
        simulator.update(delta)
        val cues = simulator.drainCombatCues()
        val snapshot = simulator.snapshot()
        stage.update(delta, cues, snapshot)
        audio.update(delta, cues, snapshot)
        while (pendingCommands.isNotEmpty()) {
            when (pendingCommands.removeFirst()) {
                BattleCommand.EXIT -> {
                    assets.stopBattleSounds()
                    onExit(formation)
                }
                BattleCommand.RESULTS -> if (simulator.snapshot().finished) {
                    assets.stopBattleSounds()
                    onFinished(formation, dungeon, simulator.result())
                }
            }
        }
    }

    override fun renderGame(batch: SpriteBatch) {
        val snapshot = simulator.snapshot()
        savedProjection.set(batch.projectionMatrix)
        batch.projectionMatrix = stageProjection
        stage.render(batch, snapshot, dungeon)
        Ui.text(assets, batch, "COMBAT EVENTS", Style.EVENT_X, Style.EVENT_TITLE_Y, Style.LABEL_SCALE, Color.LIGHT_GRAY)
        simulator.recentEvents(Style.EVENT_COUNT).forEachIndexed { index, event ->
            Ui.text(assets, batch, "${"%.1f".format(event.time)}  ${event.message}",
                Style.EVENT_X, Style.EVENT_Y - index * Style.EVENT_STEP, Style.EVENT_SCALE, Color.LIGHT_GRAY)
        }
        if (snapshot.finished) {
            Ui.button(assets, batch,
                if (snapshot.victory) "VICTORY - VIEW RESULTS" else "DEFEAT - VIEW RESULTS",
                Style.RESULT_X, Style.RESULT_Y, Style.RESULT_WIDTH, Style.RESULT_HEIGHT, selected = snapshot.victory)
        } else {
            Ui.text(assets, batch, "AUTO BATTLE  /  ESC: RETURN", Style.AUTO_X, Style.AUTO_Y, Style.LABEL_SCALE, Color.LIGHT_GRAY)
            if (snapshot.abilityCastRemaining > 0f) {
                Ui.text(assets, batch, "CASTING... ${"%.1f".format(snapshot.abilityCastRemaining)} SEC  /  INTERRUPT",
                    Style.RESULT_X, Style.CAST_Y, Style.CAST_SCALE, Color.SALMON)
            }
        }
        batch.projectionMatrix = savedProjection
    }

    /** 고정 전투 좌표를 실제 창의 가로·세로 배율로 변환해 버튼 입력 영역을 맞춘다. */
    private fun resultButtonBounds(): Bounds {
        val scaleX = Gdx.graphics.width / Style.WIDTH
        val scaleY = Gdx.graphics.height / Style.HEIGHT
        return Bounds(Style.RESULT_X * scaleX, Style.RESULT_Y * scaleY, Style.RESULT_WIDTH * scaleX, Style.RESULT_HEIGHT * scaleY)
    }

}
