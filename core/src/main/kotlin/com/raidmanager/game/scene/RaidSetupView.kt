package com.raidmanager.game.scene

import com.raidmanager.game.scene.SceneStyle.RaidSetup as Style
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.CharacterDefinition
import com.raidmanager.game.model.RaidFormation
import com.raidmanager.game.model.RaidSetupState
import com.raidmanager.game.model.StatCalculator

/** 편성 화면의 배치와 표시만 담당하며, 편성 규칙은 RaidSetupState에서 조회한다. */
internal class RaidSetupView(private val assets: GameAssets, private val roster: List<CharacterDefinition>) {
    fun render(batch: SpriteBatch, state: RaidSetupState, message: String, statusIndex: Int?) {
        Ui.text(assets, batch, "BUILD YOUR RAID", margin(), Gdx.graphics.height - Style.TITLE_TOP, Style.TITLE_SCALE)
        Ui.text(assets, batch, message, margin(), Gdx.graphics.height - Style.SUMMARY_TOP, Style.SUMMARY_SCALE, Color.LIGHT_GRAY)
        Ui.text(assets, batch, "MONSTERS", margin(), Style.MONSTER_LABEL_Y, Style.SUMMARY_SCALE, Color.LIGHT_GRAY)
        val minus = monsterCountBounds(-1)
        val plus = monsterCountBounds(1)
        Ui.button(assets, batch, "-", minus.x, minus.y, minus.width, minus.height, state.monsterCount > RaidFormation.MIN_MONSTER_COUNT)
        Ui.text(
            assets,
            batch,
            "${state.monsterCount}",
            margin() + Style.MONSTER_VALUE_X,
            Style.MONSTER_VALUE_Y,
            Style.MONSTER_VALUE_SCALE,
            Color.WHITE,
        )
        Ui.button(assets, batch, "+", plus.x, plus.y, plus.width, plus.height, state.monsterCount < RaidFormation.MAX_MONSTER_COUNT)

        roster.forEachIndexed { index, character ->
            val bounds = cardBounds(index)
            val selected = character.id in state.selectedIds
            Ui.button(assets, batch, "${character.name}  [${character.role}]", bounds.x, bounds.y, bounds.width, bounds.height, selected)
            Ui.text(
                assets,
                batch,
                "HP ${character.maxHp.toInt()}  ATK ${character.attackPower.toInt()}  ${character.skillName}",
                bounds.x + Style.CARD_TEXT_X,
                bounds.y + Style.CARD_TEXT_Y,
                Style.CARD_TEXT_SCALE,
                Color.LIGHT_GRAY,
            )
            val status = statusButtonBounds(index)
            Ui.button(assets, batch, "STATUS", status.x, status.y, status.width, status.height, enabled = true)
        }

        statusIndex?.let { renderStatusWindow(batch, roster[it]) }

        val bounds = continueBounds()
        Ui.button(
            assets,
            batch,
            "CHOOSE DUNGEON",
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height,
            enabled = state.isComplete,
        )
        Ui.text(assets, batch, "ESC: MAIN MENU", margin(), Style.FOOTER_Y, Style.FOOTER_SCALE, Color.GRAY)
    }

    private fun renderStatusWindow(batch: SpriteBatch, character: CharacterDefinition) {
        val window = statusBounds()
        batch.color = Style.STATUS_BACKGROUND
        batch.draw(assets.buttonTexture, window.x, window.y, window.width, window.height)
        batch.color = Color.WHITE
        val stats = character.stats
        val combat = StatCalculator.derive(character)
        Ui.text(
            assets,
            batch,
            "${character.name} STATUS",
            window.x + Style.STATUS_PADDING,
            window.y + window.height - Style.STATUS_TITLE_TOP,
            Style.STATUS_TITLE_SCALE,
        )
        Ui.text(
            assets,
            batch,
            "${character.role} · ${character.skillName}",
            window.x + Style.STATUS_PADDING,
            window.y + window.height - Style.STATUS_SUBTITLE_TOP,
            Style.STATUS_SUBTITLE_SCALE,
            Color.LIGHT_GRAY,
        )
        val lines = listOf(
            "VIT ${stats.vitality}     STR ${stats.strength}",
            "INT ${stats.intelligence}     DEX ${stats.dexterity}",
            "END ${stats.endurance}     WIS ${stats.wisdom}",
            "MAX HP ${combat.maxHp.toInt()}     ATTACK ${combat.attackPower.toInt()}",
            "DEF ${combat.defense.toInt()}     ATK SPEED ${combat.attackInterval.formatStat()}",
            "SKILL POWER ${combat.skillPower.formatStat()}x     COOLDOWN ${combat.skillCooldown.formatStat()}s",
        )
        lines.forEachIndexed { index, line ->
            Ui.text(
                assets,
                batch,
                line,
                window.x + Style.STATUS_PADDING,
                window.y + window.height - Style.STATUS_CONTENT_TOP - index * Style.STATUS_LINE_HEIGHT,
                Style.STATUS_TEXT_SCALE,
                Color.WHITE,
            )
        }
        Ui.button(
            assets,
            batch,
            "CLOSE",
            window.x + window.width - Style.CLOSE_RIGHT,
            window.y + Style.CLOSE_BOTTOM,
            Style.CLOSE_WIDTH,
            Style.CLOSE_HEIGHT,
        )
    }

    fun statusButtonBounds(index: Int): Bounds {
        val card = cardBounds(index)
        return Bounds(
            card.x + card.width - Style.STATUS_BUTTON_RIGHT,
            card.y + Style.STATUS_BUTTON_BOTTOM,
            Style.STATUS_BUTTON_WIDTH,
            Style.STATUS_BUTTON_HEIGHT,
        )
    }

    fun statusBounds(): Bounds = Bounds(
        Gdx.graphics.width * Style.MODAL_X_RATIO,
        Gdx.graphics.height * Style.MODAL_Y_RATIO,
        Gdx.graphics.width * Style.MODAL_WIDTH_RATIO,
        Gdx.graphics.height * Style.MODAL_HEIGHT_RATIO,
    )

    private fun margin(): Float = Gdx.graphics.width * Style.MARGIN_RATIO

    /** 행/열 인덱스로 2열 카드의 위치를 계산하며 화면 너비에서 여백과 간격을 먼저 제외한다. */
    fun cardBounds(index: Int): Bounds {
        val gap = Style.CARD_GAP
        val width = (Gdx.graphics.width - margin() * 2f - gap) / Style.CARD_COLUMNS
        val height = Style.CARD_HEIGHT
        val column = index % Style.CARD_COLUMNS
        val row = index / Style.CARD_COLUMNS
        val x = margin() + column * (width + gap)
        val y = Gdx.graphics.height - Style.CARD_TOP - row * (height + gap)
        return Bounds(x, y, width, height)
    }

    fun continueBounds(): Bounds {
        val width = Style.CONTINUE_WIDTH
        return Bounds(Gdx.graphics.width - margin() - width, Style.CONTINUE_Y, width, Style.BUTTON_HEIGHT)
    }

    fun monsterCountBounds(delta: Int): Bounds {
        val size = Style.MONSTER_BUTTON_WIDTH
        val x = if (delta < 0) margin() else margin() + Style.PLUS_OFFSET
        return Bounds(x, Style.MONSTER_BUTTON_Y, size, Style.MONSTER_BUTTON_HEIGHT)
    }


    private fun Float.formatStat(): String = "%.2f".format(this)
}
