package com.raidmanager.game.scene

import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.AttackStyle
import com.raidmanager.game.model.BattleSimulator
import com.raidmanager.game.model.RaidFormation

/** 전투 이벤트를 효과음으로 변환하고 중복 재생을 제한한다. 리소스 수명은 GameAssets가 관리한다. */
internal class BattleAudio(private val assets: GameAssets, private val formation: RaidFormation) {
    private val cooldowns = mutableMapOf<String, Float>()
    private var started = false
    private var ended = false

    fun update(delta: Float, cues: List<BattleSimulator.CombatCue>, snapshot: BattleSimulator.Snapshot) {
        cooldowns.replaceAll { _, value -> (value - delta).coerceAtLeast(0f) }
        if (!started) {
            play("start", BattleSoundStyle.START_VOLUME)
            started = true
        }
        if (snapshot.finished) {
            if (!ended) {
                assets.stopBattleSounds()
                play(if (snapshot.victory) "victory" else "defeat", BattleSoundStyle.RESULT_VOLUME)
                ended = true
            }
            return
        }
        // 광역 회복·보호막 이벤트의 중복음을 제거한 뒤 기술음을 우선 재생한다.
        val sounds = cues.map { cue ->
            when (cue.type) {
                BattleSimulator.CueType.HEAL -> "heal"
                BattleSimulator.CueType.SHIELD -> "shield"
                BattleSimulator.CueType.INTERRUPT -> "interrupt"
                BattleSimulator.CueType.WAVE -> "wave"
                BattleSimulator.CueType.SUNDER -> "skill"
                BattleSimulator.CueType.TAUNT -> "shield"
                BattleSimulator.CueType.HIT -> when {
                    cue.skillType != null || cue.rearAttack -> "skill"
                    formation.members.any { it.id == cue.source && it.attackStyle == AttackStyle.RANGED } -> "shot"
                    else -> "hit"
                }
            }
        }.distinct().sortedBy { if (it == "hit" || it == "shot") 1 else 0 }.take(BattleSoundStyle.MAX_SOUNDS_PER_UPDATE)
        sounds.forEach { play(it, if (it == "hit" || it == "shot") BattleSoundStyle.BASIC_VOLUME else BattleSoundStyle.SKILL_VOLUME) }
    }

    private fun play(name: String, volume: Float) {
        if ((cooldowns[name] ?: 0f) > 0f) return
        assets.playBattleSound(name, volume)
        cooldowns[name] = if (name == "hit" || name == "shot") BattleSoundStyle.BASIC_COOLDOWN else BattleSoundStyle.SKILL_COOLDOWN
    }
}
