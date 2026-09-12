package com.raidmanager.game.model

/** 전투 평면의 방향 벡터로 후방 공격을 판정한다. */
internal object BattleFacing {
    /** 시선 벡터와 공격자 방향의 내적이 음수이면 후방이며, 수직 경계의 오차는 제외한다. */
    fun isRear(dx: Float, dy: Float, facingX: Float, facingY: Float): Boolean =
        dx * facingX + dy * facingY < -BattleRules.DIRECTION_EPSILON

    fun damage(amount: Float, rear: Boolean): Float = amount * if (rear) BattleRules.REAR_DAMAGE_MULTIPLIER else 1f
}
