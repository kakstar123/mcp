package com.korku.game3d

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.ScreenUtils

/**
 * Oyunun tek ekranı: 3D sahneyi render eder, oyuncuyu günceller,
 * hikaye tetikleyicilerini (mutfak/anahtar, ön kapı QTE, bodrum),
 * evin her yerine gizlenmiş anı sayfalarını ve dolaşan "Takipçi"
 * canavarını kontrol eder, metin/QTE/son arayüzünü çizer.
 *
 * NOT: Bu kod bu ortamda derlenip test edilemedi (internet erişimi kapalı,
 * LibGDX bağımlılıkları indirilemiyor). API çağrıları elle dikkatlice
 * kontrol edildi; kendi makinende ilk çalıştırmada hata alırsan mesajı
 * bana ilet.
 */
class GameScreen : Screen {

    private lateinit var camera: PerspectiveCamera
    private lateinit var modelBatch: ModelBatch
    private lateinit var environment: Environment
    private lateinit var world: World
    private lateinit var player: Player
    private lateinit var monster: Monster
    private lateinit var mert: Mert
    private lateinit var flashlightChild: FlashlightChild

    /** Müzik/efekt/diyalog seslendirme sistemi (bkz. AudioManager.kt ve assets/audio/README.md). */
    private val audio = AudioManager()

    private val spriteBatch = SpriteBatch()
    private val font = BitmapFont().apply { data.setScale(1.3f) }
    private val shapeRenderer = ShapeRenderer()

    private var phase = Phase.EXPLORING
    private var hasKey = false

    // --- Anı sayfaları (amaç: evdeki tüm gizli sayfaları bulmak) ---
    private var pagesCollected = 0
    private val totalPages = 6
    private val monsterActivationThreshold = 2 // bu kadar sayfa toplanınca Takipçi aktive olur
    private val flashlightChildActivationThreshold = 3

    // --- Korku/algı sistemi (bkz. Fear.kt) — oyuncuya asla doğrudan gösterilmez ---
    private val fear = FearSystem()
    // --- Gizli cesaret sistemi (bkz. Courage.kt, madde 26) — oyuncuya asla doğrudan gösterilmez ---
    private val courage = CourageSystem()
    private var whisperTimer = 2f
    private var whisperText = ""
    private var whisperAlpha = 0f
    private val whisperPool = listOf(
        "Fısıltı: \"...hatırlıyor musun...\"",
        "Uzakta bir kapı gıcırdıyor.",
        "Birinin nefes aldığını duyar gibisin.",
        "\"Evin...\" diye bir ses fısıldıyor.",
        "Arkanı dönüp bakma isteği duyuyorsun."
    )
    /** Bulunulan odaya göre daha spesifik ortam sesi — item 9'daki "konuma göre ses" fikri. */
    private val roomWhispers = mapOf(
        "Koridor" to listOf("Tık... tık... adımların mı, yoksa başka birinin mi?"),
        "Banyo" to listOf("Musluktan durmadan su damlıyor."),
        "Mutfak" to listOf("Metal bir şey birbirine sürtünüyor."),
        "Çocuk Odası" to listOf("Çok hafif, çocuksu bir fısıltı duyuyorsun."),
        "Salon" to listOf("Uzaktan boğuk bir ses geliyor.")
    )
    private var monsterApproachWarned = false

    // --- Zaman sistemi: anlatımdaki "Saat 03:14" ile aynı noktadan başlar, gerçek zamanla akar ---
    private val startGameMinutes = 3f * 60f + 14f
    private var gameMinutes = startGameMinutes
    private val clockMinutesPerRealSecond = 0.12f
    private var hit0333 = false
    private var hallucinationTimer = 0f
    private var hallucinationCheckTimer = 4f

    // --- Fener sistemi ---
    private var flashlightOn = true
    private var wasFlashlightOn = true
    private var flashlightBattery = 100f

    // --- Mert diyalog/güven bayrakları ---
    private var mertSecondLineShown = false
    private var mertIdleChatCooldown = 0f

    // --- Seçim geçmişi (bkz. madde 21) ---
    private val choiceHistory = ChoiceHistory()

    // --- Saklanma sistemi ---
    private var isHiding = false
    private var nearHidingSpot: HidingSpot? = null
    private var nearLockedDoor: Door? = null

    // --- Aynalar (bkz. madde 19) ---
    private var nearMirror: Mirror? = null
    private var mirrorCooldown = 0f
    /** Hangi aynanın "özel" (ilk kez / önemli) sahnesi zaten gösterildi — sonrakiler daha kısa. */
    private val mirrorSeen: MutableSet<String> = mutableSetOf()
    private var mirrorFinaleShown = false

    /** Bir kapı ilk kez kilidini açtığında bir kereye mahsus tepki gösterildi mi (bkz. onDoorUnlocked). */
    private val revealedDoorIds: MutableSet<String> = mutableSetOf()

    // --- Anlatım metni (daktilo efekti) ---
    private var textQueue: MutableList<String> = mutableListOf()
    private var currentFullLine: String = ""
    private var currentRevealed: StringBuilder = StringBuilder()
    private var revealTimer = 0f
    private val revealSpeed = 0.02f // karakter başına saniye
    private var onTextFinished: (() -> Unit)? = null

    // --- İki seçenekli Mert diyalogları (Phase.CHOICE) ---
    private var choiceLabelA = ""
    private var choiceLabelB = ""
    private var onChoiceA: (() -> Unit)? = null
    private var onChoiceB: (() -> Unit)? = null

    // --- QTE ---
    private var qteTargetKey: Int = Input.Keys.F
    private var qteTargetLabel: String = "F"
    private var qteTimeLeft = 0f
    private var qteTotalTime = 2.5f

    // --- Jumpscare flaşı ---
    private var flashTimer = 0f
    private var flashColor: Color = Color.CLEAR

    private var basementMessageCooldown = 0f
    private var endingTitle: String = ""

    // --- New Game+ (bkz. madde 27) — restart() tarafından KASITLI olarak sıfırlanmaz ---
    private var hasCompletedOnce = false

    /** Takipçi bir anıyı unutturduğunda buraya yazılır; sayfa tekrar bulununca Mert'in tepkisini tetikler. */
    private var lastForgottenPageId: String? = null

    override fun show() {
        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.near = 0.1f
        camera.far = 100f

        modelBatch = ModelBatch()
        environment = Environment()
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.35f, 0.3f, 0.35f, 1f))
        environment.add(DirectionalLight().set(0.9f, 0.85f, 0.85f, -0.4f, -0.8f, -0.3f))

        world = World()
        player = Player(camera)
        player.resetTo(0f, 2.5f)
        monster = Monster(world.monsterWaypoints)
        mert = Mert(world.mertSpawn)
        flashlightChild = FlashlightChild(world.flashlightFarSpots)

        Gdx.input.isCursorCatched = true
        audio.setAmbientState("calm")

        startNarration(
            listOf(
                "\"Anne?\"",
                "Cevap yok. Sadece duvar saatinin tik takları.",
                "Saat 03:14. Elektrikler kesik.",
                "Dairende yalnız değilsin — Mert de burada. Bir yerlerden metalik, ıslak bir ses geliyor.",
                "Aslında en büyük korkun karanlık değil. Unutulmak.",
                "Evin her köşesine gizlenmiş $totalPages anı sayfasını bul, gerçeği öğren.",
                "Bodrumun anahtarı bir yerlerde. Ama yalnız olmayabilirsin...",
                "WASD: hareket, Fare: bak, T: fener, E: konuş/aynaya bak, C: saklan, ESC: fareyi serbest bırak."
            )
        )
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0.02f, 0.02f, 0.03f, 1f, true)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.input.isCursorCatched = !Gdx.input.isCursorCatched
        }

        updateAmbientMusic()
        audio.update(delta)

        if (phase == Phase.EXPLORING) {
            updateFlashlight(delta)
            updateFearAndAtmosphere(delta)
            updateCourage(delta)
            updateHiding()
            updateMirrors(delta)
            updateClock(delta)
            updateDoors()
        }

        updatePhase(delta)

        val blockedByHallucination = hallucinationTimer > 0f
        val colliders = world.wallColliders + world.doors.filter { it.locked }.map { it.bounds }
        player.update(
            delta, colliders,
            inputEnabled = phase == Phase.EXPLORING && !blockedByHallucination && !isHiding
        )
        if (blockedByHallucination) {
            hallucinationTimer -= delta
            player.driftYaw(18f, delta)
            player.shake(0.03f)
        }

        mert.update(delta, player.position)

        if (phase == Phase.EXPLORING) {
            monster.update(delta, player.position, isHiding, pagesCollected, monsterActivationThreshold, onCatch = { onMonsterCatch() })
        }
        flashlightChild.update(delta)

        environment.set(ColorAttribute(ColorAttribute.AmbientLight, ambientColorForCurrentState()))

        modelBatch.begin(camera)
        world.instances.forEach { modelBatch.render(it, environment) }
        if (!hasKey) modelBatch.render(world.keyPickupInstance, environment)
        world.doors.forEach { door -> if (door.locked) modelBatch.render(door.modelInstance, environment) }
        world.mirrors.forEach { modelBatch.render(it.modelInstance, environment) }
        world.memoryPages.forEach { page ->
            if (!page.collected) modelBatch.render(page.modelInstance, environment)
        }
        if (monster.visible) {
            modelBatch.render(monster.bodyInstance, environment)
            modelBatch.render(monster.headInstance, environment)
        }
        modelBatch.render(mert.bodyInstance, environment)
        modelBatch.render(mert.headInstance, environment)
        if (flashlightOn && flashlightChild.appeared) {
            modelBatch.render(flashlightChild.bodyInstance, environment)
            modelBatch.render(flashlightChild.headInstance, environment)
        }
        modelBatch.end()

        renderOverlay()
        renderFearVignette()
        renderFlash(delta)
    }

    /** T tuşuyla feneri aç/kapat, pili yönet, kapalıdan açığa geçişte Fener Çocuğu'nu tetikle. */
    private fun updateFlashlight(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.T) && flashlightBattery > 0f) {
            flashlightOn = !flashlightOn
            audio.playSfx("flashlight_click")
        }
        if (flashlightOn) {
            flashlightBattery = (flashlightBattery - 6f * delta).coerceAtLeast(0f)
            if (flashlightBattery <= 0f) flashlightOn = false
        } else {
            flashlightBattery = (flashlightBattery + 4f * delta).coerceAtMost(100f)
        }

        if (flashlightOn && !wasFlashlightOn) {
            flashlightChild.onFlashlightTurnedOn(player.position, player.forwardXZ()) {
                triggerJumpScare(Color(0.9f, 0.9f, 0.85f, 0.55f))
                audio.playSfx("jumpscare_flashlight_child")
                fear.add(30f)
                startNarration(
                    listOf(
                        "Feneri açtığında, tam arkanda duran bir şey görüyorsun.",
                        "Döndüğünde hiçbir şey yok."
                    )
                )
            }
        }
        wasFlashlightOn = flashlightOn
    }

    /** Oyuncu bir saklanma noktasının yarıçapındaysa C ile saklanır/çıkar. Saklıyken Takipçi yakalayamaz. */
    private fun updateHiding() {
        nearHidingSpot = world.hidingSpots.firstOrNull { spot ->
            val dx = spot.position.x - player.position.x
            val dz = spot.position.z - player.position.z
            dx * dx + dz * dz < spot.radius * spot.radius
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            if (isHiding) {
                isHiding = false
                audio.playSfx("hide_exit")
            } else if (nearHidingSpot != null) {
                isHiding = true
                audio.playSfx("hide_enter")
            }
        }
    }

    /** Oyuncu bir aynanın önündeyse not eder; asıl olay E'ye basınca updateExploring içinde tetiklenir. */
    private fun updateMirrors(delta: Float) {
        if (mirrorCooldown > 0f) mirrorCooldown -= delta
        nearMirror = world.mirrors.firstOrNull { it.bounds.contains(player.position) }
    }

    /** Gerçek zaman oyun saatine akar; 03:33'e ulaşınca Takipçi daha saldırgan hale gelir. */
    private fun updateClock(delta: Float) {
        gameMinutes += clockMinutesPerRealSecond * delta
        if (!hit0333 && gameMinutes >= 3f * 60f + 33f) {
            hit0333 = true
            monster.escalate()
            mert.pastThreshold = true
            whisperText = "03:33. Bir yerde bir kilit takırdayıp açılıyor."
            whisperAlpha = 3f
        }
    }

    private fun clockLabel(): String {
        val totalMinutes = gameMinutes.toInt() % (24 * 60)
        return "%02d:%02d".format(totalMinutes / 60, totalMinutes % 60)
    }

    /** Kilitli kapıların açılma koşulunu kontrol eder ve yakındaki kilitli kapıyı HUD ipucu için not eder. */
    private fun updateDoors() {
        world.doors.forEach { door ->
            if (!door.locked) return@forEach
            val unlocked = when (door.kind) {
                DoorKind.TIME_GATED -> gameMinutes >= door.unlockMinute
                DoorKind.KEY_GATED -> hasKey
            }
            if (unlocked) {
                door.locked = false
                audio.playSfx("door_unlock")
                onDoorUnlocked(door)
            }
        }
        nearLockedDoor = world.doors.firstOrNull { door ->
            if (!door.locked) return@firstOrNull false
            val closestX = player.position.x.coerceIn(door.bounds.min.x, door.bounds.max.x)
            val closestZ = player.position.z.coerceIn(door.bounds.min.z, door.bounds.max.z)
            val dx = player.position.x - closestX
            val dz = player.position.z - closestZ
            dx * dx + dz * dz < 2.2f * 2.2f
        }
    }

    /**
     * Bir kapı ilk kez açıldığında, kapı "hiçbir şey olmamış gibi" değil, oyuncunun
     * o ana kadarki seçim geçmişini hatırlıyormuş gibi tepki verir (bkz. madde 10 ve 21).
     * Aynı kapı için bu sadece bir kez tetiklenir.
     */
    private fun onDoorUnlocked(door: Door) {
        if (door.id in revealedDoorIds) return
        revealedDoorIds.add(door.id)
        val line = when (door.id) {
            "door_kidsroom" -> when {
                choiceHistory.has(ChoiceFlag.LIED_ABOUT_BASEMENT) ->
                    "Çocuk Odası'nın kapısı gıcırdayarak açılıyor. Yataklardan biri hâlâ toplanmamış — tıpkı Mert'e \"hiç inmedik\" dediğin gece gibi."
                choiceHistory.has(ChoiceFlag.TOLD_TRUTH_ABOUT_BASEMENT) ->
                    "Çocuk Odası'nın kapısı açılıyor. Bir an içerisi, Mert'e gerçeği söylediğin geceki kadar tanıdık geliyor."
                else ->
                    "Çocuk Odası'nın kapısı gıcırdayarak açılıyor."
            }
            "door_bathroom" -> when {
                choiceHistory.has(ChoiceFlag.FORGOT_A_MEMORY) ->
                    "Saat 03:33. Banyo kapısı kendiliğinden aralanıyor. Aynadaki yansıma, az önce unuttuğun anı gibi bulanık."
                choiceHistory.has(ChoiceFlag.LIED_ABOUT_BASEMENT) ->
                    "Saat 03:33. Banyo kapısı kendiliğinden aralanıyor. Aynaya bakıyorsun; söylediğin yalanı biliyormuş gibi bakıyor sana."
                else ->
                    "Saat 03:33'ü geçti; banyo kapısı kendiliğinden aralanıyor."
            }
            else -> null
        }
        line?.let { startNarration(listOf(it)) }
    }

    /** Oyuncu E ile bir aynaya baktığında, aynanın türüne göre uygun sahneyi başlatır. */
    private fun lookIntoMirror(mirror: Mirror) {
        audio.playSfx("mirror_look")
        when (mirror.kind) {
            MirrorKind.BATHROOM -> lookIntoBathroomMirror()
            MirrorKind.GHOST_REFLECTION -> lookIntoGhostMirror(mirror)
            MirrorKind.FINALE -> lookIntoFinaleMirror()
        }
    }

    /** Genel atmosferik ayna — korku bandı yükseldikçe yansıma gitgide daha tuhaflaşır (bkz. madde 19). */
    private fun lookIntoBathroomMirror() {
        val lines = when (fear.band()) {
            FearBand.NORMAL -> listOf(
                "Aynaya bakıyorsun. Yorgun görünüyorsun, hepsi bu."
            )
            FearBand.UNEASY -> listOf(
                "Aynaya bakıyorsun.",
                "Yansıman bir tık geç gülümsüyor. Sen gülümsemedin."
            )
            FearBand.DISTORTED -> {
                triggerJumpScare(scareColorFor("Ayna Kadını"))
                audio.playSfx("jumpscare_mirror")
                fear.add(6f)
                listOf(
                    "Aynaya bakıyorsun.",
                    "Yansımandaki gözler seninkilerden bir an önce kırpıyor.",
                    "Sonra her şey normale dönüyor. Ya da öyle olduğuna inanmak istiyorsun."
                )
            }
            FearBand.HALLUCINATING -> {
                triggerJumpScare(scareColorFor("Ayna Kadını"))
                audio.playSfx("jumpscare_mirror")
                fear.add(4f)
                listOf(
                    "Aynaya bakıyorsun.",
                    "Yansıman sana bakmıyor; boşluğa, senin arkana bakıyor.",
                    "Dönüp arkana bakıyorsun. Hiçbir şey yok. Yansıman hâlâ oraya bakıyor."
                )
            }
        }
        startNarration(lines)
    }

    /**
     * Çocuk Odası'ndaki ayna — Mert uzaktaysa yansımada onu gösterip sonra
     * yok eder (bkz. tasarım notundaki "aynada Mert görünür, gerçekte odada
     * Mert yoktur" örneği). Mert yakınsa sıradan bir yansımadır.
     */
    private fun lookIntoGhostMirror(mirror: Mirror) {
        val mertFar = player.position.dst(mert.position) > 6f
        if (!mertFar) {
            startNarration(listOf("Aynaya bakıyorsun. Sadece kendini görüyorsun, Mert de yanında duruyor."))
            return
        }
        val firstTime = mirror.id !in mirrorSeen
        mirrorSeen.add(mirror.id)
        triggerJumpScare(Color(0.35f, 0.05f, 0.4f, 0.5f))
        audio.playSfx("jumpscare_mirror")
        fear.add(10f)
        val lines = if (firstTime) {
            listOf(
                "Aynaya bakıyorsun.",
                "Ama yansımanda yalnız değilsin — Mert hemen arkanda duruyor.",
                "Dönüp bakıyorsun. Arkanda kimse yok.",
                "Aynaya tekrar baktığında Mert de kayboluyor."
            )
        } else {
            listOf("Aynaya bakıyorsun. Bu sefer arkanda kimse yok — ne aynada, ne gerçekte.")
        }
        startNarration(lines)
    }

    /**
     * Salon'daki büyük ayna — tüm anılar toplanmadan sıradan bir yansımadır.
     * Hepsi toplandıktan sonra ilk bakışta oyuncunun seçim geçmişini
     * (bkz. ChoiceHistory) yansımada gösterir; sonraki bakışlarda kısa bir
     * yankı bırakır (bkz. madde 19'daki "final aynası" fikri).
     */
    private fun lookIntoFinaleMirror() {
        if (pagesCollected < totalPages) {
            startNarration(listOf("Aynaya bakıyorsun. Henüz net değil — sanki hâlâ eksik bir şeyler var."))
            return
        }
        if (mirrorFinaleShown) {
            startNarration(listOf("Aynaya tekrar bakıyorsun. Kararların hâlâ orada, sessizce sıralı duruyor."))
            return
        }
        mirrorFinaleShown = true
        triggerJumpScare(Color(0.55f, 0.7f, 0.9f, 0.4f))
        val summary = choiceHistory.summaryLines()
        val lines = listOf(
            "Aynaya bakıyorsun.",
            "Yansımanda sen değil, bu gece boyunca aldığın kararlar beliriyor, art arda:"
        ) + summary + listOf("Sonra yansıma yeniden sadece sen oluyor.")
        startNarration(lines)
    }

    /** Korku değerini günceller; bant yükseldikçe fısıltılar ve kısa kontrol kayıpları tetiklenir. */
    private fun updateFearAndAtmosphere(delta: Float) {
        val monsterDistance = if (monster.visible) monster.position.dst(player.position) else 999f
        fear.update(delta, monster.visible, monsterDistance)

        val band = fear.band()

        whisperAlpha = (whisperAlpha - delta).coerceAtLeast(0f)
        if (band != FearBand.NORMAL) {
            whisperTimer -= delta
            if (whisperTimer <= 0f) {
                val roomLines = roomWhispers[currentRoomName()]
                whisperText = if (roomLines != null && Math.random() < 0.5) roomLines.random() else whisperPool.random()
                whisperAlpha = 2.6f
                audio.playRandomWhisper()
                whisperTimer = when (band) {
                    FearBand.UNEASY -> 10f + Math.random().toFloat() * 6f
                    FearBand.DISTORTED -> 6f + Math.random().toFloat() * 4f
                    else -> 3f + Math.random().toFloat() * 3f
                }
            }
        }

        if (monster.visible) {
            monsterApproachWarned = false
        } else if (monster.isActivated && monster.timeUntilAppear in 0f..1.6f && !monsterApproachWarned) {
            monsterApproachWarned = true
            whisperText = "Ayak sesleri yaklaşıyor..."
            whisperAlpha = 2.2f
            audio.playSfx("footstep_close")
        }

        if (band == FearBand.HALLUCINATING) {
            hallucinationCheckTimer -= delta
            if (hallucinationCheckTimer <= 0f) {
                hallucinationCheckTimer = 5f + Math.random().toFloat() * 5f
                if (Math.random() < 0.5 && hallucinationTimer <= 0f) {
                    hallucinationTimer = 1.0f + Math.random().toFloat() * 0.8f
                }
            }
        }
    }

    /**
     * Cesaret puanı sessizce birikir: Takipçi görünürken saklanmak yerine ona
     * göğüs germek ya da feneri kapatıp karanlıkta yürümeye devam etmek —
     * ikisi de "korkuyu yönetme" biçimidir ve oyuncuya asla bildirilmez.
     */
    private fun updateCourage(delta: Float) {
        if (monster.visible && !isHiding) {
            courage.add(9f * delta)
        }
        if (!flashlightOn && !isHiding) {
            courage.add(2f * delta)
        }
    }

    private fun currentRoomName(): String? =
        world.rooms.firstOrNull { it.bounds.contains(player.position) }?.name

    /**
     * O anki oyun durumuna göre hangi arkaplan müziğinin çalması gerektiğine
     * karar verir (bkz. AudioManager — 4 korku bandı + takip + 4 son türü).
     * Bir son'a ulaşıldığında bu fonksiyon bir daha müdahale etmez; ending
     * müziği `startNarration`ın `onFinished` bloklarında ayrıca başlatılır.
     */
    private fun updateAmbientMusic() {
        if (phase == Phase.ENDED) return
        val desired = when {
            monster.visible -> "chase"
            else -> when (fear.band()) {
                FearBand.NORMAL -> "calm"
                FearBand.UNEASY -> "uneasy"
                FearBand.DISTORTED -> "distorted"
                FearBand.HALLUCINATING -> "hallucinating"
            }
        }
        audio.setAmbientState(desired)
    }

    private fun ambientColorForCurrentState(): Color {
        val ambientMultiplier = when (fear.band()) {
            FearBand.NORMAL -> 1f
            FearBand.UNEASY -> 0.85f
            FearBand.DISTORTED -> 0.55f
            FearBand.HALLUCINATING -> 0.32f
        }
        val flashlightMultiplier = if (flashlightOn) 1f else 0.18f
        val r = 0.35f * ambientMultiplier * flashlightMultiplier
        val g = 0.3f * ambientMultiplier * flashlightMultiplier
        val b = 0.35f * ambientMultiplier * flashlightMultiplier
        return Color(r, g, b, 1f)
    }

    private fun updatePhase(delta: Float) {
        when (phase) {
            Phase.EXPLORING -> updateExploring(delta)
            Phase.TEXT -> updateText(delta)
            Phase.CHOICE -> updateChoice()
            Phase.QTE -> updateQte(delta)
            Phase.ENDED -> updateEnded()
        }
    }

    private fun updateExploring(delta: Float) {
        if (basementMessageCooldown > 0f) basementMessageCooldown -= delta
        if (mertIdleChatCooldown > 0f) mertIdleChatCooldown -= delta

        if (pagesCollected >= flashlightChildActivationThreshold) flashlightChild.activate()

        nearMirror?.let { mirror ->
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) && mirrorCooldown <= 0f) {
                mirrorCooldown = 1.2f
                lookIntoMirror(mirror)
                return
            }
        }

        if (!mert.askedBasementQuestion && pagesCollected >= 1 &&
            player.position.dst(mert.position) < mert.interactionRadius &&
            Gdx.input.isKeyJustPressed(Input.Keys.E)
        ) {
            mert.askedBasementQuestion = true
            startNarration(
                listOf(
                    "Mert yanına yaklaşıyor.",
                    "MERT: \"Evin, dün gece bodruma indiğimizi hatırlıyor musun?\""
                ),
                onFinished = {
                    startChoice(
                        labelA = "[1] \"Evet, birlikte indik.\" (gerçeği söyle)",
                        labelB = "[2] \"Hayır, hiç inmedik.\" (yalan söyle)",
                        onA = {
                            mert.adjustTrust(15f)
                            choiceHistory.record(ChoiceFlag.TOLD_TRUTH_ABOUT_BASEMENT, gameMinutes)
                            startNarration(listOf("MERT: \"İyi ki hatırlıyorsun. Ben de biliyordum ki delirmemişim.\""))
                        },
                        onB = {
                            mert.adjustTrust(-15f)
                            mert.liedAboutBasement = true
                            choiceHistory.record(ChoiceFlag.LIED_ABOUT_BASEMENT, gameMinutes)
                            startNarration(listOf("MERT: \"...Emin misin? Neyse. Boş ver.\""))
                        }
                    )
                }
            )
            return
        }

        if (!mertSecondLineShown && pagesCollected >= 4) {
            mertSecondLineShown = true
            // 03:33 eşiği geçildiyse Mert'in tavrı gerçekten farklılaşır: güven/yalan
            // durumundan bağımsız olarak önce zaman eksenindeki değişim konuşur.
            val line = when {
                mert.pastThreshold && mert.trusts() ->
                    "MERT: \"Saat kaç oldu bilmiyorum artık Evin. Ama sana söz verdim, gitmiyorum.\""
                mert.pastThreshold ->
                    "MERT: \"Az önce ne konuştuk biz? ...Boş ver. Sesin bile farklı geliyor kulağıma.\""
                mert.trusts() -> "MERT: \"Sana güveniyorum Evin. Ne olursa olsun yanındayım.\""
                mert.liedAboutBasement -> "MERT: \"Bodrumda ne gördüğümüzü kimseye söyleme demiştim sanki... yoksa söylemedim mi? Artık emin değilim.\""
                else -> "MERT: \"Bir şeyler hatırlamıyormuşum gibi hissediyorum. Sen de mi?\""
            }
            startNarration(listOf(line))
            return
        }

        // Serbest sohbet: yukarıdaki özel diyaloglardan biri tetiklenmediyse ve oyuncu
        // Mert'e yaklaşıp E'ye basarsa kısa bir replik döner. Bu replik havuzu saat
        // 03:33'ü geçince gerçekten değişir — kozmetik bir ses tonu değil, farklı
        // cümlelerdir (bkz. Mert.idleLine).
        if (player.position.dst(mert.position) < mert.interactionRadius &&
            Gdx.input.isKeyJustPressed(Input.Keys.E) &&
            mertIdleChatCooldown <= 0f
        ) {
            mertIdleChatCooldown = 1.5f
            startNarration(listOf(mert.idleLine(courage.isCourageous())))
            return
        }

        if (!hasKey && world.keyBounds.contains(player.position)) {
            hasKey = true
            audio.playSfx("key_pickup")
            startNarration(
                listOf(
                    "Elini uzatıp anahtarı alıyorsun.",
                    "Bu, bodrum kapısının anahtarına benziyor."
                )
            )
            return
        }

        for (page in world.memoryPages) {
            if (page.collected) continue
            if (!page.bounds.contains(player.position)) continue
            page.collected = true
            pagesCollected++
            audio.playSfx("page_pickup")
            if (page.jumpscareCharacter != null) {
                triggerJumpScare(scareColorFor(page.jumpscareCharacter))
                audio.playSfx(sfxIdForCharacter(page.jumpscareCharacter))
            }
            // Bu, Takipçi'nin daha önce unutturduğu anıysa: NPC hafızası çelişkili
            // tepki verir — Mert yarım yamalak hatırlar, sonra kendinden şüphe eder.
            val reclaimLines = if (page.id == lastForgottenPageId) {
                lastForgottenPageId = null
                listOf(
                    "Bunu daha önce de bulmuştun. Şimdi ilk kez buluyormuşsun gibi geliyor.",
                    "MERT: \"Bunu elinde daha önce de görmüştüm sanki... yoksa görmedim mi? Bilmiyorum artık.\""
                )
            } else {
                emptyList()
            }
            // New Game+: ilk anı sayfasındaki isim, oyunu bir kez bitirmiş oyuncuya
            // fazladan bir satır gösterir — ilk turda hiç görünmeyen bir ayrıntı.
            val newGamePlusLines = if (page.id == "page_bedroom" && hasCompletedOnce) {
                listOf("Yazının altında, çok daha soluk bir el yazısıyla: \"1998'de de buradaydım.\"")
            } else {
                emptyList()
            }
            startNarration(reclaimLines + page.lines + newGamePlusLines + "Anı: $pagesCollected/$totalPages")
            return
        }

        for (trigger in world.triggers) {
            if (trigger.fired && trigger.kind != TriggerKind.BASEMENT_CHECK) continue
            if (!trigger.bounds.contains(player.position)) continue

            when (trigger.kind) {
                TriggerKind.KITCHEN_JUMPSCARE -> {
                    trigger.fired = true
                    triggerJumpScare()
                    audio.playSfx("jumpscare_generic")
                    startNarration(
                        listOf(
                            "Lavabo taşmış, yerde kahverengi bir sıvı birikmiş.",
                            "Ve o sıvı... hareket ediyor."
                        )
                    )
                    return
                }
                TriggerKind.FRONT_DOOR_QTE -> {
                    trigger.fired = true
                    triggerJumpScare(Color(1f, 1f, 1f, 0.6f))
                    audio.playSfx("jumpscare_generic")
                    qteTotalTime = if (mert.trusts()) 3.6f else 2.5f
                    choiceHistory.record(
                        if (mert.trusts()) ChoiceFlag.TRUSTED_MERT_AT_DOOR else ChoiceFlag.LEFT_MERT_BEHIND,
                        gameMinutes
                    )
                    if (hit0333) choiceHistory.record(ChoiceFlag.SURVIVED_PAST_0333, gameMinutes)
                    val doorLine = when {
                        mert.trusts() && mert.pastThreshold -> "Mert elini tutuyor, tereddütle: \"Ben... buradayım. Koş, Evin!\""
                        mert.trusts() -> "Mert elini tutuyor: \"Ben buradayım, koş!\""
                        mert.pastThreshold -> "Yanındaki ses artık Mert'e hiç benzemiyor. Yalnız koşuyorsun."
                        else -> "Yalnızsın. Kapıya doğru koşuyorsun."
                    }
                    startNarration(
                        listOf(doorLine),
                        onFinished = { startQte() }
                    )
                    return
                }
                TriggerKind.BASEMENT_CHECK -> {
                    if (basementMessageCooldown > 0f) return
                    basementMessageCooldown = 2f
                    if (hasKey) {
                        trigger.fired = true
                        if (pagesCollected >= totalPages) {
                            val mertLine = if (mert.trusts())
                                "Mert az arkanda, sessizce elini omzuna koyuyor."
                            else
                                "Mert çoktan geride kaldı; onu burada bıraktığını fark ediyorsun."
                            val courageLines = if (courage.isCourageous())
                                listOf("Korkunun ortasında bile geri adım atmadın; ev bunu senden önce fark etti.")
                            else emptyList()
                            startNarration(
                                listOf(
                                    "Anahtar tam uyuyor. Kapı gıcırdayarak açılıyor.",
                                    mertLine,
                                    "Merdivenlerin dibinde eski sen duruyor. Yıllar önce burada unutulmuşsun.",
                                    "\"Sonunda geldin,\" diyor. Sesi senin sesin.",
                                    "\"Ben kimim?\" diye soruyorsun.",
                                    "\"Ben senin unuttuğun tarafınım,\" diyor eski sen.",
                                    "Topladığın $totalPages anı, gerçeği tek tek önüne seriyor: bu ev hiç terk edilmedi, sen hep buradaydın.",
                                    "\"Beni artık bırakabilirsin,\" diyor. \"Tek istediğim hatırlanmaktı.\"",
                                    "\"Seni hiç unutmayacağım,\" diyorsun.",
                                    "Bazı yerlerde hayaletler ölüler değildir. Hatırlanmayı bekleyenlerdir.",
                                    "Bu gece verdiğin kararlar seninle kaldı:"
                                ) + courageLines + choiceHistory.summaryLines() + "== GERÇEK SON ==",
                                onFinished = { phase = Phase.ENDED; endingTitle = "GERÇEK SON"; audio.playEnding("ending_true") }
                            )
                        } else {
                            startNarration(
                                listOf(
                                    "Anahtar tam uyuyor. Kapı gıcırdayarak açılıyor.",
                                    "Merdivenlerin dibinde bir şey var ama karanlıkta tam seçemiyorsun.",
                                    "\"Beni hatırlamıyorsun,\" diye fısıldıyor karanlık.",
                                    "Topladığın $pagesCollected/$totalPages anı yetersiz — gerçeğin sadece bir parçasını anlıyorsun.",
                                    "Bu gece verdiğin kararlar seninle kaldı:"
                                ) + choiceHistory.summaryLines() + "== YARIM SON ==",
                                onFinished = { phase = Phase.ENDED; endingTitle = "YARIM SON"; audio.playEnding("ending_half") }
                            )
                        }
                    } else {
                        startNarration(listOf("Kapı kilitli. Bir anahtara ihtiyacın var."))
                    }
                    return
                }
            }
        }
    }

    /** Yakalanınca GAME OVER yerine daha önce bulunmuş bir anı sayfası unutulur — tekrar bulunması gerekir. */
    private fun onMonsterCatch() {
        triggerJumpScare(Color(0.6f, 0.02f, 0.02f, 0.75f))
        audio.playSfx("monster_catch")
        player.resetTo(0f, 2.5f)

        val forgotten = world.memoryPages.filter { it.collected }.randomOrNull()
        val closingLine = if (forgotten != null) {
            forgotten.collected = false
            pagesCollected--
            lastForgottenPageId = forgotten.id
            choiceHistory.record(ChoiceFlag.FORGOT_A_MEMORY, gameMinutes)
            "Zihninde bir şey siliniyor... az önce hatırladığın bir anı artık bulanık. Onu tekrar bulman gerekecek."
        } else {
            "Takipçi seni bir süreliğine kaybetti... ama evde yalnız değilsin."
        }

        startNarration(
            listOf(
                "Soğuk, ince parmaklar omzuna dokunuyor!",
                "Kulağına kendi çocukluk sesin fısıldıyor: \"Anne...\"",
                "Kalbin küt küt atarken kendini yatak odasına doğru sürüklüyorsun.",
                closingLine
            )
        )
    }

    private fun scareColorFor(character: String): Color = when (character) {
        "Ayna Kadını" -> Color(0.55f, 0.7f, 0.9f, 0.55f)
        "Gölge Adam" -> Color(0.35f, 0.05f, 0.4f, 0.65f)
        "Oyuncak Bebek" -> Color(0.4f, 0.85f, 0.45f, 0.5f)
        else -> Color(1f, 1f, 1f, 0.6f)
    }

    private fun sfxIdForCharacter(character: String): String = when (character) {
        "Ayna Kadını" -> "jumpscare_mirror"
        "Gölge Adam" -> "jumpscare_shadow"
        "Oyuncak Bebek" -> "jumpscare_doll"
        else -> "jumpscare_generic"
    }

    private fun startNarration(lines: List<String>, onFinished: (() -> Unit)? = null) {
        textQueue = lines.toMutableList()
        onTextFinished = onFinished
        phase = Phase.TEXT
        advanceLine()
    }

    private fun advanceLine() {
        if (textQueue.isEmpty()) {
            phase = Phase.EXPLORING
            val callback = onTextFinished
            onTextFinished = null
            callback?.invoke()
            return
        }
        currentFullLine = textQueue.removeAt(0)
        currentRevealed = StringBuilder()
        revealTimer = 0f
        // Ekrana gelen HER anlatım/diyalog satırı otomatik olarak seslendirme
        // dosyası arar (bkz. AudioManager.playVoiceForLine ve VOICE_MANIFEST.md).
        audio.playVoiceForLine(currentFullLine)
    }

    private fun updateText(delta: Float) {
        if (currentRevealed.length < currentFullLine.length) {
            revealTimer += delta
            while (revealTimer >= revealSpeed && currentRevealed.length < currentFullLine.length) {
                revealTimer -= revealSpeed
                currentRevealed.append(currentFullLine[currentRevealed.length])
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            advanceLine()
        }
    }

    private fun startChoice(labelA: String, labelB: String, onA: () -> Unit, onB: () -> Unit) {
        choiceLabelA = labelA
        choiceLabelB = labelB
        onChoiceA = onA
        onChoiceB = onB
        phase = Phase.CHOICE
    }

    private fun updateChoice() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            audio.playSfx("ui_choice")
            val callback = onChoiceA
            onChoiceA = null; onChoiceB = null
            phase = Phase.EXPLORING
            callback?.invoke()
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            audio.playSfx("ui_choice")
            val callback = onChoiceB
            onChoiceA = null; onChoiceB = null
            phase = Phase.EXPLORING
            callback?.invoke()
        }
    }

    private fun startQte() {
        val options = listOf(Input.Keys.F to "F", Input.Keys.G to "G", Input.Keys.H to "H", Input.Keys.J to "J")
        val (key, label) = options.random()
        qteTargetKey = key
        qteTargetLabel = label
        qteTimeLeft = qteTotalTime
        phase = Phase.QTE
        audio.playSfx("qte_start")
    }

    private fun updateQte(delta: Float) {
        qteTimeLeft -= delta
        if (Gdx.input.isKeyJustPressed(qteTargetKey)) {
            audio.playSfx("qte_success")
            startNarration(
                listOf(
                    "Tam zamanında kaçtın! Kapıyı arkanda kilitleyip uzaklaşıyorsun.",
                    "Ama arkana bakmıyorsun — içindeki bir ses bunun bitmediğini biliyor.",
                    "== KAÇIŞ SONU =="
                ),
                onFinished = { phase = Phase.ENDED; endingTitle = "KAÇIŞ SONU"; audio.playEnding("ending_escape") }
            )
            return
        }
        if (qteTimeLeft <= 0f) {
            audio.playSfx("qte_fail")
            startNarration(
                listOf(
                    "Tepki veremeden soğuk, ıslak bir el bileğini kavrıyor.",
                    "Karanlıkta bir ses fısıldıyor: \"Burada sadece hatırlanmayanlar kalır.\"",
                    "== YAKALANDIN =="
                ),
                onFinished = { phase = Phase.ENDED; endingTitle = "YAKALANDIN"; audio.playEnding("ending_caught") }
            )
        }
    }

    private fun updateEnded() {
        // Herhangi bir sona ulaşmak "bir kez bitirdin" sayılır; bu bayrak restart()
        // tarafından sıfırlanmaz — bir sonraki R ile yeniden başlatmada New Game+
        // farkları (bkz. buildIntroLines, page_bedroom) sessizce devreye girer.
        hasCompletedOnce = true
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restart()
        }
    }

    private fun restart() {
        hasKey = false
        pagesCollected = 0
        world.triggers.forEach { it.fired = false }
        world.memoryPages.forEach { it.collected = false }
        monster.reset()
        mert.reset(world.mertSpawn)
        mertSecondLineShown = false
        mertIdleChatCooldown = 0f
        choiceHistory.reset()
        isHiding = false
        nearHidingSpot = null
        nearMirror = null
        mirrorCooldown = 0f
        mirrorSeen.clear()
        mirrorFinaleShown = false
        world.doors.forEach { it.locked = true }
        nearLockedDoor = null
        revealedDoorIds.clear()
        lastForgottenPageId = null
        flashlightChild.reset()
        flashlightOn = true
        wasFlashlightOn = true
        flashlightBattery = 100f
        fear.reset()
        courage.reset()
        whisperAlpha = 0f
        whisperTimer = 4f
        monsterApproachWarned = false
        gameMinutes = startGameMinutes
        hit0333 = false
        hallucinationTimer = 0f
        hallucinationCheckTimer = 4f
        player.resetTo(0f, 2.5f)
        endingTitle = ""
        // New Game+: hasCompletedOnce burada BİLEREK sıfırlanmıyor. Oyuncu bir kez
        // bitirdiyse, bir sonraki turda küçük ama gerçek bir fark görür (bkz. altta,
        // ilk anı sayfasında da tekrar eden aynı fikir).
        val introLine = if (hasCompletedOnce)
            "Yeniden başlıyorsun... ama bu sefer bir şeyler tanıdık geliyor."
        else
            "Yeniden başlıyorsun..."
        startNarration(listOf(introLine))
    }

    private fun triggerJumpScare(color: Color = Color(1f, 1f, 1f, 0.6f)) {
        flashTimer = 0.25f
        flashColor = color
        player.shake(0.15f)
    }

    private fun renderOverlay() {
        spriteBatch.begin()
        when (phase) {
            Phase.TEXT -> font.draw(
                spriteBatch, currentRevealed.toString(),
                40f, 110f, Gdx.graphics.width.toFloat() - 80f, Align.left, true
            )
            Phase.CHOICE -> {
                font.draw(spriteBatch, choiceLabelA, 40f, 140f, Gdx.graphics.width.toFloat() - 80f, Align.left, true)
                font.draw(spriteBatch, choiceLabelB, 40f, 100f, Gdx.graphics.width.toFloat() - 80f, Align.left, true)
            }
            Phase.QTE -> font.draw(
                spriteBatch,
                "HIZLI OL! '$qteTargetLabel' TUŞUNA BAS! (${"%.1f".format(qteTimeLeft)} sn)",
                40f, 110f
            )
            Phase.ENDED -> {
                font.draw(spriteBatch, endingTitle, 40f, Gdx.graphics.height.toFloat() - 40f)
                font.draw(spriteBatch, "Yeniden oynamak için R'ye bas.", 40f, Gdx.graphics.height.toFloat() - 80f)
            }
            Phase.EXPLORING -> {
                font.draw(spriteBatch, "Anılar: $pagesCollected/$totalPages", 20f, 30f)
                if (hasKey) font.draw(spriteBatch, "Envanter: Eski Anahtar", 20f, 55f)
                val flashlightLabel = if (flashlightOn) "AÇIK" else "KAPALI"
                font.draw(spriteBatch, "Fener: $flashlightLabel (Pil: %${flashlightBattery.toInt()})", 20f, 80f)
                font.draw(spriteBatch, "Saat: ${clockLabel()}", 20f, 105f)
                if (whisperAlpha > 0f) {
                    font.color = Color(1f, 1f, 1f, whisperAlpha.coerceAtMost(1f))
                    font.draw(spriteBatch, whisperText, 20f, Gdx.graphics.height.toFloat() - 30f)
                    font.color = Color.WHITE
                }
                if (isHiding) {
                    font.draw(spriteBatch, "Saklanıyorsun... (çıkmak için C)", 20f, Gdx.graphics.height.toFloat() - 60f)
                } else if (nearHidingSpot != null) {
                    font.draw(spriteBatch, "${nearHidingSpot!!.label}: saklanmak için C", 20f, Gdx.graphics.height.toFloat() - 60f)
                }
                nearMirror?.let { mirror ->
                    font.draw(spriteBatch, "${mirror.label}: bakmak için E", 20f, Gdx.graphics.height.toFloat() - 120f)
                }
                nearLockedDoor?.let { door ->
                    val hint = when {
                        door.id == "door_kidsroom" && door.kind == DoorKind.KEY_GATED && choiceHistory.has(ChoiceFlag.LIED_ABOUT_BASEMENT) ->
                            "${door.label} kilitli. İçeriden gelen sessizlik, söylediğin yalanı biliyormuş gibi hissettiriyor."
                        door.kind == DoorKind.KEY_GATED ->
                            "${door.label} kilitli. Belki elindeki anahtar uyar."
                        else ->
                            "${door.label} kilitli. Gece ilerledikçe bir şey değişebilir."
                    }
                    font.draw(spriteBatch, hint, 20f, Gdx.graphics.height.toFloat() - 90f)
                }
            }
        }
        spriteBatch.end()
    }

    /** Korku bandı yükseldikçe ekranın kenarlarını koyulaştıran sürekli örtü — flaştan bağımsız. */
    private fun renderFearVignette() {
        val alpha = when (fear.band()) {
            FearBand.NORMAL -> 0f
            FearBand.UNEASY -> 0.08f
            FearBand.DISTORTED -> 0.18f
            FearBand.HALLUCINATING -> 0.3f
        }
        if (alpha <= 0f) return
        Gdx.gl.glEnable(GL20.GL_BLEND)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.15f, 0.02f, 0.03f, alpha)
        shapeRenderer.rect(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    private fun renderFlash(delta: Float) {
        if (flashTimer <= 0f) return
        flashTimer -= delta
        Gdx.gl.glEnable(GL20.GL_BLEND)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = flashColor
        shapeRenderer.rect(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        modelBatch.dispose()
        world.dispose()
        monster.dispose()
        mert.dispose()
        flashlightChild.dispose()
        audio.dispose()
        spriteBatch.dispose()
        font.dispose()
        shapeRenderer.dispose()
    }
}
