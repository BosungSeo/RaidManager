package com.raidmanager.game.scene

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets
import java.util.ArrayDeque

class GameScene(private val assets: GameAssets, private val onExit: () -> Unit) : InputAdapter(), Scene {
    private val pendingCommands = ArrayDeque<GameCommand>()
    private var elapsedTime = 0f

    override fun keyDown(keycode: Int): Boolean {
        if (keycode != Input.Keys.ESCAPE) return false

        pendingCommands.addLast(GameCommand.EXIT)
        return true
    }

    override fun updateGame(delta: Float) {
        processCommands()
        elapsedTime += delta
    }

    override fun renderGame(batch: SpriteBatch) {
        assets.font.color = Color.WHITE
        assets.font.data.setScale(1.5f)
        assets.font.draw(
            batch,
            "RAID READY",
            Gdx.graphics.width * 0.08f,
            Gdx.graphics.height * 0.62f,
        )

        assets.font.color = Color.LIGHT_GRAY
        assets.font.data.setScale(0.9f)
        assets.font.draw(
            batch,
            "TIME  ${elapsedTime.toInt()}   /   ESC: MAIN MENU",
            Gdx.graphics.width * 0.08f,
            Gdx.graphics.height * 0.54f,
        )
    }

    private fun processCommands() {
        while (pendingCommands.isNotEmpty()) {
            when (pendingCommands.removeFirst()) {
                GameCommand.EXIT -> onExit()
            }
        }
    }

    private enum class GameCommand {
        EXIT,
    }
}
