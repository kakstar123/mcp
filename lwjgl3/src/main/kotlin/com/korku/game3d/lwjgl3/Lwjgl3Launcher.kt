package com.korku.game3d.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.korku.game3d.KorkuGame3D

fun main() {
    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("Korku Oyunu 3D")
    config.setWindowedMode(1280, 720)
    config.useVsync(true)
    config.setForegroundFPS(60)
    Lwjgl3Application(KorkuGame3D(), config)
}
