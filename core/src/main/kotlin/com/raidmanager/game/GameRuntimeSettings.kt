package com.raidmanager.game

import com.badlogic.gdx.graphics.Color

internal object GameRuntimeSettings {
    /** 긴 프레임 후 한 번에 진행할 수 있는 최대 시간(초). */
    const val MAX_FRAME_DELTA = 0.1f
    /** Scene을 그리기 전에 화면을 지우는 기본 배경색. */
    val BACKGROUND_COLOR = Color(0.08f, 0.09f, 0.13f, 1f)
}
