package com.raidmanager.game.scene

import com.raidmanager.game.GameAssets.SpritePose
import com.raidmanager.game.model.BattleSimulator.CombatCue
import com.raidmanager.game.model.BattleSimulator.CueType
import com.raidmanager.game.model.DungeonMechanic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MonsterAnimationTest {
    @Test
    fun `only the addressed monster reacts to combat cues`() {
        val first = MonsterAnimation("boss")
        val second = MonsterAnimation("boss-1")
        val cues = listOf(CombatCue(CueType.HIT, "boss-1", "aegis", 10f))
        first.update(0f, cues, 100f, false)
        second.update(0f, cues, 100f, false)
        assertEquals(SpritePose.IDLE, first.motion(DungeonMechanic.BURST).pose)
        assertEquals(SpritePose.ACTION, second.motion(DungeonMechanic.BURST).pose)
    }

    @Test
    fun `boss hit triggers leftward action then returns to idle`() {
        val animation = MonsterAnimation()
        animation.update(0f, listOf(CombatCue(CueType.HIT, "boss", "aegis", 10f)), 100f, false)
        animation.update(0.15f, emptyList(), 100f, false)
        val motion = animation.motion(DungeonMechanic.BURST)
        assertEquals(SpritePose.ACTION, motion.pose)
        assertTrue(motion.offsetX < 0f)
        animation.update(0.5f, emptyList(), 100f, false)
        assertEquals(SpritePose.IDLE, animation.motion(DungeonMechanic.BURST).pose)
    }

    @Test
    fun `incoming damage flashes without hiding an ongoing attack`() {
        val animation = MonsterAnimation()
        animation.update(0f, listOf(CombatCue(CueType.HIT, "boss", "aegis", 10f)), 100f, false)
        animation.update(0.1f, listOf(CombatCue(CueType.HIT, "rook", "boss", 12f)), 88f, false)
        val motion = animation.motion(DungeonMechanic.SWARM)
        assertEquals(SpritePose.ACTION, motion.pose)
        assertTrue(motion.hurt)
    }

    @Test
    fun `interrupt cancels wave regardless of cue order`() {
        val cues = listOf(CombatCue(CueType.INTERRUPT, "boss", "boss"), CombatCue(CueType.WAVE, "boss", "boss"))
        listOf(cues, cues.reversed()).forEach {
            val animation = MonsterAnimation()
            animation.update(0f, it, 100f, false)
            assertEquals(SpritePose.HURT, animation.motion(DungeonMechanic.BURST).pose)
            assertEquals(0f, animation.motion(DungeonMechanic.BURST).wave)
            animation.update(0.55f, emptyList(), 100f, false)
            assertEquals(SpritePose.IDLE, animation.motion(DungeonMechanic.BURST).pose)
        }
    }

    @Test
    fun `wave attacks and self healing have distinct presentation`() {
        val animation = MonsterAnimation()
        animation.update(0f, listOf(CombatCue(CueType.WAVE, "boss", "boss")), 100f, false)
        assertEquals(SpritePose.ACTION, animation.motion(DungeonMechanic.SWARM).pose)
        assertTrue(animation.motion(DungeonMechanic.SWARM).wave > 0f)
        animation.update(1f, listOf(CombatCue(CueType.HEAL, "boss", "boss", 32f)), 132f, false)
        val recovery = animation.motion(DungeonMechanic.REGEN)
        assertEquals(SpritePose.IDLE, recovery.pose)
        assertTrue(recovery.recovery > 0f)
        assertFalse(recovery.hurt)
    }

    @Test
    fun `damage produces hurt pose but blocked damage does not`() {
        val animation = MonsterAnimation()
        animation.update(0f, listOf(CombatCue(CueType.HIT, "rook", "boss", 0f)), 100f, false)
        assertEquals(SpritePose.IDLE, animation.motion(DungeonMechanic.BURST).pose)
        animation.update(0f, listOf(CombatCue(CueType.HIT, "rook", "boss", 12f)), 88f, false)
        assertEquals(SpritePose.HURT, animation.motion(DungeonMechanic.BURST).pose)
        animation.update(0.25f, emptyList(), 88f, false)
        assertEquals(SpritePose.IDLE, animation.motion(DungeonMechanic.BURST).pose)
    }

    @Test
    fun `death collapses and fades even after battle ends`() {
        val animation = MonsterAnimation()
        animation.update(0f, emptyList(), 0f, true)
        val first = animation.motion(DungeonMechanic.REGEN)
        animation.update(0.8f, emptyList(), 0f, true)
        val last = animation.motion(DungeonMechanic.REGEN)
        assertEquals(SpritePose.HURT, last.pose)
        assertTrue(last.scaleY < first.scaleY)
        assertTrue(last.alpha < first.alpha)
    }

    @Test
    fun `surviving boss stops attacking on battle completion`() {
        val animation = MonsterAnimation()
        animation.update(0f, listOf(CombatCue(CueType.WAVE, "boss", "boss")), 100f, true)
        assertEquals(MonsterAnimation.Motion(SpritePose.IDLE), animation.motion(DungeonMechanic.BURST))
    }
}
