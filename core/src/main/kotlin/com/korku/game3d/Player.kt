package com.korku.game3d

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.BoundingBox
import com.badlogic.gdx.math.Vector3

/**
 * Birinci şahıs kamera + hareket + basit çarpışma kontrolcüsü.
 * Oyuncu bir "nokta + yarıçap" (silindir yaklaşımı, sadece X/Z düzleminde)
 * olarak modellenir; Y ekseninde (zıplama/eğilme) yok — sade bir temel.
 */
class Player(private val camera: PerspectiveCamera) {

    companion object {
        const val EYE_HEIGHT = 1.7f
    }

    var position: Vector3 = Vector3(0f, EYE_HEIGHT, 2f)
        private set

    private var yaw = 0f    // sağ-sol bakış (derece)
    private var pitch = 0f  // yukarı-aşağı bakış (derece)

    private val moveSpeed = 4.2f
    private val mouseSensitivity = 0.15f
    val radius = 0.35f

    fun update(delta: Float, colliders: List<BoundingBox>, inputEnabled: Boolean) {
        if (inputEnabled) {
            yaw -= Gdx.input.deltaX * mouseSensitivity
            pitch = (pitch - Gdx.input.deltaY * mouseSensitivity).coerceIn(-80f, 80f)

            val yawRad = Math.toRadians(yaw.toDouble())
            val forward = Vector3(Math.sin(yawRad).toFloat(), 0f, -Math.cos(yawRad).toFloat())
            val right = Vector3(forward.z, 0f, -forward.x)

            val move = Vector3()
            if (Gdx.input.isKeyPressed(Input.Keys.W)) move.add(forward)
            if (Gdx.input.isKeyPressed(Input.Keys.S)) move.sub(forward)
            if (Gdx.input.isKeyPressed(Input.Keys.D)) move.add(right)
            if (Gdx.input.isKeyPressed(Input.Keys.A)) move.sub(right)

            if (move.len2() > 0f) {
                move.nor().scl(moveSpeed * delta)
                tryMove(move, colliders)
            }
        }

        camera.position.set(position)
        val pitchRad = Math.toRadians(pitch.toDouble())
        val yawRad2 = Math.toRadians(yaw.toDouble())
        val cosPitch = Math.cos(pitchRad).toFloat()
        camera.direction.set(
            (Math.sin(yawRad2).toFloat() * cosPitch),
            Math.sin(pitchRad).toFloat(),
            (-Math.cos(yawRad2).toFloat() * cosPitch)
        ).nor()
        camera.up.set(Vector3.Y)
        camera.update()
    }

    /** X ve Z eksenleri ayrı ayrı denenir; bu, duvara paralel kaymayı (sliding) doğal olarak sağlar. */
    private fun tryMove(delta: Vector3, colliders: List<BoundingBox>) {
        val nextX = Vector3(position.x + delta.x, position.y, position.z)
        if (!collides(nextX, colliders)) position.x = nextX.x

        val nextZ = Vector3(position.x, position.y, position.z + delta.z)
        if (!collides(nextZ, colliders)) position.z = nextZ.z
    }

    private fun collides(pos: Vector3, colliders: List<BoundingBox>): Boolean {
        for (box in colliders) {
            val closestX = pos.x.coerceIn(box.min.x, box.max.x)
            val closestZ = pos.z.coerceIn(box.min.z, box.max.z)
            val dx = pos.x - closestX
            val dz = pos.z - closestZ
            if (dx * dx + dz * dz < radius * radius) return true
        }
        return false
    }

    /** Yatay (Y=0) bakış yönü — fener çocuğu gibi oyuncuya göreli konumlandırma için. */
    fun forwardXZ(): Vector3 {
        val yawRad = Math.toRadians(yaw.toDouble())
        return Vector3(Math.sin(yawRad).toFloat(), 0f, -Math.cos(yawRad).toFloat())
    }

    /** Halüsinasyon anlarında oyuncunun kontrolü dışında kamerayı yavaşça çevirir. */
    fun driftYaw(degreesPerSecond: Float, delta: Float) {
        yaw += degreesPerSecond * delta
    }

    fun shake(amount: Float) {
        camera.position.add(
            (Math.random().toFloat() - 0.5f) * amount,
            (Math.random().toFloat() - 0.5f) * amount,
            (Math.random().toFloat() - 0.5f) * amount
        )
        camera.update()
    }

    fun resetTo(x: Float, z: Float) {
        position = Vector3(x, EYE_HEIGHT, z)
        yaw = 0f
        pitch = 0f
    }
}
