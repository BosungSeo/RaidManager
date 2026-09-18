package com.raidmanager.game.model

import kotlin.test.*

class ExpandedRaidTest {
    @Test
    fun timeSelectionClampsAndCopiesDungeon() {
        val state = DungeonTimeSelection()
        assertEquals(40, state.seconds)
        state.adjust(-1000)
        assertEquals(10, state.seconds)
        state.select(600)
        state.adjust(10)
        assertEquals("10:00", state.label)
        val original = PrototypeContent.dungeons.first()
        assertEquals(600f, state.applyTo(original).timeLimit)
        assertEquals(40f, original.timeLimit)
    }

    @Test
    fun selectionAcceptsThreeThroughSixAndRejectsSeventh() {
        val roster = PrototypeContent.characters + PrototypeContent.characters.first().copy(id = "extra")
        val state = RaidSetupState(roster)
        repeat(6) { index ->
            assertTrue(state.toggle(index))
            assertEquals(index >= 2, state.isComplete)
            if (index >= 2) assertEquals(index + 1, state.createFormation()!!.members.size)
        }
        assertFalse(state.toggle(6))
        assertEquals(state.createFormation(), RaidSetupState(roster, state.createFormation()).createFormation())
        assertFailsWith<IllegalArgumentException> { RaidFormation(roster) }
        assertFailsWith<IllegalArgumentException> { RaidFormation(roster.take(2)) }
    }

    @Test
    fun sixMembersStayInBoundsAndSelectedTimeReplacesFortySeconds() {
        val heroes = PrototypeContent.characters.map { it.copy(maxHp = 100000f) }
        val simulator = BattleSimulator(RaidFormation(heroes, 3), PrototypeContent.dungeons.first().copy(
            enemyMaxHp = 10000000f, enemyAttack = 0f, mechanicInterval = 10000f, timeLimit = 600f,
        ))
        repeat(6002) { step ->
            simulator.update(0.1f)
            val state = simulator.snapshot()
            assertEquals(6, state.members.size)
            state.members.forEach {
                assertTrue(it.x in BattleRules.MIN_X..BattleRules.MAX_X)
                assertTrue(it.y in BattleRules.MIN_Y..BattleRules.MAX_Y)
            }
            if (step == 500) assertFalse(state.finished)
        }
        assertTrue(simulator.snapshot().finished)
        assertEquals(600f, simulator.snapshot().elapsedTime, 0.11f)
    }
}
