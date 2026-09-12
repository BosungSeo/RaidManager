package com.raidmanager.game.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BattleSimulatorTest {
    @Test
    fun `attacks wait for range while melee closes and ranged keeps distance`() {
        val simulator = BattleSimulator(formation("aegis", "luna", "rook"),
            PrototypeContent.dungeons.first().copy(enemyMaxHp = 10000f, enemyAttack = 0f, mechanicInterval = 100f))
        val start = simulator.snapshot()
        simulator.update(0.1f)
        assertTrue(simulator.drainCombatCues().none { it.type == BattleSimulator.CueType.HIT })
        assertTrue(simulator.snapshot().members.first().x > start.members.first().x)
        val attacked = mutableSetOf<String>()
        repeat(100) {
            simulator.update(0.1f)
            val snapshot = simulator.snapshot()
            simulator.drainCombatCues().filter { it.type == BattleSimulator.CueType.HIT && !it.source.startsWith("boss") }
                .forEach { cue ->
                    val member = snapshot.members.first { it.character.id == cue.source }
                    val enemy = snapshot.enemies.first { it.id == cue.target }
                    val distance = kotlin.math.hypot(member.x - enemy.x, member.y - enemy.y)
                    assertTrue(distance <= member.character.attackStyle.range + 0.01f)
                    attacked += cue.source
                }
        }
        assertEquals(setOf("aegis", "luna", "rook"), attacked)
        val members = simulator.snapshot().members
        assertTrue(members.first { it.character.id == "luna" }.x < members.first { it.character.id == "rook" }.x - 150f)
    }

    @Test
    fun `ranged retreats from pursuing enemy and positions freeze after finish`() {
        val simulator = BattleSimulator(formation("luna", "ember", "mira"),
            PrototypeContent.dungeons.first().copy(enemyMaxHp = 10000f, enemyAttack = 0f, mechanicInterval = 100f))
        var retreated = false
        repeat(470) {
            simulator.update(0.1f)
            retreated = retreated || simulator.snapshot().members.any { it.intent == BattleSimulator.MovementIntent.RETREAT }
        }
        assertTrue(retreated, "Ranged heroes retreat only when an enemy enters their safety distance")
        val finished = simulator.snapshot()
        assertTrue(finished.finished)
        repeat(10) { simulator.update(0.1f) }
        assertEquals(finished, simulator.snapshot())
    }

    @Test
    fun `all living combatants maintain collision spacing for every formation`() {
        allFormations().forEach { raid ->
            val simulator = BattleSimulator(raid.copy(monsterCount = 3),
                PrototypeContent.dungeons.first().copy(enemyMaxHp = 10000f, enemyAttack = 0f))
            repeat(400) {
                simulator.update(0.1f)
                val snapshot = simulator.snapshot()
                val bodies = snapshot.members.filter { it.hp > 0f }.map { Triple(it.x, it.y, 55f) } +
                    snapshot.enemies.filter { it.hp > 0f }.map { Triple(it.x, it.y, 65f) }
                for (i in bodies.indices) for (j in i + 1 until bodies.size) {
                    val a = bodies[i]
                    val b = bodies[j]
                    assertTrue(kotlin.math.hypot(a.first - b.first, a.second - b.second) >= a.third + b.third - 0.1f,
                        "Overlap at ${snapshot.elapsedTime}: $i / $j in ${raid.members.map { it.id }}")
                }
            }
        }
    }

    @Test
    fun `ranged hero holds position when target moves within safe range`() {
        val simulator = BattleSimulator(formation("aegis", "luna", "rook"),
            PrototypeContent.dungeons.first().copy(enemyMaxHp = 10000f, enemyAttack = 0f))
        var heldSteps = 0
        repeat(100) {
            val before = simulator.snapshot().members[1]
            simulator.update(0.1f)
            val after = simulator.snapshot().members[1]
            if (after.intent == BattleSimulator.MovementIntent.HOLD) {
                assertEquals(before.x, after.x, 0.1f)
                assertEquals(before.y, after.y, 0.1f)
                heldSteps++
            }
        }
        assertTrue(heldSteps > 20)
    }

    @Test
    fun `threat swap exchanges percentages on one enemy at three second cadence`() {
        val heroes = formation("luna", "rook", "mira").members.map {
            it.copy(attackInterval = if (it.id == "rook") 1f else 100f, skillCooldown = 100f)
        }
        val simulator = BattleSimulator(RaidFormation(heroes, 3),
            PrototypeContent.dungeons.first().copy(enemyMaxHp = 10000f, enemyAttack = 0f, mechanicInterval = 100f))
        repeat(29) {
            simulator.update(0.1f)
            assertTrue(simulator.drainCombatCues().none { it.type == BattleSimulator.CueType.TAUNT })
        }
        val before = simulator.snapshot().enemies
        simulator.update(0.1f)
        val cue = simulator.drainCombatCues().single { it.type == BattleSimulator.CueType.TAUNT }
        val after = simulator.snapshot().enemies
        val old = before.first { it.id == cue.target }
        val updated = after.first { it.id == cue.target }
        assertEquals("mira", cue.source)
        assertEquals(old.aggro.getValue("rook"), updated.aggro.getValue("mira"), 0.001f)
        assertEquals(old.aggro.getValue("mira"), updated.aggro.getValue("rook"), 0.001f)
        assertEquals("mira", updated.targetId)
        before.filter { it.id != cue.target }.forEach { enemy ->
            assertEquals(enemy.aggro, after.first { it.id == enemy.id }.aggro)
        }
        repeat(29) {
            simulator.update(0.1f)
            assertTrue(simulator.drainCombatCues().none { it.type == BattleSimulator.CueType.TAUNT })
        }
        simulator.update(0.1f)
        assertEquals(1, simulator.drainCombatCues().count { it.type == BattleSimulator.CueType.TAUNT })
    }

    @Test
    fun `highest max hp hero protects allies regardless of class`() {
        val raid = formation("luna", "rook", "mira")
        val simulator = BattleSimulator(raid.copy(monsterCount = 3),
            PrototypeContent.dungeons.first().copy(enemyMaxHp = 10000f, enemyAttack = 0f, mechanicInterval = 100f))
        assertEquals("mira", simulator.snapshot().protectorId)
        val attacked = mutableSetOf<String>()
        repeat(50) {
            simulator.update(0.1f)
            attacked += simulator.drainCombatCues().filter { it.source == "mira" && it.type == BattleSimulator.CueType.HIT }
                .map { it.target }
        }
        assertEquals(3, attacked.size)
        assertTrue(simulator.snapshot().enemies.all { it.targetId == "mira" })
        assertEquals(Role.SUPPORT, raid.members.last().role)
    }

    @Test
    fun `aggro begins equally and is calculated separately for each monster`() {
        val simulator = BattleSimulator(formation("aegis", "luna", "rook").copy(monsterCount = 2),
            PrototypeContent.dungeons.first())
        simulator.snapshot().enemies.forEach { enemy ->
            enemy.aggro.values.forEach { assertEquals(100f / 3f, it, 0.001f) }
        }
        repeat(12) { simulator.update(0.1f) }
        val enemies = simulator.snapshot().enemies
        assertTrue(enemies[0].aggro != enemies[1].aggro)
        enemies.forEach { assertEquals(100f, it.aggro.values.sum(), 0.001f) }
        assertEquals("aegis", enemies[0].targetId)
    }

    @Test
    fun `each normal monster attack follows highest living threat`() {
        val simulator = BattleSimulator(formation("aegis", "luna", "rook").copy(monsterCount = 3),
            PrototypeContent.dungeons.first().copy(mechanicInterval = 100f))
        var healerLed = false
        repeat(150) {
            simulator.update(0.1f)
            val snapshot = simulator.snapshot()
            healerLed = healerLed || snapshot.enemies.any { it.targetId == "luna" }
            simulator.drainCombatCues().filter { it.source.startsWith("boss") && it.type == BattleSimulator.CueType.HIT }
                .forEach { cue ->
                    val enemy = snapshot.enemies.first { it.id == cue.source }
                    if (snapshot.members.first { it.character.id == cue.target }.hp > 0f) {
                        assertEquals(enemy.aggro.values.max(), enemy.aggro.getValue(cue.target), 0.001f)
                    }
                }
        }
        assertTrue(healerLed, "Actual healing should draw aggro from untouched monsters")
    }

    @Test
    fun `enemies have independent health attacks and casting times`() {
        val raid = formation("aegis", "luna", "rook").copy(monsterCount = 3)
        val dungeon = PrototypeContent.dungeons.first().copy(enemyMaxHp = 10000f, enemyAttack = 0f)
        val simulator = BattleSimulator(raid, dungeon)
        val firstAttacks = mutableMapOf<String, Float>()
        val firstCasts = mutableMapOf<String, Float>()
        repeat(130) {
            simulator.update(0.1f)
            val snapshot = simulator.snapshot()
            simulator.drainCombatCues().filter { it.source.startsWith("boss") && it.type == BattleSimulator.CueType.HIT }
                .forEach { firstAttacks.putIfAbsent(it.source, snapshot.elapsedTime) }
            snapshot.enemies.filter { it.castRemaining > 0f }
                .forEach { firstCasts.putIfAbsent(it.id, snapshot.elapsedTime) }
        }
        assertEquals(3, firstAttacks.values.toSet().size)
        assertEquals(3, firstCasts.values.toSet().size)
        assertTrue(simulator.snapshot().enemies.first().hp < 10000f)
        assertTrue(simulator.snapshot().enemies.first().hp < simulator.snapshot().enemies[1].hp)
    }

    @Test
    fun `dead enemies stop acting and heroes switch targets`() {
        val simulator = BattleSimulator(formation("aegis", "luna", "rook").copy(monsterCount = 3),
            PrototypeContent.dungeons.first().copy(enemyMaxHp = 45f, enemyAttack = 0f))
        val dead = mutableSetOf<String>()
        val targets = mutableSetOf<String>()
        repeat(300) {
            simulator.update(0.1f)
            val cues = simulator.drainCombatCues()
            assertTrue(cues.none { it.source in dead })
            targets += cues.filter { it.source == "rook" && it.type == BattleSimulator.CueType.HIT }.map { it.target }
            dead += simulator.snapshot().enemies.filter { it.hp <= 0f }.map { it.id }
        }
        assertEquals(3, dead.size)
        assertEquals(3, targets.size)
        assertTrue(simulator.snapshot().victory)
    }

    @Test
    fun `heroes open at different times and retain their own attack intervals`() {
        val raid = formation("aegis", "luna", "rook")
        val simulator = BattleSimulator(raid, PrototypeContent.dungeons.first().copy(enemyMaxHp = 10000f))
        val times = raid.members.associate { it.id to mutableListOf<Float>() }
        repeat(45) {
            simulator.update(0.1f)
            simulator.drainCombatCues().filter { it.type == BattleSimulator.CueType.HIT && it.skillType == null }
                .forEach { times[it.source]?.add(simulator.snapshot().elapsedTime) }
        }
        assertEquals(3, times.values.map { it.first() }.toSet().size)
        raid.members.forEach { hero ->
            val attacks = times.getValue(hero.id)
            assertEquals(hero.attackInterval, attacks[1] - attacks[0], 0.11f)
        }
    }

    @Test
    fun `animation cues report actual damage once without changing the battle result`() {
        val raid = formation("aegis", "luna", "rook")
        val dungeon = PrototypeContent.dungeons.first()
        val simulator = BattleSimulator(raid, dungeon)
        var damage = 0f
        repeat(500) {
            simulator.update(0.1f)
            val cues = simulator.drainCombatCues()
            damage += cues.filter { it.type == BattleSimulator.CueType.HIT && it.target == "boss" }
                .sumOf { it.amount.toDouble() }.toFloat()
            assertTrue(simulator.drainCombatCues().isEmpty())
        }
        val result = simulator.result()
        assertEquals(result.members.sumOf { it.damageDealt.toDouble() }.toFloat(), damage, 0.01f)
        assertEquals(completeBattle(raid, dungeon), result)
    }

    @Test
    fun `same formation and dungeon produce the same result`() {
        val formation = formation("aegis", "luna", "rook")
        val dungeon = PrototypeContent.dungeons.first()

        val first = completeBattle(formation, dungeon)
        val second = completeBattle(formation, dungeon)

        assertEquals(first.victory, second.victory)
        assertEquals(first.elapsedTime, second.elapsedTime)
        assertEquals(first.enemyHpRemaining, second.enemyHpRemaining)
        assertEquals(first.members, second.members)
        assertEquals(first.events, second.events)
    }

    @Test
    fun `each dungeon has winning and losing formations`() {
        PrototypeContent.dungeons.forEach { dungeon ->
            val results = allFormations().map { completeBattle(it, dungeon) }
            assertTrue(results.any { it.victory }, "${dungeon.name} needs at least one winning formation")
            assertTrue(results.any { !it.victory }, "${dungeon.name} needs at least one losing formation")
        }
    }

    @Test
    fun `missing the regeneration counter produces actionable analysis`() {
        val dungeon = PrototypeContent.dungeons.first { it.mechanic == DungeonMechanic.REGEN }
        val result = completeBattle(formation("aegis", "luna", "nyx"), dungeon)

        assertFalse(result.victory)
        assertTrue(result.analysis.contains("MIRA"))
    }

    private fun completeBattle(formation: RaidFormation, dungeon: DungeonDefinition): BattleResult {
        val simulator = BattleSimulator(formation, dungeon)
        repeat(1_000) {
            simulator.update(0.1f)
            if (simulator.snapshot().finished) return simulator.result()
        }
        error("Battle did not finish")
    }

    private fun formation(vararg ids: String): RaidFormation {
        val characters = ids.map { id -> PrototypeContent.characters.first { it.id == id } }
        return RaidFormation(characters)
    }

    private fun allFormations(): List<RaidFormation> {
        val roster = PrototypeContent.characters
        return buildList {
            for (first in 0 until roster.size - 2) {
                for (second in first + 1 until roster.size - 1) {
                    for (third in second + 1 until roster.size) {
                        add(RaidFormation(listOf(roster[first], roster[second], roster[third])))
                    }
                }
            }
        }
    }
}
