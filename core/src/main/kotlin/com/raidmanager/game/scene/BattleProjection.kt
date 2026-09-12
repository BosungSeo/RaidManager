package com.raidmanager.game.scene

/** Oblique ground plane; all actors, targets and effects share the same foot coordinates. */
internal object BattleProjection {
    fun x(x: Float, y: Float): Float = 640f + (x - 640f) * 0.82f - (y - 400f) * 0.6f
    fun y(x: Float, y: Float): Float = 340f + (x - 640f) * 0.24f + (y - 400f) * 0.44f
}
