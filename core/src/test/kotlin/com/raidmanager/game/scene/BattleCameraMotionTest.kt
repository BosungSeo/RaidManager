package com.raidmanager.game.scene

import com.raidmanager.game.model.BattleSimulator.CombatCue
import com.raidmanager.game.model.BattleSimulator.CueType
import com.raidmanager.game.model.SkillType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BattleCameraMotionTest {
    @Test
    fun repeatedHitsStayBoundedAndCameraReturnsToNeutral() {
        val camera = BattleCameraMotion()
        camera.update(2f)
        camera.impact(1f, 1200f, 650f)
        assertEquals(920f, camera.focusX)
        assertEquals(480f, camera.focusY)
        assertEquals(1200f, camera.impactX)
        repeat(100) {
            camera.impact(10f, 50f, 30f)
            camera.update(0.01f)
            assertTrue(camera.zoom in 1f..1.091f)
            assertTrue(kotlin.math.abs(camera.offsetX) <= 5f)
        }
        camera.update(1f)
        assertEquals(1f, camera.zoom)
        assertEquals(0f, camera.offsetX)
        assertEquals(0f, camera.offsetY)
    }

    @Test
    fun waveDoesNotShakeOrZoomByItself() {
        val camera = BattleCameraMotion()
        camera.update(2f)
        camera.impact(1f, 700f, 350f, zoom = false)
        camera.update(0.03f)
        assertEquals(1f, camera.zoom)
        assertEquals(0f, camera.offsetX)
        assertEquals(0f, camera.offsetY)
        camera.update(1f)
        camera.impact(0.8f, 700f, 350f)
        assertTrue(camera.zoom > 1f)
    }

    @Test
    fun onlyPowerfulDamageShakesWithIndependentCooldown() {
        val camera = BattleCameraMotion()
        camera.update(2f)
        camera.shakeForHits(listOf(
            CombatCue(CueType.HIT, "a", "b", 49f, SkillType.STRIKE, rearAttack = true),
            CombatCue(CueType.HIT, "a", "b", 0f),
            CombatCue(CueType.WAVE, "a", "b", 100f),
        ))
        camera.update(0.03f)
        assertEquals(0f, camera.offsetX)
        val strongHit = CombatCue(CueType.HIT, "a", "b", 50f)
        camera.impact(1f, 700f, 350f, zoom = false)
        camera.shakeForHits(listOf(CombatCue(CueType.WAVE, "a", "b"), strongHit))
        camera.update(0.03f)
        assertTrue(kotlin.math.abs(camera.offsetX) > 0f)
        assertEquals(1f, camera.zoom)
        camera.update(0.3f)
        camera.shakeForHits(listOf(strongHit))
        camera.update(0.03f)
        assertEquals(0f, camera.offsetX)
        assertEquals(0f, camera.offsetY)
        camera.update(1f)
        camera.shakeForHits(listOf(strongHit))
        camera.update(0.03f)
        assertTrue(kotlin.math.abs(camera.offsetX) > 0f)
    }

    @Test
    fun blockedAndOrdinaryHitsDoNotTriggerCameraPunch() {
        assertEquals(0f, BattleCameraMotion.emphasis(CombatCue(CueType.HIT, "a", "b", 0f, SkillType.STRIKE)))
        assertEquals(0f, BattleCameraMotion.emphasis(CombatCue(CueType.HIT, "a", "b", 5f)))
        assertTrue(BattleCameraMotion.emphasis(CombatCue(CueType.HIT, "a", "b", 5f, SkillType.STRIKE)) > 0f)
        assertEquals(1f, BattleCameraMotion.emphasis(CombatCue(CueType.WAVE, "a", "b")))
    }
}
