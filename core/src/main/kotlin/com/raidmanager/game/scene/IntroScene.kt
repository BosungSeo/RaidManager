package com.raidmanager.game.scene

import com.raidmanager.game.scene.SceneStyle.Intro as Style
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets

class IntroScene(private val assets: GameAssets, private val onFinished: () -> Unit,) : Scene {
    private var elapsedTime = 0f
    private val duration = Style.DURATION
    private val fadeOutStart = Style.FADE_START

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
        assets.font.data.setScale(Style.TITLE_SCALE)
        assets.font.draw(
            batch,
            "RaidManager",
            Gdx.graphics.width * Style.TITLE_X_RATIO,
            Gdx.graphics.height * Style.TITLE_Y_RATIO,
        )
    }

    private fun drawTitleImage(batch: SpriteBatch) =
        Ui.titleBackground(assets, batch, Gdx.graphics.width, Gdx.graphics.height)

}
