package com.raidmanager.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.raidmanager.game.scene.GameScene
import com.raidmanager.game.scene.IntroScene
import com.raidmanager.game.scene.MainMenuScene
import com.raidmanager.game.scene.Scene

/** Shared game code used by both Android and macOS launchers. */
class RaidManagerGame : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var assets: GameAssets
    private lateinit var currentScene: Scene

    override fun create() {
        batch = SpriteBatch()
        assets = GameAssets()
        changeScene(IntroScene(assets, ::openMainMenu))
    }

    override fun render() {
        // 긴 프레임 뒤 게임 상태가 한꺼번에 크게 변하지 않도록 delta의 최댓값을 제한한다.
        val delta = Gdx.graphics.deltaTime.coerceAtMost(0.1f)
        currentScene.updateGame(delta)

        ScreenUtils.clear(Color(0.08f, 0.09f, 0.13f, 1f))
        batch.begin()
        currentScene.renderGame(batch)
        batch.end()
    }

    private fun openMainMenu() {
        changeScene(
            MainMenuScene(
                assets = assets,
                onNewGame = ::openGame,
            ),
        )
    }

    private fun openGame() {
        changeScene(
            GameScene(
                assets = assets,
                onExit = ::openMainMenu,
            ),
        )
    }

    private fun changeScene(scene: Scene) {
        currentScene = scene
        Gdx.input.inputProcessor = scene as? InputProcessor
    }

    override fun dispose() {
        Gdx.input.inputProcessor = null
        batch.dispose()
        assets.dispose()
    }
}
