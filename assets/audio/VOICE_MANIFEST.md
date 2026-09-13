# Diyalog Seslendirme Manifestosu

Bu dosya, oyundaki metinlerin otomatik olarak ürettiği ses dosyası adlarını listeler. `GameScreen`in `advanceLine()` fonksiyonu, ekranda gösterilen HER satır için bu tabloya göre `assets/audio/voice/<dosya_adi>.ogg` dosyasını arar; dosya varsa çalar, yoksa sessizce atlar (oyun asla çökmez).

**Nasıl kullanılır:** Aşağıdaki metni seslendirin (kendiniz seslendirin, bir seslendirme sanatçısına verin ya da bir metin-okuma/TTS aracından geçirin), her satırı MP3 değil **.ogg** formatında, tam olarak belirtilen dosya adıyla `assets/audio/voice/` klasörüne koyun. Dosya adları metne göre otomatik türetildiği için burada elle değiştirmeyin; metni değiştirirseniz (`GameScreen.kt`/`Mert.kt`/`World.kt` içinde), dosya adı da değişir — bu yüzden manifestoyu yeniden üretmek gerekir.

Not: `Anı: x/6`, `Topladığın x/6 anı yetersiz...` gibi sayıya göre değişen birkaç durum satırı kasıtlı olarak bu listede yok; bunlar kısa HUD/durum satırları, seslendirilmesi gerekmiyor. İstersen onlar için de oyun otomatik olarak aynı slugify sistemiyle bir dosya adı arayacaktır, sadece bu manifestoda yazılı değiller.

| Bölüm | Dosya adı | Metin |
|---|---|---|
| Giris (intro) | `anne_c07c29.ogg` | "Anne?" |
| Giris (intro) | `cevap_yok_sadece_duvar_saatinin_tik_takl_97cd2c.ogg` | Cevap yok. Sadece duvar saatinin tik takları. |
| Giris (intro) | `saat_03_14_elektrikler_kesik_eee234.ogg` | Saat 03:14. Elektrikler kesik. |
| Giris (intro) | `dairende_yalniz_degilsin_mert_de_burada_61a1ed.ogg` | Dairende yalnız değilsin — Mert de burada. Bir yerlerden metalik, ıslak bir ses geliyor. |
| Giris (intro) | `aslinda_en_buyuk_korkun_karanlik_degil_u_dc85c3.ogg` | Aslında en büyük korkun karanlık değil. Unutulmak. |
| Giris (intro) | `evin_her_kosesine_gizlenmis_6_ani_sayfas_33ee67.ogg` | Evin her köşesine gizlenmiş 6 anı sayfasını bul, gerçeği öğren. |
| Giris (intro) | `bodrumun_anahtari_bir_yerlerde_ama_yalni_8d38f0.ogg` | Bodrumun anahtarı bir yerlerde. Ama yalnız olmayabilirsin... |
| Giris (intro) | `wasd_hareket_fare_bak_t_fener_e_konus_ay_f12325.ogg` | WASD: hareket, Fare: bak, T: fener, E: konuş/aynaya bak, C: saklan, ESC: fareyi serbest bırak. |
| Fener Cocugu | `feneri_actiginda_tam_arkanda_duran_bir_s_1f7a3e.ogg` | Feneri açtığında, tam arkanda duran bir şey görüyorsun. |
| Fener Cocugu | `dondugunde_hicbir_sey_yok_7e743f.ogg` | Döndüğünde hiçbir şey yok. |
| Ayna - genel | `aynaya_bakiyorsun_ccf6ec.ogg` | Aynaya bakıyorsun. |
| Ayna - banyo normal | `aynaya_bakiyorsun_yorgun_gorunuyorsun_he_067651.ogg` | Aynaya bakıyorsun. Yorgun görünüyorsun, hepsi bu. |
| Ayna - banyo uneasy | `yansiman_bir_tik_gec_gulumsuyor_sen_gulu_78e40e.ogg` | Yansıman bir tık geç gülümsüyor. Sen gülümsemedin. |
| Ayna - banyo distorted | `yansimandaki_gozler_seninkilerden_bir_an_b6c2c7.ogg` | Yansımandaki gözler seninkilerden bir an önce kırpıyor. |
| Ayna - banyo distorted | `sonra_her_sey_normale_donuyor_ya_da_oyle_0592df.ogg` | Sonra her şey normale dönüyor. Ya da öyle olduğuna inanmak istiyorsun. |
| Ayna - banyo hallucinating | `yansiman_sana_bakmiyor_bosluga_senin_ark_8aaed3.ogg` | Yansıman sana bakmıyor; boşluğa, senin arkana bakıyor. |
| Ayna - banyo hallucinating | `donup_arkana_bakiyorsun_hicbir_sey_yok_y_411d33.ogg` | Dönüp arkana bakıyorsun. Hiçbir şey yok. Yansıman hâlâ oraya bakıyor. |
| Ayna - hayalet (Mert yakin) | `aynaya_bakiyorsun_sadece_kendini_goruyor_41d743.ogg` | Aynaya bakıyorsun. Sadece kendini görüyorsun, Mert de yanında duruyor. |
| Ayna - hayalet ilk kez | `ama_yansimanda_yalniz_degilsin_mert_heme_62bec5.ogg` | Ama yansımanda yalnız değilsin — Mert hemen arkanda duruyor. |
| Ayna - hayalet ilk kez | `donup_bakiyorsun_arkanda_kimse_yok_0a9a0b.ogg` | Dönüp bakıyorsun. Arkanda kimse yok. |
| Ayna - hayalet ilk kez | `aynaya_tekrar_baktiginda_mert_de_kaybolu_6ab86c.ogg` | Aynaya tekrar baktığında Mert de kayboluyor. |
| Ayna - hayalet tekrar | `aynaya_bakiyorsun_bu_sefer_arkanda_kimse_a88a76.ogg` | Aynaya bakıyorsun. Bu sefer arkanda kimse yok — ne aynada, ne gerçekte. |
| Ayna - final eksik | `aynaya_bakiyorsun_henuz_net_degil_sanki_c9d4e0.ogg` | Aynaya bakıyorsun. Henüz net değil — sanki hâlâ eksik bir şeyler var. |
| Ayna - final tekrar | `aynaya_tekrar_bakiyorsun_kararlarin_h_l_8b81fa.ogg` | Aynaya tekrar bakıyorsun. Kararların hâlâ orada, sessizce sıralı duruyor. |
| Ayna - final ilk | `yansimanda_sen_degil_bu_gece_boyunca_ald_802296.ogg` | Yansımanda sen değil, bu gece boyunca aldığın kararlar beliriyor, art arda: |
| Ayna - final ilk | `sonra_yansima_yeniden_sadece_sen_oluyor_e857b8.ogg` | Sonra yansıma yeniden sadece sen oluyor. |
| Kapi - cocuk odasi yalan | `cocuk_odasi_nin_kapisi_gicirdayarak_acil_aeb20f.ogg` | Çocuk Odası'nın kapısı gıcırdayarak açılıyor. Yataklardan biri hâlâ toplanmamış — tıpkı Mert'e "hiç inmedik" dediğin gece gibi. |
| Kapi - cocuk odasi gercek | `cocuk_odasi_nin_kapisi_aciliyor_bir_an_i_8093ac.ogg` | Çocuk Odası'nın kapısı açılıyor. Bir an içerisi, Mert'e gerçeği söylediğin geceki kadar tanıdık geliyor. |
| Kapi - cocuk odasi normal | `cocuk_odasi_nin_kapisi_gicirdayarak_acil_fb09c9.ogg` | Çocuk Odası'nın kapısı gıcırdayarak açılıyor. |
| Kapi - banyo unuttu | `saat_03_33_banyo_kapisi_kendiliginden_ar_2fb2aa.ogg` | Saat 03:33. Banyo kapısı kendiliğinden aralanıyor. Aynadaki yansıma, az önce unuttuğun anı gibi bulanık. |
| Kapi - banyo yalan | `saat_03_33_banyo_kapisi_kendiliginden_ar_466864.ogg` | Saat 03:33. Banyo kapısı kendiliğinden aralanıyor. Aynaya bakıyorsun; söylediğin yalanı biliyormuş gibi bakıyor sana. |
| Kapi - banyo normal | `saat_03_33_u_gecti_banyo_kapisi_kendilig_1e8a59.ogg` | Saat 03:33'ü geçti; banyo kapısı kendiliğinden aralanıyor. |
| Mert - bodrum sorusu | `mert_yanina_yaklasiyor_b4eca0.ogg` | Mert yanına yaklaşıyor. |
| Mert - bodrum sorusu | `mert_evin_dun_gece_bodruma_indigimizi_ha_1cb747.ogg` | MERT: "Evin, dün gece bodruma indiğimizi hatırlıyor musun?" |
| Mert - gercek soylersen | `mert_iyi_ki_hatirliyorsun_ben_de_biliyor_761bdc.ogg` | MERT: "İyi ki hatırlıyorsun. Ben de biliyordum ki delirmemişim." |
| Mert - yalan soylersen | `mert_emin_misin_neyse_bos_ver_e00919.ogg` | MERT: "...Emin misin? Neyse. Boş ver." |
| Mert - 4.anidan sonra | `mert_saat_kac_oldu_bilmiyorum_artik_evin_2e0cfe.ogg` | MERT: "Saat kaç oldu bilmiyorum artık Evin. Ama sana söz verdim, gitmiyorum." |
| Mert - 4.anidan sonra | `mert_az_once_ne_konustuk_biz_bos_ver_ses_577f24.ogg` | MERT: "Az önce ne konuştuk biz? ...Boş ver. Sesin bile farklı geliyor kulağıma." |
| Mert - 4.anidan sonra | `mert_sana_guveniyorum_evin_ne_olursa_ols_e9bb39.ogg` | MERT: "Sana güveniyorum Evin. Ne olursa olsun yanındayım." |
| Mert - 4.anidan sonra | `mert_bodrumda_ne_gordugumuzu_kimseye_soy_194e84.ogg` | MERT: "Bodrumda ne gördüğümüzü kimseye söyleme demiştim sanki... yoksa söylemedim mi? Artık emin değilim." |
| Mert - 4.anidan sonra | `mert_bir_seyler_hatirlamiyormusum_gibi_h_597cc1.ogg` | MERT: "Bir şeyler hatırlamıyormuşum gibi hissediyorum. Sen de mi?" |
| Mert - idle once | `mert_iyi_olacagiz_degil_mi_sadece_elektr_f49890.ogg` | MERT: "İyi olacağız, değil mi? Sadece elektrikler gitti." |
| Mert - idle once | `mert_sesi_duydun_mu_sen_de_yoksa_ben_mi_fe921c.ogg` | MERT: "Sesi duydun mu sen de yoksa ben mi hayal ediyorum?" |
| Mert - idle once | `mert_yanindan_ayrilmayacagim_soz_52fc7b.ogg` | MERT: "Yanından ayrılmayacağım, söz." |
| Mert - idle sonra guven | `mert_buraya_ne_zaman_geldigimi_hatirlami_307f34.ogg` | MERT: "Buraya ne zaman geldiğimi hatırlamıyorum Evin. Ama yanındayım." |
| Mert - idle sonra guven | `mert_saat_kac_simdi_bos_ver_onemli_degil_a667d8.ogg` | MERT: "Saat kaç şimdi? Boş ver, önemli değil. Sen iyi misin?" |
| Mert - idle sonra guven | `mert_bir_sey_soylemek_istedim_ama_unuttu_f3a9d6.ogg` | MERT: "Bir şey söylemek istedim ama... unuttum. Tuhaf." |
| Mert - idle sonra guvensiz | `mert_sen_misin_evin_sesini_taniyamadim_b_00f5d8.ogg` | MERT: "Sen misin, Evin? Sesini... tanıyamadım bir an." |
| Mert - idle sonra guvensiz | `mert_bir_sey_sormustum_sana_cevap_vermem_818baa.ogg` | MERT: "Bir şey sormuştum sana. Cevap vermemiştin. Ya da vermiş miydin?" |
| Mert - idle sonra guvensiz | `mert_neden_bana_oyle_bakiyorsun_71b97a.ogg` | MERT: "Neden bana öyle bakıyorsun?" |
| Mert - idle once cesur | `mert_sen_hic_korkmuyor_musun_ben_bacakla_1a86d7.ogg` | MERT: "Sen hiç korkmuyor musun? Ben bacaklarımı hissetmiyorum bile." |
| Mert - idle once cesur | `mert_beni_bu_kadar_sakin_tutan_senin_dur_f7d092.ogg` | MERT: "Beni bu kadar sakin tutan senin duruşun, biliyor musun?" |
| Mert - idle sonra cesur | `mert_saat_kac_bilmiyorum_ama_sen_h_l_dim_ad3a66.ogg` | MERT: "Saat kaç bilmiyorum ama sen hâlâ dimdik duruyorsun. Bu bir şey ifade ediyor olmalı." |
| Mert - idle sonra cesur | `mert_korkuyorum_evin_ama_sen_korkmuyor_g_726946.ogg` | MERT: "Korkuyorum Evin. Ama sen korkmuyor gibisin. Bu tuhaf bir şekilde beni rahatlatıyor." |
| Anahtar | `elini_uzatip_anahtari_aliyorsun_b60ed2.ogg` | Elini uzatıp anahtarı alıyorsun. |
| Anahtar | `bu_bodrum_kapisinin_anahtarina_benziyor_680dcf.ogg` | Bu, bodrum kapısının anahtarına benziyor. |
| Anilar - page_bedroom | `yatagin_altinda_tozlu_bir_sayfa_buluyors_ad657d.ogg` | Yatağın altında, tozlu bir sayfa buluyorsun. |
| Anilar - page_bedroom | `kenarina_kucuk_cocuksu_bir_el_yazisiyla_3e97d3.ogg` | Kenarına küçük, çocuksu bir el yazısıyla bir isim karalanmış: senin adın. |
| Anilar - page_bedroom | `ilk_ani_burada_buyudum_bu_oda_hep_benimd_6e6a49.ogg` | "...ilk anı: burada büyüdüm. Bu oda hep benimdi." |
| Anilar - page_bathroom | `ayna_dolabini_aciyorsun_arkasinda_katlan_4c4a84.ogg` | Ayna dolabını açıyorsun. Arkasında katlanmış bir sayfa var. |
| Anilar - page_bathroom | `aynada_senin_degil_baska_birinin_yansima_4ca076.ogg` | Aynada, senin değil başka birinin yansımasını görüyorsun — bir an için. Gülümsüyor gibi. |
| Anilar - page_bathroom | `ikinci_ani_aynada_beni_gordugunde_o_zate_4d0eb1.ogg` | "...ikinci anı: aynada beni gördüğünde, o zaten çoktan gitmişti. Ama bir parçası hep burada kaldı." |
| Anilar - page_kitchen | `lavabonun_altindaki_dolapta_islanmis_ama_a59039.ogg` | Lavabonun altındaki dolapta, ıslanmış ama okunabilir bir sayfa var. |
| Anilar - page_kitchen | `ucuncu_ani_o_gece_mutfakta_bir_sey_bozul_377de0.ogg` | "...üçüncü anı: o gece mutfakta bir şey bozuldu. Sadece boru değildi." |
| Anilar - page_study | `kitapligi_iteklediginde_arkasinda_dar_bi_c1f3ba.ogg` | Kitaplığı iteklediğinde arkasında dar bir boşluk açılıyor. |
| Anilar - page_study | `bosluktaki_karanlikta_bir_sey_kipirdiyor_c66316.ogg` | Boşluktaki karanlıkta bir şey kıpırdıyor, sonra sayfayı bırakıp geri çekiliyor. |
| Anilar - page_study | `dorduncu_ani_bu_evi_hic_terk_etmedim_hat_b0e693.ogg` | "...dördüncü anı: bu evi hiç terk etmedim. Hatırlanmayı bekledim, çalışma odasında saklanarak." |
| Anilar - page_kidsroom | `oyuncak_sandigini_aciyorsun_bir_bebegin_b3216d.ogg` | Oyuncak sandığını açıyorsun. Bir bebeğin altında sayfa duruyor. |
| Anilar - page_kidsroom | `bebek_sen_bakmiyorken_bir_tik_daha_sana_261c0a.ogg` | Bebek, sen bakmıyorken bir tık daha sana dönmüş gibi. |
| Anilar - page_kidsroom | `besinci_ani_burada_bir_cocuk_vardi_adini_c42477.ogg` | "...beşinci anı: burada bir çocuk vardı. Adını unuttular. Ama oyuncakları hâlâ onu bekliyor." |
| Anilar - page_livingroom | `somine_rafinin_altinda_gizli_bir_bolme_b_4c7d38.ogg` | Şömine rafının altında gizli bir bölme buluyorsun. Son sayfa burada. |
| Anilar - page_livingroom | `son_ani_bodrumda_seni_bekleyen_senden_on_9886a6.ogg` | "...son anı: bodrumda seni bekleyen, senden önce buraya kilitlenen kişiydi. Şimdi sıra sende değil — hep sendeydi." |
| Anilar - page_livingroom | `sayfanin_altinda_son_bir_cumle_var_beni_c4c5da.ogg` | Sayfanın altında son bir cümle var: "Beni hatırladığın sürece kaybolmayacağım." |
| Anilar - unutulan tekrar | `bunu_daha_once_de_bulmustun_simdi_ilk_ke_387544.ogg` | Bunu daha önce de bulmuştun. Şimdi ilk kez buluyormuşsun gibi geliyor. |
| Anilar - unutulan tekrar | `mert_bunu_elinde_daha_once_de_gormustum_2b8ba3.ogg` | MERT: "Bunu elinde daha önce de görmüştüm sanki... yoksa görmedim mi? Bilmiyorum artık." |
| Anilar - New Game+ | `yazinin_altinda_cok_daha_soluk_bir_el_ya_d159af.ogg` | Yazının altında, çok daha soluk bir el yazısıyla: "1998'de de buradaydım." |
| Mutfak jumpscare | `lavabo_tasmis_yerde_kahverengi_bir_sivi_43c221.ogg` | Lavabo taşmış, yerde kahverengi bir sıvı birikmiş. |
| Mutfak jumpscare | `ve_o_sivi_hareket_ediyor_d291dd.ogg` | Ve o sıvı... hareket ediyor. |
| On kapi - guven+esik | `mert_elini_tutuyor_tereddutle_ben_burada_194059.ogg` | Mert elini tutuyor, tereddütle: "Ben... buradayım. Koş, Evin!" |
| On kapi - guven | `mert_elini_tutuyor_ben_buradayim_kos_d19132.ogg` | Mert elini tutuyor: "Ben buradayım, koş!" |
| On kapi - esik | `yanindaki_ses_artik_mert_e_hic_benzemiyo_08d8b1.ogg` | Yanındaki ses artık Mert'e hiç benzemiyor. Yalnız koşuyorsun. |
| On kapi - yalniz | `yalnizsin_kapiya_dogru_kosuyorsun_395fa9.ogg` | Yalnızsın. Kapıya doğru koşuyorsun. |
| Bodrum kilitli | `kapi_kilitli_bir_anahtara_ihtiyacin_var_663f3f.ogg` | Kapı kilitli. Bir anahtara ihtiyacın var. |
| Bodrum - kapi aciliyor | `anahtar_tam_uyuyor_kapi_gicirdayarak_aci_acf3fc.ogg` | Anahtar tam uyuyor. Kapı gıcırdayarak açılıyor. |
| Gercek son - mert guven | `mert_az_arkanda_sessizce_elini_omzuna_ko_a06c3f.ogg` | Mert az arkanda, sessizce elini omzuna koyuyor. |
| Gercek son - mert guvensiz | `mert_coktan_geride_kaldi_onu_burada_bira_e00ce2.ogg` | Mert çoktan geride kaldı; onu burada bıraktığını fark ediyorsun. |
| Gercek son - cesaret | `korkunun_ortasinda_bile_geri_adim_atmadi_902da9.ogg` | Korkunun ortasında bile geri adım atmadın; ev bunu senden önce fark etti. |
| Gercek son | `merdivenlerin_dibinde_eski_sen_duruyor_y_f03db9.ogg` | Merdivenlerin dibinde eski sen duruyor. Yıllar önce burada unutulmuşsun. |
| Gercek son | `sonunda_geldin_diyor_sesi_senin_sesin_79960b.ogg` | "Sonunda geldin," diyor. Sesi senin sesin. |
| Gercek son | `ben_kimim_diye_soruyorsun_688e91.ogg` | "Ben kimim?" diye soruyorsun. |
| Gercek son | `ben_senin_unuttugun_tarafinim_diyor_eski_3d26df.ogg` | "Ben senin unuttuğun tarafınım," diyor eski sen. |
| Gercek son | `topladigin_6_ani_gercegi_tek_tek_onune_s_5ba81a.ogg` | Topladığın 6 anı, gerçeği tek tek önüne seriyor: bu ev hiç terk edilmedi, sen hep buradaydın. |
| Gercek son | `beni_artik_birakabilirsin_diyor_tek_iste_8d2b26.ogg` | "Beni artık bırakabilirsin," diyor. "Tek istediğim hatırlanmaktı." |
| Gercek son | `seni_hic_unutmayacagim_diyorsun_a1fc0d.ogg` | "Seni hiç unutmayacağım," diyorsun. |
| Gercek son | `bazi_yerlerde_hayaletler_oluler_degildir_4ffa8e.ogg` | Bazı yerlerde hayaletler ölüler değildir. Hatırlanmayı bekleyenlerdir. |
| Son - ortak | `bu_gece_verdigin_kararlar_seninle_kaldi_c9af3c.ogg` | Bu gece verdiğin kararlar seninle kaldı: |
| Yarim son | `merdivenlerin_dibinde_bir_sey_var_ama_ka_1a58e1.ogg` | Merdivenlerin dibinde bir şey var ama karanlıkta tam seçemiyorsun. |
| Yarim son | `beni_hatirlamiyorsun_diye_fisildiyor_kar_685d67.ogg` | "Beni hatırlamıyorsun," diye fısıldıyor karanlık. |
| Yakalanma - Takipci | `soguk_ince_parmaklar_omzuna_dokunuyor_059490.ogg` | Soğuk, ince parmaklar omzuna dokunuyor! |
| Yakalanma - Takipci | `kulagina_kendi_cocukluk_sesin_fisildiyor_9cc082.ogg` | Kulağına kendi çocukluk sesin fısıldıyor: "Anne..." |
| Yakalanma - Takipci | `kalbin_kut_kut_atarken_kendini_yatak_oda_d97211.ogg` | Kalbin küt küt atarken kendini yatak odasına doğru sürüklüyorsun. |
| Yakalanma - anı unutuldu | `zihninde_bir_sey_siliniyor_az_once_hatir_4329d6.ogg` | Zihninde bir şey siliniyor... az önce hatırladığın bir anı artık bulanık. Onu tekrar bulman gerekecek. |
| Yakalanma - anı kaybolmadi | `takipci_seni_bir_sureligine_kaybetti_ama_2f9621.ogg` | Takipçi seni bir süreliğine kaybetti... ama evde yalnız değilsin. |
| QTE - basarili | `tam_zamaninda_kactin_kapiyi_arkanda_kili_a242b1.ogg` | Tam zamanında kaçtın! Kapıyı arkanda kilitleyip uzaklaşıyorsun. |
| QTE - basarili | `ama_arkana_bakmiyorsun_icindeki_bir_ses_e9d2de.ogg` | Ama arkana bakmıyorsun — içindeki bir ses bunun bitmediğini biliyor. |
| QTE - basarisiz | `tepki_veremeden_soguk_islak_bir_el_bileg_23f3f1.ogg` | Tepki veremeden soğuk, ıslak bir el bileğini kavrıyor. |
| QTE - basarisiz | `karanlikta_bir_ses_fisildiyor_burada_sad_9c0fc7.ogg` | Karanlıkta bir ses fısıldıyor: "Burada sadece hatırlanmayanlar kalır." |
| Restart - NG+ | `yeniden_basliyorsun_ama_bu_sefer_bir_sey_e6bdd0.ogg` | Yeniden başlıyorsun... ama bu sefer bir şeyler tanıdık geliyor. |
| Restart | `yeniden_basliyorsun_da1cb3.ogg` | Yeniden başlıyorsun... |
