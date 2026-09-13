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
 * Mert — Evin'in oda arkadaşı ve oyundaki tek gerçek, sahnede duran NPC.
 * Oyuncuyu birkaç adım geriden takip eder. Oyuncu ona yaklaşıp E'ye
 * bastığında bir diyalog açılır (bkz. GameScreen); verilen cevaplar
 * `trust` değerini değiştirir. Trust; Mert'in ön kapıda yardım edip
 * etmeyeceğini ve bazı repliklerinin tonunu belirler — basit bir
 * "NPC hafızası/güveni" hissi yaratır.
 */
class Mert(startPos: Vector3) {

    private val modelBuilder = ModelBuilder()
    val models: MutableList<Model> = mutableListOf()
    lateinit var bodyInstance: ModelInstance
        private set
    lateinit var headInstance: ModelInstance
        private set

    var position: Vector3 = startPos.cpy()
        private set

    var trust: Float = 50f
        private set

    val interactionRadius = 1.8f

    // Diyalog bayrakları — GameScreen bunları okuyup hikayeyi dallandırır.
    var askedBasementQuestion = false
    var liedAboutBasement = false

    /**
     * 03:33 eşiği geçildiğinde GameScreen bunu true yapar. Bu andan sonra Mert'in
     * tavrı kozmetik bir "daha korkunç ses tonu" değil, gerçekten farklı bir
     * davranış moduna geçer: cümleleri daha kısa, kendinden şüpheli ve zaman
     * zaman kendi anılarını sorgular hale gelir (bkz. idleLine).
     */
    var pastThreshold = false

    /** idleLine() her çağrıldığında ilerler, aynı ton içinde tekrar tekrar aynı repliği duymayı engeller. */
    private var idleLineIndex = 0

    private val idleLinesBeforeThreshold = listOf(
        "MERT: \"İyi olacağız, değil mi? Sadece elektrikler gitti.\"",
        "MERT: \"Sesi duydun mu sen de yoksa ben mi hayal ediyorum?\"",
        "MERT: \"Yanından ayrılmayacağım, söz.\""
    )

    /** trusts() true iken 03:33 sonrası — Mert hâlâ yanında ama artık emin değil. */
    private val idleLinesAfterThresholdTrusted = listOf(
        "MERT: \"Buraya ne zaman geldiğimi hatırlamıyorum Evin. Ama yanındayım.\"",
        "MERT: \"Saat kaç şimdi? Boş ver, önemli değil. Sen iyi misin?\"",
        "MERT: \"Bir şey söylemek istedim ama... unuttum. Tuhaf.\""
    )

    /** trusts() false iken (güvensiz/yalan) 03:33 sonrası — Mert'in sesi artık tanıdık gelmiyor. */
    private val idleLinesAfterThresholdDistrust = listOf(
        "MERT: \"Sen misin, Evin? Sesini... tanıyamadım bir an.\"",
        "MERT: \"Bir şey sormuştum sana. Cevap vermemiştin. Ya da vermiş miydin?\"",
        "MERT: \"Neden bana öyle bakıyorsun?\""
    )

    /** Oyuncu gizli cesaret eşiğini geçmişse (bkz. CourageSystem) — Mert bunu fark eder. */
    private val idleLinesBeforeThresholdCourageous = listOf(
        "MERT: \"Sen hiç korkmuyor musun? Ben bacaklarımı hissetmiyorum bile.\"",
        "MERT: \"Beni bu kadar sakin tutan senin duruşun, biliyor musun?\""
    )

    private val idleLinesAfterThresholdCourageous = listOf(
        "MERT: \"Saat kaç bilmiyorum ama sen hâlâ dimdik duruyorsun. Bu bir şey ifade ediyor olmalı.\"",
        "MERT: \"Korkuyorum Evin. Ama sen korkmuyor gibisin. Bu tuhaf bir şekilde beni rahatlatıyor.\""
    )

    /**
     * Yakınında, saklanma dışında konuşulduğunda dönülecek kısa replik.
     * Ton iki eksende gerçekten değişir: zaman (03:33 öncesi/sonrası) ve
     * [courageous] — oyuncunun gizli cesaret eşiğini geçip geçmediği.
     */
    fun idleLine(courageous: Boolean = false): String {
        val pool = when {
            pastThreshold && courageous -> idleLinesAfterThresholdCourageous
            !pastThreshold && courageous -> idleLinesBeforeThresholdCourageous
            !pastThreshold -> idleLinesBeforeThreshold
            trusts() -> idleLinesAfterThresholdTrusted
            else -> idleLinesAfterThresholdDistrust
        }
        val line = pool[idleLineIndex % pool.size]
        idleLineIndex++
        return line
    }

    /** Oyuncunun geçmiş konumlarını tutar, Mert bir miktar gecikmeyle bu noktaları takip eder. */
    private val trail: MutableList<Vector3> = mutableListOf()
    private val trailDelay = 40

    init {
        val bodyMat = Material(ColorAttribute.createDiffuse(Color(0.15f, 0.25f, 0.45f, 1f)))
        val bodyModel = modelBuilder.createBox(0.5f, 1.5f, 0.35f, bodyMat, (Usage.Position or Usage.Normal).toLong())
        models.add(bodyModel)
        bodyInstance = ModelInstance(bodyModel)

        val headMat = Material(ColorAttribute.createDiffuse(Color(0.75f, 0.6f, 0.5f, 1f)))
        val headModel = modelBuilder.createSphere(0.26f, 0.26f, 0.26f, 10, 10, headMat, (Usage.Position or Usage.Normal).toLong())
        models.add(headModel)
        headInstance = ModelInstance(headModel)

        updateTransforms()
    }

    fun update(delta: Float, playerPos: Vector3) {
        trail.add(playerPos.cpy())
        if (trail.size > trailDelay) {
            val target = trail.removeAt(0)
            val lerpAmount = (delta * 3f).coerceIn(0f, 1f)
            position = position.cpy().lerp(target, lerpAmount)
            updateTransforms()
        }
    }

    fun adjustTrust(amount: Float) {
        trust = (trust + amount).coerceIn(0f, 100f)
    }

    fun trusts(): Boolean = trust >= 60f

    fun reset(startPos: Vector3) {
        position = startPos.cpy()
        trust = 50f
        trail.clear()
        askedBasementQuestion = false
        liedAboutBasement = false
        pastThreshold = false
        idleLineIndex = 0
        updateTransforms()
    }

    private fun updateTransforms() {
        bodyInstance.transform.setToTranslation(position.x, 0.75f, position.z)
        headInstance.transform.setToTranslation(position.x, 1.63f, position.z)
    }

    fun dispose() {
        models.forEach { it.dispose() }
    }
}
