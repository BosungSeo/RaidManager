package com.raidmanager.game.model

object StatCalculator {
    /** 기본 전투 수치에 능력치별 증가량을 더하고, 공격·스킬 대기 시간에는 최소 간격을 보장한다. */
    fun derive(character: CharacterDefinition): CombatStats = CombatStats(
        maxHp = character.maxHp + character.stats.vitality * StatRules.HP_PER_VITALITY,
        attackPower = character.attackPower + character.stats.strength * StatRules.ATTACK_PER_STRENGTH,
        defense = character.stats.endurance * StatRules.DEFENSE_PER_ENDURANCE,
        attackInterval =
            (character.attackInterval * (1f - character.stats.dexterity * StatRules.HASTE_PER_DEXTERITY))
                .coerceAtLeast(StatRules.MIN_ATTACK_INTERVAL),
        skillPower = 1f + character.stats.intelligence * StatRules.POWER_PER_INTELLIGENCE,
        skillCooldown =
            (character.skillCooldown * (1f - character.stats.wisdom * StatRules.COOLDOWN_PER_WISDOM))
                .coerceAtLeast(StatRules.MIN_SKILL_COOLDOWN),
        healingPower = 1f + character.stats.intelligence * StatRules.POWER_PER_INTELLIGENCE,
    )
}

