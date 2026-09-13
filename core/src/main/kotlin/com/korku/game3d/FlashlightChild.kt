package com.korku.game3d

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3

/**
 * "Fener Çocuğu" — SADECE fener açıkken görünür. Kapalıyken hiçbir şey
 * yoktur. Oyuncu feneri kapatıp tekrar açtığında (bir "blink") figür bir
 * adım daha yaklaşır; son adımda oyuncunun tam arkasında belirir ve bir
 * jump-scare tetikler, ardından uzun süreliğine tamamen pasifleşir.
 *
 * `farSpots`: figürün ilk belirdiği, oyuncudan uzak koridor noktaları.
 */
class FlashlightChild(private val farSpots: List<Vector3>) {

    private val modelBuilder = ModelBuilder()
    val models: MutableList<Model> = mutableListOf()
    lateinit var bodyInstance: ModelInstance
        private set
    lateinit var headInstance: ModelInstance
        private set

    var position: Vector3 = Vector3(0f, -50f, 0f)
        private set

    /** Bu "blink döngüsü" boyunca figür belirmiş mi (render sadece fener açıkken + bu true iken yapılır). */
    var appeared = false
        private set

    private var activated = false
    private var approachStep = 0
    private val maxSteps = 3
    private var cooldown = 0f

    init {
        val bodyMat = Material(ColorAttribute.createDiffuse(Color(0.75f, 0.75f, 0.7f, 1f)))
        val bodyModel = modelBuilder.createBox(0.35f, 1.1f, 0.25f, bodyMat, (Usage.Position or Usage.Normal).toLong())
        models.add(bodyModel)
        bodyInstance = ModelInstance(bodyModel)

        val headMat = Material(ColorAttribute.createDiffuse(Color(0.85f, 0.82f, 0.78f, 1f)))
        val headModel = modelBuilder.createSphere(0.2f, 0.2f, 0.2f, 10, 10, headMat, (Usage.Position or Usage.Normal).toLong())
        models.add(headModel)
        headInstance = ModelInstance(headModel)

        updateTransforms()
    }

    fun activate() {
        activated = true
    }

    fun update(delta: Float) {
        if (cooldown > 0f) cooldown -= delta
    }

    /**
     * Fener KAPALIYKEN AÇILDIĞINDA çağrılır. `onFinalScare` son adımda
     * (figür tam arkada belirdiğinde) tetiklenir.
     */
    fun onFlashlightTurnedOn(playerPos: Vector3, playerForward: Vector3, onFinalScare: () -> Unit) {
        if (!activated || cooldown > 0f) return

        when {
            approachStep == 0 -> {
                position = farSpots.random()
                approachStep = 1
                appeared = true
            }
            approachStep < maxSteps -> {
                approachStep++
                // Ara adımlarda hâlâ önde ama gittikçe daha yakın duruyor.
                val aheadDistance = (maxSteps - approachStep + 1) * 1.3f
                position = playerPos.cpy().add(playerForward.cpy().scl(aheadDistance)).also { it.y = 0f }
                appeared = true
            }
            else -> {
                // Son adım: artık önünde değil, tam arkasında.
                position = playerPos.cpy().sub(playerForward.cpy().scl(0.9f)).also { it.y = 0f }
                appeared = true
                updateTransforms()
                onFinalScare()
                reset(cooldownSeconds = 50f)
                return
            }
        }
        updateTransforms()
    }

    fun reset(cooldownSeconds: Float = 0f) {
        approachStep = 0
        appeared = false
        position = Vector3(0f, -50f, 0f)
        cooldown = cooldownSeconds
        updateTransforms()
    }

    private fun updateTransforms() {
        bodyInstance.transform.setToTranslation(position.x, 0.55f, position.z)
        headInstance.transform.setToTranslation(position.x, 1.25f, position.z)
    }

    fun dispose() {
        models.forEach { it.dispose() }
    }
}
