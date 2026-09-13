# Korku Oyunu 3D

LibGDX + Kotlin ile yapılmış birinci şahıs 3D korku-gerilim oyunu. WASD ile
yürüyüp fareyle etrafına bakabildiğin, **geniş, çok odalı bir evde** geçen;
gizli köşelere saklanmış anıları toplayıp evin ve kendi geçmişinin gerçeğini
ortaya çıkarmaya çalıştığın bir deneyim.

## Hikaye

Saat 03:14. Elektrikler kesik. Kimsenin olmaması gereken bir evde
yalnızsın — ya da öyle sanıyorsun. Evin her köşesine, geçmişini anlatan
**6 "anı sayfası"** gizlenmiş. Bazıları masum bir çekmecede, bazıları
aynanın arkasında, bazıları oyuncak sandığının dibinde. Ama bazı gizli
yerlerin bir bekçisi var.

**Amaç:** Evdeki 6 anı sayfasının tamamını bulmak, bodrumun anahtarını
ele geçirmek ve bodruma inip gerçeği tam olarak öğrenmek (**GERÇEK SON**).
Anahtarı bulup yeterli anıyı toplamadan bodruma inersen eksik, belirsiz
bir son yaşarsın (**YARIM SON**). İstersen hiç uğraşmadan ön kapıdan
kaçmayı da deneyebilirsin — ama gerçeği hiç öğrenemezsin (**KAÇIŞ SONU**
ya da **YAKALANDIN**).

Sayfaları topladıkça evde yalnız olmadığını fark edeceksin.

## Karakterler / Jumpscare ihtimalleri

Oyun boyunca karşına çıkabilecek korku unsurları:

| Karakter | Nerede | Ne zaman |
| --- | --- | --- |
| **Islak Şey** | Mutfak | Mutfağa ilk girdiğin an, sabit sahne |
| **Ayna Kadını** | Banyo | Aynanın arkasındaki anı sayfasını alırken |
| **Gölge Adam** | Çalışma Odası | Kitaplığın arkasındaki anı sayfasını alırken |
| **Oyuncak Bebek** | Çocuk Odası | Oyuncak sandığındaki anı sayfasını alırken |
| **Yakalayan El** | Ön Kapı | Kapıya yaklaşınca, QTE ile kaçman gerekir |
| **Takipçi** | Evin her yeri | **Rastgele!** 2+ anı topladıktan sonra aktive olur, evin içinde belirip kaybolur; sana çok yaklaşırsan seni yakalar ve yatak odasına sürükler (oyunu bitirmez, sadece korkutur ve geciktirir) |
| **Eski Sen** | Bodrum | Gerçek Son'da karşına çıkan final figürü |

Takipçi, klasik bir "her an her yerde olabilir" mekaniği ile çalışır:
belirli aralıklarla evin rastgele bir noktasında (sana çok yakın ya da
çok uzak olmayan bir mesafede) beliriyor, birkaç saniye kalıp kayboluyor.
O anda ona çok yaklaşırsan jump-scare tetiklenir.

## Kontroller
- **WASD** — yürü
- **Fare** — etrafına bak
- **T** — feneri aç/kapat
- **E** — Mert'e yakınken onunla konuş
- **ESC** — fareyi pencereden serbest bırak / tekrar yakala
- **SPACE / ENTER** — anlatım metnini ilerlet
- **1 / 2** — Mert diyaloglarında iki seçenekten birini seç
- **F / G / H / J** — QTE (hızlı-tepki) anında ekranda hangisi yazıyorsa ona bas
- **R** — bir sona ulaştıktan sonra yeniden başla

## Yeni sistemler: korku/algı, fener ve Mert (NPC + güven)

### Görünmez korku/algı sistemi (`Fear.kt`)
Evin'in görünmez bir korku değeri (0-100) var. Takipçi görünürken ve
yaklaşırken yükselir, güvendeyken yavaşça düşer. Oyuncuya hiçbir zaman
sayı ya da "halüsinasyon görüyorsun" gibi bir mesaj gösterilmez —
sadece bantlara göre etkiler hissettirilir:
- **0-25 (normal):** ortam ışığı normal.
- **25-50:** ortam biraz kararır, ara sıra fısıltı metinleri belirir (ekranın üstünde, kısa süreliğine).
- **50-75:** ortam belirgin şekilde kararır, fısıltılar sıklaşır, hafif kırmızımsı bir vinyet (kenar koyulaşması) belirir.
- **75-100:** ortam çok kararır, vinyet güçlenir ve birkaç saniyeliğine **kontrolün Evin'in elinden çıktığı** anlar olabilir (kamera kendiliğinden döner, hafif sarsılma) — WASD o an çalışmaz.

### Fener sistemi (`FlashlightChild.kt`, `Player.forwardXZ`)
Fener **T** ile açılıp kapanıyor ve sınırlı bir pili var (açıkken azalır,
kapalıyken dolar; biterse otomatik söner — HUD'da "Pil: %" görünür).
Fener kapalıyken ortam neredeyse simsiyah. Ayrıca 3. anı sayfasından
sonra aktive olan bir **"Fener Çocuğu"** var: fener kapalıyken hiçbir
şey yok; her kapatıp-açtığında (bir "blink") figür bir adım daha
yaklaşıyor — önce uzak bir koridorda, sonra gittikçe daha yakında,
son adımda ise artık **tam arkanda** beliriyor ve bir jump-scare
tetikliyor. Sonrasında uzun süreliğine tamamen pasifleşiyor.

### Mert — NPC + güven sistemi (`Mert.kt`)
Mert artık sahnede duran, seni gecikmeli olarak takip eden gerçek bir
karakter. En az 1 anı sayfası topladıktan sonra ona yaklaşıp **E**'ye
basınca bodrumla ilgili bir soru soruyor; **1** ile gerçeği söylersin
(güven +15), **2** ile yalan söylersin (güven -15, bir bayrak set edilir).
4. anı sayfasından sonra Mert, güven durumuna göre farklı bir replik
söylüyor (yüksek güvende destekleyici, düşükte kendi hafızasından şüphe
eden, paranoyakça bir replik — "NPC hafıza karmaşası" hissi). Güven,
ön kapı QTE'sinde Mert'in yardımını (daha uzun tepki süresi) ve
GERÇEK SON'daki final sahnesinin küçük bir ayrıntısını değiştiriyor.

Bu üç sistem birbirinden bağımsız çalışacak şekilde ayrı dosyalarda
tutuldu (`Fear.kt`, `FlashlightChild.kt`, `Mert.kt`) — yeni bir NPC ya
da başka bir "sadece fenerle görünen" varlık eklemek isterseniz bu
dosyalar örnek alınabilir.

HUD'da her zaman kaç anı sayfası topladığını ("Anılar: x/6") ve anahtarı
alıp almadığını görebilirsin.

## ÖNEMLİ — okumadan başlamayın

Bu proje, internet erişimi ve ekran/GPU çıkışı olmayan bir ortamda
yazıldı. Yani:
- **Burada derlenip çalıştırılamadı.** Kodu satır satır, LibGDX API'lerini
  tek tek kontrol ederek elle yazdım, ama ilk `./gradlew` çalıştırmanda
  küçük bir versiyon uyumsuzluğu ya da unutulmuş bir import çıkarsa
  **hata mesajını bana gönder, hemen düzeltirim.**
- İlk çalıştırmada Gradle, LibGDX/LWJGL3 kütüphanelerini indirecek
  (birkaç yüz MB) — bunun için **internet bağlantısı gerekiyor.**
- Texture/3D model dosyası yok — tüm yüzeyler düz renkli kutular
  (prosedürel geometri), Takipçi de dahil. Gerçekçi görünüm değil ama
  tam gezilebilir 3D ve fonksiyonel bir korku deneyimi.

## Nasıl çalıştırılır

### IntelliJ IDEA (önerilen)
1. `korku-oyunu-3d` klasörünü **File > Open** ile aç.
2. Gradle senkronize olsun (internet ister, biraz sürebilir).
3. `lwjgl3/src/.../Lwjgl3Launcher.kt` dosyasını aç, `main` fonksiyonunun
   yanındaki yeşil oka bas.

### Komut satırı
```
gradle wrapper
./gradlew lwjgl3:run
```

## Kat planı ve hikaye akışı

Ev artık tek katlı ama çok daha geniş — 10 farklı bölümden oluşuyor:

```
                         Banyo
                           |  (batı kapı)
Yatak Odası (spawn) -- Koridor 1
                           |
        Çocuk Odası -- Kavşak ------------------------
        (kuzeybatı kapı)  |                           |
                     (batı kapı)                 (doğu kapı)
                       Mutfak                  Çalışma Odası
                     [altın anahtar]
                           |
                       Koridor 2
                           |
                         Salon
                    +-- (sol geçit) Ön Kapı bölümü
                    |      -> jump-scare + QTE
                    |         başarılı: "KAÇIŞ SONU"
                    |         başarısız: "YAKALANDIN"
                    +-- (sağ geçit) Bodrum Kapısı
                           -> anahtar + 6/6 anı: "GERÇEK SON"
                           -> anahtar + eksik anı: "YARIM SON"
                           -> anahtar yoksa: "kilitli" mesajı
```

**Gizli anı sayfaları (her biri hikayenin bir parçasını anlatır):**

| # | Konum | Saklandığı yer |
| --- | --- | --- |
| 1 | Yatak Odası | Yatağın altı |
| 2 | Banyo | Ayna dolabının arkası (Ayna Kadını burada!) |
| 3 | Mutfak | Lavabo altındaki dolap |
| 4 | Çalışma Odası | Kitaplığın arkasındaki boşluk (Gölge Adam burada!) |
| 5 | Çocuk Odası | Oyuncak sandığı (Oyuncak Bebek burada!) |
| 6 | Salon | Şömine rafının altındaki gizli bölme |

Tüm koordinatlar, çarpışma kutuları, anı sayfaları ve tetikleyiciler
`World.kt` içinde. Kod dosyaları:

```
core/src/main/kotlin/com/korku/game3d/
├── GameTypes.kt      -> Trigger / TriggerKind / Phase / Collectible tipleri
├── World.kt           -> Duvarlar, odalar, anahtar, anı sayfaları, tetikleyiciler (İÇERİK burada)
├── Monster.kt          -> "Takipçi": dolaşan, olası jumpscare yaratan canavar AI'ı
├── Player.kt           -> Kamera, WASD hareket, fare bakışı, çarpışma
├── GameScreen.kt        -> Ana döngü: render, hikaye durumu, QTE, metin kutusu, HUD
└── KorkuGame3D.kt        -> Game sınıfı, başlangıç ekranını ayarlar

lwjgl3/src/main/kotlin/com/korku/game3d/lwjgl3/
└── Lwjgl3Launcher.kt      -> Masaüstü pencere başlatıcı (main fonksiyonu)
```

## Yeni sahne / oda eklemek

`World.kt`'deki `wallX(x1, x2, z, ...)` ve `wallZ(x, z1, z2, ...)`
yardımcı fonksiyonlarıyla yeni bir oda çizip, `buildTriggers()`
içine yeni bir `Trigger(...)` ekleyip `GameScreen.updateExploring()`
içindeki `when (trigger.kind)` bloğuna yeni bir `TriggerKind` dalı
eklemen yeterli. Yeni bir gizli anı/eşya eklemek için `buildMemoryPages()`
içine `addPage(...)` çağrısı eklemen yeterli — istersen `jumpscareCharacter`
parametresiyle yanına bir korku karakteri de ekleyebilirsin.

## Not — eklenen diyalog/atmosfer parçaları
Kullanıcının paylaştığı "Evin — Bodrumdaki Son Gece" senaryosundan, evin
kurgusuna oturan birkaç motif uyarlandı (okul/karakterler değiştirilmedi,
sadece dil/atmosfer):
- Açılış: "Anne?" fısıltısı ve oyunun asıl korkusunun "unutulmak" olduğu vurgusu.
- Anı sayfaları: "diğer sen / hatırlanmayı bekleyen" temasıyla zenginleştirildi.
- Takipçi yakaladığında kulağa fısıldanan "Anne..." sesi.
- GERÇEK SON'da "eski sen" ile geçen kısa diyalog ve kapanış cümlesi
  ("Bazı yerlerde hayaletler ölüler değildir. Hatırlanmayı bekleyenlerdir.").
- YARIM SON, KAÇIŞ SONU ve YAKALANDIN sonlarına birer atmosfer cümlesi eklendi.

## Sıradaki geliştirme fikirleri
- Gerçek ses efektleri (`javax.sound` ya da LibGDX `Sound`/`Music` API'si —
  bunlar da .wav/.ogg dosyası gerektirir, indirmen gerekir); özellikle
  Takipçi belirdiğinde bir kalp atışı/nefes sesi çok işe yarar.
- Texture'lı duvarlar/zemin (kendi görsellerini `assets/` klasörüne
  koyup `TextureAttribute` ile Material'e eklemen yeterli).
- Takipçi için gerçek pathfinding (şu an oda-merkezi noktalarında
  "ışınlanarak" beliriyor; A* ile koridorlardan yürüyerek gelmesi
  gerilimi artırır).
- Bodrumun kendisini de gezilebilir bir oda yapmak (şu an sadece metinle
  anlatılıyor) ve "Eski Sen" için görünür bir model eklemek.
- İkinci kat / tavan arası ekleyip evi daha da büyütmek.
