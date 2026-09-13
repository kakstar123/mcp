package com.korku.game3d

import com.badlogic.gdx.Game

class KorkuGame3D : Game() {
    override fun create() {
        setScreen(GameScreen())
    }
}
