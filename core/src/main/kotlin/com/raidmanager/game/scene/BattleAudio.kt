package com.raidmanager.game.scene

import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.AttackStyle
import com.raidmanager.game.model.BattleSimulator
import com.raidmanager.game.model.RaidFormation

/** Scene-local playback limits; sound resources belong to GameAssets. */
internal class BattleAudio(private val assets: GameAssets, private val formation: RaidFormation) {
    private val cooldowns = mutableMapOf<String, Float>()
    private var started = false
    private var ended = false

    fun update(delta: Float, cues: List<BattleSimulator.CombatCue>, snapshot: BattleSimulator.Snapshot) {
        cooldowns.replaceAll { _, value -> (value - delta).coerceAtLeast(0f) }
        if (!started) {
            play("start", 0.25f)
            started = true
        }
        if (snapshot.finished) {
            if (!ended) {
                assets.stopBattleSounds()
                play(if (snapshot.victory) "victory" else "defeat", 0.35f)
                ended = true
            }
            return
        }
        // At most three different sounds per update; group-wide shield/heal cues play once.
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
        }.distinct().sortedBy { if (it == "hit" || it == "shot") 1 else 0 }.take(3)
        sounds.forEach { play(it, if (it == "hit" || it == "shot") 0.16f else 0.25f) }
    }

    private fun play(name: String, volume: Float) {
        if ((cooldowns[name] ?: 0f) > 0f) return
        assets.playBattleSound(name, volume)
        cooldowns[name] = if (name == "hit" || name == "shot") 0.12f else 0.3f
    }
}
