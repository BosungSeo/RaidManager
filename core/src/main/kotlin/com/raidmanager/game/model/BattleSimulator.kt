package com.raidmanager.game.model

import kotlin.math.max
import kotlin.math.min

class BattleSimulator(
    private val formation: RaidFormation,
    val dungeon: DungeonDefinition,
) {
    data class MemberSnapshot(
        val character: CharacterDefinition,
        val hp: Float,
        val shield: Float,
        val damageDealt: Float,
        val healingDone: Float,
    )

    data class Snapshot(
        val elapsedTime: Float,
        val enemyHp: Float,
        val enemyMaxHp: Float,
        val members: List<MemberSnapshot>,
        val finished: Boolean,
        val victory: Boolean,
    )

    private data class MemberState(
        val character: CharacterDefinition,
        var hp: Float = character.maxHp,
        var shield: Float = 0f,
        var attackTimer: Float = character.attackInterval,
        var skillTimer: Float = character.skillCooldown * 0.55f,
        var damageDealt: Float = 0f,
        var healingDone: Float = 0f,
        var damageTaken: Float = 0f,
        var skillUses: Int = 0,
    )

    private val members = formation.members.map(::MemberState)
    private val events = mutableListOf<BattleEvent>()
    private var accumulator = 0f
    private var elapsedTime = 0f
    private var enemyHp = dungeon.enemyMaxHp
    private var enemyAttackTimer = dungeon.enemyAttackInterval
    private var mechanicTimer = dungeon.mechanicInterval
    private var interruptReady = false
    private var sunderTime = 0f
    private var finished = false
    private var victory = false

    init {
        log("Battle started against ${dungeon.enemyName}")
    }

    fun update(delta: Float) {
        if (finished) return

        accumulator += delta.coerceAtMost(0.1f)
        while (accumulator >= STEP && !finished) {
            simulateStep(STEP)
            accumulator -= STEP
        }
    }

    fun snapshot(): Snapshot = Snapshot(
        elapsedTime = elapsedTime,
        enemyHp = enemyHp,
        enemyMaxHp = dungeon.enemyMaxHp,
        members = members.map {
            MemberSnapshot(it.character, it.hp, it.shield, it.damageDealt, it.healingDone)
        },
        finished = finished,
        victory = victory,
    )

    fun recentEvents(limit: Int): List<BattleEvent> = events.takeLast(limit)

    fun result(): BattleResult {
        check(finished) { "Battle result is only available after the battle finishes." }
        val memberResults = members.map {
            CharacterBattleResult(
                character = it.character,
                damageDealt = it.damageDealt,
                healingDone = it.healingDone,
                damageTaken = it.damageTaken,
                skillUses = it.skillUses,
                survived = it.hp > 0f,
            )
        }
        return BattleResult(victory, elapsedTime, enemyHp, memberResults, events.toList(), analyze(memberResults))
    }

    private fun simulateStep(delta: Float) {
        elapsedTime += delta
        sunderTime = max(0f, sunderTime - delta)

        members.filter { it.hp > 0f }.forEach { member ->
            member.attackTimer -= delta
            member.skillTimer -= delta
            if (member.attackTimer <= 0f) {
                damageEnemy(member, member.character.attackPower)
                member.attackTimer += member.character.attackInterval
            }
            if (member.skillTimer <= 0f) {
                useSkill(member)
                member.skillTimer += member.character.skillCooldown
            }
        }

        if (enemyHp <= 0f) {
            finish(true, "${dungeon.enemyName} defeated")
            return
        }

        enemyAttackTimer -= delta
        if (enemyAttackTimer <= 0f) {
            enemyAttack()
            enemyAttackTimer += dungeon.enemyAttackInterval
        }

        mechanicTimer -= delta
        if (mechanicTimer <= 0f) {
            triggerMechanic()
            mechanicTimer += dungeon.mechanicInterval
        }

        if (members.none { it.hp > 0f }) {
            finish(false, "Raid wiped out")
        } else if (elapsedTime >= dungeon.timeLimit) {
            finish(false, "Time limit reached")
        }
    }

    private fun useSkill(member: MemberState) {
        member.skillUses += 1
        when (member.character.skillType) {
            SkillType.GUARD -> {
                members.filter { it.hp > 0f }.forEach { it.shield = min(MAX_SHIELD, it.shield + 13f) }
                log("${member.character.name} used ${member.character.skillName}: party shielded")
            }

            SkillType.HEAL -> {
                val target = members.filter { it.hp > 0f }.minByOrNull { it.hp / it.character.maxHp } ?: return
                val healed = min(30f, target.character.maxHp - target.hp)
                target.hp += healed
                member.healingDone += healed
                if (healed > 0f) log("${member.character.name} healed ${target.character.name} for ${healed.toInt()}")
            }

            SkillType.STRIKE -> {
                damageEnemy(member, 32f)
                log("${member.character.name} used ${member.character.skillName} for 32")
            }

            SkillType.CLEAVE -> {
                val damage = if (dungeon.mechanic == DungeonMechanic.SWARM) 50f else 22f
                damageEnemy(member, damage)
                log("${member.character.name} used ${member.character.skillName} for ${damage.toInt()}")
            }

            SkillType.INTERRUPT -> {
                interruptReady = true
                damageEnemy(member, 10f)
                log("${member.character.name} prepared an interrupt")
            }

            SkillType.SUNDER -> {
                sunderTime = 6f
                damageEnemy(member, 16f)
                log("${member.character.name} applied healing reduction")
            }
        }
    }

    private fun enemyAttack() {
        val alive = members.filter { it.hp > 0f }
        val target = alive.firstOrNull { it.character.role == Role.TANK } ?: alive.minByOrNull { it.hp } ?: return
        val reduction = if (target.character.role == Role.TANK) 0.68f else 1f
        damageMember(target, dungeon.enemyAttack * reduction)
    }

    private fun triggerMechanic() {
        if (dungeon.mechanic != DungeonMechanic.REGEN && interruptReady) {
            interruptReady = false
            log("${dungeon.enemyName}'s special attack was interrupted")
            return
        }

        when (dungeon.mechanic) {
            DungeonMechanic.BURST -> {
                members.filter { it.hp > 0f }.forEach { damageMember(it, 22f) }
                log("COLOSSAL SLAM hit the entire raid")
            }

            DungeonMechanic.SWARM -> {
                members.filter { it.hp > 0f }.forEach { damageMember(it, 11f) }
                log("A swarm wave hit the entire raid")
            }

            DungeonMechanic.REGEN -> {
                val recovery = if (sunderTime > 0f) 7f else 32f
                enemyHp = min(dungeon.enemyMaxHp, enemyHp + recovery)
                log("${dungeon.enemyName} regenerated ${recovery.toInt()} HP")
            }
        }
    }

    private fun damageEnemy(source: MemberState, amount: Float) {
        val applied = min(amount, enemyHp)
        enemyHp -= applied
        source.damageDealt += applied
    }

    private fun damageMember(target: MemberState, amount: Float) {
        val absorbed = min(target.shield, amount)
        target.shield -= absorbed
        val hpDamage = amount - absorbed
        target.hp = max(0f, target.hp - hpDamage)
        target.damageTaken += hpDamage
        if (target.hp <= 0f) log("${target.character.name} was defeated")
    }

    private fun finish(didWin: Boolean, message: String) {
        victory = didWin
        finished = true
        log(message)
    }

    private fun analyze(results: List<CharacterBattleResult>): String {
        if (victory) return "Victory. Review the stats and try another composition."
        if (results.none { it.character.role == Role.HEALER }) return "The raid lacked recovery. Add a Healer or more protection."
        if (dungeon.mechanic == DungeonMechanic.REGEN && results.none { it.character.skillType == SkillType.SUNDER }) {
            return "Enemy regeneration erased your progress. Bring MIRA's healing reduction."
        }
        if (dungeon.mechanic == DungeonMechanic.SWARM && results.none { it.character.skillType == SkillType.CLEAVE }) {
            return "The swarm survived too long. EMBER's cleave is effective here."
        }
        if (results.count { it.survived } <= 1) return "Incoming damage overwhelmed the raid. Add protection or an interrupt."
        return "Damage was too low for the time limit. Add a DPS or choose stronger offense."
    }

    private fun log(message: String) {
        events += BattleEvent(elapsedTime, message)
    }

    companion object {
        private const val STEP = 0.1f
        private const val MAX_SHIELD = 30f
    }
}
