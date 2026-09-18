package com.raidmanager.game.model

enum class Role {
    TANK,
    HEALER,
    DPS,
    SUPPORT,
}

enum class AttackStyle(val range: Float) {
    MELEE(BattleRules.MELEE_RANGE),
    RANGED(BattleRules.RANGED_RANGE),
}

enum class SkillType {
    GUARD,
    HEAL,
    STRIKE,
    CLEAVE,
    INTERRUPT,
    SUNDER,
}

data class CharacterStats(
    val vitality: Int,
    val strength: Int,
    val intelligence: Int,
    val dexterity: Int,
    val endurance: Int,
    val wisdom: Int,
)

data class CombatStats(
    val maxHp: Float,
    val attackPower: Float,
    val defense: Float,
    val attackInterval: Float,
    val skillPower: Float,
    val skillCooldown: Float,
    val healingPower: Float,
)

data class CharacterDefinition(
    val id: String,
    val name: String,
    val role: Role,
    val maxHp: Float,
    val attackPower: Float,
    val attackInterval: Float,
    val skillName: String,
    val skillType: SkillType,
    val skillCooldown: Float,
    val attackStyle: AttackStyle = if (role == Role.TANK || skillType == SkillType.STRIKE) AttackStyle.MELEE else AttackStyle.RANGED,
    val moveSpeed: Float = if (attackStyle == AttackStyle.MELEE) BattleRules.DEFAULT_MELEE_SPEED else BattleRules.DEFAULT_RANGED_SPEED,
    val stats: CharacterStats = CharacterStats(
        BattleRules.DEFAULT_STAT, BattleRules.DEFAULT_STAT, BattleRules.DEFAULT_STAT,
        BattleRules.DEFAULT_STAT, BattleRules.DEFAULT_STAT, BattleRules.DEFAULT_STAT,
    ),
) {
    init {
        require(moveSpeed.isFinite() && moveSpeed >= 0f) { "Character moveSpeed must be finite and non-negative" }
    }
}

enum class DungeonMechanic {
    BURST,
    SWARM,
    REGEN,
}

enum class MonsterAbility {
    COLOSSAL_SLAM,
    BROOD_CALL,
    REGENERATE,
}

data class DungeonDefinition(
    val id: String,
    val name: String,
    val description: String,
    val enemyName: String,
    val enemyMaxHp: Float,
    val enemyAttack: Float,
    val enemyAttackInterval: Float,
    val mechanic: DungeonMechanic,
    val mechanicInterval: Float,
    val ability: MonsterAbility = when (mechanic) {
        DungeonMechanic.BURST -> MonsterAbility.COLOSSAL_SLAM
        DungeonMechanic.SWARM -> MonsterAbility.BROOD_CALL
        DungeonMechanic.REGEN -> MonsterAbility.REGENERATE
    },
    val timeLimit: Float = BattleRules.DEFAULT_TIME_LIMIT,
    val enemyMoveSpeed: Float = BattleRules.DEFAULT_ENEMY_SPEED,
) {
    init {
        require(enemyMoveSpeed.isFinite() && enemyMoveSpeed >= 0f) { "Enemy moveSpeed must be finite and non-negative" }
    }
}

data class RaidFormation(val members: List<CharacterDefinition>, val monsterCount: Int = BattleRules.MIN_MONSTER_COUNT) {
    init {
        require(members.size in MIN_PARTY_SIZE..MAX_PARTY_SIZE) { "A raid formation must contain 3 to 6 members." }
        require(members.map { it.id }.distinct().size == members.size) { "Raid members must be unique." }
        require(monsterCount in MIN_MONSTER_COUNT..MAX_MONSTER_COUNT) {
            "Monster count must be between $MIN_MONSTER_COUNT and $MAX_MONSTER_COUNT."
        }
    }

    companion object {
        /** 공격대 편성 인원. */
        const val MIN_PARTY_SIZE = BattleRules.MIN_PARTY_SIZE
        const val MAX_PARTY_SIZE = BattleRules.MAX_PARTY_SIZE
        /** 최소 몬스터 수. */
        const val MIN_MONSTER_COUNT = BattleRules.MIN_MONSTER_COUNT
        /** 최대 몬스터 수. */
        const val MAX_MONSTER_COUNT = BattleRules.MAX_MONSTER_COUNT
    }
}

data class CharacterBattleResult(
    val character: CharacterDefinition,
    val damageDealt: Float,
    val healingDone: Float,
    val damageTaken: Float,
    val skillUses: Int,
    val survived: Boolean,
)

data class BattleEvent(val time: Float, val message: String)

data class BattleResult(
    val victory: Boolean,
    val elapsedTime: Float,
    val enemyHpRemaining: Float,
    val members: List<CharacterBattleResult>,
    val events: List<BattleEvent>,
    val analysis: String,
)
