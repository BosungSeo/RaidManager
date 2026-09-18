package com.raidmanager.game.graphics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharacterMotionTest {
    @Test
    fun blockedHitsDoNotRecoilAndDamageRecoilSettles() {
        assertEquals(0f, CharacterMotion.recoil(0.09f, 0f))
        assertEquals(0f, CharacterMotion.recoil(null, 20f))
        assertEquals(0f, CharacterMotion.recoil(0f, 20f))
        assertEquals(0f, CharacterMotion.recoil(CharacterMotion.HIT_DURATION, 20f))
        assertTrue(CharacterMotion.recoil(0.09f, 20f) > CharacterMotion.recoil(0.09f, 5f))
        assertTrue(CharacterMotion.recoil(0.09f, 1000f) <= 8f)
    }

    @Test
    fun reactionTintFadesAndAnimationTimingsAgree() {
        assertEquals(1f, CharacterMotion.hitPulse(0f))
        assertEquals(0f, CharacterMotion.hitPulse(CharacterMotion.HIT_DURATION))
        CharacterModels.definitions.values.forEach {
            assertEquals(CharacterMotion.ATTACK_DURATION, it.clips.attackDuration)
            assertEquals(CharacterMotion.HIT_DURATION, it.clips.hitDuration)
        }
    }
}
