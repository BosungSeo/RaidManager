package com.raidmanager.game.scene

import com.raidmanager.game.scene.SceneStyle.Ui as Style
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets

internal object Ui {
    /** 이미지 종횡비를 유지하는 최소 배율로 화면 안에 맞추고 양축 중앙에 배치한다. */
    fun titleBackground(assets: GameAssets, batch: SpriteBatch, screenWidth: Int, screenHeight: Int) {
        val scale = minOf(
            screenWidth.toFloat() / assets.titleImage.width,
            screenHeight.toFloat() / assets.titleImage.height,
        )
        val width = assets.titleImage.width * scale
        val height = assets.titleImage.height * scale
        batch.color = Color.WHITE
        batch.draw(assets.titleImage, (screenWidth - width) / 2f, (screenHeight - height) / 2f, width, height)
    }

    fun text(
        assets: GameAssets,
        batch: SpriteBatch,
        value: String,
        x: Float,
        y: Float,
        scale: Float = 1f,
        color: Color = Color.WHITE,
    ) {
        assets.font.color = color
        assets.font.data.setScale(scale)
        assets.font.draw(batch, value, x, y)
    }

    fun button(
        assets: GameAssets,
        batch: SpriteBatch,
        label: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        selected: Boolean = false,
        enabled: Boolean = true,
    ) {
        val color = when {
            !enabled -> Style.DISABLED_COLOR
            selected -> Style.SELECTED_COLOR
            else -> Style.BUTTON_COLOR
        }
        batch.color = color
        batch.draw(assets.buttonTexture, x, y, width, height)
        batch.color = Color.WHITE

        assets.font.data.setScale(Style.BUTTON_TEXT_SCALE)
        assets.textLayout.setText(assets.font, label)
        val textColor = if (enabled) Color.WHITE else Color.GRAY
        text(
            assets,
            batch,
            label,
            x + (width - assets.textLayout.width) / 2f,
            y + height / 2f + Style.BUTTON_TEXT_OFFSET,
            Style.BUTTON_TEXT_SCALE,
            textColor,
        )
    }

    fun bar(
        assets: GameAssets,
        batch: SpriteBatch,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        ratio: Float,
        fill: Color,
    ) {
        batch.color = Style.BAR_BACKGROUND
        batch.draw(assets.buttonTexture, x, y, width, height)
        batch.color = fill
        batch.draw(assets.buttonTexture, x, y, width * ratio.coerceIn(0f, 1f), height)
        batch.color = Color.WHITE
    }

    fun contains(screenX: Int, screenY: Int, x: Float, y: Float, width: Float, height: Float, screenHeight: Int): Boolean {
        val worldX = screenX.toFloat()
        val worldY = screenHeight - screenY.toFloat()
        return worldX in x..(x + width) && worldY in y..(y + height)
    }
}
