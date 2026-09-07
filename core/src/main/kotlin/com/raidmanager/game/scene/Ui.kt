package com.raidmanager.game.scene

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets

internal object Ui {
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
            !enabled -> Color(0.14f, 0.15f, 0.18f, 0.9f)
            selected -> Color(0.18f, 0.46f, 0.66f, 0.96f)
            else -> Color(0.13f, 0.18f, 0.27f, 0.96f)
        }
        batch.color = color
        batch.draw(assets.buttonTexture, x, y, width, height)
        batch.color = Color.WHITE

        assets.font.data.setScale(0.86f)
        assets.textLayout.setText(assets.font, label)
        val textColor = if (enabled) Color.WHITE else Color.GRAY
        text(
            assets,
            batch,
            label,
            x + (width - assets.textLayout.width) / 2f,
            y + height / 2f + 8f,
            0.86f,
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
        batch.color = Color(0.14f, 0.15f, 0.18f, 1f)
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
