package com.raidmanager.game.model

/** 전투마다 초기화되는 선형 피로 규칙. 몬스터에는 적용하지 않는다. */
object BattleFatigue {
    const val RATE_PER_SECOND = 0.005f
    const val MAX_PENALTY = 0.7f
    const val SUPPORT_BASE_CAST = 0.6f

    fun penalty(seconds: Float): Float = (seconds * RATE_PER_SECOND).coerceIn(0f, MAX_PENALTY)
    fun skillMultiplier(role: Role, seconds: Float): Float = if (role == Role.DPS) 1f - penalty(seconds) else 1f
    fun healMultiplier(role: Role, seconds: Float): Float = if (role == Role.HEALER) 1f - penalty(seconds) else 1f
    fun castDuration(role: Role, seconds: Float): Float =
        if (role == Role.SUPPORT) SUPPORT_BASE_CAST * (1f + penalty(seconds)) else 0f
    fun hpLossPerSecond(character: CharacterDefinition): Float =
        if (character.role == Role.TANK) character.maxHp * RATE_PER_SECOND else 0f
}
