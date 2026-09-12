package com.raidmanager.game.scene

import com.raidmanager.game.scene.SceneStyle.MainMenu as Style
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets
import java.util.ArrayDeque

class MainMenuScene(private val assets: GameAssets, private val onNewGame: () -> Unit) : InputAdapter(), Scene {
    private val pendingCommands = ArrayDeque<MenuCommand>()
    private var message: String? = null

    private val buttonWidth = Style.BUTTON_WIDTH
    private val buttonHeight = Style.BUTTON_HEIGHT

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        // 입력 좌표는 좌측 상단 기준이므로 렌더링 좌표처럼 좌측 하단 기준으로 변환한다.
        val worldX = screenX.toFloat()
        val worldY = Gdx.graphics.height - screenY.toFloat()

        return when {
            isInsideButton(worldX, worldY, 1) -> {
                pendingCommands.addLast(MenuCommand.NEW_GAME)
                true
            }

            isInsideButton(worldX, worldY, 0) -> {
                pendingCommands.addLast(MenuCommand.LOAD_GAME)
                true
            }

            else -> false
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode != Input.Keys.ENTER) return false

        pendingCommands.addLast(MenuCommand.NEW_GAME)
        return true
    }

    override fun updateGame(delta: Float) {
        while (pendingCommands.isNotEmpty()) {
            when (pendingCommands.removeFirst()) {
                MenuCommand.NEW_GAME -> onNewGame()
                MenuCommand.LOAD_GAME -> message = "NO SAVE DATA"
            }
        }
    }

    override fun renderGame(batch: SpriteBatch) {
        drawTitleImage(batch)

        val x = menuX()
        drawMenuButton("NEW GAME", x, buttonY(1), batch)
        drawMenuButton("LOAD GAME", x, buttonY(0), batch)

        message?.let {
            assets.font.color = Color.LIGHT_GRAY
            assets.font.data.setScale(Style.MESSAGE_SCALE)
            assets.font.draw(batch, it, x, buttonY(1) - Style.MESSAGE_OFFSET)
        }
    }

    private fun drawTitleImage(batch: SpriteBatch) =
        Ui.titleBackground(assets, batch, Gdx.graphics.width, Gdx.graphics.height)


    private fun drawMenuButton(label: String, x: Float, y: Float, batch: SpriteBatch) {
        batch.color = Style.BUTTON_COLOR
        batch.draw(assets.buttonTexture, x, y, buttonWidth, buttonHeight)
        batch.setColor(1f, 1f, 1f, 1f)

        assets.font.color = Color.WHITE
        assets.font.data.setScale(1f)
        assets.textLayout.setText(assets.font, label)
        assets.font.draw(
            batch,
            label,
            x + (buttonWidth - assets.textLayout.width) / 2f,
            y + buttonHeight / 2f + Style.TEXT_OFFSET,
        )
    }

    private fun isInsideButton(x: Float, y: Float, index: Int): Boolean {
        val buttonX = menuX()
        val buttonY = buttonY(index)
        return x in buttonX..(buttonX + buttonWidth) && y in buttonY..(buttonY + buttonHeight)
    }

    private fun menuX(): Float = Gdx.graphics.width * Style.X_RATIO

    private fun buttonY(index: Int): Float =
        Gdx.graphics.height * Style.Y_RATIO - index * (buttonHeight + Style.BUTTON_GAP)

    private enum class MenuCommand {
        NEW_GAME,
        LOAD_GAME,
    }
}
