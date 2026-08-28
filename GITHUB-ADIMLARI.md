# GitHub'a yükleme — adım adım

Depo hazır, commit atıldı, `v1.3.0` etiketi kondu. Kalan tek şey: yetkilendirme.
Bunu ben yapamam (ve yapmamalıyım) — aşağıdaki adımlar sana ait.

---

## 0 · ÖNCE: şifreni değiştir

Şifreni sohbette paylaştın. Hemen değiştir:
**github.com/settings/security** → Change password

Ayrıca iki adımlı doğrulamayı aç: **github.com/settings/security** → Two-factor authentication

---

## 1 · Yetkilendirme

GitHub 2021'den beri git işlemlerinde şifre kabul etmiyor. İki yol var:

### Yol A — GitHub CLI (önerilen, en kolay)

Kur: <https://cli.github.com> · ya da PowerShell'de:
```powershell
winget install --id GitHub.cli
```

Sonra bu oturumda `!` ile çalıştır:
```
! gh auth login
```
Sorulara: **GitHub.com** → **HTTPS** → **Login with a web browser** → çıkan kodu tarayıcıya yapıştır.

### Yol B — Personal Access Token

1. **github.com/settings/tokens** → *Generate new token (classic)*
2. Kapsam: **`repo`** ve **`workflow`** işaretle
3. Üretilen tokeni kopyala — bir daha gösterilmez
4. `git push` sırasında şifre sorulduğunda **tokeni** yapıştır

---

## 2 · Depoyu oluştur ve gönder

### `gh` kurduysan (tek komut)

```bash
gh repo create besop --public --source=. --remote=origin --push \
  --description "iz bırakmadan siler — 50 geçiş üzerine yazan güvenli dosya silme uygulaması"
git push origin v1.3.0
```

### `gh` yoksa

Önce **github.com/new** adresinden `besop` adıyla boş bir depo aç
(README/lisans **ekleme**, zaten var). Sonra:

```bash
git remote add origin https://github.com/cybergah/besop.git
git push -u origin main
git push origin v1.3.0
```

> Kullanıcı adın `cybergah` değilse adresi ona göre düzelt.

---

## 3 · İmza anahtarını GitHub'a sakla

Etiket atınca GitHub Actions üç platformu birden derler. Android APK'sının
**senin anahtarınla** imzalanması için anahtarı gizli değer olarak eklemen gerekir.
Eklemezsen derleme yine çalışır ama APK debug anahtarıyla imzalanır — mağazaya
o APK'yı yükleyemezsin.

Anahtarı base64'e çevir:

```bash
base64 -w0 besop-release.jks > keystore.b64
```

**github.com/cybergah/besop/settings/secrets/actions** → *New repository secret*
ile dört değer ekle:

| Ad | Değer |
|---|---|
| `KEYSTORE_BASE64` | `keystore.b64` dosyasının içeriği |
| `KEYSTORE_PASSWORD` | anahtar deposu parolası |
| `KEY_ALIAS` | `guvenlisilme` |
| `KEY_PASSWORD` | anahtar parolası |

Sonra `keystore.b64` dosyasını sil — işi bitti.

> Parolaları `keystore.properties` dosyasında bulabilirsin (o dosya depoya girmiyor).

---

## 4 · Sürüm yayınla

Etiket gönderdiğinde otomatik başlar. Elle tetiklemek için de
**Actions** sekmesi → *Release* → *Run workflow*.

İş bitince **Releases** sayfasında dört dosya olur:

| Dosya | Platform |
|---|---|
| `Besop-Android-1.3.0.apk` | Android |
| `Besop-Windows-Kurulum-1.3.0.msi` | Windows kurulum |
| `Besop-Windows-Portable-1.3.0.zip` | Windows kurulumsuz |
| `Besop-macOS-1.3.0.dmg` | **macOS — Mac'e ihtiyacın yok** |

Sonraki sürümler için:

```bash
# sürüm numaralarını güncelle:
#   app/build.gradle.kts        versionCode / versionName
#   desktop/build.gradle.kts    packageVersion
#   shared/.../Model.kt         APP_VERSION
git commit -am "1.4"
git tag -a v1.4.0 -m "Bêşop 1.4.0"
git push origin main v1.4.0
```

---

## 5 · Play Store

Google Play **AAB** ister, APK değil. Mağazaya yüklerken:

```bash
./gradlew :app:bundleRelease
# çıktı: app/build/outputs/bundle/release/app-release.aab
```

Gereken metinler ve görseller hazır:

| Ne | Nerede |
|---|---|
| Başlık, kısa/uzun açıklama (5 dil) | `store/magaza-metinleri.md` |
| Mağaza ikonu 512×512 | `brand/besop-icon-512.png` |
| Ekran görüntüleri | `docs/screenshots/` |
| Gizlilik politikası cümlesi | `store/magaza-metinleri.md` sonu |
| Telegram duyuruları | `store/telegram-duyuru.md` |

**Hâlâ üretmen gereken:** öne çıkan görsel (1024×500). Play Store zorunlu tutuyor.

> Play Console'da "Data safety" bölümünde **hiçbir veri toplanmıyor** diyeceksin —
> uygulamanın internet izni bile yok, bu doğru.

---

## Depoya giren / girmeyen

**Giren:** kaynak kod, README, lisans, değişiklik günlüğü, logo dosyaları,
mağaza metinleri, ekran görüntüleri, GitHub Actions tanımı.

**Girmeyen** (`.gitignore` içinde):
`besop-release.jks` · `keystore.properties` · `local.properties` · `dist/` · `build/`

İmza anahtarını **kaybetme**. Kaybedersen aynı uygulamanın üzerine bir daha
güncelleme yayınlayamazsın — Play Store yeni anahtarla imzalanmış APK'yı reddeder.
Yedeğini bulut dışında, şifreli bir yerde tut.
