package com.korku.game3d

import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.Vector3

/** Bir tetikleyici bölgesinin türü — oyuncu bu bölgeye girince ne olacağını belirler. */
enum class TriggerKind {
    KITCHEN_JUMPSCARE,
    FRONT_DOOR_QTE,
    BASEMENT_CHECK
}

/**
 * 3D dünyada belirli bir hacme (BoundingBox) bağlı hikaye tetikleyicisi.
 * Oyuncu bu hacmin içine girince `kind`e göre bir olay tetiklenir.
 */
data class Trigger(
    val id: String,
    val bounds: BoundingBox,
    val kind: TriggerKind,
    var fired: Boolean = false
)

/**
 * Evin içinde bir yere gizlenmiş "anı sayfası" — hikayenin bir parçasını
 * anlatan toplanabilir bir eşya. Bazılarının yanında bir korku karakteri
 * pusuya yatmıştır (`jumpscareCharacter` dolu ise).
 */
data class Collectible(
    val id: String,
    val bounds: BoundingBox,
    val modelInstance: ModelInstance,
    val lines: List<String>,
    val jumpscareCharacter: String? = null,
    var collected: Boolean = false
)

/** Oyuncunun saklanabileceği sabit bir nokta — yarıçapı içindeyken C ile saklanılır. */
data class HidingSpot(val position: Vector3, val radius: Float, val label: String)

/** Bir kapının nasıl açıldığını belirler. */
enum class DoorKind { TIME_GATED, KEY_GATED }

/**
 * Bazı geçitler başta fiziksel olarak kilitlidir (duvar gibi engeller).
 * Koşulu sağlanınca (`unlockMinute`'a ulaşmak ya da anahtarı almak)
 * kalıcı olarak açılır.
 */
data class Door(
    val id: String,
    val bounds: BoundingBox,
    val modelInstance: ModelInstance,
    val kind: DoorKind,
    val label: String,
    val unlockMinute: Float = 0f,
    var locked: Boolean = true
)

/** Adlandırılmış bir oda hacmi — oyuncunun o an hangi odada olduğunu bulmak için (konuma göre ortam sesi). */
data class Room(val name: String, val bounds: BoundingBox)

/**
 * Bir aynanın davranış türü (bkz. tasarım notu madde 19):
 * - BATHROOM: genel, korku bandına göre değişen atmosferik yansıma tuhaflıkları.
 * - GHOST_REFLECTION: Mert uzaktayken yansımada Mert'i gösterir.
 * - FINALE: tüm anılar toplanınca yansımada oyuncunun seçim geçmişini gösterir.
 */
enum class MirrorKind { BATHROOM, GHOST_REFLECTION, FINALE }

/**
 * Duvara sabit, oyuncunun E ile bakabileceği bir ayna. `bounds` oyuncunun
 * "aynanın önünde" sayıldığı bölgedir; gerçek etkileşim GameScreen'de
 * `kind`e göre seçilen bir olay dizisi tetikler.
 */
data class Mirror(
    val id: String,
    val bounds: BoundingBox,
    val modelInstance: ModelInstance,
    val kind: MirrorKind,
    val label: String
)

/** Oyunun o anki genel durumu — girdi ve render mantığı buna göre dallanır. */
enum class Phase {
    EXPLORING,  // serbest gezinme
    TEXT,       // anlatım metni gösteriliyor (hareket kilitli)
    CHOICE,     // Mert diyaloğunda iki seçenekten birini bekliyor (1 / 2 tuşu)
    QTE,        // hızlı-tepki anı
    ENDED       // bir sona ulaşıldı
}
