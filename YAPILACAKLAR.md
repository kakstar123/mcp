# YAPILACAKLAR

Bu dosya, "Yeni Metin Belgesi.txt" içindeki 28 maddelik tasarım notuna göre
projenin şu anki durumunu takip eder. Her oturumda güncellenir; en son
değişiklik en üstte belirtilir.

## Son değişiklik
**#19 Aynalar** eklendi:
- `GameTypes.kt`: `MirrorKind` enum'u (`BATHROOM`, `GHOST_REFLECTION`, `FINALE`) ve `Mirror` veri sınıfı.
- `World.kt`: `buildMirrors()` — Banyo, Çocuk Odası ve Salon'a birer duvar aynası yerleştirir.
- `GameScreen.kt`: `updateMirrors`, `lookIntoMirror` ve üç alt senaryo:
  - **Banyo aynası** — korku bandına göre değişen atmosferik tuhaflıklar (sıradan → gecikmeli gülümseme → göz kırpma → yansımanın başka yöne bakması).
  - **Çocuk Odası aynası** — Mert oyuncudan uzaktaysa yansımada beliriyor, dönünce kayboluyor (tasarım notundaki "aynada Mert görünür, gerçekte yoktur" örneği).
  - **Salon'daki büyük ayna** — tüm anı sayfaları toplanmadan sıradan; toplandıktan sonra ilk bakışta `ChoiceHistory` özetini yansımada gösterir (tasarım notundaki "final aynası" fikri), sonraki bakışlarda kısa bir yankı bırakır.
- HUD: aynanın önündeyken "Ayna: bakmak için E" ipucu; giriş metnindeki tuş listesi güncellendi.
- `restart()` içine ayna durumlarının sıfırlanması eklendi.

## Tamamlananlar (tasarım notu madde numarasıyla)
- #10 Kapı sistemi — zamana/anahtara bağlı kilit, ilk açılışta seçim geçmişine göre farklı tepki.
- #13 Ölüm yerine "hafıza kaybı" — yakalanınca GAME OVER yerine bir anı sayfası unutuluyor.
- #15 NPC hafıza sistemi — Mert'in bodrum konusunda çelişkili/şüpheci repликleri.
- #18 NPC davranışlarının zamana göre değişmesi — Mert'in 03:33 sonrası farklı replikleri.
- #19 Aynalar — yukarıda detaylandırıldı.
- #21 Oyuncunun seçim geçmişi — `ChoiceHistory.kt`, final ekranında özet.
- #26 Gizli cesaret sistemi — `Courage.kt`, bazı diyalog satırlarını etkiliyor.
- #27 New Game+ (minimal) — ilk anı sayfasında ekstra satır, giriş metninde küçük fark.

## Henüz hiç dokunulmayanlar
- #1 Dinamik dünya sistemi (rastgele kilitli kapılar, oda içeriğinin değişmesi)
- #4 Telefon arayüzü (kamera/mesaj/harita/not sekmeleri)
- #5 Fotoğraf mekaniği (görünmeyen şeylerin fotoğrafta görünmesi)
- #6 Oynanabilir flashback sahneleri (anı parçasına dokununca başka karakteri kontrol etmek)
- #8 Dinamik hava sistemi
- #14 Fizik tabanlı bulmacalar
- #17 Haritanın yalan söylemesi
- #20 Döngü sistemi (aynı sahnenin küçük farklarla tekrarı)
- #22 Mikrofon/gerçek ses mekaniği
- #23 Deniz seviyesi sistemi
- #24 VHS / eski kamera filtresi
- #25 Mert ile ilişki sisteminin derinleştirilmesi (şu an sadece trust/pastThreshold var; şakalaşma/kavga gibi ayrı dallar yok)
- #28 Oyuncunun fark edemeyeceği küçük değişiklikler (aynı odanın eşya sayısının sessizce değişmesi)

## Benim yapamayacağım, kullanıcının yapması gereken
- **Gerçek derleme**: Bu ortamda internet erişimi LibGDX/Gradle bağımlılıklarını
  indiremiyor (yalnızca npm/pypi/crates/github gibi alan adlarına izin var).
  Kod hiç derlenip çalıştırılmadı. Kendi makinende
  `./gradlew lwjgl3:run` ile ilk çalıştırman gerekiyor; hata çıkarsa mesajı
  buraya yapıştır, birlikte düzeltiriz.
- **Görsel/ses varlıkları**: Her şey hâlâ basit kutu/küre modeller ve metin.
  Gerçek 3D modeller, dokular, müzik/ses dosyaları ayrı bir iştir.
- **Oynanış dengesi**: Korku bandı eşikleri, aynaların tetiklenme mesafeleri
  gibi sayılar tahminî; gerçek oynanışta ince ayar gerekebilir.

## Sıradaki adım için öneri
Bir sonraki maddeyi seçmek istersen: **#28** (fark edilemeyen küçük
değişiklikler) mevcut oda/eşya sistemine en az yeni altyapı gerektiren ve en
"ucuz" psikolojik etkiyi yaratan madde olduğu için iyi bir aday. **#6**
(oynanabilir flashback) ise en büyük yeni altyapıyı gerektirir (ayrı bir
"Aras" kontrol modu).
