# Bêşop — Telegram Duyuruları

Kopyala-yapıştır hazır. Telegram Markdown biçimi (`*kalın*`, `_italik_`, `` `kod` ``).
Görsel olarak `brand/besop-icon-1024.png` kullan — kare, koyu zemin, kanalda iyi durur.

---

## 1 · Kısa duyuru (Türkçe)

> 🔴 *Bêşop* çıktı.
>
> Sildiğin dosya aslında gitmiyor — sistem sadece "burası boş" diyor, içerik diskte duruyor. Kurtarma yazılımları da tam onu buluyor.
>
> Bêşop silmeden önce dosyanın üzerine *50 kez rastgele veri* yazıyor. Geriye kurtarılacak bir şey kalmıyor.
>
> ✓ Üç düğme, başka ayar yok
> ✓ 5 dil — Kurmancî, Soranî, Arapça, Türkçe, İngilizce
> ✓ İnternet izni yok — hiçbir yere veri gidemez
> ✓ Reklam yok, takip yok, hesap yok
> ✓ Android + Windows + macOS
>
> _Bêşop = "izsiz"_
>
> 📥 [İndir](LINK)

---

## 2 · Kısa duyuru (Kurmancî)

> 🔴 *Bêşop* derket.
>
> Pelê ku tu jê dibî bi rastî naçe — pergal tenê dibêje "ev cih vala ye", naverok li ser dîskê dimîne. Bernameyên vegerandinê tam wê dibînin.
>
> Bêşop berî jêbirinê *50 caran bi daneyên tesadufî* li ser pelê dinivîse. Tiştek namîne ku bê vegerandin.
>
> ✓ Sê bişkok, ne zêdetir
> ✓ 5 ziman — Kurmancî, Soranî, Erebî, Tirkî, Îngilîzî
> ✓ Destûra înternetê tune — dane naçe tu deverê
> ✓ Reklam tune, şopandin tune, hesab tune
> ✓ Android + Windows + macOS
>
> _Bêşop = "bêyî şop"_
>
> 📥 [Dakêşe](LINK)

---

## 3 · Kısa duyuru (English)

> 🔴 *Bêşop* is out.
>
> A deleted file isn't really gone — the system just marks the space free while the contents stay on disk. That's exactly what recovery tools find.
>
> Bêşop overwrites the file *50 times with random data* before deleting it. Nothing is left to recover.
>
> ✓ Three buttons, nothing else
> ✓ 5 languages — Kurmanji, Sorani, Arabic, Turkish, English
> ✓ No internet permission — data can't leave your device
> ✓ No ads, no tracking, no account
> ✓ Android + Windows + macOS
>
> _Bêşop = "traceless"_
>
> 📥 [Download](LINK)

---

## 4 · Tek satırlık (kanal başlığı / yeniden paylaşım)

```
🔴 Bêşop — sildiğin dosya bir daha geri gelmesin. 50 geçiş, 5 dil, sıfır takip. Android + Windows + macOS.
```

```
🔴 Bêşop — pelê ku te jê bir careke din venegere. 50 derbas, 5 ziman, bêyî şopandin.
```

---

## 5 · Teknik takipçi kitlesi için (uzun)

> *Bêşop nasıl çalışıyor?*
>
> Dosya silmenin neden yetersiz olduğunu biliyorsun: `unlink()` sadece dizin kaydını kaldırır, veri blokları olduğu gibi kalır.
>
> Bêşop'un sırası:
>
> 1️⃣ İçeriğin üzerine 50 geçiş rastgele veri
> 2️⃣ Her geçişten sonra `fsync` — sayfa önbelleğinde kalmasın, gerçekten diske insin
> 3️⃣ `truncate` → 0 byte
> 4️⃣ Kalıcı silme (çöp kutusuna değil)
> 5️⃣ Silmenin gerçekleştiğini *doğrulama* — olmadıysa açıkça söyler
>
> Masaüstünde ek olarak silmeden önce dosya adı 3 kez rastgeleye çevriliyor, böylece dizin kaydındaki eski isim de bozuluyor. Android'de bu adım yok: SAF izni belge kimliğine bağlı, ad değişince izin düşüyor ve silme başarısız oluyor — bunu test sırasında yaşayıp kaldırdık.
>
> *Dürüst sınır:* flash bellekte wear levelling var, denetleyici eski fiziksel bloğa yazmıyor. 50 geçiş, 1 geçişten daha güvenli değil. Kurtarma yazılımlarına karşı yeterli — mutlak garanti isteyen tam disk şifreleme kullanmalı. Bunu uygulamanın içinde de yazıyoruz, gizlemiyoruz.
>
> Masaüstünde ayrıca *disk modu* var: bir USB bellek/SD kart/harici diski seçip tamamını biçimlendirip 3 geçiş yazdırabiliyorsun. Sistem diski listede kilitli.
>
> 📥 [İndir](LINK)

---

## 6 · Sürüm notu biçimi (sonraki güncellemeler için)

> 🔴 *Bêşop 1.3*
>
> • Aydınlık / karanlık / sistem teması seçilebiliyor
> • Yeni palet ve logo
> • Silme butonu: "Yok et"
> • Disk modu (masaüstü): tüm diski biçimlendir + 3 geçiş
> • Kalan süre göstergesi
>
> 📥 [İndir](LINK)

---

## Kullanım notu

- `LINK` yerine indirme adresini koy (GitHub Releases, doğrudan APK, ya da mağaza bağlantısı)
- Telegram'da APK'yı doğrudan dosya olarak paylaşırsan bazı istemciler uyarı gösterir; GitHub Releases bağlantısı daha temiz durur
- Görsel + metni tek gönderide birleştir: resmi ekleyip açıklamaya metni yaz (caption sınırı 1024 karakter — 1, 2, 3 numaralı metinler sığar, 5 numaralı sığmaz, onu ayrı mesaj yap)
