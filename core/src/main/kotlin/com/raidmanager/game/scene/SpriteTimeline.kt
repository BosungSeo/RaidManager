package com.raidmanager.game.scene

import com.raidmanager.game.scene.SpriteAnimationStyle as Style

/** 사망·종료·피격·공격 순서로 우선순위를 정하고, 경과 시간을 프레임 번호로 변환한다. */
internal object SpriteTimeline {
    fun frame(clock: Float, moving: Boolean, actionAge: Float?, hitAge: Float?, alive: Boolean, finished: Boolean): Int {
        if (!alive) return Style.DEAD_FRAME
        if (finished) return Style.IDLE_FRAME
        if (hitAge != null && hitAge < Style.HURT_DURATION) {
            return Style.HURT_FRAME + (hitAge / Style.HURT_FRAME_TIME).toInt().coerceIn(0, Style.FRAMES_PER_POSE - 1)
        }
        if (actionAge != null && actionAge < Style.ACTION_DURATION) {
            return Style.ACTION_FRAME + (actionAge / Style.ACTION_FRAME_TIME).toInt().coerceIn(0, Style.FRAMES_PER_POSE - 1)
        }
        val startFrame = if (moving) Style.RUN_FRAME else Style.IDLE_FRAME
        val frameTime = if (moving) Style.ACTION_FRAME_TIME else Style.IDLE_FRAME_TIME
        return startFrame + (clock / frameTime).toInt().mod(Style.FRAMES_PER_POSE)
    }
}
