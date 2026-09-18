package com.raidmanager.game.model

/** 던전 원본 데이터는 변경하지 않고 이번 전투에만 제한 시간을 적용한다. */
class DungeonTimeSelection {
    var seconds: Int = BattleRules.DEFAULT_TIME_LIMIT.toInt()
        private set

    fun select(value: Int) { seconds = value.coerceIn(MIN_SECONDS, MAX_SECONDS) }
    fun adjust(delta: Int) { select(seconds + delta) }
    fun applyTo(dungeon: DungeonDefinition): DungeonDefinition = dungeon.copy(timeLimit = seconds.toFloat())
    val label: String get() = "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"

    companion object {
        const val MIN_SECONDS = 10
        const val MAX_SECONDS = 600
    }
}
