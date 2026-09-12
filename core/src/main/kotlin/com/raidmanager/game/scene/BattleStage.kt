package com.raidmanager.game.scene

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.raidmanager.game.model.AttackStyle
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.BattleSimulator
import com.raidmanager.game.model.BattleSimulator.CombatCue
import com.raidmanager.game.model.BattleSimulator.CueType
import com.raidmanager.game.model.DungeonDefinition
import com.raidmanager.game.model.DungeonMechanic
import com.raidmanager.game.model.RaidFormation
import com.raidmanager.game.model.Role
import com.raidmanager.game.scene.BattleStageStyle as Style
import kotlin.math.sin
import kotlin.math.abs

/** 전투 상태를 읽어 화면에 표현한다. 게임 규칙은 변경하지 않으며 좌표계는 1280 × 720이다. */
internal class BattleStage(private val assets: GameAssets, private val formation: RaidFormation) {
    private data class Effect(val cue: CombatCue, var age: Float = 0f)
    private data class Popup(
        val text: String, val x: Float, val y: Float, val color: Color,
        val target: String, val emphasis: Boolean, var age: Float = 0f,
    )
    private val effects = mutableListOf<Effect>()
    private val popups = mutableListOf<Popup>()
    private var clock = 0f
    private var shake = 0f
    private var previousEnemyHp = Float.MAX_VALUE
    private var resultAge = 0f
    private val positions = mutableMapOf<String, Vector2>()
    private val moving = mutableSetOf<String>()
    private val monsterAnimations = mutableMapOf<String, MonsterAnimation>()
    private val gold = Color.valueOf("F4CD85")
    private val blue = Color.valueOf("62C9FF")
    private val green = Color.valueOf("70EDAD")
    private val red = Color.valueOf("FF795F")
    private val violet = Color.valueOf("C292FF")
    private val danger = Color.valueOf("FF3D59")

    fun update(delta: Float, cues: List<CombatCue>, snapshot: BattleSimulator.Snapshot) {
        clock += delta
        if (snapshot.finished) resultAge += delta
        shake = (shake - delta * Style.SHAKE_DECAY).coerceAtLeast(0f)
        effects.forEach { it.age += delta }
        popups.forEach { it.age += delta }
        effects.removeAll { it.age > Style.EFFECT_LIFETIME }
        popups.removeAll { it.age > Style.POPUP_LIFETIME }
        addCueEffects(cues, snapshot)
        if (previousEnemyHp != Float.MAX_VALUE && snapshot.enemyHp <= 0f && previousEnemyHp > 0f) shake = Style.DEATH_SHAKE
        previousEnemyHp = snapshot.enemyHp
        moving.clear()
        fun follow(id: String, x: Float, y: Float, alive: Boolean) {
            val projected = Vector2(BattleProjection.x(x, y), BattleProjection.y(x, y))
            val position = positions.getOrPut(id) { projected.cpy() }
            if (alive && !snapshot.finished && position.dst(projected) > Style.MOVEMENT_THRESHOLD) moving += id
            if (!alive || snapshot.finished) position.set(projected)
            else position.lerp(projected, (delta * Style.FOLLOW_SPEED).coerceIn(0f, 1f))
        }
        snapshot.members.forEach { follow(it.character.id, it.x, it.y, it.hp > 0f) }
        snapshot.enemies.forEach { follow(it.id, it.x, it.y, it.hp > 0f) }
        snapshot.enemies.forEachIndexed { index, enemy ->
            monsterAnimations.getOrPut(enemy.id) { MonsterAnimation(enemy.id, index * Style.MONSTER_PHASE_SPACING) }
                .update(delta, cues, enemy.hp, snapshot.finished)
        }
    }

    private fun addCueEffects(cues: List<CombatCue>, snapshot: BattleSimulator.Snapshot) {
        cues.forEach { cue ->
            effects += Effect(cue)
            val isDamage = cue.type == CueType.HIT && cue.amount > 0f
            val popupColor = if (isDamage && isEnemy(cue.target)) gold else if (isDamage) red else when (cue.type) {
                CueType.HEAL -> green
                CueType.SHIELD -> blue
                else -> violet
            }
            val text = when {
                isDamage -> "${if (cue.rearAttack) "BACK! " else ""}-${cue.amount.toInt()}"
                cue.type == CueType.HEAL -> "+${cue.amount.toInt()}"
                cue.type == CueType.SHIELD -> "SHIELD"
                cue.type == CueType.INTERRUPT -> "INTERRUPTED"
                cue.type == CueType.SUNDER -> "BREAK!"
                cue.type == CueType.TAUNT -> "TAUNT"
                cue.type == CueType.HIT -> "BLOCK"
                cue.type == CueType.WAVE -> "RAID ATTACK"
                else -> null
            }
            if (text != null) {
                val lane = popups.count { it.target == cue.target && it.age < Style.POPUP_LANE_WINDOW } % Style.POPUP_LANES
                val targetPosition = snapshot.members.firstOrNull { it.character.id == cue.target }
                    ?.let { Vector2(BattleProjection.x(it.x, it.y), BattleProjection.y(it.x, it.y)) }
                    ?: snapshot.enemies.firstOrNull { it.id == cue.target }
                        ?.let { Vector2(BattleProjection.x(it.x, it.y), BattleProjection.y(it.x, it.y)) }
                    ?: Vector2(x(cue.target), y(cue.target))
                popups += Popup(text, targetPosition.x + (lane % 2 * 2 - 1) * Style.POPUP_X_SPACING,
                    targetPosition.y + Style.POPUP_BASE_HEIGHT + lane * Style.POPUP_ROW_HEIGHT, popupColor, cue.target,
                    cue.rearAttack || cue.skillType != null || cue.type == CueType.INTERRUPT)
            }
            if (isDamage && cue.amount >= Style.SHAKE_DAMAGE_THRESHOLD || cue.type == CueType.WAVE) shake = maxOf(shake, Style.HIT_SHAKE)
        }
    }

    private fun isEnemy(id: String): Boolean = id == "boss" || id.startsWith("boss-")
    private fun enemyIndex(id: String): Int = id.substringAfter("boss-", "0").toIntOrNull() ?: 0
    private fun x(id: String): Float = positions[id]?.x ?: if (isEnemy(id)) {
        if (formation.monsterCount == 1) 960f else 830f + enemyIndex(id) * 125f
    } else {
        if (formation.members.firstOrNull { it.id == id }?.role == Role.TANK) 430f else 270f
    }

    private fun y(id: String): Float = positions[id]?.y ?: if (isEnemy(id)) {
        if (formation.monsterCount == 1) 380f else 440f - enemyIndex(id) * 85f
    } else {
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
        drawBackgroundAndHeader(batch, snapshot, dungeon, theme)

        for (gx in 180..1100 step 92) {
            line(batch, BattleProjection.x(gx.toFloat(), 150f), BattleProjection.y(gx.toFloat(), 150f),
                BattleProjection.x(gx.toFloat(), 650f), BattleProjection.y(gx.toFloat(), 650f), blue, 1f, 0.12f)
        }
        for (gy in 150..650 step 50) {
            line(batch, BattleProjection.x(180f, gy.toFloat()), BattleProjection.y(180f, gy.toFloat()),
                BattleProjection.x(1100f, gy.toFloat()), BattleProjection.y(1100f, gy.toFloat()), blue, 1f, 0.12f)
        }
        snapshot.members.filter { it.hp > 0f }.forEach { member ->
            val target = member.targetId
            if (target != null && !snapshot.finished) {
                val tint = if (member.intent == BattleSimulator.MovementIntent.RETREAT) red else color(member.character.role)
                line(batch, x(member.character.id), y(member.character.id), x(target), y(target), tint, 1f, 0.18f)
            }
        }

        if (dungeon.mechanic != DungeonMechanic.REGEN) drawThreatWarnings(batch, snapshot)
        val actors = snapshot.members.map { it.character.id } + snapshot.enemies.map { it.id }
        actors.sortedByDescending { y(it) }.forEach { id ->
            val member = snapshot.members.firstOrNull { it.character.id == id }
            if (member != null) drawMember(batch, member, snapshot.finished)
            else drawEnemy(batch, snapshot.enemies.first { it.id == id }, dungeon, snapshot.finished)
        }
        drawCombatHud(batch, snapshot)
        effects.forEach { drawEffect(batch, it) }
        drawMonsterEffects(batch, snapshot, dungeon)
        popups.forEach { drawPopup(batch, it) }
        drawStatusIcons(batch, snapshot)
        if (!snapshot.finished && snapshot.abilityCastRemaining > 0f && dungeon.mechanic != DungeonMechanic.REGEN) {
            for (i in 0 until 10) {
                val tint = Color(danger).also { it.a = (1f - i / 10f) * (0.12f + abs(sin(clock * 5f)) * 0.1f) }
                rect(batch, i * 5f, 175f, 5f, 395f, tint)
                rect(batch, 1275f - i * 5f, 175f, 5f, 395f, tint)
            }
        }
        if (clock < Style.ENGAGE_DURATION && !snapshot.finished) {
            val alpha = ((Style.ENGAGE_DURATION - clock) / 0.5f).coerceIn(0f, 1f)
            rect(batch, 420f, 440f, 440f, 58f, Color(0.02f, 0.03f, 0.06f, alpha * 0.8f))
            Ui.text(assets, batch, "RAID ENGAGE", 530f, 479f, 1.4f, Color(gold).also { it.a = alpha })
        }
        if (snapshot.finished && snapshot.victory) drawVictoryBurst(batch)
        batch.color = Color.WHITE
    }

    private fun drawBackgroundAndHeader(
        batch: SpriteBatch, snapshot: BattleSimulator.Snapshot, dungeon: DungeonDefinition, theme: Color,
    ) {
        batch.setColor(0.88f, 0.88f, 0.88f, 1f)
        val offsetX = if (shake > 0f) sin(clock * 91f) * shake * 18f else 0f
        val offsetY = if (shake > 0f) sin(clock * 73f) * shake * 12f else 0f
        batch.draw(assets.battleBackground(dungeon.id), -8f + offsetX, -8f + offsetY, 1296f, 736f)
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
        Ui.text(assets, batch, "${dungeon.enemyName} x${formation.monsterCount}", 770f, 625f, 0.85f, theme)
        Ui.bar(assets, batch, 770f, 593f, 440f, 10f, snapshot.enemyHp / snapshot.enemyMaxHp, red)
        Ui.text(assets, batch, "${snapshot.enemyHp.toInt()} HP", 1120f, 625f, 0.7f)

    }

    private fun drawCombatHud(batch: SpriteBatch, snapshot: BattleSimulator.Snapshot) {
        // Fixed HUD rows keep labels and threat percentages out of the moving sprites.
        snapshot.members.forEachIndexed { index, member ->
            val target = member.targetId?.let { "#${enemyIndex(it) + 1}" } ?: "-"
            val action = when (member.intent) {
                BattleSimulator.MovementIntent.APPROACH -> "APPROACH"
                BattleSimulator.MovementIntent.RETREAT -> "TOO CLOSE / RETREAT"
                BattleSimulator.MovementIntent.SPACING -> "MAKE ROOM"
                BattleSimulator.MovementIntent.DOWN -> "DOWN"
                BattleSimulator.MovementIntent.HOLD -> "IN RANGE / HOLD"
            }
            Ui.text(assets, batch, "${member.character.name} > $target  $action", 44f, 640f - index * 22f,
                0.6f, color(member.character.role))
        }
        snapshot.enemies.forEachIndexed { enemySlot, enemy ->
            Ui.text(assets, batch, "ENEMY #${enemySlot + 1}", 770f + enemySlot * 150f, 580f, 0.48f, red)
            formation.members.forEachIndexed { index, character ->
                val active = character.id == enemy.targetId
                val percentage = enemy.aggro.getValue(character.id)
                Ui.text(assets, batch,
                    "${if (active) ">" else " "}${character.name} ${"%.1f".format(java.util.Locale.ROOT, percentage)}%",
                    770f + enemySlot * 150f, 565f - index * 14f, 0.48f, if (active) gold else Color.LIGHT_GRAY)
            }
        }
    }

    /** 공격 전진·피격 반동·호흡을 합성하고 발 기준 회전으로 캐릭터 위치를 유지한다. */
    private fun drawMember(batch: SpriteBatch, member: BattleSimulator.MemberSnapshot, finished: Boolean) {
        val id = member.character.id
        val actionAge = effects.filter { it.cue.source == id }
            .minOfOrNull { it.age }
        val hitAge = effects.filter { it.cue.target == id && it.cue.type == CueType.HIT }
            .minOfOrNull { it.age }
        val alive = member.hp > 0f
        val acting = alive && actionAge != null && actionAge < 0.28f
        val hurt = alive && hitAge != null && hitAge < 0.24f
        val melee = member.character.attackStyle == AttackStyle.MELEE
        val facing = if (BattleProjection.facingX(member.facingX, member.facingY) < 0f) -1f else 1f
        val advance = if (acting && !hurt && melee) sin(actionAge!! / 0.28f * MathUtils.PI) * 8f else 0f
        val recoil = if (hurt) sin(hitAge!! / 0.24f * MathUtils.PI) * -4f else 0f
        val px = x(id) + (advance + recoil) * facing
        val running = id in moving && alive && !acting && !hurt
        val py = y(id) + 37f + if (running) kotlin.math.abs(sin(clock * 18f + id.hashCode())) * 3f else 0f
        val tint = color(member.character.role)
        disc(batch, px, py - 37f, 32f, Color.BLACK, 0.4f, 0.28f)
        if (running) {
            for (i in 1..3) disc(batch, px - i * 10f, y(id), 5f - i, tint, 0.2f, 0.5f)
        }
        val frame = assets.animatedFrame(id,
            SpriteTimeline.frame(clock + id.length * 0.13f, running, actionAge, hitAge, alive, finished))
        // Keep subtle foot-anchored motion on top of the authored frame animation.
        val breath = if (alive && !acting && !hurt && !finished) sin(clock * 3f + y(id)) * 0.018f else 0f
        val castLean = if (acting && !melee && !hurt) sin(actionAge!! / 0.28f * MathUtils.PI) * -5f else 0f
        batch.setColor(1f, if (hurt) 0.6f else 1f, if (hurt) 0.6f else 1f, if (alive) 1f else 0.45f)
        val previousShader = batch.shader
        batch.shader = assets.heroShader
        val scale = 86f / frame.region.regionHeight
        val footX = frame.footX * scale
        val footY = frame.footY * scale
        batch.draw(frame.region, px - footX, py - 37f - footY, footX, footY,
            frame.region.regionWidth * scale, frame.region.regionHeight * scale,
            facing, 1f + breath, (if (!alive) 80f else if (running) -6f else castLean) * facing)
        batch.shader = previousShader
        batch.color = Color.WHITE
        if (alive && member.shield > 0f) {
            drawPersistentShield(batch, px, py, member.shield, hitAge)
        }
        if (!alive) Ui.text(assets, batch, "DOWN", px - 22f, py + 14f, 0.7f, red)
        Ui.text(assets, batch, member.character.name, px - 28f, py + 38f, 0.5f, tint)
        Ui.bar(assets, batch, px - 46f, py - 53f, 92f, 5f, member.hp / member.character.maxHp, green)
    }

    private fun drawEnemy(
        batch: SpriteBatch, enemy: BattleSimulator.EnemySnapshot, dungeon: DungeonDefinition, finished: Boolean,
    ) {
        val motion = monsterAnimations[enemy.id]?.motion(dungeon.mechanic) ?: MonsterAnimation.Motion(GameAssets.SpritePose.IDLE)
        val actionAge = effects.filter { it.cue.source == enemy.id }.minOfOrNull { it.age }
        val hitAge = effects.filter { it.cue.target == enemy.id && it.cue.type == CueType.HIT }
            .minOfOrNull { it.age }
        val monster = assets.animatedFrame(dungeon.id,
            SpriteTimeline.frame(clock, enemy.id in moving, actionAge, hitAge, enemy.hp > 0f, finished))
        val scale = (if (dungeon.mechanic == DungeonMechanic.REGEN) 112f else 145f) / monster.region.regionHeight
        val px = x(enemy.id)
        val groundY = y(enemy.id)
        val footX = monster.footX * scale
        val footY = monster.footY * scale
        val facing = if (BattleProjection.facingX(enemy.facingX, enemy.facingY) > 0f) -1f else 1f
        disc(batch, px, groundY, 65f, Color.BLACK, 0.5f, 0.22f)
        val previousShader = batch.shader
        batch.shader = assets.monsterShader
        batch.setColor(1f, if (motion.hurt) 0.62f else 1f, if (motion.hurt) 0.62f else 1f, motion.alpha)
        batch.draw(monster.region, px + motion.offsetX * facing - footX, groundY - footY, footX, footY,
            monster.region.regionWidth * scale, monster.region.regionHeight * scale,
            motion.scaleX * facing, motion.scaleY, motion.rotation * facing)
        batch.shader = previousShader
        batch.color = Color.WHITE
        Ui.bar(assets, batch, px - 48f, groundY - 16f, 96f, 6f, enemy.hp / enemy.maxHp, red)
        Ui.text(assets, batch, "#${enemyIndex(enemy.id) + 1}  ${enemy.hp.toInt()} HP", px - 48f, groundY - 23f, 0.55f)
    }

    /** 보호막 양과 피격 경과 시간을 정규화해 크기와 불투명도에 호흡 진동을 더한다. */
    private fun drawPersistentShield(batch: SpriteBatch, px: Float, py: Float, amount: Float, hitAge: Float?) {
        val pulse = sin(clock * 2.8f + px * 0.01f)
        val strength = (amount / 30f).coerceIn(0f, 1f)
        val impact = if (hitAge == null) 0f else (1f - hitAge / 0.22f).coerceIn(0f, 1f)
        val width = 120f + pulse * 3f + impact * 8f
        val height = width * 1.08f
        val source = batch.blendSrcFunc
        val destination = batch.blendDstFunc
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE)
        batch.setColor(1f, 1f, 1f, (0.32f + strength * 0.25f + pulse * 0.04f + impact * 0.25f).coerceAtMost(0.9f))
        batch.draw(assets.heroEffect(com.raidmanager.game.model.SkillType.GUARD),
            px - width / 2f, py + 5f - height / 2f, width, height)
        batch.setBlendFunction(source, destination)
        batch.color = Color.WHITE
    }

    /** Persistent statuses come from simulation state, never from the short-lived VFX queue. */
    private fun drawStatusIcons(batch: SpriteBatch, snapshot: BattleSimulator.Snapshot) {
        snapshot.members.filter { it.hp > 0f && it.shield > 0f }.forEach { member ->
            statusIcon(batch, x(member.character.id), y(member.character.id) + 96f, blue,
                kotlin.math.ceil(member.shield).toInt().toString(), shield = true)
        }
        snapshot.enemies.filter { it.hp > 0f && it.healingReductionRemaining > 0f }.forEach { enemy ->
            statusIcon(batch, x(enemy.id), y(enemy.id) + 158f, violet,
                "${kotlin.math.ceil(enemy.healingReductionRemaining).toInt()}s", shield = false)
        }
    }

    private fun statusIcon(batch: SpriteBatch, px: Float, py: Float, tint: Color, label: String, shield: Boolean) {
        // Opaque backing and distinct silhouettes remain legible over additive attack effects.
        rect(batch, px - 15f, py - 15f, 30f, 30f, tint)
        rect(batch, px - 13f, py - 13f, 26f, 26f, Color(0.025f, 0.035f, 0.065f, 0.96f))
        if (shield) {
            line(batch, px - 7f, py + 7f, px + 7f, py + 7f, tint, 2f)
            line(batch, px - 7f, py + 7f, px - 6f, py - 2f, tint, 2f)
            line(batch, px + 7f, py + 7f, px + 6f, py - 2f, tint, 2f)
            line(batch, px - 6f, py - 2f, px, py - 8f, tint, 2f)
            line(batch, px + 6f, py - 2f, px, py - 8f, tint, 2f)
        } else {
            rect(batch, px - 2f, py - 7f, 4f, 14f, tint)
            rect(batch, px - 7f, py - 2f, 14f, 4f, tint)
            line(batch, px - 9f, py - 9f, px + 9f, py + 9f, Color.WHITE, 2f)
        }
        Ui.text(assets, batch, label, px - 10f + 1f, py - 19f - 1f, 0.6f, Color.BLACK)
        Ui.text(assets, batch, label, px - 10f, py - 19f, 0.6f, Color.WHITE)
    }

    private fun drawThreatWarnings(batch: SpriteBatch, snapshot: BattleSimulator.Snapshot) {
        snapshot.enemies.filter { it.castRemaining > 0f && !snapshot.finished }.forEach { enemy ->
            val progress = 1f - enemy.castRemaining / snapshot.abilityCastDuration
            ring(batch, x(enemy.id), y(enemy.id), 82f + progress * 18f, danger, 0.35f + progress * 0.5f)
            snapshot.members.filter { it.hp > 0f }.forEach { member ->
                // Current offensive boss abilities hit the entire living raid.
                if (snapshot.abilityCastRemaining > 0f) {
                    disc(batch, x(member.character.id), y(member.character.id), 48f, danger, 0.12f + progress * 0.12f, 0.4f)
                    ring(batch, x(member.character.id), y(member.character.id), 48f - progress * 20f, danger, 0.7f)
                }
            }
        }
    }

    /** 표시 시간을 정규화하고 이차 감속 곡선으로 숫자를 상승시키며 끝부분을 흐리게 한다. */
    private fun drawPopup(batch: SpriteBatch, popup: Popup) {
        val progress = (popup.age / Style.POPUP_LIFETIME).coerceIn(0f, 1f)
        val alpha = ((1f - progress) / 0.35f).coerceIn(0f, 1f)
        val scale = (if (popup.emphasis) 1.05f else 0.85f) + 0.25f * (1f - popup.age / 0.16f).coerceIn(0f, 1f)
        val px = popup.x.coerceIn(50f, 1100f)
        val py = (popup.y + (1f - (1f - progress) * (1f - progress)) * 45f).coerceAtMost(520f)
        Ui.text(assets, batch, popup.text, px + 2f, py - 2f, scale, Color(0f, 0f, 0f, alpha))
        Ui.text(assets, batch, popup.text, px, py, scale, Color(popup.color).also { it.a = alpha })
    }

    /** 원주를 등분한 각도에 입자를 배치하고 시간에 따라 반지름을 늘린다. */
    private fun drawVictoryBurst(batch: SpriteBatch) {
        if (resultAge > Style.VICTORY_DURATION) return
        for (i in 0 until 18) {
            val angle = i * MathUtils.PI2 / 18f
            val radius = 50f + resultAge * 140f
            disc(batch, 640f + MathUtils.cos(angle) * radius, 390f + MathUtils.sin(angle) * radius,
                5f, gold, (1f - resultAge / Style.VICTORY_DURATION).coerceIn(0f, 1f))
        }
    }

    private fun drawMonsterEffects(batch: SpriteBatch, snapshot: BattleSimulator.Snapshot, dungeon: DungeonDefinition) {
        val row = when (dungeon.mechanic) {
            DungeonMechanic.BURST -> 0
            DungeonMechanic.SWARM -> 1
            DungeonMechanic.REGEN -> 2
        }
        snapshot.enemies.filter { !snapshot.finished && it.hp > 0f && it.castRemaining > 0f }.forEach { enemy ->
            val progress = (1f - enemy.castRemaining / snapshot.abilityCastDuration).coerceIn(0f, 1f)
            val px = x(enemy.id)
            val py = y(enemy.id)
            drawVfx(batch, row, px, py, 150f + progress * 60f, 0.15f + progress * 0.25f)
            Ui.text(assets, batch, dungeon.ability.name.replace('_', ' '), px - 65f, py + 95f, 0.5f, gold)
            Ui.bar(assets, batch, px - 48f, py + 75f, 96f, 5f, progress, gold)
        }
        effects.filter { isEnemy(it.cue.source) }.forEach { effect ->
            val px = x(effect.cue.source)
            val py = y(effect.cue.source)
            val progress = (effect.age / Style.EFFECT_LIFETIME).coerceIn(0f, 1f)
            val alpha = (1f - progress) * 0.9f
            when (effect.cue.type) {
                CueType.WAVE -> {
                    if (row == 1) {
                        drawVfx(batch, row, px - progress * (px - 350f), py, 300f, alpha)
                    } else {
                        drawVfx(batch, row, px - 100f, py - 40f, 260f + progress * 380f, alpha)
                    }
                }
                CueType.HEAL -> drawVfx(batch, 2, px, py + progress * 55f, 320f, alpha)
                CueType.INTERRUPT -> drawVfx(batch, 3, px, py, 180f + progress * 200f, alpha)
                else -> Unit
            }
        }
    }

    private fun drawVfx(batch: SpriteBatch, index: Int, px: Float, py: Float, size: Float, alpha: Float) {
        val source = batch.blendSrcFunc
        val destination = batch.blendDstFunc
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE)
        batch.setColor(1f, 1f, 1f, alpha)
        batch.draw(assets.monsterEffects[index], px - size / 2f, py - size / 2f, size, size)
        batch.setBlendFunction(source, destination)
        batch.color = Color.WHITE
    }

    private fun drawEffect(batch: SpriteBatch, effect: Effect) {
        val cue = effect.cue
        val age = effect.age
        val alpha = (1f - age / Style.EFFECT_LIFETIME).coerceIn(0f, 1f)
        val tx = x(cue.target)
        val ty = y(cue.target) + 20f
        val skill = cue.skillType
        val tint = when (cue.type) {
            CueType.HEAL -> green
            CueType.SHIELD -> blue
            CueType.INTERRUPT, CueType.SUNDER -> violet
            else -> if (isEnemy(cue.source)) red else gold
        }
        if (skill != null && !isEnemy(cue.source)) {
            drawHeroEffect(batch, cue, age)
        } else if (cue.type == CueType.HIT) {
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
        val rangedBasic = cue.skillType == null && formation.members.any {
            it.id == cue.source && it.attackStyle == AttackStyle.RANGED
        }
        if (!rangedBasic && cue.type == CueType.HIT && cue.amount > 0f && age < 0.28f) {
            val impact = 1f - age / 0.28f
            disc(batch, tx, ty, 12f * impact, Color.WHITE, impact)
            ring(batch, tx, ty, 10f + age * 160f, tint, impact)
            for (i in 0 until 6) {
                val angle = i * MathUtils.PI2 / 6f
                val radius = 15f + age * 160f
                disc(batch, tx + MathUtils.cos(angle) * radius, ty + MathUtils.sin(angle) * radius,
                    3f * impact, tint, impact)
            }
        }
    }

    private fun drawHeroEffect(batch: SpriteBatch, cue: CombatCue, age: Float) {
        val skill = cue.skillType ?: return
        // SUNDER emits both a status cue and damage cue; render its sprite only once.
        if (skill == com.raidmanager.game.model.SkillType.SUNDER && cue.type == CueType.HIT) return
        val progress = (age / Style.EFFECT_LIFETIME).coerceIn(0f, 1f)
        val size = when (skill) {
            com.raidmanager.game.model.SkillType.GUARD -> 160f + progress * 25f
            com.raidmanager.game.model.SkillType.HEAL -> 150f
            com.raidmanager.game.model.SkillType.STRIKE -> 190f + progress * 45f
            com.raidmanager.game.model.SkillType.CLEAVE -> 220f + progress * 100f
            com.raidmanager.game.model.SkillType.INTERRUPT -> 160f + progress * 60f
            com.raidmanager.game.model.SkillType.SUNDER -> 180f
        }
        val rise = when (skill) {
            com.raidmanager.game.model.SkillType.HEAL -> progress * 45f
            com.raidmanager.game.model.SkillType.SUNDER -> -progress * 20f
            else -> 0f
        }
        val src = batch.blendSrcFunc
        val dst = batch.blendDstFunc
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE)
        batch.setColor(1f, 1f, 1f, (1f - progress) * 0.85f)
        batch.draw(assets.heroEffect(skill), x(cue.target) - size / 2f, y(cue.target) - size / 2f + rise, size, size)
        batch.setBlendFunction(src, dst)
        batch.color = Color.WHITE
    }

    private fun drawBasicAttack(batch: SpriteBatch, cue: CombatCue, age: Float, alpha: Float, tint: Color, tx: Float, ty: Float) {
        val melee = isEnemy(cue.source) || formation.members.firstOrNull { it.id == cue.source }
            ?.attackStyle == AttackStyle.MELEE
        if (melee) {
            if (age < 0.3f) {
                slash(batch, tx, ty, 35f + age * 45f, -0.65f, tint, 1f - age / 0.3f)
                ring(batch, tx, ty, 12f + age * 90f, tint, 1f - age / 0.3f)
            }
            return
        }
        drawRangedAttack(batch, cue, age, tx, ty)
    }

    /** 직선 보간에 사인 높이를 더해 포물선 형태의 궤적을 만들고 잔상과 충돌 확산을 순서대로 그린다. */
    private fun drawRangedAttack(batch: SpriteBatch, cue: CombatCue, age: Float, tx: Float, ty: Float) {
        val character = formation.members.firstOrNull { it.id == cue.source } ?: return
        val tint = when (character.id) {
            "ember" -> red
            "luna" -> green
            "mira" -> Color.PINK
            else -> violet
        }
        val sx = x(cue.source)
        val sy = y(cue.source) + 35f
        val flight = Style.PROJECTILE_FLIGHT
        val arc = if (character.id == "ember") 34f else 16f
        val heading = MathUtils.atan2(ty - sy, tx - sx) * MathUtils.radiansToDegrees
        val oldSrc = batch.blendSrcFunc
        val oldDst = batch.blendDstFunc
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE)
        fun sprite(px: Float, py: Float, size: Float, opacity: Float, rotation: Float) {
            batch.setColor(1f, 1f, 1f, opacity)
            batch.draw(assets.heroEffect(character.skillType), px - size / 2f, py - size / 2f,
                size / 2f, size / 2f, size, size, 1f, 1f, rotation)
        }
        if (age < flight) {
            val progress = age / flight
            sprite(sx, sy, 54f + progress * 30f, (1f - progress) * 0.65f, -age * 180f)
            // Fading sprite copies follow the same curved path as the projectile.
            for (i in 4 downTo 0) {
                val p = progress - i * 0.065f
                if (p < 0f) continue
                val px = MathUtils.lerp(sx, tx, p)
                val py = MathUtils.lerp(sy, ty, p) + sin(p * MathUtils.PI) * arc
                sprite(px, py, 62f - i * 8f, 0.95f / (i + 1f), heading + age * 420f)
                if (i == 0) disc(batch, px, py, 5f, tint, 0.9f)
            }
        } else {
            val impact = ((age - flight) / 0.45f).coerceIn(0f, 1f)
            if (impact < 1f) {
                sprite(tx, ty, 65f + impact * 95f, (1f - impact) * 0.95f, heading + impact * 60f)
                disc(batch, tx, ty, 14f * (1f - impact), Color.WHITE, 1f - impact)
                ring(batch, tx, ty, 12f + impact * 48f, tint, 1f - impact)
                for (i in 0 until 8) {
                    val angle = i * MathUtils.PI2 / 8f
                    val radius = 12f + impact * 62f
                    disc(batch, tx + MathUtils.cos(angle) * radius, ty + MathUtils.sin(angle) * radius,
                        4f * (1f - impact), tint, 1f - impact)
                }
            }
        }
        batch.setBlendFunction(oldSrc, oldDst)
        batch.color = Color.WHITE
    }

    private fun slash(batch: SpriteBatch, x: Float, y: Float, length: Float, angle: Float, color: Color, alpha: Float) {
        val dx = MathUtils.cos(angle) * length
        val dy = MathUtils.sin(angle) * length
        line(batch, x - dx, y - dy, x + dx, y + dy, color, 5f, alpha)
    }

    /** 시작점과 끝점을 네 구간으로 나누고 중간 점에 교대 오프셋을 주어 번개를 만든다. */
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

    /** 두 점의 거리와 atan2 회전각으로 단색 텍스처를 선분에 맞춰 그린다. */
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

    /** 원을 일정 각도로 나눈 뒤 인접한 원주 좌표를 연결하여 테두리를 그린다. */
    private fun ring(batch: SpriteBatch, x: Float, y: Float, r: Float, color: Color, alpha: Float) {
        for (i in 0 until Style.RING_SEGMENTS) {
            val a = i * MathUtils.PI2 / Style.RING_SEGMENTS
            val b = (i + 1) * MathUtils.PI2 / Style.RING_SEGMENTS
            line(batch, x + MathUtils.cos(a) * r, y + MathUtils.sin(a) * r,
                x + MathUtils.cos(b) * r, y + MathUtils.sin(b) * r, color, 2f, alpha)
        }
    }
}
