package com.raidmanager.game.model

enum class Role {
    TANK,
    HEALER,
    DPS,
    SUPPORT,
}

enum class AttackStyle(val range: Float) {
    MELEE(145f),
    RANGED(520f),
}

enum class SkillType {
    GUARD,
    HEAL,
    STRIKE,
    CLEAVE,
    INTERRUPT,
    SUNDER,
}

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
    val moveSpeed: Float = if (attackStyle == AttackStyle.MELEE) 430f else 260f,
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
    val timeLimit: Float = 40f,
    val enemyMoveSpeed: Float = 95f,
) {
    init {
        require(enemyMoveSpeed.isFinite() && enemyMoveSpeed >= 0f) { "Enemy moveSpeed must be finite and non-negative" }
    }
}

data class RaidFormation(val members: List<CharacterDefinition>, val monsterCount: Int = 1) {
    init {
        require(members.size == PARTY_SIZE) { "A raid formation must contain exactly $PARTY_SIZE members." }
        require(monsterCount in MIN_MONSTER_COUNT..MAX_MONSTER_COUNT) {
            "Monster count must be between $MIN_MONSTER_COUNT and $MAX_MONSTER_COUNT."
        }
    }

    companion object {
        const val PARTY_SIZE = 3
        const val MIN_MONSTER_COUNT = 1
        const val MAX_MONSTER_COUNT = 3
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
