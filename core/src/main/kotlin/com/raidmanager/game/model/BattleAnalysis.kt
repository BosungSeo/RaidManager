package com.raidmanager.game.model

/** 전투 결과를 분석해 파티 개선 방향을 결정한다. */
internal object BattleAnalysis {
    fun summarize(victory: Boolean, dungeon: DungeonDefinition, results: List<CharacterBattleResult>): String {
        if (victory) return "Victory. Review the stats and try another composition."
        if (results.none { it.character.role == Role.HEALER }) return "The raid lacked recovery. Add a Healer or more protection."
        if (dungeon.mechanic == DungeonMechanic.REGEN && results.none { it.character.skillType == SkillType.SUNDER }) {
            return "Enemy regeneration erased your progress. Bring MIRA's healing reduction."
        }
        if (dungeon.mechanic == DungeonMechanic.SWARM && results.none { it.character.skillType == SkillType.CLEAVE }) {
            return "The swarm survived too long. EMBER's cleave is effective here."
        }
        if (results.count { it.survived } <= BattleRules.LOW_SURVIVOR_COUNT) {
            return "Incoming damage overwhelmed the raid. Add protection or an interrupt."
        }
        return "Damage was too low for the time limit. Add a DPS or choose stronger offense."
    }

}
