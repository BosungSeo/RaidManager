package com.raidmanager.game.scene

import kotlin.test.Test
import kotlin.test.assertEquals

class SpriteTimelineTest {
    @Test
    fun `cycles select all sixteen frames and stop at battle end`() {
        assertEquals((0..3).toList(), List(4) { SpriteTimeline.frame(it * 0.2f, false, null, null, true, false) })
        assertEquals((4..7).toList(), List(4) { SpriteTimeline.frame(it * 0.1f, true, null, null, true, false) })
        assertEquals((8..11).toList(), List(4) { SpriteTimeline.frame(0f, false, it * 0.1f, null, true, false) })
        assertEquals((12..15).toList(), List(4) { SpriteTimeline.frame(0f, false, null, it * 0.07f, true, false) })
        assertEquals(13, SpriteTimeline.frame(10f, true, 0f, 0f, false, false))
        assertEquals(0, SpriteTimeline.frame(10f, true, 0f, 0f, true, true))
    }
}
