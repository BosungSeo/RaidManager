package com.raidmanager.game.scene

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.BattleSimulator
import com.raidmanager.game.model.BattleSimulator.CombatCue
import com.raidmanager.game.model.BattleSimulator.CueType
import com.raidmanager.game.model.DungeonDefinition
import com.raidmanager.game.model.DungeonMechanic
import com.raidmanager.game.model.RaidFormation
import com.raidmanager.game.model.Role
import kotlin.math.sin

/** Presentation only: consumes combat cues without changing simulation rules. Coordinates: 1280 x 720. */
internal class BattleStage(private val assets: GameAssets, private val formation: RaidFormation) {
    private data class Effect(val cue: CombatCue, var age: Float = 0f)
    private val effects = mutableListOf<Effect>()
    private var clock = 0f
    private val monsterAnimation = MonsterAnimation()
    private val gold = Color.valueOf("F4CD85")
    private val blue = Color.valueOf("62C9FF")
    private val green = Color.valueOf("70EDAD")
    private val red = Color.valueOf("FF795F")
    private val violet = Color.valueOf("C292FF")

    fun update(delta: Float, cues: List<CombatCue>, snapshot: BattleSimulator.Snapshot) {
        clock += delta
        effects.forEach { it.age += delta }
        effects.removeAll { it.age > 1.15f }
        effects += cues.map(::Effect)
        monsterAnimation.update(delta, cues, snapshot.enemyHp, snapshot.finished)
    }

    private fun x(id: String): Float = if (id == "boss") 960f else {
        if (formation.members.firstOrNull { it.id == id }?.role == Role.TANK) 430f else 270f
    }

    private fun y(id: String): Float = if (id == "boss") 380f else {
        480f - formation.members.indexOfFirst { it.id == id }.coerceAtLeast(0) * 105f
    }

    private fun color(role: Role): Color = when (role) {
        Role.TANK -> blue
        Role.HEALER -> green
        Role.DPS -> gold
        Role.SUPPORT -> violet
    }

    fun render(batch: SpriteBatch, snapshot: BattleSimulator.Snapshot, dungeon: DungeonDefinition) {
        val theme = when (dungeon.mechanic) {
            DungeonMechanic.BURST -> blue
            DungeonMechanic.SWARM -> violet
            DungeonMechanic.REGEN -> green
        }
        batch.setColor(0.88f, 0.88f, 0.88f, 1f)
        batch.draw(assets.battleBackground(dungeon.id), 0f, 0f, 1280f, 720f)
        batch.color = Color.WHITE
        // Retain the environmental art behind the HUD while protecting text and health-bar contrast.
        rect(batch, 0f, 570f, 1280f, 150f, Color(0.025f, 0.04f, 0.07f, 0.72f))
        rect(batch, 0f, 0f, 1280f, 175f, Color(0.025f, 0.04f, 0.07f, 0.82f))
        for (i in 0..24) {
            val px = (i * 179f + clock * 9f) % 1280f
            val py = 205f + (i * 67f + clock * 14f) % 350f
            disc(batch, px, py, 2f, if (dungeon.mechanic == DungeonMechanic.BURST) gold else theme, 0.2f)
        }
        Ui.text(assets, batch, "RAID / ${dungeon.name}", 44f, 679f, 1.2f, gold)
        Ui.text(assets, batch, "${snapshot.elapsedTime.toInt()} / ${dungeon.timeLimit.toInt()} SEC", 1060f, 679f, 0.85f)
        Ui.text(assets, batch, dungeon.enemyName, 770f, 625f, 0.85f, theme)
        Ui.bar(assets, batch, 770f, 593f, 440f, 10f, snapshot.enemyHp / snapshot.enemyMaxHp, red)
        Ui.text(assets, batch, "${snapshot.enemyHp.toInt()} HP", 1120f, 625f, 0.7f)

        snapshot.members.forEach { member ->
            val id = member.character.id
            val actionAge = effects.filter { it.cue.source == id }
                .minOfOrNull { it.age }
            val hitAge = effects.filter { it.cue.target == id && it.cue.type == CueType.HIT }
                .minOfOrNull { it.age }
            val alive = member.hp > 0f
            val acting = alive && actionAge != null && actionAge < 0.5f
            val hurt = alive && hitAge != null && hitAge < 0.24f
            val pose = when {
                !alive || hurt -> GameAssets.SpritePose.HURT
                acting -> GameAssets.SpritePose.ACTION
                else -> GameAssets.SpritePose.IDLE
            }
            val melee = member.character.role == Role.TANK || member.character.id == "rook"
            val advance = if (acting && !hurt && melee) sin(actionAge!! / 0.5f * MathUtils.PI) * 36f else 0f
            val recoil = if (hurt) sin(hitAge!! / 0.24f * MathUtils.PI) * -12f else 0f
            val px = x(id) + advance + recoil
            val py = y(id)
            val tint = color(member.character.role)
            disc(batch, px, py - 37f, 32f, Color.BLACK, 0.4f, 0.28f)
            val frame = assets.heroFrame(id, pose)
            // Three authored poses plus foot-anchored breathing, lunging, recoil and defeat transforms.
            val breath = if (alive && !acting && !hurt && !snapshot.finished) sin(clock * 3f + y(id)) * 0.018f else 0f
            val castLean = if (acting && !melee && !hurt) sin(actionAge!! / 0.5f * MathUtils.PI) * -5f else 0f
            batch.setColor(1f, if (hurt) 0.6f else 1f, if (hurt) 0.6f else 1f, if (alive) 1f else 0.45f)
            val previousShader = batch.shader
            batch.shader = assets.heroShader
            val scale = 0.4f
            val footX = frame.footX * scale
            val footY = frame.footY * scale
            batch.draw(frame.region, px - footX, py - 37f - footY, footX, footY,
                frame.region.regionWidth * scale, frame.region.regionHeight * scale,
                1f, 1f + breath, if (alive) castLean else 80f)
            batch.shader = previousShader
            batch.color = Color.WHITE
            if (alive && member.shield > 0f) {
                ring(batch, px, py + 10f, 58f, blue, 0.5f + sin(clock * 4f) * 0.15f)
            }
            if (!alive) Ui.text(assets, batch, "DOWN", px - 22f, py + 14f, 0.7f, red)
            Ui.text(assets, batch, member.character.name, px - 128f, py + 15f, 0.7f, tint)
            Ui.bar(assets, batch, px - 46f, py - 53f, 92f, 5f, member.hp / member.character.maxHp, green)
        }

        val motion = monsterAnimation.motion(dungeon.mechanic)
        val monster = assets.monsterFrame(dungeon.id, motion.pose)
        val monsterScale = 0.6f
        val footX = monster.footX * monsterScale
        val footY = monster.footY * monsterScale
        disc(batch, 960f, 282f, 94f, Color.BLACK, 0.5f, 0.22f)
        val previousShader = batch.shader
        batch.shader = assets.monsterShader
        batch.setColor(1f, if (motion.hurt) 0.62f else 1f, if (motion.hurt) 0.62f else 1f, motion.alpha)
        batch.draw(monster.region, 960f + motion.offsetX - footX, 282f - footY, footX, footY,
            monster.region.regionWidth * monsterScale, monster.region.regionHeight * monsterScale,
            motion.scaleX, motion.scaleY, motion.rotation)
        batch.shader = previousShader
        batch.color = Color.WHITE
        if (motion.recovery > 0f) {
            ring(batch, 960f, 350f, 60f + (1f - motion.recovery) * 65f, green, motion.recovery * 0.7f)
        }
        if (motion.wave > 0f) {
            ring(batch, 960f, 290f, 45f + (1f - motion.wave) * 160f, theme, motion.wave * 0.7f)
        }
        effects.forEach { drawEffect(batch, it) }
        batch.color = Color.WHITE
    }

    private fun drawEffect(batch: SpriteBatch, effect: Effect) {
        val cue = effect.cue
        val age = effect.age
        val alpha = (1f - age / 1.15f).coerceIn(0f, 1f)
        val tx = x(cue.target)
        val ty = y(cue.target) + 20f
        val skill = cue.skillType
        val tint = when (cue.type) {
            CueType.HEAL -> green
            CueType.SHIELD -> blue
            CueType.INTERRUPT, CueType.SUNDER -> violet
            else -> if (cue.source == "boss") red else gold
        }
        if (cue.type == CueType.HIT) {
            when (skill) {
                null -> drawBasicAttack(batch, cue, age, alpha, tint, tx, ty)
                com.raidmanager.game.model.SkillType.STRIKE -> {
                    slash(batch, tx, ty, 48f, -0.65f, tint, alpha)
                    slash(batch, tx, ty, 48f, 0.65f, tint, alpha)
                    ring(batch, tx, ty, 25f + age * 80f, tint, alpha)
                }
                com.raidmanager.game.model.SkillType.CLEAVE -> {
                    for (offset in -1..1) slash(batch, tx, ty + offset * 22f, 62f, -0.55f, tint, alpha)
                    for (i in 0..5) {
                        val angle = i * MathUtils.PI2 / 6f
                        line(batch, tx, ty, tx + MathUtils.cos(angle) * (45f + age * 30f),
                            ty + MathUtils.sin(angle) * (45f + age * 30f), tint, 2f, alpha)
                    }
                }
                com.raidmanager.game.model.SkillType.INTERRUPT -> {
                    lightning(batch, x(cue.source), y(cue.source) + 20f, tx, ty, violet, alpha)
                    ring(batch, tx, ty, 40f + age * 70f, violet, alpha)
                }
                com.raidmanager.game.model.SkillType.SUNDER -> {
                    for (i in 0..3) ring(batch, tx, ty, 18f + i * 18f + age * 20f, violet, alpha * (1f - i * 0.15f))
                }
                else -> drawBasicAttack(batch, cue, age, alpha, tint, tx, ty)
            }
        } else if (cue.type == CueType.WAVE) {
            ring(batch, tx, ty, 80f + age * 650f, red, alpha)
        } else {
            ring(batch, tx, ty, 30f + age * 45f, tint, alpha)
            if (skill == com.raidmanager.game.model.SkillType.HEAL) {
                rect(batch, tx - 3f, ty - 12f + age * 35f, 6f, 24f, tint)
                rect(batch, tx - 12f, ty - 3f + age * 35f, 24f, 6f, tint)
                for (i in 0..3) disc(batch, tx - 18f + i * 12f, ty + age * 45f, 4f, green, alpha)
            } else if (skill == com.raidmanager.game.model.SkillType.GUARD) {
                ring(batch, tx, ty, 42f + age * 20f, blue, alpha)
                ring(batch, tx, ty, 25f + age * 20f, blue, alpha)
            } else if (cue.type == CueType.INTERRUPT) {
                lightning(batch, tx - 35f, ty + 40f, tx + 20f, ty - 25f, violet, alpha)
            }
        }
        val label = when (cue.type) {
            CueType.HIT -> if (cue.amount > 0f) "-${cue.amount.toInt()}" else "BLOCK"
            CueType.HEAL -> "+${cue.amount.toInt()}"
            CueType.SHIELD -> "SHIELD"
            CueType.INTERRUPT -> "INTERRUPTED"
            CueType.SUNDER -> "SUNDER"
            CueType.WAVE -> "RAID ATTACK"
        }
        Ui.text(assets, batch, label, tx + 32f, ty + 45f + age * 45f, 0.85f, Color(tint).also { it.a = alpha })
    }

    private fun drawBasicAttack(batch: SpriteBatch, cue: CombatCue, age: Float, alpha: Float, tint: Color, tx: Float, ty: Float) {
        if (age < 0.2f) line(batch, x(cue.source), y(cue.source) + 20f, tx, ty, tint, 3f, alpha)
        val progress = (age / 0.22f).coerceIn(0f, 1f)
        if (age < 0.22f) {
            disc(batch, x(cue.source) + (tx - x(cue.source)) * progress,
                y(cue.source) + 20f + (ty - y(cue.source) - 20f) * progress, 9f, tint)
        }
        for (i in 0..7) {
            val angle = i * MathUtils.PI2 / 8f
            val radius = 12f + age * 72f
            disc(batch, tx + MathUtils.cos(angle) * radius, ty + MathUtils.sin(angle) * radius, 4f * alpha, tint, alpha)
        }
    }

    private fun slash(batch: SpriteBatch, x: Float, y: Float, length: Float, angle: Float, color: Color, alpha: Float) {
        val dx = MathUtils.cos(angle) * length
        val dy = MathUtils.sin(angle) * length
        line(batch, x - dx, y - dy, x + dx, y + dy, color, 5f, alpha)
    }

    private fun lightning(batch: SpriteBatch, x: Float, y: Float, tx: Float, ty: Float, color: Color, alpha: Float) {
        val dx = (tx - x) / 4f
        val dy = (ty - y) / 4f
        var px = x
        var py = y
        for (i in 1..4) {
            val nx = x + dx * i + if (i < 4) (if (i % 2 == 0) 18f else -18f) else 0f
            val ny = y + dy * i + if (i < 4) (if (i % 2 == 0) -12f else 12f) else 0f
            line(batch, px, py, nx, ny, color, 4f, alpha)
            px = nx
            py = ny
        }
    }

    private fun rect(batch: SpriteBatch, x: Float, y: Float, w: Float, h: Float, color: Color) {
        batch.color = color
        batch.draw(assets.buttonTexture, x, y, w, h)
        batch.color = Color.WHITE
    }

    private fun disc(batch: SpriteBatch, x: Float, y: Float, r: Float, color: Color, alpha: Float = 1f, aspect: Float = 1f) {
        batch.setColor(color.r, color.g, color.b, alpha)
        batch.draw(assets.circleTexture, x - r, y - r * aspect, r * 2f, r * 2f * aspect)
        batch.color = Color.WHITE
    }

    private fun line(batch: SpriteBatch, x: Float, y: Float, tx: Float, ty: Float, color: Color,
                     thickness: Float = 2f, alpha: Float = 1f) {
        val dx = tx - x
        val dy = ty - y
        batch.setColor(color.r, color.g, color.b, alpha)
        batch.draw(assets.buttonTexture, x, y, 0f, thickness / 2f,
            kotlin.math.sqrt(dx * dx + dy * dy), thickness, 1f, 1f,
            MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees, 0, 0, 1, 1, false, false)
        batch.color = Color.WHITE
    }

    private fun ring(batch: SpriteBatch, x: Float, y: Float, r: Float, color: Color, alpha: Float) {
        for (i in 0 until 48) {
            val a = i * MathUtils.PI2 / 48f
            val b = (i + 1) * MathUtils.PI2 / 48f
            line(batch, x + MathUtils.cos(a) * r, y + MathUtils.sin(a) * r,
                x + MathUtils.cos(b) * r, y + MathUtils.sin(b) * r, color, 2f, alpha)
        }
    }
}
