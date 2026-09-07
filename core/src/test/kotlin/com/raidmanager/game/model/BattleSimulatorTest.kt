package com.raidmanager.game.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BattleSimulatorTest {
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
