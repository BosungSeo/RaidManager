package com.raidmanager.game.scene

/** 전장 좌표에 비스듬한 평면 변환을 적용하여 캐릭터와 효과가 동일한 발 위치를 공유하게 한다. */
internal object BattleProjection {
    fun facingX(x: Float, y: Float): Float = x * ProjectionStyle.X_SCALE - y * ProjectionStyle.Y_SKEW

    fun x(x: Float, y: Float): Float = ProjectionStyle.CENTER_X + (x - ProjectionStyle.CENTER_X) * ProjectionStyle.X_SCALE - (y - ProjectionStyle.CENTER_Y) * ProjectionStyle.Y_SKEW
    fun y(x: Float, y: Float): Float = ProjectionStyle.SCREEN_Y + (x - ProjectionStyle.CENTER_X) * ProjectionStyle.X_SKEW + (y - ProjectionStyle.CENTER_Y) * ProjectionStyle.Y_SCALE
}
