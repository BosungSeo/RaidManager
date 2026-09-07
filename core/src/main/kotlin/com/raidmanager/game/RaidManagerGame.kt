package com.raidmanager.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.raidmanager.game.model.BattleResult
import com.raidmanager.game.model.DungeonDefinition
import com.raidmanager.game.model.PrototypeContent
import com.raidmanager.game.model.RaidFormation
import com.raidmanager.game.scene.BattleResultScene
import com.raidmanager.game.scene.DungeonSelectScene
import com.raidmanager.game.scene.GameScene
import com.raidmanager.game.scene.IntroScene
import com.raidmanager.game.scene.MainMenuScene
import com.raidmanager.game.scene.RaidSetupScene
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
        openRaidSetup(null)
    }

    private fun openRaidSetup(formation: RaidFormation?) {
        changeScene(
            RaidSetupScene(
                assets = assets,
                roster = PrototypeContent.characters,
                initialFormation = formation,
                onContinue = ::openDungeonSelect,
                onBack = ::openMainMenu,
            ),
        )
    }

    private fun openDungeonSelect(formation: RaidFormation) {
        changeScene(
            DungeonSelectScene(
                assets = assets,
                formation = formation,
                dungeons = PrototypeContent.dungeons,
                onStart = ::openBattle,
                onBack = ::openRaidSetup,
            ),
        )
    }

    private fun openBattle(formation: RaidFormation, dungeon: DungeonDefinition) {
        changeScene(
            GameScene(
                assets = assets,
                formation = formation,
                dungeon = dungeon,
                onFinished = ::openBattleResult,
                onExit = ::openDungeonSelect,
            ),
        )
    }

    private fun openBattleResult(formation: RaidFormation, dungeon: DungeonDefinition, result: BattleResult) {
        changeScene(
            BattleResultScene(
                assets = assets,
                formation = formation,
                dungeon = dungeon,
                result = result,
                onRetry = ::openBattle,
                onChangeRaid = ::openRaidSetup,
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
