package com.korku.game3d

/**
 * Evin'in görünmez "cesaret" değeri (bkz. tasarım notu madde 26).
 *
 * FearSystem'e kasıtlı olarak simetrik: oyuncuya hiçbir zaman bir sayı ya da
 * "cesur davranıyorsun" mesajı gösterilmez. Belirli davranışlar (Takipçi
 * görünürken saklanmak yerine ona göğüs germek, feneri kapatıp karanlıkta
 * ilerlemek) sessizce puan biriktirir. Eşiği geçince bazı diyaloglar ve
 * final metinleri buna göre değişir — GameScreen bu değeri okuyup Mert'in
 * repliklerine ve epiloglara yansıtır.
 */
class CourageSystem {
    var value: Float = 0f
        private set

    private val threshold = 40f

    fun add(amount: Float) {
        value = (value + amount).coerceIn(0f, 100f)
    }

    fun isCourageous(): Boolean = value >= threshold

    fun reset() {
        value = 0f
    }
}
