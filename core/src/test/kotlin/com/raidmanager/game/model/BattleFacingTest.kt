package com.raidmanager.game.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BattleFacingTest {
    @Test
    fun `front and exact side have no bonus while rear works in both directions`() {
        assertFalse(BattleFacing.isRear(10f, 0f, 1f, 0f))
        assertFalse(BattleFacing.isRear(0f, 10f, 1f, 0f))
        assertTrue(BattleFacing.isRear(-10f, 0f, 1f, 0f))
        assertTrue(BattleFacing.isRear(10f, 0f, -1f, 0f))
        assertTrue(BattleFacing.isRear(0f, -10f, 0f, 1f))
        assertEquals(15f, BattleFacing.damage(10f, true))
        assertEquals(10f, BattleFacing.damage(10f, false))
    }

    @Test
    fun `rook circles behind a tanked enemy and earns rear damage`() {
        val formation = RaidFormation(listOf("aegis", "luna", "rook").map { id ->
            PrototypeContent.characters.first { it.id == id }
        })
        val simulator = BattleSimulator(formation, PrototypeContent.dungeons.first().copy(
            enemyMaxHp = 10000f, enemyAttack = 0f, mechanicInterval = 100f,
        ))
        var rearHits = 0
        repeat(200) {
            simulator.update(0.1f)
            simulator.drainCombatCues().filter {
                it.source == "rook" && it.type == BattleSimulator.CueType.HIT && it.rearAttack && it.skillType == null
            }.forEach {
                rearHits++
                assertEquals(formation.members.last().attackPower * 1.5f, it.amount)
            }
        }
        assertTrue(rearHits >= 2, "Rook should maintain a rear position and attack repeatedly")
    }
}
