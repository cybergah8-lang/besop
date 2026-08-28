# Bêşop — Mağaza Metinleri

Google Play / uygulama mağazaları için hazır metinler.
Karakter sınırları: **başlık 30**, **kısa açıklama 80**, **uzun açıklama 4000**.

---

## Görseller (`brand/` klasöründe)

| Dosya | Kullanım |
|---|---|
| `besop-icon-512.png` | Mağaza ikonu (512×512, zorunlu) |
| `besop-icon-1024.png` | Yüksek çözünürlük / App Store |
| `besop-logo-512-transparent.png` | Şeffaf zeminli logo (basın, tanıtım) |
| `besop-logo-1024-transparent.png` | Büyük şeffaf logo |
| `besop-logo-dark.svg` / `besop-logo-light.svg` | Vektör (istediğin boyutta) |

**Hâlâ üretmen gerekenler:** ekran görüntüleri (en az 2, telefon için 1080×1920) ve
öne çıkan görsel (1024×500). Uygulamayı açıp ekran görüntüsü almak yeterli.

---

# TÜRKÇE

**Başlık (30)**
```
Bêşop — Güvenli Silme
```

**Kısa açıklama (80)**
```
Dosyanın üzerine 50 kez yazar, kalıcı siler. Kurtarma yazılımları geri getiremez.
```

**Uzun açıklama**
```
Sildiğin dosya gerçekten gitti mi?

Normalde hayır. Bir dosyayı sildiğinde işletim sistemi sadece "burası boş"
işareti koyar; içerik diskte durmaya devam eder. Kurtarma yazılımları da tam
olarak bunu bulur.

Bêşop bunu çözer. Dosyayı silmeden önce içeriğinin üzerine rastgele veriyle
50 kez yazar. Geriye kurtarılacak bir şey kalmaz.

NASIL ÇALIŞIR
• İçeriğin üzerine 50 kez rastgele veri yazılır
• Her geçişten sonra fsync — veri disk önbelleğinde kalmaz, gerçekten yazılır
• Dosya 0 byte'a indirilir
• Kalıcı olarak silinir — çöp kutusuna gitmez
• Dosyanın gerçekten gittiği doğrulanır

SADE
Üç düğme: Dosya seç, Klasör seç, Yok et. Başka ayar yok, karışıklık yok.

BEŞ DİL
Kurmancî · Kurdiya Navîn (Soranî) · Arapça · Türkçe · İngilizce
Sağdan sola yazılan diller tam destekli.

GİZLİLİK
• İnternet izni yok — uygulama hiçbir yere veri gönderemez
• Reklam yok, takip yok, analitik yok
• Hesap yok, kayıt yok
• Sadece senin seçtiğin dosyalara erişir

DÜRÜST UYARI
Flash bellekte (eMMC/UFS/SSD) aşınma dengeleme vardır: denetleyici veriyi eski
fiziksel bloğa yazmaz. Bu yüzden 50 geçiş, 1 geçişten daha güvenli değildir.
Ama kurtarma yazılımlarına karşı bu yeterlidir — uygulamanın amacı da budur.
Askeri düzeyde mutlak garanti isteyen bir senaryoda tek yol tam disk şifreleme
ve anahtar imhasıdır.

Not: Galeri fotoğrafı silerken Google Fotoğraflar gibi uygulamaların kendi çöp
kutusunda ayrı bir kopya olabilir; onu ilgili uygulamadan ayrıca temizle.

Bêşop — "izsiz". Adı yaptığı işten geliyor.
```

---

# KURMANCÎ

**Sernav (30)**
```
Bêşop — Jêbirina Ewle
```

**Danasîna kurt (80)**
```
50 caran li ser pelê dinivîse û mayînde jê dibe. Nayê vegerandin.
```

**Danasîna dirêj**
```
Pelê ku te jê bir bi rastî çû?

Bi gelemperî na. Dema tu pelekî jê dibî, pergal tenê nîşan dide ku "ev cih
vala ye"; naverok li ser dîskê dimîne. Bernameyên vegerandinê tam vê dibînin.

Bêşop vê çareser dike. Berî ku pelê jê bibe, 50 caran bi daneyên tesadufî li
ser naveroka wî dinivîse. Tiştek namîne ku bê vegerandin.

ÇAWA DIXEBITE
• 50 caran bi daneyên tesadufî li ser tê nivîsandin
• Piştî her derbasê fsync — dane bi rastî diçe dîskê
• Mezinahiya pelê dibe 0 byte
• Bi awayekî mayînde tê jêbirin — naçe sergoyê
• Tê pejirandin ku pel bi rastî çûye

SADE
Sê bişkok: Pelan hilbijêre, Peldankê hilbijêre, Tune bike. Ne zêde, ne tevlihev.

PÊNC ZIMAN
Kurmancî · Kurdiya Navîn · Erebî · Tirkî · Îngilîzî
Zimanên ji rastê ber bi çepê ve bi tevahî têne piştgirî kirin.

NIHÊNÎ
• Destûra înternetê tune — nikare daneyan bişîne tu deverê
• Reklam tune, şopandin tune, analîtîk tune
• Hesab tune, tomarkirin tune
• Tenê digihîje pelên ku te hilbijartine

HIŞYARIYA RAST
Di bîra flash de (eMMC/UFS/SSD) "wear levelling" heye: kontrolker daneyan li
bloka fizîkî ya kevn nanivîse. Ji ber vê yekê 50 derbas ji 1 derbasî ewletir
nîne. Lê li dijî bernameyên vegerandinê ev têrê dike — armanca sepanê jî ev e.

Bêşop — "bêyî şop". Nav ji karê wê tê.
```

---

# کوردیی ناوەندی (SORANÎ)

**ناونیشان (30)**
```
بێشۆپ — سڕینەوەی پارێزراو
```

**پێناسەی کورت (80)**
```
٥٠ جار بەسەر فایلدا دەنووسێتەوە و بە یەکجاری دەیسڕێتەوە. ناگەڕێتەوە.
```

**پێناسەی درێژ**
```
ئایا ئەو فایلەی سڕیتەوە بەڕاستی نەماوە؟

بەزۆری نەخێر. کاتێک فایلێک دەسڕیتەوە، سیستەم تەنها نیشانەی «ئەم شوێنە بەتاڵە»
دادەنێت؛ ناوەڕۆکەکە لەسەر دیسک دەمێنێتەوە. نەرمامێری گەڕاندنەوەش هەر ئەوە دەدۆزێتەوە.

بێشۆپ ئەمە چارەسەر دەکات. پێش سڕینەوە، ٥٠ جار بە داتای هەڕەمەکی بەسەر
ناوەڕۆکەکەیدا دەنووسێتەوە. هیچ نامێنێتەوە بۆ گەڕاندنەوە.

چۆن کار دەکات
• ٥٠ جار بە داتای هەڕەمەکی بەسەریدا دەنووسرێتەوە
• دوای هەر جارێک fsync — داتاکە بەڕاستی دەنووسرێتە سەر دیسک
• قەبارەی فایلەکە دەکرێتە ٠ بایت
• بە یەکجاری دەسڕدرێتەوە — ناچێتە تەنەکەی خۆڵ
• پشتڕاست دەکرێتەوە کە بەڕاستی نەماوە

سادە
سێ دوگمە: فایل هەڵبژێرە، بوخچە هەڵبژێرە، لەناوی ببە. هیچی تر.

پێنج زمان
کوردیی باکوور · کوردیی ناوەندی · عەرەبی · تورکی · ئینگلیزی

پاراستنی تایبەتمەندی
• مۆڵەتی ئینتەرنێتی نییە — ناتوانێت داتا بنێرێت
• ڕیکلام نییە، شوێنکەوتن نییە
• هەژمار نییە، تۆمارکردن نییە

ئاگاداری ڕاستگۆیانە
لە بیرگەی فلاشدا (eMMC/UFS/SSD) «wear levelling» هەیە: کۆنترۆڵەر داتاکە لە
بلۆکی کۆنی فیزیکی نانووسێتەوە. بۆیە ٥٠ جار لە ١ جار پارێزراوتر نییە. بەڵام
بەرامبەر نەرمامێری گەڕاندنەوە ئەمە بەسە — ئامانجی ئەپەکەش هەر ئەوەیە.

بێشۆپ — «بەبێ شوێن». ناوەکەی لە کارەکەیەوە هاتووە.
```

---

# العربية

**العنوان (30)**
```
Bêşop — المحو الآمن
```

**الوصف القصير (80)**
```
يكتب فوق الملف ٥٠ مرة ثم يحذفه نهائيًا. برامج الاستعادة لا تُرجعه.
```

**الوصف الطويل**
```
هل اختفى الملف الذي حذفته فعلًا؟

غالبًا لا. عند حذف ملف، يضع النظام علامة "هذا المكان فارغ" فقط؛ أما المحتوى
فيبقى على القرص. وهذا تحديدًا ما تجده برامج الاستعادة.

Bêşop يحل هذه المشكلة. قبل الحذف يكتب فوق محتوى الملف ٥٠ مرة ببيانات عشوائية.
لا يبقى شيء يمكن استرجاعه.

كيف يعمل
• يُكتب فوق المحتوى ٥٠ مرة ببيانات عشوائية
• fsync بعد كل مرة — تُكتب البيانات فعليًا على القرص
• يُصغَّر حجم الملف إلى صفر بايت
• يُحذف نهائيًا — لا يذهب إلى سلة المهملات
• يتم التحقق من اختفاء الملف فعلًا

بسيط
ثلاثة أزرار: اختر ملفات، اختر مجلدًا، أبِدْه. لا إعدادات ولا تعقيد.

خمس لغات
الكردية الشمالية · الكردية الوسطى · العربية · التركية · الإنجليزية
دعم كامل للغات من اليمين إلى اليسار.

الخصوصية
• لا يملك إذن الإنترنت — لا يستطيع إرسال أي بيانات
• لا إعلانات ولا تتبع ولا تحليلات
• لا حساب ولا تسجيل

تنبيه صادق
في ذاكرة الفلاش (eMMC/UFS/SSD) يوجد "توزيع التآكل": المتحكم لا يكتب البيانات
في الكتلة الفيزيائية القديمة. لذلك ٥٠ مرة ليست أأمن من مرة واحدة. لكنها كافية
ضد برامج الاستعادة — وهذا هو الغرض من التطبيق.

Bêşop — "بلا أثر". الاسم من عمله.
```

---

# ENGLISH

**Title (30)**
```
Bêşop — Secure Erase
```

**Short description (80)**
```
Overwrites your file 50 times, then deletes it. Recovery software can't undo it.
```

**Full description**
```
Is that deleted file really gone?

Usually not. When you delete a file, the system only marks the space as free —
the contents stay on the disk. That is exactly what recovery software finds.

Bêşop fixes this. Before deleting, it overwrites the file's contents 50 times
with random data. There is nothing left to recover.

HOW IT WORKS
• Contents overwritten 50 times with random data
• fsync after every pass — data really reaches the disk, not just the cache
• File truncated to 0 bytes
• Permanently deleted — it does not go to the trash
• Deletion is verified

SIMPLE
Three buttons: choose files, choose folder, destroy. No settings, no clutter.

FIVE LANGUAGES
Kurmanji · Sorani · Arabic · Turkish · English
Full right-to-left support.

PRIVACY
• No internet permission — the app cannot send data anywhere
• No ads, no tracking, no analytics
• No account, no sign-up
• Only touches the files you pick

AN HONEST WARNING
Flash memory (eMMC/UFS/SSD) uses wear levelling: the controller does not write
to the old physical block. So 50 passes are no safer than 1. Against recovery
software, however, this is enough — and that is what the app is for. If you
need an absolute guarantee, the only answer is full-disk encryption plus key
destruction.

Bêşop — "traceless". The name is the job.
```

---

## Etiketler / Anahtar kelimeler

```
güvenli silme, dosya silme, kalıcı silme, veri kurtarma önleme, gizlilik,
dosya imha, shredder, secure delete, file shredder, wipe, privacy,
جێبرن, سڕینەوە, محو آمن
```

## Kategori
Araçlar (Tools) · İkincil: Verimlilik

## İçerik derecelendirmesi
Herkes (3+) — reklam yok, kullanıcı içeriği yok, internet yok

## Gizlilik politikası için gereken cümle
```
Bêşop hiçbir kişisel veri toplamaz, saklamaz veya iletmez. Uygulamanın
internet izni yoktur. Tüm işlemler cihazda yerel olarak yapılır.
```
