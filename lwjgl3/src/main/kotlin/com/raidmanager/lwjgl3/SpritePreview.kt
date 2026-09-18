package com.raidmanager.lwjgl3

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.PrototypeContent
import com.raidmanager.game.model.RaidFormation
import com.raidmanager.game.scene.GameScene

/** Renders both hero groups and all three dungeons through real battle scenes for visual QA. */
fun main() {
    val monsterCount = (System.getenv("RAID_PREVIEW_MONSTERS")?.toIntOrNull() ?: 1).coerceIn(1, 3)
    val tankPreview = System.getenv("RAID_PREVIEW_TANK") == "true"
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
            scene = GameScene(assets, RaidFormation(heroes, monsterCount), dungeon, { _, _, _ -> }, {})
        }

        override fun render() {
            if (tankPreview) {
                renderTankPoses()
                Gdx.app.exit()
                return
            }
            scene.updateGame(0.1f)
            ScreenUtils.clear(0f, 0f, 0f, 1f)
            batch.begin()
            scene.renderGame(batch)
            batch.end()
            frames++
            val capture = if (group < 2) frames in listOf(24, 30, 34, 40, 48) else {
                frames in listOf(1, 15, 22, 42, 52, 72, 85, 100, 400)
            }
            if (capture) {
                val pixels = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight)
                val file = if (group < 2) "/tmp/raid-sprites-$group-$frames.png" else {
                    "/tmp/raid-monsters-${PrototypeContent.dungeons[group - 2].id}-$monsterCount-$frames.png"
                }
                PixmapIO.writePNG(Gdx.files.absolute(file), pixels, -1, true)
                pixels.dispose()
            }
            if (frames == if (group < 2) 48 else 400) {
                if (group == 4) Gdx.app.exit() else {
                    group++
                    frames = 0
                    openScene()
                }
            }
        }

        private fun renderTankPoses() {
            for (facing in listOf(1f, -1f)) {
                ScreenUtils.clear(0.045f, 0.06f, 0.09f, 1f)
                batch.begin()
                for (index in 0..15) {
                    val x = index % 4 * 320f
                    val y = (3 - index / 4) * 180f + 22f
                    assets.font.draw(batch, "${listOf("IDLE", "RUN", "ATTACK", "HURT")[index / 4]} ${index % 4 + 1}", x + 10f, y + 143f)
                    val sprite = assets.animatedFrame("aegis", index)
                    val spriteScale = 136f / sprite.region.regionHeight
                    batch.shader = assets.heroShader
                    batch.draw(sprite.region, x + 76f - sprite.footX * spriteScale, y - sprite.footY * spriteScale,
                        sprite.footX * spriteScale, sprite.footY * spriteScale, 136f, 136f, facing, 1f, 0f)
                    batch.shader = null
                    val model = assets.tankFrame(batch, index)
                    val modelScale = 200f / model.region.regionHeight
                    batch.draw(model.region, x + 228f - model.footX * modelScale, y - model.footY * modelScale,
                        model.footX * modelScale, model.footY * modelScale, 200f, 200f, facing, 1f, 0f)
                    batch.color = Color.WHITE
                }
                batch.end()
                check(Gdx.gl.glGetError() == com.badlogic.gdx.graphics.GL20.GL_NO_ERROR) { "Tank preview OpenGL error" }
                val pixels = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight)
                PixmapIO.writePNG(Gdx.files.absolute("/tmp/raid-tank-poses-${if (facing > 0) "right" else "left"}.png"),
                    pixels, -1, true)
                pixels.dispose()
            }
        }

        override fun dispose() {
            batch.dispose()
            assets.dispose()
        }
    }, config)
}
