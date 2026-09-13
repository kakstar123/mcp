package com.korku.game3d

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound

/**
 * Oyunun tüm sesini yöneten merkezi sistem:
 *  - Katmanlı/karışımlı (crossfade) arkaplan müziği (korku bandına, Takipçi'nin
 *    görünürlüğüne ve oyun fazına göre otomatik değişir),
 *  - Kısa efektler (SFX: sıçrama anları, sayfa/anahtar alma, kapı, QTE, vb.),
 *  - Diyalog/anlatım seslendirmeleri (voice-over): ekranda gösterilen HER metin
 *    satırı için otomatik olarak bir ses dosyası adı üretilir ve o dosya
 *    `assets/audio/voice/` altında varsa çalınır.
 *
 * ÖNEMLİ — gerçek ses dosyaları bu depoda YOK (bkz. assets/audio/README.md ve
 * assets/audio/VOICE_MANIFEST.md). Her yükleme/çalma denemesi önce dosyanın
 * var olup olmadığını kontrol eder; yoksa sessizce atlanır ve sadece bir kere
 * konsola not düşülür. Yani ses dosyalarını henüz eklemediysen oyun hatasız
 * çalışır, sadece sessiz kalır — dosyaları assets/audio/ altına ekledikçe
 * oyun otomatik olarak seslenmeye başlar, kod tarafında hiçbir değişiklik
 * gerekmez.
 */
class AudioManager {

    // ---- Arkaplan müziği: durum -> dosya yolu ----------------------------
    // Her biri "assets/audio/music/<dosya>.ogg" olarak beklenir. Sahnede aynı
    // anda tek bir müzik katmanı çalar; durum değiştiğinde kısa bir crossfade
    // (bkz. update()) ile bir sonrakine geçilir. "Geniş" bir müzik yelpazesi
    // için 8 farklı durum tanımlı: 4 korku bandı + takip + 3 son türü.
    private val musicPaths: Map<String, String> = mapOf(
        "calm" to "audio/music/ambient_calm.ogg",
        "uneasy" to "audio/music/ambient_uneasy.ogg",
        "distorted" to "audio/music/ambient_distorted.ogg",
        "hallucinating" to "audio/music/ambient_hallucinating.ogg",
        "chase" to "audio/music/chase.ogg",
        "ending_true" to "audio/music/ending_true.ogg",
        "ending_half" to "audio/music/ending_half.ogg",
        "ending_escape" to "audio/music/ending_escape.ogg",
        "ending_caught" to "audio/music/ending_caught.ogg"
    )

    // ---- Kısa efektler: id -> dosya yolu -----------------------------------
    private val sfxPaths: Map<String, String> = mapOf(
        "jumpscare_generic" to "audio/sfx/jumpscare_generic.ogg",
        "jumpscare_mirror" to "audio/sfx/jumpscare_mirror.ogg",
        "jumpscare_shadow" to "audio/sfx/jumpscare_shadow.ogg",
        "jumpscare_doll" to "audio/sfx/jumpscare_doll.ogg",
        "jumpscare_flashlight_child" to "audio/sfx/jumpscare_flashlight_child.ogg",
        "page_pickup" to "audio/sfx/page_pickup.ogg",
        "key_pickup" to "audio/sfx/key_pickup.ogg",
        "door_unlock" to "audio/sfx/door_unlock.ogg",
        "mirror_look" to "audio/sfx/mirror_look.ogg",
        "flashlight_click" to "audio/sfx/flashlight_click.ogg",
        "hide_enter" to "audio/sfx/hide_enter.ogg",
        "hide_exit" to "audio/sfx/hide_exit.ogg",
        "qte_start" to "audio/sfx/qte_start.ogg",
        "qte_success" to "audio/sfx/qte_success.ogg",
        "qte_fail" to "audio/sfx/qte_fail.ogg",
        "monster_catch" to "audio/sfx/monster_catch.ogg",
        "whisper_1" to "audio/sfx/whisper_1.ogg",
        "whisper_2" to "audio/sfx/whisper_2.ogg",
        "whisper_3" to "audio/sfx/whisper_3.ogg",
        "footstep_close" to "audio/sfx/footstep_close.ogg",
        "ui_choice" to "audio/sfx/ui_choice.ogg"
    )

    private val loadedMusic: MutableMap<String, Music> = mutableMapOf()
    private val loadedSfx: MutableMap<String, Sound> = mutableMapOf()
    // Sesli anlatım dosyaları çok sayıda (100+) olabileceğinden hepsini baştan
    // yüklemek yerine ihtiyaç oldukça (satır ekrana gelince) yükleyip önbelleğe
    // alıyoruz. `null` değer, "bu satır için dosya yok, bir daha deneme" anlamına gelir.
    private val voiceCache: MutableMap<String, Sound?> = mutableMapOf()
    private val missingLogged: MutableSet<String> = mutableSetOf()

    var musicVolume: Float = 0.55f
    var sfxVolume: Float = 0.9f
    var voiceVolume: Float = 1f
    var muted: Boolean = false
        set(value) {
            field = value
            if (value) {
                currentMusic?.volume = 0f
                nextMusic?.volume = 0f
            }
        }

    private var currentMusicKey: String? = null
    private var currentMusic: Music? = null
    private var nextMusicKey: String? = null
    private var nextMusic: Music? = null
    private var fadeElapsed = 0f
    private val fadeDuration = 1.4f

    // Aynı anda sadece tek bir seslendirme satırı çalsın diye, en son çalınan
    // sesin kendisini ve döndürdüğü örnek id'sini saklıyoruz.
    private var currentVoiceSound: Sound? = null
    private var currentVoiceId: Long = -1L

    // -------------------------------------------------------------------
    // Yükleme yardımcıları — dosya yoksa null döner, exception fırlatmaz.
    // -------------------------------------------------------------------

    private fun music(key: String): Music? {
        loadedMusic[key]?.let { return it }
        val path = musicPaths[key] ?: return null
        val handle = Gdx.files.internal(path)
        if (!handle.exists()) {
            logMissingOnce("music:$key", path)
            return null
        }
        return try {
            val m = Gdx.audio.newMusic(handle)
            loadedMusic[key] = m
            m
        } catch (e: Exception) {
            Gdx.app.error("AudioManager", "Müzik yüklenemedi: $path", e)
            null
        }
    }

    private fun sfx(id: String): Sound? {
        loadedSfx[id]?.let { return it }
        val path = sfxPaths[id] ?: return null
        val handle = Gdx.files.internal(path)
        if (!handle.exists()) {
            logMissingOnce("sfx:$id", path)
            return null
        }
        return try {
            val s = Gdx.audio.newSound(handle)
            loadedSfx[id] = s
            s
        } catch (e: Exception) {
            Gdx.app.error("AudioManager", "Efekt yüklenemedi: $path", e)
            null
        }
    }

    private fun logMissingOnce(cacheKey: String, path: String) {
        if (missingLogged.add(cacheKey)) {
            Gdx.app.log("AudioManager", "Ses dosyası bulunamadı (atlanıyor): $path")
        }
    }

    // -------------------------------------------------------------------
    // Arkaplan müziği: durum bazlı crossfade
    // -------------------------------------------------------------------

    /**
     * Her frame çağrılır. Hedef durumu (`desiredKey`) ayarlar; anlık durumdan
     * farklıysa yumuşak bir geçiş (crossfade) başlatır/ilerletir.
     */
    fun setAmbientState(desiredKey: String) {
        if (muted) return
        if (desiredKey == currentMusicKey || desiredKey == nextMusicKey) return
        val target = music(desiredKey)
        // Hedef dosya yoksa (henüz eklenmediyse) mevcut müziği kesmeden bekleriz.
        if (target == null) return
        nextMusicKey = desiredKey
        nextMusic = target
        fadeElapsed = 0f
        target.isLooping = !desiredKey.startsWith("ending_")
        target.volume = 0f
        target.play()
    }

    fun update(delta: Float) {
        val next = nextMusic ?: return
        fadeElapsed += delta
        val t = (fadeElapsed / fadeDuration).coerceIn(0f, 1f)
        currentMusic?.volume = (1f - t) * musicVolume
        next.volume = t * musicVolume
        if (t >= 1f) {
            currentMusic?.stop()
            currentMusic = next
            currentMusicKey = nextMusicKey
            nextMusic = null
            nextMusicKey = null
        }
    }

    /** Bir son (ending) ekranına girildiğinde döngüsüz bitiş müziğine geçer. */
    fun playEnding(key: String) {
        setAmbientState(key)
    }

    fun stopMusic() {
        currentMusic?.stop()
        nextMusic?.stop()
        currentMusic = null
        nextMusic = null
        currentMusicKey = null
        nextMusicKey = null
    }

    // -------------------------------------------------------------------
    // Kısa efektler
    // -------------------------------------------------------------------

    fun playSfx(id: String, volume: Float = 1f) {
        if (muted) return
        sfx(id)?.play(sfxVolume * volume)
    }

    fun playRandomWhisper() {
        playSfx(listOf("whisper_1", "whisper_2", "whisper_3").random())
    }

    // -------------------------------------------------------------------
    // Diyalog / anlatım seslendirmesi — satır metninden otomatik dosya adı
    // -------------------------------------------------------------------

    private val turkishMap = mapOf(
        'ç' to 'c', 'Ç' to 'c', 'ğ' to 'g', 'Ğ' to 'g', 'ı' to 'i', 'I' to 'i',
        'İ' to 'i', 'ö' to 'o', 'Ö' to 'o', 'ş' to 's', 'Ş' to 's', 'ü' to 'u', 'Ü' to 'u'
    )

    /**
     * `assets/audio/VOICE_MANIFEST.md` dosyasındaki üretim algoritmasıyla
     * BİREBİR aynı olmalı — biri değişirse diğeri de güncellenmeli.
     */
    fun voiceIdFor(text: String): String {
        val sb = StringBuilder()
        for (ch in text) sb.append((turkishMap[ch] ?: ch).lowercaseChar())
        val stripped = sb.toString().replace(Regex("[^a-z0-9]+"), "_").trim('_')
        val base = stripped.take(40).trim('_')
        val hash = (text.hashCode() and 0xFFFFFF).toString(16).padStart(6, '0')
        return (if (base.isEmpty()) "line" else base) + "_" + hash
    }

    /**
     * GameScreen'in `advanceLine()` fonksiyonu her yeni satır gösterdiğinde
     * çağırır. Boş/çok kısa satırlar (== SON BAŞLIĞI == gibi) ve `null`/boş
     * metin için hiçbir şey yapmaz.
     */
    fun playVoiceForLine(text: String) {
        if (muted) return
        if (text.isBlank()) return
        if (text.startsWith("==")) return // "== GERÇEK SON ==" gibi başlıklar seslendirilmez
        val id = voiceIdFor(text)
        val cached = voiceCache[id]
        if (cached == null && voiceCache.containsKey(id)) return // daha önce "yok" olarak işaretlendi
        val sound = cached ?: run {
            val path = "audio/voice/$id.ogg"
            val handle = Gdx.files.internal(path)
            val loaded = if (handle.exists()) {
                try {
                    Gdx.audio.newSound(handle)
                } catch (e: Exception) {
                    Gdx.app.error("AudioManager", "Seslendirme yüklenemedi: $path", e)
                    null
                }
            } else {
                logMissingOnce("voice:$id", path)
                null
            }
            voiceCache[id] = loaded
            loaded
        }
        stopCurrentVoice()
        sound?.let {
            currentVoiceSound = it
            currentVoiceId = it.play(voiceVolume)
        }
    }

    /** Yeni bir satır başlarken bir öncekinin sesi hâlâ çalıyorsa keser (üst üste binmesin diye). */
    private fun stopCurrentVoice() {
        if (currentVoiceId >= 0L) {
            currentVoiceSound?.stop(currentVoiceId)
        }
        currentVoiceSound = null
        currentVoiceId = -1L
    }

    fun dispose() {
        loadedMusic.values.forEach { it.dispose() }
        loadedSfx.values.forEach { it.dispose() }
        voiceCache.values.forEach { it?.dispose() }
        loadedMusic.clear()
        loadedSfx.clear()
        voiceCache.clear()
    }
}
