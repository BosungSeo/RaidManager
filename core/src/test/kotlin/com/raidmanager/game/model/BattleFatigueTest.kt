package com.raidmanager.game.model

import com.raidmanager.game.model.BattleSimulator.CueType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BattleFatigueTest {
    private val dungeon = PrototypeContent.dungeons.first().copy(
        enemyMaxHp = 100000f, enemyAttack = 0f, mechanicInterval = 1000f, timeLimit = 180f,
    )

    @Test
    fun tankLosesCurrentHpWithoutHitCuesAndOtherRolesKeepHp() {
        val heroes = PrototypeContent.characters.take(3).map { it.copy(skillCooldown = 1000f) }
        val simulator = BattleSimulator(RaidFormation(heroes, 1), dungeon)
        repeat(100) { simulator.update(0.1f) }
        val snapshot = simulator.snapshot()
        val tank = snapshot.members.first()
        assertEquals(heroes.first().maxHp * 0.95f, tank.hp, 0.01f)
        assertEquals(heroes.first().maxHp * 0.05f, tank.fatigueHpLoss, 0.01f)
        assertEquals(0.05f, tank.fatigue, 0.001f)
        snapshot.members.drop(1).forEach { assertEquals(it.character.maxHp, it.hp) }
        assertTrue(simulator.drainCombatCues().none { it.target == tank.character.id && it.amount > 0f })
    }

    @Test
    fun skillDamageUsesFatigueButNormalAttacksKeepTheirPower() {
        val simulator = BattleSimulator(RaidFormation(PrototypeContent.characters.take(3), 1), dungeon)
        var skills = 0
        var attacks = 0
        repeat(200) {
            simulator.update(0.1f)
            val snapshot = simulator.snapshot()
            simulator.drainCombatCues().filter { it.source == "rook" && it.type == CueType.HIT }.forEach { cue ->
                val rook = snapshot.members.first { it.character.id == "rook" }
                val base = if (cue.skillType == null) rook.character.attackPower else BattleRules.STRIKE_DAMAGE
                val multiplier = if (cue.skillType == null) 1f else rook.skillDamageMultiplier
                assertEquals(BattleFacing.damage(base * multiplier, cue.rearAttack), cue.amount, 0.001f)
                if (cue.skillType == null) attacks++ else skills++
            }
        }
        assertTrue(skills >= 2 && attacks >= 2)
    }

    @Test
    fun healerOutputDeclinesAndMatchesActualUncappedHeals() {
        val simulator = BattleSimulator(RaidFormation(PrototypeContent.characters.take(3), 1),
            dungeon.copy(enemyAttack = 12f, enemyAttackInterval = 0.8f))
        var fullHeals = 0
        repeat(250) {
            val before = simulator.snapshot()
            simulator.update(0.1f)
            val after = simulator.snapshot()
            simulator.drainCombatCues().filter { it.source == "luna" && it.type == CueType.HEAL }.forEach { cue ->
                val healer = after.members.first { it.character.id == "luna" }
                val limit = BattleRules.HEAL_AMOUNT * healer.healingMultiplier
                assertTrue(cue.amount <= limit + 0.001f)
                val target = before.members.first { it.character.id == cue.target }
                if (target.character.maxHp - target.hp > limit) {
                    assertEquals(limit, cue.amount, 0.001f)
                    fullHeals++
                }
            }
        }
        assertTrue(fullHeals > 0)
    }

    @Test
    fun supportWaitsForCastAndLaterCastsTakeLonger() {
        val simulator = BattleSimulator(RaidFormation(PrototypeContent.characters.takeLast(3), 1), dungeon)
        val casts = mutableListOf<Float>()
        repeat(250) {
            val before = simulator.snapshot().members.first { it.character.id == "mira" }
            simulator.update(0.1f)
            val after = simulator.snapshot().members.first { it.character.id == "mira" }
            val cues = simulator.drainCombatCues()
            if (before.skillCastRemaining == 0f && after.skillCastRemaining > 0f) casts += after.skillCastDuration
            if (after.skillCastRemaining > 0f) {
                assertTrue(cues.none { it.source == "mira" && it.skillType == SkillType.SUNDER })
            }
            if (before.skillCastRemaining > 0.1001f) {
                assertEquals(before.skillCastDuration, after.skillCastDuration)
                assertEquals(before.skillCastRemaining - 0.1f, after.skillCastRemaining, 0.001f)
            }
        }
        assertTrue(casts.size >= 2)
        assertTrue(casts.last() > casts.first())
    }

    @Test
    fun penaltiesAreBoundedAndFinishedBattleFreezesAndNewBattleResets() {
        assertEquals(0.7f, BattleFatigue.penalty(1000f))
        assertEquals(0.3f, BattleFatigue.skillMultiplier(Role.DPS, 1000f), 0.001f)
        assertEquals(1f, BattleFatigue.skillMultiplier(Role.TANK, 1000f))
        assertEquals(1f, BattleFatigue.healMultiplier(Role.SUPPORT, 1000f))
        val formation = RaidFormation(PrototypeContent.characters.take(3), 1)
        val simulator = BattleSimulator(formation, dungeon.copy(timeLimit = 2f))
        repeat(30) { simulator.update(0.1f) }
        val finished = simulator.snapshot()
        assertTrue(finished.finished)
        repeat(30) { simulator.update(0.1f) }
        assertEquals(finished, simulator.snapshot())
        BattleSimulator(formation, dungeon).snapshot().members.forEach {
            assertEquals(0f, it.fatigue)
            assertEquals(it.character.maxHp, it.hp)
        }
    }
}
