package com.raidmanager.lwjgl3

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.PrototypeContent
import com.raidmanager.game.model.RaidFormation
import com.raidmanager.game.scene.GameScene

/** Renders both hero groups and all three dungeons through real battle scenes for visual QA. */
fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("RaidManager Sprite Preview")
        setWindowedMode(1280, 720)
    }
    Lwjgl3Application(object : ApplicationAdapter() {
        private lateinit var assets: GameAssets
        private lateinit var batch: SpriteBatch
        private lateinit var scene: GameScene
        private var group = 0
        private var frames = 0

        override fun create() {
            assets = GameAssets()
            batch = SpriteBatch()
            openScene()
        }

        private fun openScene() {
            val heroes = PrototypeContent.characters.drop(if (group == 1) 3 else 0).take(3)
            val dungeon = PrototypeContent.dungeons[if (group < 2) 0 else group - 2]
            scene = GameScene(assets, RaidFormation(heroes), dungeon, { _, _, _ -> }, {})
        }

        override fun render() {
            scene.updateGame(0.1f)
            ScreenUtils.clear(0f, 0f, 0f, 1f)
            batch.begin()
            scene.renderGame(batch)
            batch.end()
            frames++
            val capture = if (group < 2) frames == 34 else frames in listOf(1, 12, 22, 42, 52, 72, 400)
            if (capture) {
                val pixels = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight)
                val file = if (group < 2) "/tmp/raid-sprites-$group.png" else {
                    "/tmp/raid-monsters-${PrototypeContent.dungeons[group - 2].id}-$frames.png"
                }
                PixmapIO.writePNG(Gdx.files.absolute(file), pixels, -1, true)
                pixels.dispose()
            }
            if (frames == if (group < 2) 34 else 400) {
                if (group == 4) Gdx.app.exit() else {
                    group++
                    frames = 0
                    openScene()
                }
            }
        }

        override fun dispose() {
            batch.dispose()
            assets.dispose()
        }
    }, config)
}
