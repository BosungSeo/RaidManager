package com.raidmanager.game.model

/** 화면과 독립적으로 공격대 선택 및 몬스터 수 규칙을 관리한다. */
class RaidSetupState(
    private val roster: List<CharacterDefinition>,
    initialFormation: RaidFormation? = null,
) {
    private val selection = initialFormation?.members?.map { it.id }?.toMutableSet() ?: mutableSetOf()
    val selectedIds: Set<String> get() = selection.toSet()
    var monsterCount: Int = initialFormation?.monsterCount ?: BattleRules.MIN_MONSTER_COUNT
        private set
    val isComplete: Boolean get() = selection.size in BattleRules.MIN_PARTY_SIZE..BattleRules.MAX_PARTY_SIZE

    fun toggle(index: Int): Boolean {
        val character = roster.getOrNull(index) ?: return false
        if (selection.remove(character.id)) return true
        if (selection.size >= BattleRules.MAX_PARTY_SIZE) return false
        selection += character.id
        return true
    }

    fun adjustMonsterCount(delta: Int) {
        monsterCount = (monsterCount + delta).coerceIn(BattleRules.MIN_MONSTER_COUNT, BattleRules.MAX_MONSTER_COUNT)
    }

    fun createFormation(): RaidFormation? {
        if (!isComplete) return null
        val members = roster.filter { it.id in selection }
        return if (members.size in BattleRules.MIN_PARTY_SIZE..BattleRules.MAX_PARTY_SIZE) RaidFormation(members, monsterCount) else null
    }
}
