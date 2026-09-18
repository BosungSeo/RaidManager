package com.raidmanager.game.scene

import com.raidmanager.game.model.BattleSimulator.CombatCue
import com.raidmanager.game.model.BattleSimulator.CueType
import kotlin.math.sin

/** 화면 표현만 제어한다. 연속 타격은 누적 확대하지 않으며 전투 시간과 입력 좌표는 바꾸지 않는다. */
internal class BattleCameraMotion {
    var age = 1f
        private set
    var strength = 0f
        private set
    var focusX = 640f
        private set
    var focusY = 360f
        private set
    var impactX = 640f
        private set
    var impactY = 360f
        private set
    private var zoomEnabled = true
    private var cooldown = 0f
    private var clock = 0f
    private var shakeAge = 1f
    private var shakeCooldown = 0f
    private val shakePulse: Float get() = (1f - shakeAge / 0.22f).coerceIn(0f, 1f).let { it * it }
    val pulse: Float get() = (1f - age / 0.48f).coerceIn(0f, 1f).let { it * it } * strength
    val zoom: Float get() = 1f + (if (zoomEnabled) pulse * 0.065f else 0f) + (1f - clock / 1.3f).coerceIn(0f, 1f) * 0.025f
    val offsetX: Float get() = if (shakePulse == 0f) 0f else sin(shakeAge * 83f) * shakePulse * 5f
    val offsetY: Float get() = if (shakePulse == 0f) 0f else sin(shakeAge * 67f) * shakePulse * 3f

    fun update(delta: Float) {
        shakeAge += delta
        shakeCooldown = (shakeCooldown - delta).coerceAtLeast(0f)
        age += delta
        clock += delta
        cooldown = (cooldown - delta).coerceAtLeast(0f)
    }

    fun shakeForHits(cues: List<CombatCue>) {
        if (shakeCooldown > 0f || cues.none { it.type == CueType.HIT && it.amount >= 50f }) return
        shakeAge = 0f
        shakeCooldown = 1.2f
    }

    fun impact(amount: Float, x: Float, y: Float, zoom: Boolean = true) {
        if (amount <= 0f || cooldown > 0f) return
        zoomEnabled = zoom
        strength = amount.coerceIn(0f, 1f)
        impactX = x
        impactY = y
        focusX = x.coerceIn(360f, 920f)
        focusY = y.coerceIn(260f, 480f)
        age = 0f
        cooldown = 0.22f
    }

    companion object {
        fun emphasis(cue: CombatCue): Float = when {
            cue.type == CueType.WAVE -> 1f
            cue.type == CueType.INTERRUPT -> 0.7f
            cue.type != CueType.HIT || cue.amount <= 0f -> 0f
            cue.skillType != null -> 0.8f
            cue.rearAttack || cue.amount >= 30f -> 0.55f
            else -> 0f
        }
    }
}
