package com.raidmanager.game.graphics

import kotlin.math.sin
import kotlin.math.PI

/** 3D 골격 동작과 화면 반동이 공유하는 표현 시간. 전투 판정 시간에는 영향을 주지 않는다. */
internal object CharacterMotion {
    const val ATTACK_DURATION = 0.42f
    const val HIT_DURATION = 0.18f
    const val BODY_HEIGHT = 46f
    const val NAME_HEIGHT = 104f

    fun hitPulse(age: Float?): Float {
        if (age == null || age < 0f || age >= HIT_DURATION) return 0f
        return 1f - age / HIT_DURATION
    }

    fun recoil(age: Float?, damage: Float): Float {
        if (age == null || age < 0f || age >= HIT_DURATION || damage <= 0f) return 0f
        val strength = (3f + damage * 0.16f).coerceAtMost(8f)
        return sin(age / HIT_DURATION * PI).toFloat() * strength
    }
}
