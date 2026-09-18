package com.raidmanager.game.model

import kotlin.math.sqrt

/** 이동 선분과 원의 최초 접촉 시점. 닿은 상대를 밀지 않고 그 직전에 멈춘다. */
internal object CombatMovement {
    fun allowedFraction(x: Float, y: Float, dx: Float, dy: Float, otherX: Float, otherY: Float, radius: Float): Float {
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared < 0.000001f) return 1f
        val rx = x - otherX
        val ry = y - otherY
        val approach = rx * dx + ry * dy
        if (approach >= 0f) return 1f
        val gap = rx * rx + ry * ry - radius * radius
        if (gap <= 0f) return 0f
        val discriminant = approach * approach - lengthSquared * gap
        if (discriminant <= 0f) return 1f
        return ((-approach - sqrt(discriminant)) / lengthSquared).coerceIn(0f, 1f)
    }
}
