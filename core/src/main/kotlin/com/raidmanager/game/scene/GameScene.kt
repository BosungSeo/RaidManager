package com.raidmanager.game.scene

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
    private val stageProjection = Matrix4().setToOrtho2D(0f, 0f, 1280f, 720f)
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
        Ui.text(assets, batch, "COMBAT EVENTS", 44f, 150f, 0.75f, Color.LIGHT_GRAY)
        simulator.recentEvents(3).forEachIndexed { index, event ->
            Ui.text(assets, batch, "${"%.1f".format(event.time)}  ${event.message}",
                44f, 122f - index * 22f, 0.68f, Color.LIGHT_GRAY)
        }
        if (snapshot.finished) {
            Ui.button(assets, batch,
                if (snapshot.victory) "VICTORY - VIEW RESULTS" else "DEFEAT - VIEW RESULTS",
                770f, 55f, 440f, 64f, selected = snapshot.victory)
        } else {
            Ui.text(assets, batch, "AUTO BATTLE  /  ESC: RETURN", 820f, 95f, 0.75f, Color.LIGHT_GRAY)
            if (snapshot.abilityCastRemaining > 0f) {
                Ui.text(assets, batch, "CASTING... ${"%.1f".format(snapshot.abilityCastRemaining)} SEC  /  INTERRUPT",
                    770f, 125f, 0.7f, Color.SALMON)
            }
        }
        batch.projectionMatrix = savedProjection
    }

    private fun resultButtonBounds(): Bounds {
        val scaleX = Gdx.graphics.width / 1280f
        val scaleY = Gdx.graphics.height / 720f
        return Bounds(770f * scaleX, 55f * scaleY, 440f * scaleX, 64f * scaleY)
    }

    private data class Bounds(val x: Float, val y: Float, val width: Float, val height: Float)
}
