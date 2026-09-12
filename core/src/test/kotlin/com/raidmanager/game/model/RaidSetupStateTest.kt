package com.raidmanager.game.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RaidSetupStateTest {
    @Test
    fun `selection limits party size and forms party in roster order`() {
        val roster = PrototypeContent.characters
        val state = RaidSetupState(roster)
        assertNull(state.createFormation())
        assertTrue(state.toggle(2))
        assertTrue(state.toggle(0))
        assertTrue(state.toggle(1))
        assertTrue(state.isComplete)
        assertFalse(state.toggle(3))
        assertEquals(roster.take(3), state.createFormation()?.members)
        assertTrue(state.toggle(1))
        assertFalse(state.isComplete)
        assertNull(state.createFormation())
    }

    @Test
    fun `existing formation is restored and monster count stays within limits`() {
        val formation = RaidFormation(PrototypeContent.characters.take(3), monsterCount = 2)
        val state = RaidSetupState(PrototypeContent.characters, formation)
        assertEquals(formation, state.createFormation())
        state.adjustMonsterCount(10)
        assertEquals(3, state.monsterCount)
        state.adjustMonsterCount(-10)
        assertEquals(1, state.monsterCount)
    }
}
