package com.raidmanager.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.raidmanager.game.RaidManagerGame

fun main() {
    val configuration = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Raid Manager")
        setWindowedMode(1280, 720)
        useVsync(true)
        setForegroundFPS(60)
    }
    Lwjgl3Application(RaidManagerGame(), configuration)
}
