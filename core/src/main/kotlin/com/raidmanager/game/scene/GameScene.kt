package com.raidmanager.game.scene

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
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
        while (pendingCommands.isNotEmpty()) {
            when (pendingCommands.removeFirst()) {
                BattleCommand.EXIT -> onExit(formation)
                BattleCommand.RESULTS -> if (simulator.snapshot().finished) {
                    onFinished(formation, dungeon, simulator.result())
                }
            }
        }
    }

    override fun renderGame(batch: SpriteBatch) {
        val snapshot = simulator.snapshot()
        val margin = Gdx.graphics.width * 0.07f
        Ui.text(assets, batch, dungeon.name, margin, Gdx.graphics.height - 36f, 1.2f)
        Ui.text(
            assets,
            batch,
            "TIME ${snapshot.elapsedTime.toInt()} / ${dungeon.timeLimit.toInt()}",
            Gdx.graphics.width - margin - 170f,
            Gdx.graphics.height - 36f,
            0.75f,
            Color.LIGHT_GRAY,
        )

        Ui.text(assets, batch, dungeon.enemyName, margin, Gdx.graphics.height - 86f, 0.9f, Color(1f, 0.72f, 0.45f, 1f))
        Ui.bar(
            assets,
            batch,
            margin,
            Gdx.graphics.height - 112f,
            Gdx.graphics.width - margin * 2f,
            18f,
            snapshot.enemyHp / snapshot.enemyMaxHp,
            Color(0.72f, 0.18f, 0.16f, 1f),
        )
        Ui.text(
            assets,
            batch,
            "${snapshot.enemyHp.toInt()} / ${snapshot.enemyMaxHp.toInt()}",
            margin,
            Gdx.graphics.height - 120f,
            0.62f,
            Color.LIGHT_GRAY,
        )

        snapshot.members.forEachIndexed { index, member ->
            val y = Gdx.graphics.height - 190f - index * 72f
            Ui.text(assets, batch, "${member.character.name}  ${member.character.role}", margin, y, 0.78f)
            Ui.bar(
                assets,
                batch,
                margin,
                y - 26f,
                Gdx.graphics.width * 0.42f,
                14f,
                member.hp / member.character.maxHp,
                Color(0.18f, 0.68f, 0.34f, 1f),
            )
            Ui.text(
                assets,
                batch,
                "HP ${member.hp.toInt()}  SHIELD ${member.shield.toInt()}  DMG ${member.damageDealt.toInt()}",
                margin + Gdx.graphics.width * 0.45f,
                y - 14f,
                0.64f,
                Color.LIGHT_GRAY,
            )
        }

        Ui.text(assets, batch, "BATTLE LOG", margin, 178f, 0.8f, Color(0.55f, 0.78f, 1f, 1f))
        simulator.recentEvents(5).forEachIndexed { index, event ->
            Ui.text(
                assets,
                batch,
                "${"%.1f".format(event.time)}  ${event.message}",
                margin,
                154f - index * 22f,
                0.62f,
                Color.LIGHT_GRAY,
            )
        }

        if (snapshot.finished) {
            val bounds = resultButtonBounds()
            Ui.button(
                assets,
                batch,
                if (snapshot.victory) "VICTORY - VIEW RESULTS" else "DEFEAT - VIEW RESULTS",
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height,
                selected = snapshot.victory,
            )
        } else {
            Ui.text(assets, batch, "AUTO BATTLE IN PROGRESS", margin, 28f, 0.68f, Color.GRAY)
        }
    }

    private fun resultButtonBounds(): Bounds {
        val margin = Gdx.graphics.width * 0.07f
        return Bounds(margin, 20f, Gdx.graphics.width - margin * 2f, 52f)
    }

    private data class Bounds(val x: Float, val y: Float, val width: Float, val height: Float)
}
