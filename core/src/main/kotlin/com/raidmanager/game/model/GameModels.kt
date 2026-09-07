package com.raidmanager.game.model

enum class Role {
    TANK,
    HEALER,
    DPS,
    SUPPORT,
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
)

enum class DungeonMechanic {
    BURST,
    SWARM,
    REGEN,
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
    val timeLimit: Float = 40f,
)

data class RaidFormation(val members: List<CharacterDefinition>) {
    init {
        require(members.size == PARTY_SIZE) { "A raid formation must contain exactly $PARTY_SIZE members." }
    }

    companion object {
        const val PARTY_SIZE = 3
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
