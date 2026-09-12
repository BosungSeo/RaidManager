package com.raidmanager.game.model

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class BattleSimulator(
    private val formation: RaidFormation,
    val dungeon: DungeonDefinition,
) {
    enum class CueType { HIT, HEAL, SHIELD, INTERRUPT, SUNDER, WAVE, TAUNT }
    enum class MovementIntent { APPROACH, RETREAT, HOLD, SPACING, DOWN }

    data class CombatCue(
        val type: CueType,
        val source: String,
        val target: String,
        val amount: Float = 0f,
        val skillType: SkillType? = null,
        val rearAttack: Boolean = false,
    )

    private val cues = mutableListOf<CombatCue>()

    fun drainCombatCues(): List<CombatCue> = cues.toList().also { cues.clear() }

    data class MemberSnapshot(
        val character: CharacterDefinition,
        val hp: Float,
        val shield: Float,
        val damageDealt: Float,
        val healingDone: Float,
        val x: Float,
        val y: Float,
        val intent: MovementIntent,
        val targetId: String?,
        val facingX: Float = 1f,
        val facingY: Float = 0f,
    )

    data class Snapshot(
        val elapsedTime: Float,
        val enemyHp: Float,
        val enemyMaxHp: Float,
        val members: List<MemberSnapshot>,
        val finished: Boolean,
        val victory: Boolean,
        val abilityCastRemaining: Float,
        val abilityCastDuration: Float,
        val enemies: List<EnemySnapshot>,
        val protectorId: String?,
    )

    data class EnemySnapshot(
        val id: String,
        val hp: Float,
        val maxHp: Float,
        val castRemaining: Float,
        val aggro: Map<String, Float>,
        val targetId: String?,
        val x: Float,
        val y: Float,
        val healingReductionRemaining: Float = 0f,
        val facingX: Float = -1f,
        val facingY: Float = 0f,
    )

    private class EnemyState(val index: Int, dungeon: DungeonDefinition) {
        val id = if (index == 0) "boss" else "boss-$index"
        var hp = dungeon.enemyMaxHp
        val attackInterval = dungeon.enemyAttackInterval * (1f + index * 0.09f)
        var attackTimer = dungeon.enemyAttackInterval * (0.65f + index * 0.32f)
        var mechanicTimer = dungeon.mechanicInterval + index * 1.65f
        var castRemaining = 0f
        var sunderTime = 0f
        var facingX = -1f
        var facingY = 0f
        val threat = mutableMapOf<String, Float>()
        var tieTargetId: String? = null
        var x = 900f + index * 100f
        var y = 440f - index * 80f
    }

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
        var x: Float = 0f,
        var y: Float = 0f,
        var intent: MovementIntent = MovementIntent.HOLD,
        var facingX: Float = 1f,
        var facingY: Float = 0f,
    )

    private val members = formation.members.mapIndexed { index, character ->
        // Stable identity-based offsets preserve reproducible battles while avoiding a synchronized opening.
        val phase = (character.id.hashCode() and Int.MAX_VALUE) % 11 / 10f
        MemberState(character, attackTimer = character.attackInterval * (0.45f + phase * 0.5f),
            skillTimer = character.skillCooldown * (0.45f + phase * 0.25f),
            x = if (character.attackStyle == AttackStyle.MELEE) 430f else 300f, y = 570f - index * 180f)
    }
    private val enemies = List(formation.monsterCount) { EnemyState(it, dungeon) }
    private val events = mutableListOf<BattleEvent>()
    private var accumulator = 0f
    private var elapsedTime = 0f
    private val enemyHp: Float get() = enemies.sumOf { it.hp.toDouble() }.toFloat()
    private var interruptReady = false
    private var tauntTimer = 3f
    private var finished = false
    private var victory = false

    init {
        enemies.forEach { enemy -> members.forEach { enemy.threat[it.character.id] = 10f } }
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
        enemyMaxHp = dungeon.enemyMaxHp * formation.monsterCount,
        members = members.map {
            MemberSnapshot(it.character, it.hp, it.shield, it.damageDealt, it.healingDone, it.x, it.y,
                if (it.hp <= 0f) MovementIntent.DOWN else if (finished) MovementIntent.HOLD else it.intent,
                attackTarget(it)?.id, it.facingX, it.facingY)
        },
        finished = finished,
        victory = victory,
        abilityCastRemaining = enemies.maxOf { it.castRemaining },
        abilityCastDuration = ABILITY_CAST_TIME,
        protectorId = protector()?.character?.id,
        enemies = enemies.map { enemy ->
            val total = members.filter { it.hp > 0f }.sumOf { enemy.threat.getValue(it.character.id).toDouble() }.toFloat()
            EnemySnapshot(enemy.id, enemy.hp, dungeon.enemyMaxHp, enemy.castRemaining,
                members.associate { member ->
                    member.character.id to if (member.hp > 0f && total > 0f) {
                        enemy.threat.getValue(member.character.id) / total * 100f
                    } else 0f
                }, if (enemy.hp > 0f) threatTarget(enemy)?.character?.id else null, enemy.x, enemy.y,
                enemy.sunderTime, enemy.facingX, enemy.facingY)
        },
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
        enemies.forEach { it.sunderTime = max(0f, it.sunderTime - delta) }
        updateFacing()
        moveCombatants(delta)
        updateFacing()

        members.filter { it.hp > 0f }.forEach { member ->
            member.attackTimer = max(0f, member.attackTimer - delta)
            member.skillTimer = max(0f, member.skillTimer - delta)
            val target = attackTarget(member)
            val inRange = target != null && inRange(member, target)
            if (member.attackTimer <= 0f && inRange) {
                damageEnemy(member, member.character.attackPower)
                member.attackTimer += member.character.attackInterval
            }
            val supportSkill = member.character.skillType in listOf(SkillType.GUARD, SkillType.HEAL)
            if (member.skillTimer <= 0f && (supportSkill || inRange)) {
                useSkill(member)
                member.skillTimer += member.character.skillCooldown
            }
        }

        if (enemyHp <= 0f) {
            finish(true, "${dungeon.enemyName} defeated")
            return
        }

        tauntTimer -= delta
        if (tauntTimer <= 0.0001f) {
            useThreatSwap()
            tauntTimer += 3f
        }

        enemies.filter { it.hp > 0f }.forEach { enemy ->
            if (enemy.castRemaining > 0f) {
                enemy.castRemaining = max(0f, enemy.castRemaining - delta)
                if (interruptReady) {
                    interruptReady = false
                    enemy.castRemaining = 0f
                    cues += CombatCue(CueType.INTERRUPT, enemy.id, enemy.id)
                    log("${enemy.id}: ${dungeon.ability} interrupted")
                } else if (enemy.castRemaining <= 0f) {
                    triggerMechanic(enemy)
                }
            } else {
                enemy.attackTimer = max(0f, enemy.attackTimer - delta)
                val target = threatTarget(enemy)
                if (enemy.attackTimer <= 0f && target != null && distance(enemy.x, enemy.y, target.x, target.y) <= 155f) {
                    enemyAttack(enemy)
                    enemy.attackTimer += enemy.attackInterval
                }
                enemy.mechanicTimer -= delta
                if (enemy.mechanicTimer <= 0f) {
                    enemy.castRemaining = ABILITY_CAST_TIME
                    enemy.mechanicTimer += dungeon.mechanicInterval
                    log("${enemy.id} is casting ${dungeon.ability}")
                }
            }
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
                members.filter { it.hp > 0f }.forEach {
                    it.shield = min(MAX_SHIELD, it.shield + 13f)
                    cues += CombatCue(CueType.SHIELD, member.character.id, it.character.id, skillType = SkillType.GUARD)
                }
                log("${member.character.name} used ${member.character.skillName}: party shielded")
            }

            SkillType.HEAL -> {
                val target = members.filter { it.hp > 0f }.minByOrNull { it.hp / it.character.maxHp } ?: return
                val healed = min(30f, target.character.maxHp - target.hp)
                target.hp += healed
                member.healingDone += healed
                if (healed > 0f) {
                    enemies.filter { it.hp > 0f }.forEach { enemy ->
                        enemy.threat[member.character.id] = enemy.threat.getValue(member.character.id) + healed * 3f
                    }
                    cues += CombatCue(CueType.HEAL, member.character.id, target.character.id, healed, SkillType.HEAL)
                }
                if (healed > 0f) log("${member.character.name} healed ${target.character.name} for ${healed.toInt()}")
            }

            SkillType.STRIKE -> {
                damageEnemy(member, 32f, SkillType.STRIKE)
                log("${member.character.name} used ${member.character.skillName} for 32")
            }

            SkillType.CLEAVE -> {
                val damage = if (dungeon.mechanic == DungeonMechanic.SWARM) 50f else 22f
                enemies.filter { it.hp > 0f }.forEach { damageEnemy(member, damage, SkillType.CLEAVE, it) }
                log("${member.character.name} used ${member.character.skillName} for ${damage.toInt()}")
            }

            SkillType.INTERRUPT -> {
                interruptReady = true
                damageEnemy(member, 10f, SkillType.INTERRUPT)
                log("${member.character.name} prepared an interrupt")
            }

            SkillType.SUNDER -> {
                val target = attackTarget(member) ?: return
                target.sunderTime = 6f
                cues += CombatCue(CueType.SUNDER, member.character.id, target.id, skillType = SkillType.SUNDER)
                damageEnemy(member, 16f, SkillType.SUNDER, target)
                log("${member.character.name} applied healing reduction")
            }
        }
    }

    private fun distance(x: Float, y: Float, tx: Float, ty: Float): Float =
        sqrt((tx - x) * (tx - x) + (ty - y) * (ty - y))

    private fun inRange(member: MemberState, enemy: EnemyState): Boolean =
        distance(member.x, member.y, enemy.x, enemy.y) <= member.character.attackStyle.range

    private fun updateFacing() {
        members.filter { it.hp > 0f }.forEach { member ->
            val target = attackTarget(member) ?: return@forEach
            val length = distance(member.x, member.y, target.x, target.y)
            if (length > 0.001f) {
                member.facingX = (target.x - member.x) / length
                member.facingY = (target.y - member.y) / length
            }
        }
        enemies.filter { it.hp > 0f }.forEach { enemy ->
            val target = threatTarget(enemy) ?: return@forEach
            val length = distance(enemy.x, enemy.y, target.x, target.y)
            if (length > 0.001f) {
                enemy.facingX = (target.x - enemy.x) / length
                enemy.facingY = (target.y - enemy.y) / length
            }
        }
    }

    private fun flank(member: MemberState, enemy: EnemyState, delta: Float) {
        val dx = member.x - enemy.x
        val dy = member.y - enemy.y
        val radius = 132f
        val rearAngle = kotlin.math.atan2(-enemy.facingY, -enemy.facingX)
        val angle = kotlin.math.atan2(dy, dx)
        val difference = kotlin.math.atan2(kotlin.math.sin(rearAngle - angle), kotlin.math.cos(rearAngle - angle))
        // Orbit around the collision body instead of walking through the target.
        val nextAngle = angle + difference.coerceIn(-0.35f, 0.35f)
        val desiredX = (enemy.x + kotlin.math.cos(nextAngle) * radius).coerceIn(180f, 1100f)
        val desiredY = (enemy.y + kotlin.math.sin(nextAngle) * radius).coerceIn(150f, 650f)
        val length = distance(member.x, member.y, desiredX, desiredY)
        if (length < 3f) return
        val step = min(1f, member.character.moveSpeed * delta / length)
        member.x += (desiredX - member.x) * step
        member.y += (desiredY - member.y) * step
        member.intent = MovementIntent.APPROACH
    }

    private fun moveCombatants(delta: Float) {
        members.forEach { member ->
            val wasRetreating = member.intent == MovementIntent.RETREAT
            member.intent = MovementIntent.HOLD
            if (member.hp <= 0f) return@forEach
            val enemy = attackTarget(member) ?: return@forEach
            val melee = member.character.attackStyle == AttackStyle.MELEE
            if (melee && member.character.role == Role.DPS && threatTarget(enemy) !== member) {
                flank(member, enemy, delta)
                return@forEach
            }
            val nearest = enemies.filter { it.hp > 0f }.minByOrNull { distance(member.x, member.y, it.x, it.y) } ?: enemy
            val dangerDistance = distance(member.x, member.y, nearest.x, nearest.y)
            val retreat = !melee && dangerDistance < if (wasRetreating) 290f else 230f
            val targetDistance = distance(member.x, member.y, enemy.x, enemy.y)
            if (retreat || targetDistance > member.character.attackStyle.range - 8f) {
                val target = if (retreat) nearest else enemy
                val length = max(1f, distance(member.x, member.y, target.x, target.y))
                val desired = if (retreat) 290f else member.character.attackStyle.range - 20f
                val amount = min(member.character.moveSpeed * delta, kotlin.math.abs(length - desired))
                val sign = if (retreat) -1f else 1f
                member.x = (member.x + (target.x - member.x) / length * amount * sign).coerceIn(180f, 1100f)
                member.y = (member.y + (target.y - member.y) / length * amount * sign).coerceIn(150f, 650f)
                member.intent = if (retreat) MovementIntent.RETREAT else MovementIntent.APPROACH
            }
        }
        enemies.filter { it.hp > 0f && it.castRemaining <= 0f }.forEach { enemy ->
            val target = threatTarget(enemy) ?: return@forEach
            val length = distance(enemy.x, enemy.y, target.x, target.y)
            if (length > 135f) {
                val step = min(dungeon.enemyMoveSpeed * delta, length - 135f) / length
                enemy.x = (enemy.x + (target.x - enemy.x) * step).coerceIn(320f, 1100f)
                enemy.y = (enemy.y + (target.y - enemy.y) * step).coerceIn(150f, 650f)
            }
        }
        separateCombatants()
    }

    private data class Body(val member: MemberState? = null, val enemy: EnemyState? = null) {
        var x: Float
            get() = member?.x ?: enemy!!.x
            set(value) { if (member != null) member.x = value else enemy!!.x = value }
        var y: Float
            get() = member?.y ?: enemy!!.y
            set(value) { if (member != null) member.y = value else enemy!!.y = value }
        val radius: Float get() = if (member != null) 55f else 65f
    }

    private fun separateCombatants() {
        val bodies = members.filter { it.hp > 0f }.map { Body(member = it) } +
            enemies.filter { it.hp > 0f }.map { Body(enemy = it) }
        // Resolve all pairs, including enemies and opposing teams, before range checks.
        repeat(96) {
            var corrected = false
            for (i in bodies.indices) for (j in i + 1 until bodies.size) {
                val a = bodies[i]
                val b = bodies[j]
                val length = distance(a.x, a.y, b.x, b.y)
                val minimum = a.radius + b.radius
                if (length >= minimum) continue
                corrected = true
                val dx = if (length < 0.001f) 0f else (b.x - a.x) / length
                val dy = if (length < 0.001f) 1f else (b.y - a.y) / length
                val push = (minimum - length + 0.02f) * 0.5f
                a.x = (a.x - dx * push).coerceIn(180f, 1100f)
                a.y = (a.y - dy * push).coerceIn(150f, 650f)
                b.x = (b.x + dx * push).coerceIn(180f, 1100f)
                b.y = (b.y + dy * push).coerceIn(150f, 650f)
                if (push > 0.5f) listOfNotNull(a.member, b.member).forEach {
                    if (it.intent == MovementIntent.HOLD) it.intent = MovementIntent.SPACING
                }
            }
            if (!corrected) return
        }
    }

    private fun enemyAttack(enemy: EnemyState) {
        val target = threatTarget(enemy) ?: return
        val reduction = if (target.character.role == Role.TANK) 0.68f else 1f
        damageMember(target, dungeon.enemyAttack * reduction, enemy.id)
    }

    // Equal threat uses formation order. Dead characters never participate in target selection.
    private fun threatTarget(enemy: EnemyState): MemberState? =
        members.filter { it.hp > 0f }.maxWithOrNull(
            compareBy<MemberState> { enemy.threat.getValue(it.character.id) }
                .thenBy { if (it.character.id == enemy.tieTargetId) 1 else 0 },
        )

    private fun useThreatSwap() {
        val defender = protector() ?: return
        val enemy = attackTarget(defender) ?: return
        val previous = threatTarget(enemy) ?: return
        if (previous === defender) return
        val defenderId = defender.character.id
        val previousId = previous.character.id
        val defenderThreat = enemy.threat.getValue(defenderId)
        enemy.threat[defenderId] = enemy.threat.getValue(previousId)
        enemy.threat[previousId] = defenderThreat
        enemy.tieTargetId = defenderId
        defender.skillUses++
        cues += CombatCue(CueType.TAUNT, defenderId, enemy.id)
        log("${defender.character.name} used THREAT SWAP on ${enemy.id} with ${previous.character.name}")
    }

    private fun protector(): MemberState? =
        members.filter { it.hp > 0f }.maxByOrNull { it.character.maxHp }

    private fun attackTarget(member: MemberState): EnemyState? {
        val alive = enemies.filter { it.hp > 0f }
        if (member === protector()) {
            // First rescue the ally under the greatest threat deficit; ties retain monster order.
            val unsecured = alive.filter { threatTarget(it) !== member }
            if (unsecured.isNotEmpty()) {
                return unsecured.maxByOrNull { enemy ->
                    val target = threatTarget(enemy)
                    (target?.let { enemy.threat.getValue(it.character.id) } ?: 0f) -
                        enemy.threat.getValue(member.character.id)
                }
            }
        }
        return alive.firstOrNull()
    }

    private fun triggerMechanic(enemy: EnemyState) {
        when (dungeon.mechanic) {
            DungeonMechanic.BURST -> {
                cues += CombatCue(CueType.WAVE, enemy.id, enemy.id)
                members.filter { it.hp > 0f }.forEach { damageMember(it, 22f, enemy.id) }
                log("${dungeon.ability} hit the entire raid (${formation.monsterCount} monster(s))")
            }

            DungeonMechanic.SWARM -> {
                cues += CombatCue(CueType.WAVE, enemy.id, enemy.id)
                members.filter { it.hp > 0f }.forEach { damageMember(it, 11f, enemy.id) }
                log("${dungeon.ability} released a swarm wave")
            }

            DungeonMechanic.REGEN -> {
                val recovery = min(dungeon.enemyMaxHp - enemy.hp, if (enemy.sunderTime > 0f) 7f else 32f)
                enemy.hp += recovery
                cues += CombatCue(CueType.HEAL, enemy.id, enemy.id, recovery)
                log("${dungeon.ability}: ${enemy.id} regenerated ${recovery.toInt()} HP")
            }
        }
    }

    private fun damageEnemy(
        source: MemberState, amount: Float, skillType: SkillType? = null,
        target: EnemyState? = attackTarget(source),
    ) {
        if (target == null || target.hp <= 0f || !inRange(source, target)) return
        val rear = BattleFacing.isRear(source.x - target.x, source.y - target.y, target.facingX, target.facingY)
        val applied = min(BattleFacing.damage(amount, rear), target.hp)
        target.hp -= applied
        source.damageDealt += applied
        val multiplier = if (source === protector() || source.character.role == Role.TANK) 5f else 1f
        target.threat[source.character.id] = target.threat.getValue(source.character.id) + applied * multiplier
        if (applied > 0f) {
            cues += CombatCue(CueType.HIT, source.character.id, target.id, applied, skillType, rear)
        }
        if (target.hp <= 0f) {
            target.castRemaining = 0f
            log("${target.id} defeated")
        }
    }

    private fun damageMember(target: MemberState, amount: Float, enemyId: String) {
        val source = enemies.first { it.id == enemyId }
        val rear = BattleFacing.isRear(source.x - target.x, source.y - target.y, target.facingX, target.facingY)
        val damage = BattleFacing.damage(amount, rear)
        val absorbed = min(target.shield, damage)
        target.shield -= absorbed
        val hpDamage = min(target.hp, damage - absorbed)
        target.hp = max(0f, target.hp - hpDamage)
        target.damageTaken += hpDamage
        cues += CombatCue(CueType.HIT, enemyId, target.character.id, hpDamage, rearAttack = rear)
        if (target.hp <= 0f) log("${target.character.name} was defeated")
    }

    private fun finish(didWin: Boolean, message: String) {
        victory = didWin
        finished = true
        enemies.forEach { it.castRemaining = 0f }
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
        private const val ABILITY_CAST_TIME = 1.2f
    }
}
