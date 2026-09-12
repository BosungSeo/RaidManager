package com.raidmanager.game.scene

/** 16 authored frames: idle 0..3, run 4..7, attack 8..11, recoil/recovery 12..15. */
internal object SpriteTimeline {
    fun frame(clock: Float, moving: Boolean, actionAge: Float?, hitAge: Float?, alive: Boolean, finished: Boolean): Int {
        if (!alive) return 13
        if (finished) return 0
        if (hitAge != null && hitAge < 0.28f) return 12 + (hitAge / 0.07f).toInt().coerceIn(0, 3)
        if (actionAge != null && actionAge < 0.4f) return 8 + (actionAge / 0.1f).toInt().coerceIn(0, 3)
        return (if (moving) 4 else 0) + (clock / if (moving) 0.1f else 0.2f).toInt().mod(4)
    }
}
