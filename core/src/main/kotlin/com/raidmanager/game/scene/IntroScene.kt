package com.raidmanager.game.scene

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets

class IntroScene(private val assets: GameAssets, private val onFinished: () -> Unit,) : Scene {
    private var elapsedTime = 0f
    private val duration = 3f
    private val fadeOutStart = 1.6f

    override fun updateGame(delta: Float) {
        elapsedTime += delta
        if (elapsedTime >= duration) {
            onFinished()
        }
    }

    override fun renderGame(batch: SpriteBatch) {
        drawTitleImage(batch)

        val alpha = if (elapsedTime < fadeOutStart) {
            1f
        } else {
            1f - ((elapsedTime - fadeOutStart) / (duration - fadeOutStart))
        }

        assets.font.color.set(1f, 1f, 1f, alpha.coerceIn(0f, 1f))
        assets.font.data.setScale(1.25f)
        assets.font.draw(
            batch,
            "RaidManager",
            Gdx.graphics.width * 0.08f,
            Gdx.graphics.height * 0.72f,
        )
    }

    private fun drawTitleImage(batch: SpriteBatch) {
        val scale = minOf(
            Gdx.graphics.width.toFloat() / assets.titleImage.width,
            Gdx.graphics.height.toFloat() / assets.titleImage.height,
        )
        val width = assets.titleImage.width * scale
        val height = assets.titleImage.height * scale
        val x = (Gdx.graphics.width - width) / 2f
        val y = (Gdx.graphics.height - height) / 2f

        batch.setColor(1f, 1f, 1f, 1f)
        batch.draw(assets.titleImage, x, y, width, height)
    }
}
