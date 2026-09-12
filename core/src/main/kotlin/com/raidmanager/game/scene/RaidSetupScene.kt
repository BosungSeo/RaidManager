package com.raidmanager.game.scene

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.raidmanager.game.GameAssets
import com.raidmanager.game.model.CharacterDefinition
import com.raidmanager.game.model.RaidFormation
import com.raidmanager.game.model.RaidSetupState
import java.util.ArrayDeque

class RaidSetupScene(
    private val assets: GameAssets,
    private val roster: List<CharacterDefinition>,
    initialFormation: RaidFormation?,
    private val onContinue: (RaidFormation) -> Unit,
    private val onBack: () -> Unit,
) : InputAdapter(), Scene {
    private sealed interface SetupCommand {
        data class Toggle(val index: Int) : SetupCommand
        data class ShowStatus(val index: Int) : SetupCommand
        data class MonsterCount(val delta: Int) : SetupCommand
        data object CloseStatus : SetupCommand
        data object Continue : SetupCommand
        data object Back : SetupCommand
    }

    private val state = RaidSetupState(roster, initialFormation)
    private val view = RaidSetupView(assets, roster)
    private val pendingCommands = ArrayDeque<SetupCommand>()
    private var message = "Select exactly ${RaidFormation.PARTY_SIZE} members"
    private var statusIndex: Int? = null

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (statusIndex != null) {
            if (contains(screenX, screenY, view.statusBounds())) {
                pendingCommands.addLast(SetupCommand.CloseStatus)
            }
            return true
        }
        for (index in roster.indices) {
            if (contains(screenX, screenY, view.statusButtonBounds(index))) {
                pendingCommands.addLast(SetupCommand.ShowStatus(index))
                return true
            }
            if (contains(screenX, screenY, view.cardBounds(index))) {
                pendingCommands.addLast(SetupCommand.Toggle(index))
                return true
            }
        }
        val command = when {
            contains(screenX, screenY, view.continueBounds()) -> SetupCommand.Continue
            contains(screenX, screenY, view.monsterCountBounds(-1)) -> SetupCommand.MonsterCount(-1)
            contains(screenX, screenY, view.monsterCountBounds(1)) -> SetupCommand.MonsterCount(1)
            else -> return false
        }
        pendingCommands.addLast(command)
        return true
    }

    private fun contains(screenX: Int, screenY: Int, bounds: Bounds): Boolean =
        Ui.contains(screenX, screenY, bounds.x, bounds.y, bounds.width, bounds.height, Gdx.graphics.height)

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE -> pendingCommands.addLast(SetupCommand.Back)
            Input.Keys.ENTER -> pendingCommands.addLast(SetupCommand.Continue)
            else -> return false
        }
        return true
    }

    override fun updateGame(delta: Float) {
        while (pendingCommands.isNotEmpty()) {
            when (val command = pendingCommands.removeFirst()) {
                SetupCommand.Back -> onBack()
                SetupCommand.Continue -> continueToDungeon()
                is SetupCommand.Toggle -> toggle(command.index)
                is SetupCommand.ShowStatus -> statusIndex = command.index
                SetupCommand.CloseStatus -> statusIndex = null
                is SetupCommand.MonsterCount -> adjustMonsterCount(command.delta)
            }
        }
    }

    override fun renderGame(batch: SpriteBatch) = view.render(batch, state, message, statusIndex)

    private fun toggle(index: Int) {
        message = if (state.toggle(index)) {
            "${state.selectedIds.size} / ${RaidFormation.PARTY_SIZE} selected"
        } else {
            "Raid is full. Deselect one member first."
        }
    }

    private fun continueToDungeon() {
        val formation = state.createFormation()
        if (formation == null) {
            message = "Select exactly ${RaidFormation.PARTY_SIZE} members"
            return
        }
        onContinue(formation)
    }

    private fun adjustMonsterCount(delta: Int) {
        state.adjustMonsterCount(delta)
        message = "${state.selectedIds.size} / ${RaidFormation.PARTY_SIZE} selected · ${state.monsterCount} monster(s)"
    }
}
