# Değişiklik günlüğü

## 1.3 — 2026-08-28

- Aydınlık / karanlık / sistem teması seçilebiliyor, seçim kalıcı saklanıyor
- Yeni renk paleti ve tipografi ölçeği
- Yeni logo: solarak kaybolan iz. Uygulama ikonu ve bildirim ikonu yenilendi
- Açılış animasyonu — logo çizilir, isim ve CyberGah damgası belirir
- Hakkında ekranı: ne yaptığı, nasıl çalıştığı, dürüst sınırı — 5 dilde
- Silme butonu kısaldı: **Yok et** (TUNE BIKE / لەناوی ببە / أبِدْه / DESTROY)
- Disk modu (masaüstü): USB bellek / SD kart / harici diski tamamen
  biçimlendirir ve 3 geçiş yazar. Sistem diski kilitli, onay için sürücü
  harfini elle yazmak gerekiyor
- Geçen süre ve tahmini kalan süre göstergesi
- Masaüstü yazma tamponu 1 MB → 8 MB

### Düzeltmeler
- İptal düğmesi disk modunda gerçekte durdurmuyordu: `proc.destroy()` sadece
  `cmd.exe`'yi öldürüyor, asıl `format.com` çalışmaya devam ediyordu. Artık
  tüm süreç ağacı öldürülüyor
- Işıklı arka plana geçince `LocalContentColor` sağlanmadığı için başlık
  koyu zeminde görünmez olmuştu
- Disk modunda yazılan veri miktarı hiç güncellenmiyordu

## 1.2 — 2026-08-28

- Uygulama adı **Bêşop** oldu (Kurmancî: "izsiz")
- Beş dil: Kurmancî (ana dil), Soranî, Arapça, Türkçe, İngilizce
- Sağdan sola diller için gerçek RTL yerleşim
- Masaüstü sürümü (Windows / macOS / Linux) — Compose Multiplatform
- Arayüz ve çeviriler Android ile masaüstü arasında paylaşılıyor

## 1.0 — 2026-08-28

- İlk sürüm: dosya ve klasör seçip 50 geçiş üzerine yazma, kalıcı silme
- Ön plan servisi ve bildirimle ilerleme

### Düzeltmeler
- Silmeden önce dosya adını rastgeleye çevirmek SAF iznini düşürüyor ve silme
  sessizce başarısız oluyordu — içerik yok ediliyor ama 0 byte'lık dosya
  kaydı kalıyordu. Bu adım Android'de kaldırıldı, yerine silme doğrulaması eklendi
