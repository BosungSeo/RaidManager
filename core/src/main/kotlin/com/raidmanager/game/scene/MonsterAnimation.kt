package com.raidmanager.game.scene

import com.raidmanager.game.GameAssets.SpritePose
import com.raidmanager.game.model.BattleSimulator.CombatCue
import com.raidmanager.game.model.BattleSimulator.CueType
import com.raidmanager.game.model.DungeonMechanic
import com.raidmanager.game.scene.MonsterAnimationStyle as Style
import kotlin.math.PI
import kotlin.math.sin

/** 전투 이벤트로 몬스터의 표현 상태만 갱신한다. 피해량과 전투 결과는 변경하지 않는다. */
internal class MonsterAnimation(private val enemyId: String = "boss", phase: Float = 0f) {
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

    private var clock = phase
    private var actionAge = Style.INACTIVE_AGE
    private var hitAge = Style.INACTIVE_AGE
    private var interruptAge = Style.INACTIVE_AGE
    private var healAge = Style.INACTIVE_AGE
    private var waveAge = Style.INACTIVE_AGE
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
                cue.type == CueType.INTERRUPT && cue.target == enemyId -> interruptAge = 0f
                cue.type == CueType.HIT && cue.target == enemyId && cue.amount > 0f -> hitAge = 0f
                cue.source == enemyId && cue.type == CueType.WAVE -> {
                    actionAge = 0f
                    waveAge = 0f
                }
                cue.source == enemyId && cue.type == CueType.HEAL -> healAge = 0f
                cue.source == enemyId && cue.type == CueType.HIT -> actionAge = 0f
            }
        }
        if (interruptAge < Style.INTERRUPT_DURATION) {
            actionAge = Style.INACTIVE_AGE
            waveAge = Style.INACTIVE_AGE
        }
    }

    /** 사인 곡선으로 공격 왕복과 호흡을 합성하고, 사망·차단 상태를 우선 적용해 발 기준 변형을 구한다. */
    fun motion(mechanic: DungeonMechanic): Motion {
        if (dead) {
            val fall = (deathAge / Style.DEATH_DURATION).coerceIn(0f, 1f)
            return Motion(SpritePose.HURT, scaleX = 1f + fall * Style.DEATH_WIDTH, scaleY = 1f - fall * Style.DEATH_COLLAPSE,
                rotation = if (mechanic == DungeonMechanic.REGEN) 0f else Style.DEATH_ROTATION * fall, alpha = 1f - fall * Style.DEATH_FADE)
        }
        if (finished) return Motion(SpritePose.IDLE)
        val interrupted = interruptAge < Style.INTERRUPT_DURATION
        val acting = actionAge < Style.ACTION_DURATION && !interrupted
        val hurt = hitAge < Style.HURT_DURATION || interrupted
        val action = if (acting) sin(actionAge / Style.ACTION_DURATION * PI).toFloat() else 0f
        val wobble = sin(clock * Style.BREATH_SPEED).toFloat()
        val recovery = (1f - healAge / Style.HEAL_DURATION).coerceIn(0f, 1f)
        val slime = mechanic == DungeonMechanic.REGEN
        val stride = if (mechanic == DungeonMechanic.SWARM) Style.SWARM_STRIDE else Style.NORMAL_STRIDE
        return Motion(
            pose = when {
                interrupted -> SpritePose.HURT
                acting -> SpritePose.ACTION
                hurt -> SpritePose.HURT
                else -> SpritePose.IDLE
            },
            offsetX = -action * stride + if (hurt && !acting) Style.HURT_OFFSET else 0f,
            scaleX = if (slime) 1f + wobble * Style.SLIME_WIDTH_BREATH + action * Style.SLIME_WIDTH_ACTION else 1f,
            scaleY = 1f + wobble * (if (slime) Style.SLIME_HEIGHT_BREATH else Style.NORMAL_HEIGHT_BREATH) - action * (if (slime) Style.SLIME_HEIGHT_ACTION else Style.NORMAL_HEIGHT_ACTION),
            rotation = if (slime) wobble * Style.SLIME_ROTATION else action * Style.ACTION_ROTATION,
            hurt = hurt,
            recovery = recovery,
            wave = (1f - waveAge / Style.WAVE_DURATION).coerceIn(0f, 1f),
        )
    }
}
