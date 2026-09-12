package com.raidmanager.game.model

import com.raidmanager.game.model.BattleRules.STEP
import com.raidmanager.game.model.BattleRules.MAX_FRAME_DELTA
import com.raidmanager.game.model.BattleRules.MAX_SHIELD
import com.raidmanager.game.model.BattleRules.ABILITY_CAST_TIME
import com.raidmanager.game.model.BattleRules.ENEMY_INTERVAL_OFFSET
import com.raidmanager.game.model.BattleRules.ENEMY_OPENING_ATTACK
import com.raidmanager.game.model.BattleRules.ENEMY_ATTACK_PHASE
import com.raidmanager.game.model.BattleRules.ENEMY_MECHANIC_PHASE
import com.raidmanager.game.model.BattleRules.ENEMY_START_X
import com.raidmanager.game.model.BattleRules.ENEMY_START_Y
import com.raidmanager.game.model.BattleRules.ENEMY_SPACING_X
import com.raidmanager.game.model.BattleRules.ENEMY_SPACING_Y
import com.raidmanager.game.model.BattleRules.INITIAL_SKILL_RATIO
import com.raidmanager.game.model.BattleRules.PHASE_BUCKETS
import com.raidmanager.game.model.BattleRules.PHASE_DIVISOR
import com.raidmanager.game.model.BattleRules.OPENING_TIMER_RATIO
import com.raidmanager.game.model.BattleRules.ATTACK_PHASE_RATIO
import com.raidmanager.game.model.BattleRules.SKILL_PHASE_RATIO
import com.raidmanager.game.model.BattleRules.MELEE_START_X
import com.raidmanager.game.model.BattleRules.RANGED_START_X
import com.raidmanager.game.model.BattleRules.MEMBER_START_Y
import com.raidmanager.game.model.BattleRules.MEMBER_SPACING_Y
import com.raidmanager.game.model.BattleRules.TAUNT_INTERVAL
import com.raidmanager.game.model.BattleRules.INITIAL_THREAT
import com.raidmanager.game.model.BattleRules.TIMER_EPSILON
import com.raidmanager.game.model.BattleRules.ENEMY_ATTACK_RANGE
import com.raidmanager.game.model.BattleRules.GUARD_SHIELD
import com.raidmanager.game.model.BattleRules.HEAL_AMOUNT
import com.raidmanager.game.model.BattleRules.HEAL_THREAT_MULTIPLIER
import com.raidmanager.game.model.BattleRules.STRIKE_DAMAGE
import com.raidmanager.game.model.BattleRules.CLEAVE_SWARM_DAMAGE
import com.raidmanager.game.model.BattleRules.CLEAVE_DAMAGE
import com.raidmanager.game.model.BattleRules.INTERRUPT_DAMAGE
import com.raidmanager.game.model.BattleRules.SUNDER_DURATION
import com.raidmanager.game.model.BattleRules.SUNDER_DAMAGE
import com.raidmanager.game.model.BattleRules.DIRECTION_EPSILON
import com.raidmanager.game.model.BattleRules.FLANK_RADIUS
import com.raidmanager.game.model.BattleRules.FLANK_ANGLE_STEP
import com.raidmanager.game.model.BattleRules.MIN_X
import com.raidmanager.game.model.BattleRules.MAX_X
import com.raidmanager.game.model.BattleRules.MIN_Y
import com.raidmanager.game.model.BattleRules.MAX_Y
import com.raidmanager.game.model.BattleRules.FLANK_STOP_DISTANCE
import com.raidmanager.game.model.BattleRules.RETREAT_END_RANGE
import com.raidmanager.game.model.BattleRules.RETREAT_START_RANGE
import com.raidmanager.game.model.BattleRules.APPROACH_MARGIN
import com.raidmanager.game.model.BattleRules.APPROACH_STOP_MARGIN
import com.raidmanager.game.model.BattleRules.MIN_MOVEMENT_DISTANCE
import com.raidmanager.game.model.BattleRules.ENEMY_STOP_RANGE
import com.raidmanager.game.model.BattleRules.ENEMY_MIN_X
import com.raidmanager.game.model.BattleRules.MEMBER_RADIUS
import com.raidmanager.game.model.BattleRules.ENEMY_RADIUS
import com.raidmanager.game.model.BattleRules.SEPARATION_ITERATIONS
import com.raidmanager.game.model.BattleRules.SEPARATION_PADDING
import com.raidmanager.game.model.BattleRules.SEPARATION_SHARE
import com.raidmanager.game.model.BattleRules.SPACING_INTENT_THRESHOLD
import com.raidmanager.game.model.BattleRules.TANK_DAMAGE_MULTIPLIER
import com.raidmanager.game.model.BattleRules.BURST_DAMAGE
import com.raidmanager.game.model.BattleRules.SWARM_DAMAGE
import com.raidmanager.game.model.BattleRules.REDUCED_REGEN
import com.raidmanager.game.model.BattleRules.REGEN_AMOUNT
import com.raidmanager.game.model.BattleRules.PROTECTOR_THREAT_MULTIPLIER
import com.raidmanager.game.model.BattleRules.PERCENT_SCALE
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
        val attackInterval = dungeon.enemyAttackInterval * (1f + index * ENEMY_INTERVAL_OFFSET)
        var attackTimer = dungeon.enemyAttackInterval * (ENEMY_OPENING_ATTACK + index * ENEMY_ATTACK_PHASE)
        var mechanicTimer = dungeon.mechanicInterval + index * ENEMY_MECHANIC_PHASE
        var castRemaining = 0f
        var sunderTime = 0f
        var facingX = -1f
        var facingY = 0f
        val threat = mutableMapOf<String, Float>()
        var tieTargetId: String? = null
        var x = ENEMY_START_X + index * ENEMY_SPACING_X
        var y = ENEMY_START_Y - index * ENEMY_SPACING_Y
    }

    private data class MemberState(
        val character: CharacterDefinition,
        var hp: Float = character.maxHp,
        var shield: Float = 0f,
        var attackTimer: Float = character.attackInterval,
        var skillTimer: Float = character.skillCooldown * INITIAL_SKILL_RATIO,
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
        // 식별자 해시로 시작 시점을 분산하여 전투 재현성을 유지하면서 동시 행동을 줄인다.
        val phase = (character.id.hashCode() and Int.MAX_VALUE) % PHASE_BUCKETS / PHASE_DIVISOR
        MemberState(character, attackTimer = character.attackInterval * (OPENING_TIMER_RATIO + phase * ATTACK_PHASE_RATIO),
            skillTimer = character.skillCooldown * (OPENING_TIMER_RATIO + phase * SKILL_PHASE_RATIO),
            x = if (character.attackStyle == AttackStyle.MELEE) MELEE_START_X else RANGED_START_X,
            y = MEMBER_START_Y - index * MEMBER_SPACING_Y,
        )
    }
    private val enemies = List(formation.monsterCount) { EnemyState(it, dungeon) }
    private val events = mutableListOf<BattleEvent>()
    private var accumulator = 0f
    private var elapsedTime = 0f
    private val enemyHp: Float get() = enemies.sumOf { it.hp.toDouble() }.toFloat()
    private var interruptReady = false
    private var tauntTimer = TAUNT_INTERVAL
    private var finished = false
    private var victory = false

    init {
        enemies.forEach { enemy -> members.forEach { enemy.threat[it.character.id] = INITIAL_THREAT } }
        log("Battle started against ${dungeon.enemyName}")
    }

    /** 프레임 시간을 고정 간격으로 누적·소비하여 렌더링 빈도와 전투 속도를 분리한다. */
    fun update(delta: Float) {
        if (finished) return

        accumulator += delta.coerceAtMost(MAX_FRAME_DELTA)
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
                        enemy.threat.getValue(member.character.id) / total * PERCENT_SCALE
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
        return BattleResult(
            victory, elapsedTime, enemyHp, memberResults, events.toList(),
            BattleAnalysis.summarize(victory, dungeon, memberResults),
        )
    }

    private fun simulateStep(delta: Float) {
        elapsedTime += delta
        enemies.forEach { it.sunderTime = max(0f, it.sunderTime - delta) }
        updateFacing()
        moveCombatants(delta)
        updateFacing()

        updateMembers(delta)

        if (enemyHp <= 0f) {
            finish(true, "${dungeon.enemyName} defeated")
            return
        }

        tauntTimer -= delta
        if (tauntTimer <= TIMER_EPSILON) {
            useThreatSwap()
            tauntTimer += TAUNT_INTERVAL
        }

        updateEnemies(delta)

        if (members.none { it.hp > 0f }) {
            finish(false, "Raid wiped out")
        } else if (elapsedTime >= dungeon.timeLimit) {
            finish(false, "Time limit reached")
        }
    }

    private fun updateMembers(delta: Float) {
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
    }

    private fun updateEnemies(delta: Float) {
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
                if (enemy.attackTimer <= 0f && target != null && distance(enemy.x, enemy.y, target.x, target.y) <= ENEMY_ATTACK_RANGE) {
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
    }

    private fun useSkill(member: MemberState) {
        member.skillUses += 1
        when (member.character.skillType) {
            SkillType.GUARD -> {
                members.filter { it.hp > 0f }.forEach {
                    it.shield = min(MAX_SHIELD, it.shield + GUARD_SHIELD)
                    cues += CombatCue(CueType.SHIELD, member.character.id, it.character.id, skillType = SkillType.GUARD)
                }
                log("${member.character.name} used ${member.character.skillName}: party shielded")
            }

            SkillType.HEAL -> {
                val target = members.filter { it.hp > 0f }.minByOrNull { it.hp / it.character.maxHp } ?: return
                val healed = min(HEAL_AMOUNT, target.character.maxHp - target.hp)
                target.hp += healed
                member.healingDone += healed
                if (healed > 0f) {
                    enemies.filter { it.hp > 0f }.forEach { enemy ->
                        enemy.threat[member.character.id] = enemy.threat.getValue(member.character.id) + healed * HEAL_THREAT_MULTIPLIER
                    }
                    cues += CombatCue(CueType.HEAL, member.character.id, target.character.id, healed, SkillType.HEAL)
                }
                if (healed > 0f) log("${member.character.name} healed ${target.character.name} for ${healed.toInt()}")
            }

            SkillType.STRIKE -> {
                damageEnemy(member, STRIKE_DAMAGE, SkillType.STRIKE)
                log("${member.character.name} used ${member.character.skillName} for ${STRIKE_DAMAGE.toInt()}")
            }

            SkillType.CLEAVE -> {
                val damage = if (dungeon.mechanic == DungeonMechanic.SWARM) CLEAVE_SWARM_DAMAGE else CLEAVE_DAMAGE
                enemies.filter { it.hp > 0f }.forEach { damageEnemy(member, damage, SkillType.CLEAVE, it) }
                log("${member.character.name} used ${member.character.skillName} for ${damage.toInt()}")
            }

            SkillType.INTERRUPT -> {
                interruptReady = true
                damageEnemy(member, INTERRUPT_DAMAGE, SkillType.INTERRUPT)
                log("${member.character.name} prepared an interrupt")
            }

            SkillType.SUNDER -> {
                val target = attackTarget(member) ?: return
                target.sunderTime = SUNDER_DURATION
                cues += CombatCue(CueType.SUNDER, member.character.id, target.id, skillType = SkillType.SUNDER)
                damageEnemy(member, SUNDER_DAMAGE, SkillType.SUNDER, target)
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
            if (length > DIRECTION_EPSILON) {
                member.facingX = (target.x - member.x) / length
                member.facingY = (target.y - member.y) / length
            }
        }
        enemies.filter { it.hp > 0f }.forEach { enemy ->
            val target = threatTarget(enemy) ?: return@forEach
            val length = distance(enemy.x, enemy.y, target.x, target.y)
            if (length > DIRECTION_EPSILON) {
                enemy.facingX = (target.x - enemy.x) / length
                enemy.facingY = (target.y - enemy.y) / length
            }
        }
    }

    /** 적의 반대 방향과 현재 각도의 차이를 정규화한 뒤 제한된 각도만큼 원주를 따라 이동한다. */
    private fun flank(member: MemberState, enemy: EnemyState, delta: Float) {
        val dx = member.x - enemy.x
        val dy = member.y - enemy.y
        val radius = FLANK_RADIUS
        val rearAngle = kotlin.math.atan2(-enemy.facingY, -enemy.facingX)
        val angle = kotlin.math.atan2(dy, dx)
        val difference = kotlin.math.atan2(kotlin.math.sin(rearAngle - angle), kotlin.math.cos(rearAngle - angle))
        // 충돌 영역을 통과하지 않도록 원주를 따라 후방으로 회전한다.
        val nextAngle = angle + difference.coerceIn(-FLANK_ANGLE_STEP, FLANK_ANGLE_STEP)
        val desiredX = (enemy.x + kotlin.math.cos(nextAngle) * radius).coerceIn(MIN_X, MAX_X)
        val desiredY = (enemy.y + kotlin.math.sin(nextAngle) * radius).coerceIn(MIN_Y, MAX_Y)
        val length = distance(member.x, member.y, desiredX, desiredY)
        if (length < FLANK_STOP_DISTANCE) return
        val step = min(1f, member.character.moveSpeed * delta / length)
        member.x += (desiredX - member.x) * step
        member.y += (desiredY - member.y) * step
        member.intent = MovementIntent.APPROACH
    }

    /** 사거리와 후퇴 이력을 이용해 접근·후퇴를 결정하고, 이동 후 충돌 겹침을 해소한다. */
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
            val retreat = !melee && dangerDistance < if (wasRetreating) RETREAT_END_RANGE else RETREAT_START_RANGE
            val targetDistance = distance(member.x, member.y, enemy.x, enemy.y)
            if (retreat || targetDistance > member.character.attackStyle.range - APPROACH_MARGIN) {
                val target = if (retreat) nearest else enemy
                val length = max(MIN_MOVEMENT_DISTANCE, distance(member.x, member.y, target.x, target.y))
                val desired = if (retreat) RETREAT_END_RANGE else member.character.attackStyle.range - APPROACH_STOP_MARGIN
                val amount = min(member.character.moveSpeed * delta, kotlin.math.abs(length - desired))
                val sign = if (retreat) -1f else 1f
                member.x = (member.x + (target.x - member.x) / length * amount * sign).coerceIn(MIN_X, MAX_X)
                member.y = (member.y + (target.y - member.y) / length * amount * sign).coerceIn(MIN_Y, MAX_Y)
                member.intent = if (retreat) MovementIntent.RETREAT else MovementIntent.APPROACH
            }
        }
        enemies.filter { it.hp > 0f && it.castRemaining <= 0f }.forEach { enemy ->
            val target = threatTarget(enemy) ?: return@forEach
            val length = distance(enemy.x, enemy.y, target.x, target.y)
            if (length > ENEMY_STOP_RANGE) {
                val step = min(dungeon.enemyMoveSpeed * delta, length - ENEMY_STOP_RANGE) / length
                enemy.x = (enemy.x + (target.x - enemy.x) * step).coerceIn(ENEMY_MIN_X, MAX_X)
                enemy.y = (enemy.y + (target.y - enemy.y) * step).coerceIn(MIN_Y, MAX_Y)
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
        val radius: Float get() = if (member != null) MEMBER_RADIUS else ENEMY_RADIUS
    }

    /** 충돌 원이 겹치는 쌍을 중심 연결 방향으로 반씩 밀어내고 경계 안에서 반복 보정한다. */
    private fun separateCombatants() {
        val bodies = members.filter { it.hp > 0f }.map { Body(member = it) } +
            enemies.filter { it.hp > 0f }.map { Body(enemy = it) }
        // 사거리 판정 전에 진영에 관계없이 모든 생존 개체 쌍의 겹침을 해소한다.
        repeat(SEPARATION_ITERATIONS) {
            var corrected = false
            for (i in bodies.indices) for (j in i + 1 until bodies.size) {
                val a = bodies[i]
                val b = bodies[j]
                val length = distance(a.x, a.y, b.x, b.y)
                val minimum = a.radius + b.radius
                if (length >= minimum) continue
                corrected = true
                val dx = if (length < DIRECTION_EPSILON) 0f else (b.x - a.x) / length
                val dy = if (length < DIRECTION_EPSILON) 1f else (b.y - a.y) / length
                val push = (minimum - length + SEPARATION_PADDING) * SEPARATION_SHARE
                a.x = (a.x - dx * push).coerceIn(MIN_X, MAX_X)
                a.y = (a.y - dy * push).coerceIn(MIN_Y, MAX_Y)
                b.x = (b.x + dx * push).coerceIn(MIN_X, MAX_X)
                b.y = (b.y + dy * push).coerceIn(MIN_Y, MAX_Y)
                if (push > SPACING_INTENT_THRESHOLD) listOfNotNull(a.member, b.member).forEach {
                    if (it.intent == MovementIntent.HOLD) it.intent = MovementIntent.SPACING
                }
            }
            if (!corrected) return
        }
    }

    private fun enemyAttack(enemy: EnemyState) {
        val target = threatTarget(enemy) ?: return
        val reduction = if (target.character.role == Role.TANK) TANK_DAMAGE_MULTIPLIER else 1f
        damageMember(target, dungeon.enemyAttack * reduction, enemy.id)
    }

    // 위협 동률은 교환 대상 우선 후 편성 순서를 따르며, 사망자는 제외한다.
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

    /** 보호 담당자는 위협 부족분이 가장 큰 적을, 다른 캐릭터는 첫 생존 적을 선택한다. */
    private fun attackTarget(member: MemberState): EnemyState? {
        val alive = enemies.filter { it.hp > 0f }
        if (member === protector()) {
            // 보호 담당자의 위협 부족분이 가장 큰 적을 우선하며, 동률이면 몬스터 순서를 유지한다.
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
                members.filter { it.hp > 0f }.forEach { damageMember(it, BURST_DAMAGE, enemy.id) }
                log("${dungeon.ability} hit the entire raid (${formation.monsterCount} monster(s))")
            }

            DungeonMechanic.SWARM -> {
                cues += CombatCue(CueType.WAVE, enemy.id, enemy.id)
                members.filter { it.hp > 0f }.forEach { damageMember(it, SWARM_DAMAGE, enemy.id) }
                log("${dungeon.ability} released a swarm wave")
            }

            DungeonMechanic.REGEN -> {
                val recovery = min(dungeon.enemyMaxHp - enemy.hp, if (enemy.sunderTime > 0f) REDUCED_REGEN else REGEN_AMOUNT)
                enemy.hp += recovery
                cues += CombatCue(CueType.HEAL, enemy.id, enemy.id, recovery)
                log("${dungeon.ability}: ${enemy.id} regenerated ${recovery.toInt()} HP")
            }
        }
    }

    /** 후방 배율을 적용한 실제 피해를 체력으로 제한하고 역할에 따라 위협을 누적한다. */
    private fun damageEnemy(
        source: MemberState, amount: Float, skillType: SkillType? = null,
        target: EnemyState? = attackTarget(source),
    ) {
        if (target == null || target.hp <= 0f || !inRange(source, target)) return
        val rear = BattleFacing.isRear(source.x - target.x, source.y - target.y, target.facingX, target.facingY)
        val applied = min(BattleFacing.damage(amount, rear), target.hp)
        target.hp -= applied
        source.damageDealt += applied
        val multiplier = if (source === protector() || source.character.role == Role.TANK) PROTECTOR_THREAT_MULTIPLIER else 1f
        target.threat[source.character.id] = target.threat.getValue(source.character.id) + applied * multiplier
        if (applied > 0f) {
            cues += CombatCue(CueType.HIT, source.character.id, target.id, applied, skillType, rear)
        }
        if (target.hp <= 0f) {
            target.castRemaining = 0f
            log("${target.id} defeated")
        }
    }

    /** 후방 피해에서 보호막을 먼저 소모한 뒤 남은 피해만 체력과 피격 통계에 반영한다. */
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

    private fun log(message: String) {
        events += BattleEvent(elapsedTime, message)
    }

}
