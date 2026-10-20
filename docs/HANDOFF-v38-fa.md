# پروندهٔ تحویل کارباما / Karbama — وضعیت v38

**آخرین به‌روزرسانی:** ۱۸ اکتبر ۲۰۲۶  
**هدف این سند:** انتقال امن کار و ادامهٔ پروژه در یک حساب Arena یا محیط توسعهٔ دیگر، بدون نیاز به تاریخچهٔ این گفت‌وگو.

> **مهم — اطلاعات محرمانه:** هیچ API key، رمز، Secret Firebase، فایل `google-services.json` واقعی یا محتوای credential در این سند نیست. یکی از کلیدهای نرخ ارز قبلاً به‌اشتباه در چت نوشته شد؛ آن کلید باید در سرویس صادرکننده لغو/تولید مجدد شود و هرگز در سورس، APK، GitHub یا چت وارد نشود.

---

## ۱) فایل‌ها و محل پروژه

| مورد | مسیر |
|---|---|
| سورس Android | `Karbama/` |
| دمو HTML قابل‌کلیک | `karbama-demo.html` |
| سند طرح پرداخت | `docs/PAYMENT-v38-fa.md` |
| این سند تحویل | `docs/HANDOFF-v38-fa.md` |
| APK قابل نصب v38 | `download/Karbama-v38.apk` |
| صفحهٔ دانلود محلی | `download/index.html` |
| Functions | `Karbama/functions/` |
| قواعد Firestore | `Karbama/firestore.rules` |
| قواعد Storage | `Karbama/storage.rules` |

### فایل انتقال سورس
نسخهٔ انتقالی سورس در `download/Karbama-v38-source-transfer.zip` است. در archive موارد زیر عمداً وجود ندارند: APK، Gradle cache، `node_modules`، `local.properties`، فایل Firebase واقعی و هر Secret.

---

## ۲) آخرین APK ساخته‌شده

| مشخصه | مقدار |
|---|---|
| نام فایل | `Karbama-v38.apk` |
| package | `com.karbama.app` |
| versionName | `38.0.0` |
| versionCode | `38` |
| امضا | Android debug key، APK Signature Scheme v2 معتبر |
| MD5 | `e4f45618261b03e38842e3162f64276b` |
| SHA-256 | `a73396ace922dead91a9f772c1a2c22bba0e1c157ed0a01390ad1af2e004efdf` |

### شواهد build
آخرین اجرای موفق:

```text
:app:processDebugGoogleServices
:app:assembleDebug
BUILD SUCCESSFUL
```

APK با `aapt` و `apksigner verify` نیز بررسی شده است.

> تست نصب/کلیک با یک دستگاه Android واقعی هنوز در این sandbox انجام نشده است. تلاش برای Emulator به‌علت نبود KVM و نیاز RAM بیشتر از سقف محیط ممکن نشد. بنابراین v38 هنوز GitHub Release نشده است.

---

## ۳) وضعیت اتصال Firebase جدید

فایل Firebase واقعی از کاربر دریافت شد و فقط به این مسیر محلی منتقل شد:

```text
Karbama/app/google-services.json
```

- نام پکیج داخل فایل با `com.karbama.app` تطبیق دارد.
- permission فایل `600` است.
- این فایل در `.gitignore` قرار دارد و نباید commit، ZIP انتقالی، GitHub یا چت شود.
- Gradle آن را خوانده است (`processDebugGoogleServices`).

### سرویس‌هایی که هنوز باید در Firebase Console فعال/پیکربندی شوند

1. **Authentication**: Email/Password و Anonymous.
2. **Firestore Database**: ایجاد database و deploy قواعد `Karbama/firestore.rules`.
3. **Storage**: ایجاد bucket و deploy قواعد `Karbama/storage.rules`.
4. **Cloud Functions / Billing**: برای قیمت زنده، پرداخت‌های آنلاین و Secret Manager.

Firebase قبلی برای مالک مهم نیست؛ ساخت Firebase Project جدید با حساب Google مجاز، ساختار سورس و قابلیت‌های محلی برنامه را تغییر نمی‌دهد. تنها داده‌های ابری قبلی منتقل نمی‌شوند.

---

## ۴) پرداخت v38

### رفتار پیاده‌سازی‌شده

- مبلغ پایهٔ نمایش آگهی: **₺500** (`Config.DEFAULT_ADS_FEE`).
- مهاجرت یک‌بارۀ مقدار قدیمی `6000` در `AppPrefs.migratePaymentV38Default()` وجود دارد؛ تنظیم عمدی مدیر را overwrite نمی‌کند.
- فقط بخش آگهی‌ها در Home قفل می‌شود؛ full-page paywall استفاده نشده است.
- قفل فقط پس از پرداخت و تأیید مدیر باز می‌شود.
- سه گزینه در یک صفحهٔ پرداخت:
  1. زرین‌پال / پرداخت آنلاین ایران
  2. iyzico / پرداخت آنلاین ترکیه
  3. انتقال بانکی/کارت‌به‌کارت + رسید

### وضعیت زرین‌پال و iyzico

- زرین‌پال در UI نشان می‌دهد:

  ```text
  مبلغ معادل ریالی: — ریال (پس از نرخ try_hav)
  ```

- عدد واقعی ریالی عمداً نمایش داده نمی‌شود؛ زیرا هنوز API key نرخ امن، deploy Function و اتصال Android به quote زنده وجود ندارد.
- نباید هیچ نرخ ثابت/ساختگی یا API key داخل Android یا HTML قرار بگیرد.
- iyzico مبلغ پایهٔ ₺500 را نشان می‌دهد ولی تا دریافت merchant credential فعال نمی‌شود.
- کارت‌های آنلاین pending/paid جعلی ایجاد نمی‌کنند.

### فیش دستی

برای ارسال درخواست دستی، کاربر باید این دو مورد را وارد کند:

- کد تراکنش/واریز (حداقل ۳ کاراکتر، حداکثر ۳۲)
- تصویر فیش

تصویر با `ImageUtils` به JPEG فشرده می‌شود و در اپ تا ۵MB محدود است. هنگام اتصال Firebase در مسیر زیر قرار می‌گیرد:

```text
applicants/{uid}/payment_receipt.jpg
```

داده‌های زیر روی `Applicant` ثبت می‌شوند:

```text
paymentStatus       // "pending" | "paid" | ""
paymentAt
paymentMethod       // "manual" | "zarinpal" | "iyzico"
paymentReference
paymentReceiptUrl
```

پنل مدیر کد پیگیری، روش پرداخت، کد تراکنش، وضعیت و thumbnail رسید را نمایش می‌دهد. تنها مدیر می‌تواند پرداخت را `paid` کند.

### Functions و امنیت

در `Karbama/functions/` ساختار TypeScript Gen 2 وجود دارد:

- `createIranQuote`
- `startZarinpalPayment`
- `zarinpalCallback`
- `startIyzicoPayment`
- `iyzicoCallback`
- `reviewManualPayment`

Secretهای تعریف‌شده فقط در Firebase Secret Manager هستند:

```text
NAVASAN_API_KEY
ZARINPAL_MERCHANT_ID
IYZICO_API_KEY
IYZICO_SECRET_KEY
```

Functions عمداً در وضعیت امن/stub هستند و هیچ پرداخت آنلاینی را بدون merchant credential و verify واقعی `paid` نمی‌کنند. `npm run lint` برای Functions قبلاً موفق بوده است.

### فعال‌سازی نرخ زندهٔ ریال

برای نمایش مبلغ ریالی واقعی باید:

1. کلید تازهٔ Navasan گرفته شود (کلید قبلیِ ارسال‌شده در چت استفاده نشود).
2. Secret با نام `NAVASAN_API_KEY` در پروژه Firebase جدید ثبت شود.
3. `createIranQuote` به‌صورت امن deploy شود.
4. Android Firebase Functions SDK/callable integration را صدا بزند.
5. Function مقدار `try_hav` را بخواند، `IRR = try_hav × 500` را محاسبه کند، quote را ۱۵ دقیقه در `paymentSessions` قفل کند و عدد را به UI برگرداند.

Merchant ID زرین‌پال برای **نمایش quote نرخ** لازم نیست، اما برای شروع پرداخت زنده لازم است.

---

## ۵) نگه‌داری یوزرنیم و پسورد

در v38 پیاده‌سازی تازه‌ای در این فایل وجود دارد:

```text
Karbama/app/src/main/java/com/karbama/app/data/local/SecureLoginVault.kt
```

رفتار:

- فقط بعد از ورود موفق، یوزرنیم و پسورد ذخیره می‌شوند.
- دادهٔ ترکیبی با **AES-GCM** رمزنگاری می‌شود.
- کلید رمزنگاری در **Android Keystore** است.
- plaintext password در SharedPreferences معمولی، Firestore یا Git قرار نمی‌گیرد.
- ورود بعدی، فیلدها را پر می‌کند؛ `KbField` رمز را masked نگه می‌دارد.
- خروج از حساب، پروفایل فعال را پاک می‌کند ولی credential رمزنگاری‌شده را برای ورود بعدی حفظ می‌کند.
- ورود ناموفق credential اشتباه را ذخیره نمی‌کند.

همچنین serialize محلی `Applicant` شامل `payMethod`، `payReference` و `payReceiptUrl` شده تا رسید دستی بعد از restart از بین نرود.

---

## ۶) ویژگی‌ها و تصمیم‌های ثابت مهم

- زبان‌ها: فارسی، ترکی، انگلیسی، عربی؛ RTL برای فارسی/عربی.
- متن خوش‌آمدگویی باید دقیقاً این باشد:

  ```text
  آگهی‌های استخدام به‌روز از سراسر ترکیه، به همراه معرفی مستقیم شما به کارفرما و بدون واسطه
  ```

- ثبت‌نام ۴ مرحله‌ای است؛ ایمیل از کاربر درخواست نمی‌شود.
- نام پدر و کد ملی در محل‌های تعیین‌شده هستند؛ گذرنامه برای مدرک خواسته می‌شود.
- صفحهٔ موفقیت ثبت‌نام حذف است.
- کاربر و مدیر با یوزرنیم/رمز وارد می‌شوند؛ هیچ دکمهٔ Admin نباید در UI باشد.
- یوزرنیم باید انگلیسی باشد.
- ورود مدیر hint یا credential را در صفحهٔ Login نشان نمی‌دهد.
- admin credentials پیش‌فرض: `admin` / `karbama2026` (این یک تنظیم نمایشی/محلی قبلی پروژه است؛ برای production باید credential مدیریت واقعی و قواعد Firebase اعمال شود).
- کارت پیش‌فرض کارگزار: `1234 1234 1234 1234` و در تنظیمات مدیر قابل تغییر است.
- «فقط می‌خواهم آگهی‌ها را ببینم» نباید وجود داشته باشد.
- مدیریت admin fullscreen است و bottom tab ندارد.
- ارتباط با ما نزدیک تب پروفایل و deep link پیش‌فرض `(karbama34)` است.
- آگهی‌ها photo/video، صفحهٔ جزئیات، like و Firestore sync دارند.
- reset/sample-data button نباید نمایش داده شود.
- credit باید دقیقاً همین باشد و تغییر نام ندهد:

  ```text
  powered by naser tavakkoli @nt.galaxy
  ```

---

## ۷) قواعد Firebase

### Firestore

`Karbama/firestore.rules` شامل موارد زیر است:

- خواندن/نوشتن آگهی‌ها بر اساس admin.
- محدودیت مالک برای applicants.
- کاربر نمی‌تواند وضعیت خود را `paid` کند.
- کاربر فقط درخواست دستی معتبر با receipt URL و transaction reference ایجاد می‌کند.
- `paymentSessions` فقط توسط Cloud Functions نوشته می‌شود.
- likes و applications ساختار جدا دارند.

### Storage

`Karbama/storage.rules` شامل:

- تصویر مدارک و رسید زیر مسیر applicant.
- فقط تصویر با سقف ۵MB.
- خواندن توسط صاحب فایل یا admin.
- رسانهٔ آگهی‌ها تا ۲۵MB با دسترسی admin برای write.

> قبل از deploy، placeholderهای Admin UID در `storage.rules` باید با UID حساب مدیر Firebase جدید جایگزین شوند. Firestore admin نیز به سند `admins/{uid}` نیاز دارد.

---

## ۸) ساخت پروژه در محیط تازه

### نیازهای Android

- JDK 17
- Android SDK: Platform 35 و Build Tools 35.0.0
- Gradle 8.7

نمونهٔ محیط build:

```bash
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
./gradlew :app:assembleDebug --no-daemon --max-workers=1 \
  -Dorg.gradle.parallel=false \
  -Dorg.gradle.jvmargs='-Xmx896m -Dfile.encoding=UTF-8' \
  -Pkotlin.compiler.execution.strategy=in-process
```

برای sandbox کم‌حافظه، settings بالا موفق بوده‌اند. `local.properties` محلی است و نباید commit شود.

### Functions

```bash
cd Karbama/functions
npm ci
npm run lint
```

`functions/node_modules` و output آن نباید commit شوند.

---

## ۹) GitHub و انتشار

- repository هدف: `ntgalaxy1/karbama34`.
- APKها نباید وارد Git شوند؛ فقط GitHub Release.
- `README.md` سند GitHub-facing است؛ تاریخچهٔ طولانی در `docs/CHANGELOG-fa.md`.
- v37 آخرین Release عمومی قبلی بود؛ آن release جریان قدیمی ₺6000/manual داشت.
- v38 هنوز commit/push/release نشده است؛ قبل از release باید تست نصب واقعی و click-through انجام شود.

---

## ۱۰) وضعیت فعلی و قدم بعدی پیشنهادی

### انجام‌شده

- v38 APK ساخته شده است.
- اتصال Android به فایل Firebase جدید build شده است.
- فیش دستی، persistence محلی و viewer مدیر اضافه شده‌اند.
- Secure login vault اضافه شده است.
- Functions/rules/schema اضافه شده‌اند.
- archive انتقال سورس ساخته شده است.

### مانده

1. فعال‌سازی Authentication، Firestore و Storage در Firebase Project جدید.
2. افزودن حساب admin Firebase، گرفتن UID آن و تکمیل admin rule/config.
3. deploy rules.
4. دریافت کلید **جدید** نرخ ارز و ثبت فقط در Secret Manager پس از دسترسی معتبر Google Cloud.
5. deploy و اتصال واقعی quote Function به Android.
6. دریافت merchant credentialهای زرین‌پال و iyzico؛ سپس پیاده‌سازی و تست verify/callback سرور به سرور.
7. تست نصب/کلیک واقعی روی گوشی Android.
8. بعد از شواهد تازه: commit، push و GitHub Release v38.

---

## ۱۱) هشدارهای مهم

- API key نوسان قبلی را revoke/regenerate کنید؛ مقدار آن را در هیچ‌جا بازنویسی نکنید.
- `google-services.json` واقعی در source transfer archive وجود ندارد و باید جداگانه در محیط جدید در مسیر `Karbama/app/google-services.json` قرار گیرد.
- هیچ payment gateway را تا قبل از verification سرور به سرور، live یا paid اعلام نکنید.
- عدم وجود Function/Secret به معنی نشان‌ندادن نرخ واقعی است؛ نرخ عددی ساختگی داخل اپ نگذارید.
