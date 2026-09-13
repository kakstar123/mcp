package com.korku.game3d

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3

/** Takipçi'nin belirdiğinde takındığı tavır. */
enum class MonsterMood {
    HUNTING,  // yaklaşırsan yakalar
    WATCHING  // sadece durup bakar, saldırmaz — daha rahatsız edici
}

/**
 * "Takipçi" — evin içinde dolaşan, tam pathfinding yerine önceden
 * tanımlanmış oda-merkezi noktalarında (waypoints) "belirip" kaybolan
 * gizemli figür. Bu, "jumpscare ihtimali" mekaniğini oluşturur: her an
 * herhangi bir odada karşına çıkabilir; eğer o anda ona çok yaklaşırsan
 * ve o an avlanma modundaysa jump-scare tetiklenir.
 *
 * Her belirişinde ya avlanma ya da izleme moduna geçer (bkz. `watchChance`).
 * İzleme modunda saldırmaz, sadece durup bakar. Görünür olduğu sürece
 * oyuncunun konumunu "son görülen konum" olarak hatırlar (oyuncu saklanınca
 * bu takip kesilir) ve bir sonraki belirişinde o bölgeye yakın bir waypoint
 * seçer — yani rastgele değil, oyuncuyu "arıyormuş" gibi davranır.
 *
 * Belirli sayıda anı sayfası toplanana kadar (bkz. `activationThreshold`)
 * tamamen pasiftir — oyuncuya keşif için biraz güvenli alan tanır.
 */
class Monster(private val waypoints: List<Vector3>) {

    private val modelBuilder = ModelBuilder()
    val models: MutableList<Model> = mutableListOf()
    lateinit var bodyInstance: ModelInstance
        private set
    lateinit var headInstance: ModelInstance
        private set

    /** Görünür olmadığında ekranın çok altında, çarpışmayan bir yerde durur. */
    var position: Vector3 = Vector3(0f, -50f, 0f)
        private set
    var visible = false
        private set
    var mood: MonsterMood = MonsterMood.HUNTING
        private set

    /** Oyuncunun en son görüldüğü konum. Y = -50 iken "henüz kimseyi görmedi" anlamına gelir. */
    var lastKnownPlayerPos: Vector3 = Vector3(0f, -50f, 0f)
        private set

    /** Takipçi aktive oldu mu — henüz aktive olmadıysa hiçbir tehlike yok. */
    val isActivated: Boolean get() = activated

    /** Görünmezken bir sonraki belirişine kalan süre; görünürken 0. Yaklaşan tehlike uyarısı için kullanılır. */
    val timeUntilAppear: Float get() = if (!visible) appearTimer else 0f

    private var activated = false
    private var appearTimer = 0f
    private var visibleTimer = 0f

    private val catchRadius = 1.6f
    private val minSpawnDist = 3.5f
    private val maxSpawnDist = 9f
    private var watchChance = 0.35

    init {
        val bodyMat = Material(ColorAttribute.createDiffuse(Color(0.05f, 0.02f, 0.03f, 1f)))
        val bodyModel = modelBuilder.createBox(
            0.6f, 1.6f, 0.4f, bodyMat, (Usage.Position or Usage.Normal).toLong()
        )
        models.add(bodyModel)
        bodyInstance = ModelInstance(bodyModel)

        val headMat = Material(ColorAttribute.createDiffuse(Color(0.5f, 0.05f, 0.05f, 1f)))
        val headModel = modelBuilder.createSphere(
            0.28f, 0.28f, 0.28f, 10, 10, headMat, (Usage.Position or Usage.Normal).toLong()
        )
        models.add(headModel)
        headInstance = ModelInstance(headModel)

        updateTransforms()
    }

    /**
     * @param hidden oyuncu şu an bir saklanma noktasında mı — öyleyse yakalanamaz
     *   ve Takipçi onun konumunu "son görülen" olarak güncellemeyi bırakır
     * @param pagesCollected şu ana kadar toplanan anı sayfası sayısı
     * @param activationThreshold bu sayıya ulaşılınca canavar aktive olur
     * @param onCatch oyuncu yakalanınca çağrılır (jumpscare + hafıza kaybı GameScreen'de yapılır)
     */
    fun update(
        delta: Float,
        playerPos: Vector3,
        hidden: Boolean,
        pagesCollected: Int,
        activationThreshold: Int,
        onCatch: () -> Unit
    ) {
        if (!activated) {
            if (pagesCollected >= activationThreshold) {
                activated = true
                appearTimer = 7f + Math.random().toFloat() * 5f
            }
            return
        }

        if (visible) {
            if (!hidden) lastKnownPlayerPos.set(playerPos)
            visibleTimer -= delta
            if (mood == MonsterMood.HUNTING && !hidden && position.dst(playerPos) < catchRadius) {
                visible = false
                appearTimer = 14f + Math.random().toFloat() * 8f
                onCatch()
                return
            }
            if (visibleTimer <= 0f) {
                visible = false
                appearTimer = 9f + Math.random().toFloat() * 7f
            }
        } else {
            appearTimer -= delta
            if (appearTimer <= 0f) {
                val searchOrigin = if (lastKnownPlayerPos.y > -10f) lastKnownPlayerPos else playerPos
                val candidates = waypoints.filter {
                    val d = it.dst(searchOrigin)
                    d in minSpawnDist..maxSpawnDist
                }
                val spot = (if (candidates.isNotEmpty()) candidates else waypoints).random()
                position = Vector3(spot)
                visible = true
                mood = if (Math.random() < watchChance) MonsterMood.WATCHING else MonsterMood.HUNTING
                visibleTimer = if (mood == MonsterMood.WATCHING)
                    3f + Math.random().toFloat() * 3f
                else
                    2.5f + Math.random().toFloat() * 2f
            }
        }
        updateTransforms()
    }

    /** Yeniden başlatınca canavarı tamamen pasif duruma döndürür. */
    fun reset() {
        activated = false
        visible = false
        mood = MonsterMood.HUNTING
        watchChance = 0.35
        appearTimer = 0f
        visibleTimer = 0f
        position = Vector3(0f, -50f, 0f)
        lastKnownPlayerPos = Vector3(0f, -50f, 0f)
        updateTransforms()
    }

    /** Saat 03:33'ü geçince (bkz. GameScreen zaman sistemi) Takipçi daha az izler, daha çok avlanır. */
    fun escalate() {
        watchChance = (watchChance - 0.15).coerceAtLeast(0.1)
    }

    private fun updateTransforms() {
        bodyInstance.transform.setToTranslation(position.x, 0.8f, position.z)
        headInstance.transform.setToTranslation(position.x, 1.75f, position.z)
    }

    fun dispose() {
        models.forEach { it.dispose() }
    }
}
