package com.korku.game3d

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.Vector3

/**
 * Prosedürel (kutu tabanlı) geniş ev geometrisi.
 * ------------------------------------------------------------------
 * Texture/model dosyası KULLANMIYORUZ — internet erişimi olmadan da
 * derlenip çalışsın diye tüm yüzeyler düz renk. Gerçekçi görünüm
 * istersen ileride Material'e TextureAttribute eklemen yeterli.
 * ------------------------------------------------------------------
 *
 * Kat planı (X: sağ, Z: ileri, birim metre) — artık tek katlı ama
 * ÇOK odalı, geniş bir ev:
 *
 *   Yatak Odası (başlangıç)
 *        │
 *   Koridor 1 ── (batı) ── Banyo
 *        │
 *     Kavşak ── (batı) Mutfak       (doğu) Çalışma Odası ──
 *        │  (kuzeybatı geçidi) Çocuk Odası
 *   Koridor 2
 *        │
 *      Salon
 *      ├── (sol geçit) Ön Kapı bölümü
 *      └── (sağ geçit) Bodrum Kapısı bölümü
 *
 * Evin her köşesine hikayenin parçalarını anlatan gizli "anı sayfaları"
 * saklanmış durumda (bkz. buildMemoryPages).
 */
class World {

    private val modelBuilder = ModelBuilder()
    val models: MutableList<Model> = mutableListOf()
    val instances: MutableList<ModelInstance> = mutableListOf()
    val wallColliders: MutableList<BoundingBox> = mutableListOf()
    val triggers: MutableList<Trigger> = mutableListOf()
    val memoryPages: MutableList<Collectible> = mutableListOf()

    /** Takipçi'nin "belirebileceği" oda-merkezi noktaları. */
    val monsterWaypoints: List<Vector3> = listOf(
        Vector3(0f, 1.7f, 3f),      // Yatak Odası
        Vector3(0f, 1.7f, 8f),      // Koridor 1
        Vector3(-2.5f, 1.7f, 7.5f), // Banyo
        Vector3(0f, 1.7f, 11.5f),   // Kavşak
        Vector3(-7f, 1.7f, 11.5f),  // Mutfak
        Vector3(7f, 1.7f, 11.5f),   // Çalışma Odası
        Vector3(-3f, 1.7f, 14.5f),  // Çocuk Odası
        Vector3(0f, 1.7f, 14.5f),   // Koridor 2
        Vector3(-4f, 1.7f, 19f),    // Salon - batı
        Vector3(4f, 1.7f, 19f),     // Salon - doğu
        Vector3(0f, 1.7f, 20.5f),   // Salon - orta
        Vector3(-2.6f, 1.7f, 23.5f),// Ön Kapı bölümü
        Vector3(2.6f, 1.7f, 23.5f)  // Bodrum Kapısı bölümü
    )

    /** Mert'in başlangıç konumu — Evin'le aynı yatak odasında, spawn'a yakın. */
    val mertSpawn: Vector3 = Vector3(-1.3f, 0f, 2.6f)

    /** Fener Çocuğu'nun ilk belirebileceği, oyuncudan uzak koridor noktaları. */
    val flashlightFarSpots: List<Vector3> = listOf(
        Vector3(0f, 0f, 9.5f),      // Koridor 1
        Vector3(0f, 0f, 14.5f),     // Koridor 2
        Vector3(0f, 0f, 20.5f)      // Salon - orta
    )

    /** Saklanılabilecek sabit noktalar — oyuncu yarıçapa girip C'ye basınca saklanır. */
    val hidingSpots: List<HidingSpot> = listOf(
        HidingSpot(Vector3(2.3f, 0f, 1.8f), 1f, "Yatağın altı"),
        HidingSpot(Vector3(-3.8f, 0f, 7.6f), 0.9f, "Dolap"),
        HidingSpot(Vector3(-8f, 0f, 9.6f), 0.9f, "Mutfak dolabı altı"),
        HidingSpot(Vector3(4.5f, 0f, 19.5f), 1f, "Perde arkası")
    )

    /** Konuma göre ortam sesi seçebilmek için adlandırılmış oda hacimleri (bkz. buildWalls'taki kat planı). */
    val rooms: List<Room> = listOf(
        Room("Yatak Odası", BoundingBox(Vector3(-3f, -1f, 0f), Vector3(3f, 4f, 6f))),
        Room("Koridor", BoundingBox(Vector3(-0.7f, -1f, 6f), Vector3(0.7f, 4f, 10f))),
        Room("Banyo", BoundingBox(Vector3(-4.5f, -1f, 6f), Vector3(-0.7f, 4f, 9f))),
        Room("Kavşak", BoundingBox(Vector3(-5f, -1f, 10f), Vector3(5f, 4f, 13f))),
        Room("Mutfak", BoundingBox(Vector3(-9f, -1f, 9f), Vector3(-5f, 4f, 14f))),
        Room("Çalışma Odası", BoundingBox(Vector3(5f, -1f, 9f), Vector3(9f, 4f, 14f))),
        Room("Çocuk Odası", BoundingBox(Vector3(-5f, -1f, 13f), Vector3(-1f, 4f, 16f))),
        Room("Koridor", BoundingBox(Vector3(-1f, -1f, 13f), Vector3(1f, 4f, 16f))),
        Room("Salon", BoundingBox(Vector3(-6f, -1f, 16f), Vector3(6f, 4f, 22f))),
        Room("Ön Kapı Bölümü", BoundingBox(Vector3(-4f, -1f, 22f), Vector3(-1.2f, 4f, 25f))),
        Room("Bodrum Kapısı Bölümü", BoundingBox(Vector3(1.2f, -1f, 22f), Vector3(4f, 4f, 25f)))
    )

    /** Başta kilitli iki geçit — biri saate, biri anahtara bağlı (bkz. buildDoors). */
    val doors: MutableList<Door> = mutableListOf()

    /** Duvara sabit aynalar — bkz. madde 19 ve buildMirrors. */
    val mirrors: MutableList<Mirror> = mutableListOf()

    lateinit var keyPickupInstance: ModelInstance
        private set
    var keyBounds: BoundingBox = BoundingBox()
        private set

    init {
        buildFloorAndCeiling()
        buildWalls()
        buildKeyPickup()
        buildMemoryPages()
        buildTriggers()
        buildHidingSpots()
        buildDoors()
        buildMirrors()
    }

    private fun box(cx: Float, cy: Float, cz: Float, sx: Float, sy: Float, sz: Float, color: Color, collider: Boolean) {
        val material = Material(ColorAttribute.createDiffuse(color))
        val attrs = (Usage.Position or Usage.Normal).toLong()
        val model = modelBuilder.createBox(sx, sy, sz, material, attrs)
        val instance = ModelInstance(model)
        instance.transform.setToTranslation(cx, cy, cz)
        models.add(model)
        instances.add(instance)
        if (collider) {
            val min = Vector3(cx - sx / 2f, cy - sy / 2f, cz - sz / 2f)
            val max = Vector3(cx + sx / 2f, cy + sy / 2f, cz + sz / 2f)
            wallColliders.add(BoundingBox(min, max))
        }
    }

    /** Z sabitken X ekseni boyunca (x1..x2) bir duvar parçası. */
    private fun wallX(x1: Float, x2: Float, z: Float, height: Float = 3f, thickness: Float = 0.2f, color: Color = WALL_COLOR) {
        val length = x2 - x1
        if (length <= 0f) return
        box((x1 + x2) / 2f, height / 2f, z, length, height, thickness, color, collider = true)
    }

    /** X sabitken Z ekseni boyunca (z1..z2) bir duvar parçası. */
    private fun wallZ(x: Float, z1: Float, z2: Float, height: Float = 3f, thickness: Float = 0.2f, color: Color = WALL_COLOR) {
        val length = z2 - z1
        if (length <= 0f) return
        box(x, height / 2f, (z1 + z2) / 2f, thickness, height, length, color, collider = true)
    }

    private fun buildFloorAndCeiling() {
        box(0f, -0.1f, 12f, 40f, 0.2f, 40f, Color(0.15f, 0.15f, 0.18f, 1f), collider = false)
        box(0f, 3.1f, 12f, 40f, 0.2f, 40f, Color(0.08f, 0.08f, 0.10f, 1f), collider = false)
    }

    private fun buildWalls() {
        // --- Yatak Odası (başlangıç) X:-3..3 Z:0..6 ---
        wallX(-3f, 3f, 0f)                  // güney (spawn arkası)
        wallX(-3f, -0.7f, 6f)               // kuzey - sol parça (kapı boşluğu -0.7..0.7)
        wallX(0.7f, 3f, 6f)                 // kuzey - sağ parça
        wallZ(-3f, 0f, 6f)                  // batı
        wallZ(3f, 0f, 6f)                   // doğu

        // --- Koridor 1 X:-0.7..0.7 Z:6..10 ---
        // batı duvarında Banyo'ya açılan kapı boşluğu (7.2..8.6)
        wallZ(-0.7f, 6f, 7.2f)
        wallZ(-0.7f, 8.6f, 10f)
        wallZ(0.7f, 6f, 10f)

        // --- Banyo X:-4.5..-0.7 Z:6..9 (Koridor 1'in batısında, gizli anı burada) ---
        wallZ(-4.5f, 6f, 9f)                // batı
        wallX(-4.5f, -0.7f, 6f)             // güney
        wallX(-4.5f, -0.7f, 9f)             // kuzey

        // --- Kavşak X:-5..5 Z:10..13 ---
        wallX(-5f, -0.7f, 10f)
        wallX(0.7f, 5f, 10f)
        wallZ(5f, 10f, 10.8f)               // doğu - üst (Çalışma Odası kapısı üstü, boşluk 10.8..12.2)
        wallZ(5f, 12.2f, 13f)               // doğu - alt
        wallZ(-5f, 10f, 10.8f)              // batı - mutfak kapısı üstü (boşluk 10.8..12.2)
        wallZ(-5f, 12.2f, 13f)              // batı - mutfak kapısı altı
        wallX(-5f, -3.2f, 13f)              // kuzey - en sol (Çocuk Odası kapısı boşluğu -3.2..-1.8)
        wallX(-1.8f, -1f, 13f)              // kuzey - sol iç
        wallX(1f, 5f, 13f)                  // kuzey - sağ

        // --- Mutfak X:-9..-5 Z:9..14 ---
        wallZ(-9f, 9f, 14f)
        wallX(-9f, -5f, 9f)
        wallX(-9f, -5f, 14f)

        // --- Çalışma Odası X:5..9 Z:9..14 (Mutfağın aynası, doğu kanat) ---
        wallZ(9f, 9f, 14f)
        wallX(5f, 9f, 9f)
        wallX(5f, 9f, 14f)

        // --- Çocuk Odası X:-5..-1 Z:13..16 (Kavşak'ın kuzeybatısı) ---
        wallZ(-5f, 13f, 16f)                // batı
        wallZ(-1f, 13f, 16f)                // doğu (Koridor 2'nin batı duvarıyla aynı hizada)

        // --- Koridor 2 X:-1..1 Z:13..16 ---
        wallZ(1f, 13f, 16f)

        // --- Salon X:-6..6 Z:16..22 ---
        wallX(-6f, -1f, 16f)
        wallX(1f, 6f, 16f)
        wallZ(-6f, 16f, 22f)
        wallZ(6f, 16f, 22f)
        wallX(-6f, -3f, 22f)                // kuzey - sol (ön kapı geçidi boşluğu -3..-1.6)
        wallX(-1.6f, 1.6f, 22f)             // kuzey - orta (iki koridoru ayırır)
        wallX(3f, 6f, 22f)                  // kuzey - sağ (bodrum geçidi boşluğu 1.6..3)

        // --- Ön Kapı bölümü X:-4..-1.2 Z:22..25 ---
        wallZ(-4f, 22f, 25f)
        wallZ(-1.2f, 22f, 25f)
        wallX(-4f, -1.2f, 25f, color = Color(0.35f, 0.15f, 0.15f, 1f)) // kapı - kırmızımsı

        // --- Bodrum Kapısı bölümü X:1.2..4 Z:22..25 ---
        wallZ(1.2f, 22f, 25f)
        wallZ(4f, 22f, 25f)
        wallX(1.2f, 4f, 25f, color = Color(0.08f, 0.08f, 0.1f, 1f)) // kapı - koyu
    }

    private fun buildKeyPickup() {
        val material = Material(ColorAttribute.createDiffuse(Color.GOLD))
        val model = modelBuilder.createSphere(
            0.3f, 0.3f, 0.3f, 12, 12, material,
            (Usage.Position or Usage.Normal).toLong()
        )
        models.add(model)
        keyPickupInstance = ModelInstance(model)
        val pos = Vector3(-7f, 1f, 11.5f)
        keyPickupInstance.transform.setToTranslation(pos)
        keyBounds = BoundingBox(
            Vector3(pos.x - 0.4f, pos.y - 0.4f, pos.z - 0.4f),
            Vector3(pos.x + 0.4f, pos.y + 0.4f, pos.z + 0.4f)
        )
    }

    /** Küçük, ince, hafif parlayan bir "sayfa" modeli oluşturur. */
    private fun pageModel(color: Color): ModelInstance {
        val material = Material(ColorAttribute.createDiffuse(color))
        val model = modelBuilder.createBox(
            0.28f, 0.05f, 0.36f, material,
            (Usage.Position or Usage.Normal).toLong()
        )
        models.add(model)
        return ModelInstance(model)
    }

    private fun addPage(
        id: String,
        pos: Vector3,
        lines: List<String>,
        jumpscareCharacter: String? = null,
        color: Color = Color(0.85f, 0.78f, 0.5f, 1f)
    ) {
        val instance = pageModel(color)
        instance.transform.setToTranslation(pos)
        val bounds = BoundingBox(
            Vector3(pos.x - 0.5f, pos.y - 0.5f, pos.z - 0.5f),
            Vector3(pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f)
        )
        memoryPages.add(Collectible(id, bounds, instance, lines, jumpscareCharacter))
    }

    /**
     * Evin dört bir yanına gizlenmiş 6 "anı sayfası". Hepsini toplamak
     * oyunun ana amacı — hikayenin tamamını ancak hepsini bulursan
     * anlarsın (bkz. GameScreen.BASEMENT_CHECK mantığı).
     */
    private fun buildMemoryPages() {
        addPage(
            id = "page_bedroom",
            pos = Vector3(2.2f, 0.3f, 1.2f),
            lines = listOf(
                "Yatağın altında, tozlu bir sayfa buluyorsun.",
                "Kenarına küçük, çocuksu bir el yazısıyla bir isim karalanmış: senin adın.",
                "\"...ilk anı: burada büyüdüm. Bu oda hep benimdi.\""
            )
        )
        addPage(
            id = "page_bathroom",
            pos = Vector3(-3.9f, 1f, 7.3f),
            lines = listOf(
                "Ayna dolabını açıyorsun. Arkasında katlanmış bir sayfa var.",
                "Aynada, senin değil başka birinin yansımasını görüyorsun — bir an için. Gülümsüyor gibi.",
                "\"...ikinci anı: aynada beni gördüğünde, o zaten çoktan gitmişti. Ama bir parçası hep burada kaldı.\""
            ),
            jumpscareCharacter = "Ayna Kadını"
        )
        addPage(
            id = "page_kitchen",
            pos = Vector3(-7.6f, 0.35f, 13.3f),
            lines = listOf(
                "Lavabonun altındaki dolapta, ıslanmış ama okunabilir bir sayfa var.",
                "\"...üçüncü anı: o gece mutfakta bir şey bozuldu. Sadece boru değildi.\""
            )
        )
        addPage(
            id = "page_study",
            pos = Vector3(8.4f, 1f, 13.3f),
            lines = listOf(
                "Kitaplığı iteklediğinde arkasında dar bir boşluk açılıyor.",
                "Boşluktaki karanlıkta bir şey kıpırdıyor, sonra sayfayı bırakıp geri çekiliyor.",
                "\"...dördüncü anı: bu evi hiç terk etmedim. Hatırlanmayı bekledim, çalışma odasında saklanarak.\""
            ),
            jumpscareCharacter = "Gölge Adam"
        )
        addPage(
            id = "page_kidsroom",
            pos = Vector3(-4.3f, 0.35f, 15.3f),
            lines = listOf(
                "Oyuncak sandığını açıyorsun. Bir bebeğin altında sayfa duruyor.",
                "Bebek, sen bakmıyorken bir tık daha sana dönmüş gibi.",
                "\"...beşinci anı: burada bir çocuk vardı. Adını unuttular. Ama oyuncakları hâlâ onu bekliyor.\""
            ),
            jumpscareCharacter = "Oyuncak Bebek"
        )
        addPage(
            id = "page_livingroom",
            pos = Vector3(5.3f, 0.4f, 21.2f),
            lines = listOf(
                "Şömine rafının altında gizli bir bölme buluyorsun. Son sayfa burada.",
                "\"...son anı: bodrumda seni bekleyen, senden önce buraya kilitlenen kişiydi. Şimdi sıra sende değil — hep sendeydi.\"",
                "Sayfanın altında son bir cümle var: \"Beni hatırladığın sürece kaybolmayacağım.\""
            )
        )
    }

    private fun buildTriggers() {
        triggers.add(
            Trigger(
                id = "kitchen_jumpscare",
                bounds = BoundingBox(Vector3(-9f, 0f, 9f), Vector3(-5f, 3f, 14f)),
                kind = TriggerKind.KITCHEN_JUMPSCARE
            )
        )
        triggers.add(
            Trigger(
                id = "front_door_qte",
                bounds = BoundingBox(Vector3(-4f, 0f, 23.5f), Vector3(-1.2f, 3f, 25f)),
                kind = TriggerKind.FRONT_DOOR_QTE
            )
        )
        triggers.add(
            Trigger(
                id = "basement_check",
                bounds = BoundingBox(Vector3(1.2f, 0f, 23.5f), Vector3(4f, 3f, 25f)),
                kind = TriggerKind.BASEMENT_CHECK
            )
        )
    }

    /** Her saklanma noktasına, oyuncunun görüp fark edebileceği küçük bir işaret kutusu koyar. */
    private fun buildHidingSpots() {
        hidingSpots.forEach { spot ->
            box(spot.position.x, 0.4f, spot.position.z, 0.6f, 0.8f, 0.6f, Color(0.12f, 0.1f, 0.09f, 1f), collider = false)
        }
    }

    private fun doorModel(color: Color, sx: Float, sy: Float, sz: Float): ModelInstance {
        val material = Material(ColorAttribute.createDiffuse(color))
        val model = modelBuilder.createBox(sx, sy, sz, material, (Usage.Position or Usage.Normal).toLong())
        models.add(model)
        return ModelInstance(model)
    }

    /**
     * Başta kilitli iki geçit — koşulu sağlanana kadar `wallColliders` gibi
     * davranıp geçişi fiziksel olarak engeller (bkz. GameScreen'in oluşturduğu
     * birleşik collider listesi).
     */
    private fun buildDoors() {
        // Banyo kapısı — Koridor'un batı duvarındaki boşluk. Saat 03:33'ü geçince kendiliğinden açılır.
        run {
            val x = -0.7f; val z1 = 7.2f; val z2 = 8.6f; val height = 3f
            val instance = doorModel(Color(0.3f, 0.22f, 0.15f, 1f), 0.3f, height, z2 - z1)
            instance.transform.setToTranslation(x, height / 2f, (z1 + z2) / 2f)
            val bounds = BoundingBox(Vector3(x - 0.15f, 0f, z1), Vector3(x + 0.15f, height, z2))
            doors.add(Door("door_bathroom", bounds, instance, DoorKind.TIME_GATED, "Banyo kapısı", unlockMinute = 3f * 60f + 33f))
        }
        // Çocuk Odası kapısı — Kavşak'ın kuzey duvarındaki boşluk. Bodrum anahtarı bu kapıyı da açar.
        run {
            val x1 = -3.2f; val x2 = -1.8f; val z = 13f; val height = 3f
            val instance = doorModel(Color(0.22f, 0.18f, 0.28f, 1f), x2 - x1, height, 0.3f)
            instance.transform.setToTranslation((x1 + x2) / 2f, height / 2f, z)
            val bounds = BoundingBox(Vector3(x1, 0f, z - 0.15f), Vector3(x2, height, z + 0.15f))
            doors.add(Door("door_kidsroom", bounds, instance, DoorKind.KEY_GATED, "Çocuk Odası kapısı"))
        }
    }

    /** İnce, hafif parlak bir ayna levhası modeli oluşturur. */
    private fun mirrorModel(): ModelInstance {
        val material = Material(ColorAttribute.createDiffuse(Color(0.72f, 0.82f, 0.9f, 1f)))
        val model = modelBuilder.createBox(
            0.9f, 1.5f, 0.06f, material,
            (Usage.Position or Usage.Normal).toLong()
        )
        models.add(model)
        return ModelInstance(model)
    }

    /**
     * Üç duvar aynası (bkz. madde 19):
     * - Banyo: genel atmosferik tuhaflıklar, korku bandına göre değişir.
     * - Çocuk Odası: Mert uzaktayken yansımada Mert'i gösterebilir.
     * - Salon (şömine yanı): tüm anılar toplanınca seçim geçmişini yansıtan "final aynası".
     */
    private fun buildMirrors() {
        run {
            // Banyo, güney duvarına asılı — oda X:-4.5..-0.7 Z:6..9.
            val instance = mirrorModel()
            instance.transform.setToTranslation(-2.6f, 1.5f, 6.15f)
            val bounds = BoundingBox(Vector3(-3.4f, 0f, 6.2f), Vector3(-1.8f, 3f, 8f))
            mirrors.add(Mirror("mirror_bathroom", bounds, instance, MirrorKind.BATHROOM, "Ayna"))
        }
        run {
            // Çocuk Odası, batı duvarına asılı — oda X:-5..-1 Z:13..16.
            val instance = mirrorModel()
            instance.transform.setToTranslation(-4.87f, 1.5f, 14.5f)
            val bounds = BoundingBox(Vector3(-4.6f, 0f, 13.5f), Vector3(-3.2f, 3f, 15.5f))
            mirrors.add(Mirror("mirror_kidsroom", bounds, instance, MirrorKind.GHOST_REFLECTION, "Ayna"))
        }
        run {
            // Salon, şöminenin yakınındaki doğu duvarına asılı — oda X:-6..6 Z:16..22.
            val instance = mirrorModel()
            instance.transform.setToTranslation(5.87f, 1.5f, 20f)
            val bounds = BoundingBox(Vector3(4.2f, 0f, 19f), Vector3(5.7f, 3f, 21f))
            mirrors.add(Mirror("mirror_livingroom", bounds, instance, MirrorKind.FINALE, "Büyük ayna"))
        }
    }

    fun dispose() {
        models.forEach { it.dispose() }
    }

    companion object {
        private val WALL_COLOR = Color(0.25f, 0.22f, 0.24f, 1f)
    }
}
