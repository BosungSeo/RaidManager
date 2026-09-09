package com.raidmanager.game.scene

import com.raidmanager.game.GameAssets.SpritePose
import com.raidmanager.game.model.BattleSimulator.CombatCue
import com.raidmanager.game.model.BattleSimulator.CueType
import com.raidmanager.game.model.DungeonMechanic
import kotlin.math.PI
import kotlin.math.sin

/** Cue-driven presentation state only; does not affect damage, cooldowns or battle outcomes. */
internal class MonsterAnimation {
    data class Motion(
        val pose: SpritePose,
        val offsetX: Float = 0f,
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
        val rotation: Float = 0f,
        val alpha: Float = 1f,
        val hurt: Boolean = false,
        val recovery: Float = 0f,
        val wave: Float = 0f,
    )

    private var clock = 0f
    private var actionAge = 10f
    private var hitAge = 10f
    private var interruptAge = 10f
    private var healAge = 10f
    private var waveAge = 10f
    private var deathAge = 0f
    private var dead = false
    private var finished = false

    fun update(delta: Float, cues: List<CombatCue>, enemyHp: Float, battleFinished: Boolean) {
        val dt = delta.coerceAtLeast(0f)
        clock += dt
        actionAge += dt
        hitAge += dt
        interruptAge += dt
        healAge += dt
        waveAge += dt
        finished = battleFinished
        if (enemyHp <= 0f) {
            if (dead) deathAge += dt
            dead = true
            return
        }
        if (finished) return
        cues.forEach { cue ->
            when {
                cue.type == CueType.INTERRUPT && cue.target == "boss" -> interruptAge = 0f
                cue.type == CueType.HIT && cue.target == "boss" && cue.amount > 0f -> hitAge = 0f
                cue.source == "boss" && cue.type == CueType.WAVE -> {
                    actionAge = 0f
                    waveAge = 0f
                }
                cue.source == "boss" && cue.type == CueType.HEAL -> healAge = 0f
                cue.source == "boss" && cue.type == CueType.HIT -> actionAge = 0f
            }
        }
        if (interruptAge < 0.5f) {
            actionAge = 10f
            waveAge = 10f
        }
    }

    fun motion(mechanic: DungeonMechanic): Motion {
        if (dead) {
            val fall = (deathAge / 0.7f).coerceIn(0f, 1f)
            return Motion(SpritePose.HURT, scaleX = 1f + fall * 0.12f, scaleY = 1f - fall * 0.65f,
                rotation = if (mechanic == DungeonMechanic.REGEN) 0f else -12f * fall, alpha = 1f - fall * 0.6f)
        }
        if (finished) return Motion(SpritePose.IDLE)
        val interrupted = interruptAge < 0.5f
        val acting = actionAge < 0.6f && !interrupted
        val hurt = hitAge < 0.22f || interrupted
        val action = if (acting) sin(actionAge / 0.6f * PI).toFloat() else 0f
        val wobble = sin(clock * 3f).toFloat()
        val recovery = (1f - healAge / 0.8f).coerceIn(0f, 1f)
        val slime = mechanic == DungeonMechanic.REGEN
        val stride = if (mechanic == DungeonMechanic.SWARM) 54f else 30f
        return Motion(
            pose = when {
                interrupted -> SpritePose.HURT
                acting -> SpritePose.ACTION
                hurt -> SpritePose.HURT
                else -> SpritePose.IDLE
            },
            offsetX = -action * stride + if (hurt && !acting) 9f else 0f,
            scaleX = if (slime) 1f + wobble * 0.025f + action * 0.12f else 1f,
            scaleY = 1f + wobble * (if (slime) 0.04f else 0.012f) - action * (if (slime) 0.1f else 0.025f),
            rotation = if (slime) wobble * 1.5f else action * 3f,
            hurt = hurt,
            recovery = recovery,
            wave = (1f - waveAge / 0.7f).coerceIn(0f, 1f),
        )
    }
}
