package com.korku.game3d

/** Korku bandı — dünyanın ne kadar "bozulacağını" belirler. Oyuncuya asla gösterilmez. */
enum class FearBand { NORMAL, UNEASY, DISTORTED, HALLUCINATING }

/**
 * Evin'in görünmez korku değeri (0-100). Oyuncuya hiçbir zaman sayı ya da
 * "halüsinasyon görüyorsun" gibi bir mesaj gösterilmez — sadece etkileri
 * (ışık, fısıltılar, kısa kontrol kaybı) hissettirilir; oyuncu kendisi
 * fark etmeli.
 */
class FearSystem {
    var value: Float = 0f
        private set

    private val passiveDecayPerSecond = 0.6f
    private val proximityGainPerSecond = 6f
    private val proximityRange = 10f

    fun add(amount: Float) {
        value = (value + amount).coerceIn(0f, 100f)
    }

    fun update(delta: Float, monsterVisible: Boolean, monsterDistance: Float) {
        if (monsterVisible) {
            val proximityFactor = (proximityRange - monsterDistance.coerceIn(0f, proximityRange)) / proximityRange
            add(proximityGainPerSecond * proximityFactor * delta)
        } else {
            value = (value - passiveDecayPerSecond * delta).coerceIn(0f, 100f)
        }
    }

    fun band(): FearBand = when {
        value < 25f -> FearBand.NORMAL
        value < 50f -> FearBand.UNEASY
        value < 75f -> FearBand.DISTORTED
        else -> FearBand.HALLUCINATING
    }

    fun reset() {
        value = 0f
    }
}
