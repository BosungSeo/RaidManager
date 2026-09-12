package com.raidmanager.game.model

/** Facing is a ground-plane vector; the perpendicular boundary is not a rear hit. */
internal object BattleFacing {
    fun isRear(dx: Float, dy: Float, facingX: Float, facingY: Float): Boolean =
        dx * facingX + dy * facingY < -0.001f

    fun damage(amount: Float, rear: Boolean): Float = amount * if (rear) 1.5f else 1f
}
