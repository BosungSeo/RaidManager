package com.raidmanager.game.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CombatMovementTest {
    @Test
    fun approachingBodyStopsAtContactWithoutPushingAndCanMoveAway() {
        assertEquals(0.8f, CombatMovement.allowedFraction(0f, 0f, 100f, 0f, 200f, 0f, 120f), 0.0001f)
        assertEquals(0f, CombatMovement.allowedFraction(80f, 0f, 10f, 0f, 200f, 0f, 120f))
        assertEquals(1f, CombatMovement.allowedFraction(80f, 0f, -10f, 0f, 200f, 0f, 120f))
        assertEquals(1f, CombatMovement.allowedFraction(80f, 0f, 0f, 10f, 200f, 0f, 120f))
    }

    @Test
    fun fastMovementCannotPassThroughAnotherBody() {
        assertEquals(0.08f, CombatMovement.allowedFraction(0f, 0f, 1000f, 0f, 200f, 0f, 120f), 0.0001f)
        assertEquals(1f, CombatMovement.allowedFraction(0f, 0f, 1000f, 0f, 200f, 200f, 120f))
    }

    @Test
    fun approachingMonstersDoNotDisplaceStationaryRaidMembers() {
        val heroes = PrototypeContent.characters.take(3).map {
            it.copy(moveSpeed = 0f, attackPower = 0f, skillCooldown = 1000f)
        }
        val simulator = BattleSimulator(RaidFormation(heroes, 3), PrototypeContent.dungeons.first().copy(
            enemyAttack = 0f, mechanicInterval = 1000f, enemyMoveSpeed = 400f,
        ))
        val initial = simulator.snapshot().members
        repeat(200) {
            simulator.update(0.1f)
            simulator.snapshot().members.forEachIndexed { index, member ->
                assertEquals(initial[index].x, member.x, 0.001f)
                assertEquals(initial[index].y, member.y, 0.001f)
            }
        }
    }

    @Test
    fun repeatedApproachAtContactDoesNotOscillate() {
        var x = 0f
        repeat(500) {
            x += 10f * CombatMovement.allowedFraction(x, 0f, 10f, 0f, 200f, 0f, 120.02f)
            assertTrue(x <= 79.981f)
        }
        assertEquals(79.98f, x, 0.001f)
    }
}
