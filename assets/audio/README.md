# Ses sistemi — müzik, efekt ve diyalog seslendirme

Oyuna geniş bir müzik + efekt + diyalog seslendirme altyapısı eklendi
(`core/.../AudioManager.kt`, `GameScreen.kt` içine bağlandı). **Ama bu
depoda hiçbir ses dosyası yok** — bu ortamda internet erişimi kapalı
olduğu ve ben ses/müzik üretemediğim için gerçek `.ogg` dosyalarını
kendin eklemen gerekiyor. Sistem, dosya bulamadığında hiçbir hata
vermeden sessizce atlar; yani şu an hiç ses dosyası koymadan da oyun
sorunsuz çalışır (sadece sessizdir). Dosyaları buraya ekledikçe kod
tarafında **hiçbir değişiklik yapmadan** otomatik olarak devreye girer.

Format: **`.ogg`** (Vorbis) kullan — LibGDX'in üç platformda da en
sorunsuz çalışan formatı budur. mp3/wav de teknik olarak çalışır ama
ogg önerilir.

## Klasör yapısı

```
assets/audio/
├── music/     -> döngülü arkaplan müzikleri (8 durum, aşağıda liste)
├── sfx/       -> kısa efektler (sıçrama anları, alma sesleri, QTE, vb.)
└── voice/     -> diyalog/anlatım seslendirmeleri (satır başına 1 dosya)
```

## 1) `music/` — arkaplan müziği (8 dosya)

Oyun, korku bandına (Fear.kt) ve Takipçi'nin görünürlüğüne göre bu
parçalar arasında ~1.4 saniyelik yumuşak bir geçiş (crossfade) yapar.
Hepsi **döngülü (loop)** çalınır — sadece `ending_*` olanlar tek sefer
çalınır ve döngüsüzdür.

| Dosya adı | Ne zaman çalar |
| --- | --- |
| `ambient_calm.ogg` | Normal, sakin gezinme |
| `ambient_uneasy.ogg` | Korku bandı hafif yükselmiş |
| `ambient_distorted.ogg` | Korku bandı belirgin yüksek |
| `ambient_hallucinating.ogg` | Korku bandı zirvede (kontrol kaybı anları) |
| `chase.ogg` | Takipçi görünürken (bant ne olursa olsun önceliklidir) |
| `ending_true.ogg` | GERÇEK SON |
| `ending_half.ogg` | YARIM SON |
| `ending_escape.ogg` | KAÇIŞ SONU (QTE başarılı) |
| `ending_caught.ogg` | YAKALANDIN (QTE başarısız) |

Öneri: `ambient_calm` çok minimal/sessize yakın, `chase` ve
`ending_caught` en yoğun/gerilimli olanlar olsun — bant yükseldikçe
müzik de kademeli olarak yoğunlaşsın.

## 2) `sfx/` — kısa efektler (21 dosya)

| Dosya adı | Ne zaman çalar |
| --- | --- |
| `jumpscare_generic.ogg` | Mutfak sıçraması, ön kapı anı, genel sıçramalar |
| `jumpscare_mirror.ogg` | Ayna Kadını / ayna sıçramaları |
| `jumpscare_shadow.ogg` | Gölge Adam sıçraması |
| `jumpscare_doll.ogg` | Oyuncak Bebek sıçraması |
| `jumpscare_flashlight_child.ogg` | Fener Çocuğu son adımda belirdiğinde |
| `page_pickup.ogg` | Bir anı sayfası bulunduğunda |
| `key_pickup.ogg` | Bodrum anahtarı alındığında |
| `door_unlock.ogg` | Kilitli bir kapının kilidi açıldığında |
| `mirror_look.ogg` | Bir aynaya E ile bakıldığında (jumpscare öncesi genel ses) |
| `flashlight_click.ogg` | Fener T ile açılıp kapandığında |
| `hide_enter.ogg` / `hide_exit.ogg` | C ile saklanmaya girme/çıkma |
| `qte_start.ogg` | QTE başladığında |
| `qte_success.ogg` / `qte_fail.ogg` | QTE sonucu |
| `monster_catch.ogg` | Takipçi oyuncuyu yakaladığında |
| `whisper_1.ogg`, `whisper_2.ogg`, `whisper_3.ogg` | Rastgele fısıltı anları (biri rastgele seçilir) |
| `footstep_close.ogg` | "Ayak sesleri yaklaşıyor..." uyarısı |
| `ui_choice.ogg` | Mert diyaloğunda 1/2 seçeneği seçildiğinde |

## 3) `voice/` — diyalog / anlatım seslendirmesi (otomatik eşleşme!)

Bunu elle bağlamana gerek yok. `GameScreen.advanceLine()` ekrana
gelen **her** metin satırı için (giriş anlatımı, Mert'in tüm
replikleri, anı sayfası metinleri, sonlar, vb.) metnin kendisinden
otomatik bir dosya adı üretir (`AudioManager.voiceIdFor`) ve
`voice/<o_ad>.ogg` dosyasını arar.

Hangi metnin hangi dosya adına karşılık geldiğini görmek için
**`VOICE_MANIFEST.md`** dosyasına bak — oradaki 109 satırı seslendirip
belirtilen tam dosya adıyla bu klasöre koyman yeterli. Hepsini
seslendirmek zorunda değilsin: hangi satırlar için dosya eklersen
sadece onlar seslenir, kalanı sessiz anlatım metni olarak kalır
(oyun bunu sorun etmez).

**Not:** Metni oyun kodunda değiştirirsen (`GameScreen.kt`,
`Mert.kt`, `World.kt`), o satırın dosya adı da değişir — bu yüzden
manifestoyu güncel tutmak için metni değiştirdikten sonra
`VOICE_MANIFEST.md`'yi yeniden üretmen gerekir (aynı slugify
algoritmasıyla; script'i tekrar yazdırmamı istersen söylemen yeterli).

## Ses dosyalarını nereden bulabilirsin

- Kendi sesini kaydetmek (Mert ve anlatıcı için en özgün sonucu verir).
- Ücretsiz/CC0 ses efekti kütüphaneleri: freesound.org, opengameart.org,
  pixabay.com/sound-effects — indirdiğin dosyayı `.ogg`'a çevirip
  (ffmpeg: `ffmpeg -i giris.mp3 cikis.ogg`) doğru dosya adıyla kaydet.
- Metinden sese (TTS) araçları: kendi bilgisayarında bir TTS aracı
  kullanarak `VOICE_MANIFEST.md`'deki metinleri toplu şekilde
  seslendirebilirsin.
- Müzik için: yine opengameart.org/freesound.org'un "music" bölümleri,
  ya da kendi bilgisayarında bir müzik üretim aracıyla oluşturduğun
  loop'lar.

Bu ortamın internet erişimi sadece npm/pypi/crates/github gibi paket
kayıt sistemlerine izin veriyor (ses/müzik indirme sitelerine değil),
bu yüzden bu adımları kendi makinende yapman gerekiyor.
