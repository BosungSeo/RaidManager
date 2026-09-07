package com.raidmanager.game.scene

import com.badlogic.gdx.graphics.g2d.SpriteBatch

/** 한 화면의 게임 상태 갱신과 렌더링을 정의한다. */
interface Scene {
    fun updateGame(delta: Float)

    fun renderGame(batch: SpriteBatch)
}
