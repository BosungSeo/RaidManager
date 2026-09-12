package com.raidmanager.game.model

object StatRules {
    /** 활력 1당 추가 체력. */
    const val HP_PER_VITALITY = 10f
    /** 힘 1당 추가 공격력. */
    const val ATTACK_PER_STRENGTH = 1.5f
    /** 인내 1당 방어력. */
    const val DEFENSE_PER_ENDURANCE = 1.2f
    /** 민첩 1당 공격 간격 감소 비율. */
    const val HASTE_PER_DEXTERITY = 0.01f
    /** 지능 1당 스킬 및 회복 배율 증가량. */
    const val POWER_PER_INTELLIGENCE = 0.02f
    /** 지혜 1당 스킬 대기 시간 감소 비율. */
    const val COOLDOWN_PER_WISDOM = 0.01f
    /** 최소 공격 간격(초). */
    const val MIN_ATTACK_INTERVAL = 0.25f
    /** 최소 스킬 대기 시간(초). */
    const val MIN_SKILL_COOLDOWN = 0.5f
}
