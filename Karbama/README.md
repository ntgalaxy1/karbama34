# کارباما | Karbama — اپلیکیشن کاریابی ترکیه

اپلیکیشن اندروید کاریابی برای فارسی‌زبانان در ترکیه (به‌ویژه استانبول)، با رابط کاربری سه‌زبانه (فارسی / ترکی استانبولی / انگلیسی) و پنل مدیریت داخلی.

---

## ۱) در یک نگاه

| بخش | توضیح |
|---|---|
| تکنولوژی | Kotlin + Jetpack Compose (Material 3) |
| بک‌اند | Firebase: Firestore + Storage + Authentication |
| پسندیده‌ها و درخواست‌ها | **#29:** `likes/{applicantId}_{jobId}` و `applications/{applicantId}_{jobId}` در Firestore — `data/repo/UserActivityRepository.kt` (`like/unlike/apply/myLikedJobIds/myAppliedJobIds/observeLikes/observeApplications/mergeIds`) + `AppViewModel.toggleLike/applyToJob/syncUserActivity` و `AppPrefs.setLikedIds/setAppliedIds` برای هم‌گام‌سازی؛ نمایش در `ApplicantDetailScreen` با `ActivityCard` (پسندیده‌ها / درخواست‌های ارسالی) و `watchApplicantActivity(id)` در `MainActivity` |
| رسانه‌ی آگهی در Storage | **#29:** قاعده‌ی `match /jobs/{jobId}/{fileName}` اضافه شد — خواندن برای کاربران وارد‌شده، نوشتن فقط مدیر، سقف ۲۵MB و `image/*|video/*` (`storage.rules`) |
| راهنمای اتصال دیتابیس | **#29:** `Karbama/FIREBASE-SETUP-fa.md` — راهنمای فارسی ۵ دقیقه‌ای (پروژه، `google-services.json`، سه سرویس، حساب مدیر، Publish قواعد) |
| زبان‌ها | **فارسی (RTL)، Türkçe، English، العربية (RTL)** — دیکشنری‌های `PERSIAN/TURKISH/ENGLISH/ARABIC` در `ui/i18n/Strings.kt`؛ `LANG_AR` در `SUPPORTED_LANGS`، `langLabel/langFlag` عربی، `Loc.rtl` برای عربی هم true و ارقام لاتین می‌مانند. نام عربی ۲۸ حرفه در `data/sample/Catalog.kt` (`names["ar"]`) |
| دیزاین | پالت **v3 شیشه‌ای** در `ui/theme/Theme.kt` (`HeaderBrush`، توکن‌های شیشه `GlassFill/GlassFillSoft/GlassStroke/GlassBar`، `Bg` شفاف، `CardBg` نیمه‌شفاف) + `Modifier.brandBackdrop()` (مِش رنگی و هاله‌های بلور) در `ui/components/Ui.kt`، کارت‌ها/نوارها/هدرهای شیشه‌ای (`GlassBar`) در همه‌ی صفحه‌ها، نشان مرجانی و برچسب‌های گرادیانی در `ui/components/JobCard.kt`، نوار پایین شیشه‌ای شناور در `MainActivity.kt` — و در دمو لایه‌ی `DESIGN v3` با `backdrop-filter` واقعی |
| حداقل اندروید | Android 7 (API 24) — هدف: Android 15 (API 35) |
| خط اعتبار | در انتهای «ارتباط با ما» و در فوتر صفحه‌ی انتخاب زبان، عیناً: `powered by naser tavakkoli @nt.galaxy` — در `ui/screens/ContactScreen.kt` و `ui/screens/StartScreens.kt` (بدون ترجمه). **#28: کم‌رنگ و کم‌تر** — 9.5sp، `FontWeight.Medium/Normal`، `alpha ≈ .42–.72` |
| حذف آگهی | دکمه‌ی «حذف» اول پنجره‌ی تأیید نشان می‌دهد (`AlertDialog` **یکتا** با آگهی در انتظار: `pendingDelete`؛ `onDeleteJob` در `MainActivity` مستقیم حذف می‌کند و پنجره‌ی دومی ندارد) — «بله، حذف شود» / «انصراف». **دکمه‌ی افزودن آگهی (`AdminAddButton`) در نوار بالای مدیریت — کنار `AdminExitButton` — است؛ دیگر `FloatingActionButton`/دکمه‌ی شناور وجود ندارد** (در RTL دکمه‌ی «حذف» گوشه‌ی پایین-چپ است و با دکمه‌ی شناور هم‌پوشانی می‌کرد) |
| دکمه‌ی ❤ هدر | **#28:** در `JobDetailScreen` کنار عنوان آگهی، دکمه‌ی قلبی شیشه‌ای (`Filled.Favorite/FavoriteBorder`) برای پسندیدن بدون اسکرول |
| نشان پسندیده روی کارت | **#28:** `JobCard(liked = …)` → قرص «❤ پسندیده» کنار نشان درخواست؛ `HomeContent(likedIds)` و اتصال در `MainShell` |
| صفحه‌ی کامل آگهی | **#28:** کلیک روی هر آگهی → صفحه‌ی `Routes.JOB` (`JobDetailScreen`) با عنوان «صفحه‌ی آگهی»؛ مشخصات، گالری رسانه، حقوق، توضیحات، شرایط، تماس، «ارسال درخواست» و **دکمه‌ی «پسندیدم»** |
| بخش «پسندیده» | **#28:** `AppPrefs.likedIds()/toggleLiked` + `AppViewModel.likedIds/toggleLike`؛ دکمه در `JobDetailScreen(liked, onLike)`، نشان ❤ روی کارت، فهرست در `ProfileContent(likedJobs, onOpenJob, onUnlike)` و اتصال در `MainShell` |
| بازنشانی داده‌ها | **#28: حذف شد** — دکمه‌ی «بازنشانی داده‌های نمونه» (`onSeed`/`seed_data`) از تب «آگهی‌ها»ی پنل برداشته شد و کلید `seed_data` هم از `Strings.kt` پاک شد |
| چیدمان پنل مدیریت | سه کارت آماری «آگهی فعال / متقاضی / جدید» **زیر** دکمه‌های تب (بعد از `TabRow`) · در نوار بالای مدیریت دو دکمه: **`AdminAddButton`** (افزودن آگهی — گرادیان طلایی + آیکون `Add` + متن `add_listing`) و **`AdminExitButton`** (خروج؛ شیشه‌ای + آیکون `Logout` + متن) — هر سه تب یکسان، بدون هیچ دکمه‌ی شناور · `Scaffold` دیگر `floatingActionButton` ندارد و پدینگ پایین فهرست آگهی‌ها از ۹۶dp به ۲۴dp رسید |
| پنل مدیریت | تنظیمات = یوزرنیم/پسورد مدیر (به‌جای نام کارگزار؛ ذخیره در `AppPrefs.adminUsername/adminPassword`) — ورود مدیر در `AppViewModel.adminCredentialsMatch` · تفکیک متقاضیان با کلیک روی وضعیت‌ها (فیلتر در `AdminContent`/`visiblePeople`) · صفحه‌ی کامل متقاضی = `ApplicantDetailScreen` |
| رمز و 2FA | همه‌ی فیلدهای رمز با دکمه‌ی نمایش، **بدون هیچ ایموجی**: آیکون متریال `Visibility/VisibilityOff` (۲۰dp در دکمه‌ی ۳۸dp) که روی متن نمی‌افتد (`KbField(revealable = true)` در `ui/components/Ui.kt`) · کادرهای «تنظیمات» با قالب مشترک `AdmField`/`AdmLabel` · متن الزامی‌بودن تعهدنامه با رنگ نوک‌مدادی `Graphite #3B4753` · ورود دومرحله‌ای مدیر: `util/TotpUtil.kt` (TOTP/RFC 6238) + `AppPrefs.twoFactorEnabled/twoFactorSecret/twoFactorRecovery` + `AppViewModel.beginTwoFactorSetup/confirmTwoFactor/verifyTwoFactor/disableTwoFactor` و `LoginOutcome.ADMIN_2FA` در `ui/screens/LoginScreen.kt` |
| داده‌های نمونه | داخل خود برنامه؛ بدون Firebase هم کامل کار می‌کند |
| رسانه‌ی آگهی | `Job.media: List<JobMedia>` — عکس (image) و ویدیو (video) با پوستر؛ نمایش تمام‌صفحه با بزرگ‌نمایی (`ui/components/MediaViewer.kt`) و مدیریت در فرم آگهی (`ui/screens/FormScreens.kt`)؛ `data/repo/JobMediaStore.kt` فایل‌های گوشی را در حالت متصل به Firebase Storage آپلود و در حالت نمایشی داخل اپ کپی می‌کند |

### نقشه‌ی صفحه‌ها

1. **انتخاب زبان** — فارسی / Türkçe / English
2. **خوش‌آمدگویی** — لوگو، معرفی، دو دکمه: «شروع ثبت‌نام» و «ورود به برنامه»
3. **فرم ثبت‌نام ۴ مرحله‌ای** — هویت و تماس (نام، **نام پدر و کد ملی کنار هم**، **نام کاربری + رمز عبور**؛ نام کاربری فقط با حروف و اعداد انگلیسی و کیبورد روی همان فیلد انگلیسی می‌شود، تلفن با پیش‌شماره) ← **محل سکونت ایرانی** (استان / شهرستان / آدرس کامل) و تخصص‌ها (+ فیلد **«سایر حرفه‌ها»** زیر فهرست شغل‌ها) ← سطح ترکی ← مدارک (**عکس پاسپورت** + عکس قدی)، **تعهدنامه** و رضایت‌نامه
4. **بعد از ثبت‌نام مستقیم صفحه‌ی آگهی‌ها باز می‌شود** (صفحه‌ی جداگانه‌ی «ثبت‌نام موفق» حذف شده است)
5. **صفحه اصلی آگهی‌ها** — جدیدترین‌ها بالا، جست‌وجو، فیلتر شهر، فیلتر مناطق استانبول، ترتیب بر اساس تاریخ یا حقوق
6. **جزئیات آگهی** — حقوق، محل کار، تاریخ انتشار، شرایط، توضیحات، ارسال درخواست + **گالری عکس‌ها و ویدیوی آگهی**: هر آگهی می‌تواند چند عکس و یک ویدیو داشته باشد؛ در فهرست آگهی‌ها بندانگشتی‌ها روی کارت دیده می‌شوند و در برگه‌ی آگهی گالری کامل کنار مشخصات می‌آید. با یک لمس، عکس/ویدیو **تمام‌صفحه و بزرگ** می‌شود: عکس با دو انگشت یا دو ضربه بزرگ و با کشیدن جابه‌جا می‌شود، ویدیو با پخش‌کننده‌ی داخلی پخش می‌شود (بدون کتابخانه‌ی اضافه). مدیر در فرم آگهی با دکمه‌های «افزودن عکس/ویدیو»، انتخاب از گالری گوشی، یا افزودن با لینک، رسانه‌ها را مدیریت می‌کند؛ فایل‌های انتخابی در حالت متصل به Firebase در Storage آپلود و در حالت نمایشی داخل خود برنامه نگه داشته می‌شوند
7. **ورود به برنامه** (`LoginScreen`) — **یکی برای همه**: کاربر عادی با نام کاربری (یا شماره تلفن) و رمز خودش وارد **قسمت شخصی خودش** می‌شود؛ مدیر با نام کاربری مدیر (یا ایمیل حساب Firebase) و رمز، **مستقیم وارد پنل مدیریت** می‌شود. هیچ دکمه‌ی جداگانه‌ای برای پنل مدیریت و هیچ مسیر «ایجاد حساب جدید» در این بخش و در حالت مدیر وجود ندارد (ثبت‌نام فقط از صفحه‌ی خوش‌آمد).
8. **پروفایل من** — تخصص‌ها، سطح زبان، مدارک، درخواست‌ها، تعهدنامه‌ی امضاشده، تغییر زبان، خروج از حساب (این تب و تب «آگهی‌ها» فقط برای کاربران عادی است)
9. **ارتباط با ما** — تب سوم کاربران، کنار «پروفایل من»: هشدار «ما هیچ شعبه یا نماینده‌ای نداریم» و «تنها راه‌های ارتباطی گذاشته شده است»، سپس سه کارت با **لوگوی خودِ برنامه‌ها** (کانال تلگرام / واتس‌اپ / پیج اینستاگرام) که روی همه‌شان نام کاربری پیش‌فرض `(karbama34)` نوشته شده و لینکش هم نمایش داده می‌شود؛ **کل ردیف یک لینک است و لمس هر نقطه‌ای از آن (لوگو، عنوان، نام کاربری یا متن لینک) همان انتقال را انجام می‌دهد**؛ **ترتیب کار: ۱) خودِ برنامه با اسکیم اختصاصی + تعیین بسته‌ی همان برنامه (مستقیم تلگرام/واتس‌اپ/اینستاگرام باز می‌شود و وارد کانال/چت/پیج می‌شود، بدون انتخاب‌گر و بدون مرورگر) ۲) همان اسکیم به‌صورت عمومی ۳) اگر برنامه نصب نبود، لینک وب ۴) اگر هیچ‌کدام باز نشد، لینک در کلیپ‌بورد کپی و روی صفحه نشان داده می‌شود.** دکمه‌ی «کپی» هم لینک کامل را در کلیپ‌بورد می‌گذارد. مدیر این تب را نمی‌بیند (پنلش تمام‌صفحه است).
10. **برگه‌ی تعهدنامه (A4)** — سند «تعهدنامه تسویه حساب هزینه کارگزار» با نسبت دقیق A4؛ در مرحله‌ی ۴ ثبت‌نام (اجباری) و همچنین در پروفایل و پنل مدیریت باز می‌شود
11. **پنل مدیریت (تمام‌صفحه)** — وقتی مدیر با یوزرنیم و پسورد خودش وارد می‌شود، تب‌های پایینِ «آگهی‌ها» و «پروفایل من» حذف می‌شوند و فقط پنل با تب‌های داخلی خودش (آگهی‌ها / متقاضیان / تنظیمات) می‌آید؛ دکمه‌ی ⏻ بالا هم مدیر را به صفحه‌ی ورود برمی‌گرداند. ثبت/ویرایش/حذف آگهی + فهرست متقاضیان + جست‌وجو بر اساس تخصص/شهر/سطح زبان + دیدن مدارک + دیدن تعهدنامه‌ی امضاشده + تغییر وضعیت (جدید / بررسی‌شده / معرفی‌شده به کارفرما) + تب تنظیمات کارگزار

---

## ۲) اجرای سریع (بدون Firebase)

```bash
# در Android Studio:  File → Open → پوشه‌ی Karbama
# سپس روی گوشی یا شبیه‌ساز Run بزن.
```

تا وقتی فایل `google-services.json` را نگذاری، برنامه در **حالت نمایشی** اجرا می‌شود:
آگهی‌ها و متقاضیان نمونه روی خود گوشی هستند و همه‌ی صفحه‌ها و فیلترها کار می‌کند.
رمز پیش‌فرض پنل مدیریت: **`karbama2026`**

---

## ۳) اتصال به Firebase (برای داده‌های واقعی)

### ۳-۱. ساخت پروژه
1. به [console.firebase.google.com](https://console.firebase.google.com) برو و یک پروژه بساز (مثلاً `karbama`).
2. در همان پروژه: **Add app → Android**
   - Package name: `com.karbama.app`  ← دقیقاً همین
   - نام مستعار: Karbama
   - اثر انگشت SHA-1 لازم **نیست** (چون فقط از ورود ناشناس و ایمیل/رمز استفاده می‌کنیم).
3. فایل `google-services.json` را دانلود کن و در پوشه‌ی **`app/`** بگذار:
   ```
   Karbama/app/google-services.json
   ```
4. در Android Studio → **Sync Project with Gradle Files**. تمام.

### ۳-۲. فعال‌کردن سرویس‌ها
| سرویس | کار لازم |
|---|---|
| **Authentication** | Sign-in method → **Anonymous** و **Email/Password** را Enable کن |
| **Firestore Database** | Create database → حالت **Production** |
| **Storage** | Get started |

### ۳-۳. قواعد امنیتی (مهم!)
- محتوای فایل `firestore.rules` این پروژه را در Firestore → Rules جای‌گذاری و **Publish** کن.
- محتوای فایل `storage.rules` را در Storage → Rules جای‌گذاری کن و **قبل از Publish** مقدار `ADMIN_UID_1` را با شناسه‌ی کاربری مدیر عوض کن.

### ۳-۴. ساخت «حساب مدیر»
1. Firebase Console → Authentication → Users → **Add user** (ایمیل و رمز دلخواه، مثلاً `admin@karbama.com`).
2. از فهرست، **User UID** را کپی کن.
3. Firestore Database → **Start collection** با نام `admins` → **Document ID** را همان UID بگذار و فیلد `role` = `owner` را ذخیره کن.
4. در برنامه: صفحه‌ی خوش‌آمد → **«ورود به برنامه»** → در فیلد نام کاربری **ایمیلِ حساب مدیر** (مثلاً `admin@karbama.com`) و در فیلد رمز همان رمز را بزن.
   بدون دکمه‌ی اضافه، **مستقیم پنل مدیریت** بالا می‌آید و از این پس پنل، **متقاضیان واقعی** را نشان می‌دهد.

> اگر می‌خواهی آگهی‌های نمونه را داخل Firestore بریزی، در پنل مدیریت (حالت واقعی) یک‌بار روی «بازنشانی داده‌های نمونه» بزن.

### ۳-۴-۱. ورود کاربران عادی
هر متقاضی موقع ثبت‌نام (مرحله‌ی ۱) یک **نام کاربری** (حداقل ۳ حرف، یکتا) و یک **رمز عبور** (حداقل ۴ کاراکتر) انتخاب می‌کند.
رمز هرگز به‌صورت خام ذخیره نمی‌شود؛ فقط **هش SHA-256** آن (`passwordHash`) در پروفایل ذخیره می‌شود.
برای ورود، کاربر از صفحه‌ی خوش‌آمد روی **«ورود به برنامه»** می‌زند و با **نام کاربری یا شماره تلفن** خودش + رمز وارد می‌شود و مستقیم **قسمت شخصی خودش** را می‌بیند.
بدون Firebase هم همین ورود کار می‌کند (روی داده‌های همان گوشی).

### ۳-۵. ساختار داده در Firestore
```
jobs/{jobId}
   titleKey, titleCustom, company, city, district, salary, contract,
   conditions[], description, requirements[], contactPhone, active,
   publishedAt (عدد: میلی‌ثانیه), createdBy

applicants/{uid}
   uid, firstName, lastName, fatherName, nationalId (کد ملی), username, passwordHash (SHA-256),
   otherJobs (حرفه‌های دیگر، متن آزاد),
   phone, phoneCountry (TR|IR), phoneLocal,
   city (استان ایران), district (شهرستان), address (آدرس کامل), jobKeys[],
   trLevel (۰..۱۰۰), gender, idPhotoUrl, bodyPhotoUrl,
   status: new | reviewed | referred, consent, createdAt, lang
   undertaking: {
      signed, date (yyyy/MM/dd — روز ثبت‌نام), signedAt,
      nationalId, payerCard, fee, brokerCard,
      signature   ← امضای الکترونیکی، data:image/png;base64,...
   }
   email ← دیگر در فرم ثبت‌نام گرفته نمی‌شود؛ فیلد برای سازگاری با داده‌های قبلی
           در مدل مانده و اگر خالی باشد نمایش داده نمی‌شود.

admins/{uid}        ← فقط از کنسول ساخته می‌شود
```
عکس‌ها: `applicants/{uid}/id_card.jpg` و `applicants/{uid}/body_photo.jpg`

---

## ۴) چیزهایی که ممکن است بخواهی عوض کنی

| چه چیزی | کجا |
|---|---|
| نام کاربری و رمز مدیر (حالت بدون Firebase) | `data/Config.kt` → `ADMIN_USERNAME` (پیش‌فرض `admin`) و `ADMIN_PASSWORD` |
| حداقل طول نام کاربری/رمز و نمکِ هش | `util/AuthUtils.kt` |
| شماره تماس پیش‌فرض آگهی‌ها | همان فایل → `CONTACT_PHONE` |
| شهرها و مناطق ترکیه (محل کار در آگهی‌ها) | `data/sample/Catalog.kt` → `CITIES` و `DISTRICTS` |
| **۳۱ استان و شهرستان‌های ایران (محل سکونت متقاضی)** | `data/sample/Catalog.kt` → `IR_PROVINCES` و `countiesOf()` |
| کشورهای پیش‌شماره تلفن (الان ترکیه و ایران) | `util/PhoneUtils.kt` → enum `PhoneCountry` |
| قالب‌بندی و اعتبارسنجی شماره | `util/PhoneUtils.kt` → `PhoneUtils` |
| عناوین شغلی و ترجمه‌هایشان | `data/sample/Catalog.kt` → `JOB_TITLES` |
| متن‌های سه‌زبانه | `ui/i18n/Strings.kt` |
| گزینه‌های ترتیب فهرست (جدیدترین / بیشترین حقوق / کمترین حقوق) | `ui/AppViewModel.kt` تابع `filteredJobs` + `ui/screens/HomeScreen.kt` |
| رنگ‌های برند | `ui/theme/Theme.kt` |
| نام برنامه | `res/values/strings.xml` |
| **لوگو / آیکن برنامه** | تصویر رسمی در `res/mipmap-*/ic_launcher.png` (+ `ic_launcher_round` و `ic_launcher_foreground` برای آیکون تطبیقی اندروید ۸+) و لوگوی داخل اپ در `res/drawable-*/karbama_logo.png` — با `BrandLogo` در `ui/components/Ui.kt` نمایش داده می‌شود |
| نسخه‌های آماده‌ی لوگو برای فروشگاه/چاپ | پوشه‌ی `Logo/` در ریشه‌ی پروژه (`karbama-icon-1024.png`، `karbama-icon-512.png`، `karbama-logo-square.jpg`) |
| متن بندهای تعهدنامه (فارسی، رسمی) | `data/undertaking/UndertakingText.kt` → `CLAUSES` |
| مبلغ پیش‌فرض تعهدنامه | `data/Config.kt` → `DEFAULT_FEE` (تنظیمات پنل ندارد) |
| شماره کارت و نام کارگزارِ پیش‌فرض تعهدنامه | `data/Config.kt` → `DEFAULT_BROKER_CARD` + پنل مدیریت → تب **تنظیمات** (ذخیره در `AppPrefs`: `broker_card`، `broker_holder`) |
| مبلغ تعهدنامه‌ی هر متقاضی به‌صورت جداگانه | کارت «مبلغ این متقاضی» در پرونده‌ی هر متقاضی → `AppViewModel.setApplicantFee()` |
| ظاهر برگه‌ی A4 و بوم امضا | `ui/screens/UndertakingScreen.kt` |

> نوشتن متن‌ها از راست به چپ، انتخاب شهر/منطقه، جست‌وجو و فیلترها به‌صورت خودکار با زبان برنامه هماهنگ می‌شوند.

---

## ۵) گرفتن خروجی APK / AAB

```bash
# نسخه‌ی آزمایشی (APK برای نصب مستقیم)
./gradlew assembleDebug
# خروجی: app/build/outputs/apk/debug/app-debug.apk

# نسخه‌ی انتشار در Google Play
./gradlew bundleRelease
# خروجی: app/build/outputs/bundle/release/app-release.aab
```

برای انتشار واقعی، در `app/build.gradle.kts` بخش `signingConfigs` را با کلید امضای خودت تنظیم کن:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("karbama-release.jks")
        storePassword = "***"
        keyAlias = "karbama"
        keyPassword = "***"
    }
}
buildTypes { release { signingConfig = signingConfigs.getByName("release") } }
```

و `com.karbama.app` را در Google Play Console رزرو کن (نام پکیج را بعداً نمی‌شود عوض کرد).

---

## ۶) تعهدنامه‌ی تسویه حساب هزینه کارگزار

در مرحله‌ی آخر ثبت‌نام، بخش **«تعهدنامه»** (اجباری) قرار دارد. با زدن دکمه، یک برگه‌ی کامل A4 باز می‌شود:

**روی برگه چه چیزی هست؟**

| مورد | منبع |
|---|---|
| مبلغ هزینه کارگزار | **خودِ متقاضی هنگام پر کردن تعهدنامه روی برگه وارد می‌کند** (فیلد مبلغ در همان برگه، پیش‌پر با `Config.DEFAULT_FEE` = ۶۰۰۰ لیر). تا مبلغ وارد نشود، تعهدنامه تأیید نمی‌شود و بند اول هم نقطه‌چین می‌ماند |
| مبلغ این متقاضی | کارت «مبلغ این متقاضی» در پرونده‌ی متقاضی → **برای هر شخص جداگانه** قابل تغییر است (پس از ثبت مبلغ توسط خودش هم قابل اصلاح است) |
| نام و نام خانوادگی + شماره تماس | از مرحله‌ی ۱ ثبت‌نام (با فونت درشت‌تر روی برگه) |
| محل الصاق / اسکن کارت شناسایی | همان عکس کارت ملی که در همین مرحله بارگذاری شده |
| شماره ملی (کد ملی) | **از مرحله‌ی ۱ ثبت‌نام** (کنار نام پدر) — در فرم تعهدنامه دیگر پرسیده نمی‌شود و فقط روی برگه نمایش داده می‌شود |
| شماره کارت واریزکننده | در همان برگه از متقاضی پرسیده می‌شود (۱۶ رقم) |
| شماره کارت بانکی کارگزار | پیش‌فرض ثابت `1234 1234 1234 1234` (`Config.DEFAULT_BROKER_CARD`) — در تب «تنظیمات» پنل مدیریت **قابل تغییر** است و همان شماره روی برگه چاپ می‌شود |
| محل امضاء متقاضی | **امضای الکترونیکی**: با انگشت روی کاغذ امضا می‌کشد و به‌صورت تصویر PNG ذخیره می‌شود |
| تاریخ عقد قرارداد | همان روز ثبت‌نام (خودکار، غیرقابل تغییر) |
| نام پدر (در عبارت «فرزند …») | از مرحله‌ی ۱ ثبت‌نام (نام پدر) |
| آدرس کامل | از مرحله‌ی ۲ (محل سکونت ایران) |

**نکته:** عنوان سند با فونت درشت (۲۴sp) نوشته شده و نام و شماره تماس متقاضی روی برگه درشت‌تر از بقیه‌ی متن‌هاست. متن پنج بند تعهدنامه به فارسی رسمی و ثابت است (حتی وقتی رابط کاربری ترکی یا انگلیسی باشد) و فقط عنوان‌ها و برچسب‌ها ترجمه می‌شوند. سند در Firestore داخل فیلد `undertaking` پروفایل متقاضی ذخیره می‌شود (امضا به شکل data-URL تصویر).

---

## ۷) نکات حقوقی مهم (ترکیه)

- **KVKK** (قانون حفاظت از داده‌های شخصی ترکیه): جمع‌آوری عکس کارت ملی و عکس تمام‌قد داده‌ی حساس است.
  - متن رضایت‌نامه در فرم ثبت‌نام وجود دارد، اما بهتر است یک **سیاست حفظ حریم خصوصی (Gizlilik Politikası)** کامل هم بنویسی و در صفحه‌ی اول اپ لینکش را بگذاری.
  - داده را فقط برای هدف اعلام‌شده (معرفی به کارفرما) استفاده کن و برای کارفرما فقط پروفایلِ فردِ انتخاب‌شده را بفرست.
  - برای حذف حساب کاربری، یک راه ارتباطی (ایمیل) بگذار و درخواست حذف داده را در ۳۰ روز اجرا کن.
- **قانون کار ترکیه**: در آگهی، شرط سن/جنسیت/ملیت فقط وقتی مجاز است که واقعاً الزام شغلی باشد.

---

## ۸) ساختار پوشه‌ها

```
Karbama/
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json.example     ← نمونه؛ فایل واقعی را کنارش بگذار
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/karbama/app/
│       │   ├── KarbamaApp.kt            ← Application
│       │   ├── MainActivity.kt           ← ناوبری و تب‌ها
│       │   ├── data/                     ← مدل‌ها، Firebase، مخزن‌ها، تنظیمات
│       │   │   └── undertaking/UndertakingText.kt   ← متن رسمی پنج بند تعهدنامه
│       │   ├── ui/screens/UndertakingScreen.kt      ← برگه‌ی A4 + بوم امضای الکترونیکی
│       │   ├── ui/                       ← صفحه‌ها، کامپوننت‌ها، تم، ترجمه‌ها
│       │   └── util/                     ← ImageUtils، PhoneUtils، FormatUtils
│       └── res/                          ← آیکن، تم، متن‌ها
├── firestore.rules                       ← قواعد امنیتی Firestore
├── storage.rules                         ← قواعد امنیتی Storage
└── README.md
```

---

## ۹) پرسش‌های رایج

**نام کاربری و رمز مدیر در حالت بدون Firebase چیست؟**
`admin` و `karbama2026` (قابل تغییر در `data/Config.kt` → `ADMIN_USERNAME` و `ADMIN_PASSWORD`).
با وارد کردن آن‌ها در صفحه‌ی «ورود به برنامه»، بدون هیچ دکمه‌ی اضافه‌ای پنل مدیریت باز می‌شود.

**اگر کاربر نام کاربری‌اش را فراموش کند؟**
در این نسخه، مدیر از پنل مدیریت پرونده‌ی او را می‌بیند و می‌تواند مدارک را ببیند؛ اما تغییر رمز از داخل برنامه تعریف نشده است.
(برای نسخه‌ی بعدی: دکمه‌ی «فراموشی رمز» با ایمیل بازیابی Firebase.)

**بدون Firebase چه چیزهایی کار می‌کند؟**
همه‌ی صفحه‌ها، ثبت‌نام، فیلترها، پنل مدیریت و تغییر وضعیت متقاضیان — اما فقط روی همان گوشی.

**چطور یک زبان جدید اضافه کنم؟**
در `ui/i18n/Strings.kt` یک دیکشنری جدید بساز، در `DICTS` اضافه کن و در `SUPPORTED_LANGS` بنویس.

**می‌خواهم صفحه‌ها را چرخشی کنم:**
در `AndroidManifest.xml` داخل تگ `<activity>` بنویس: `android:screenOrientation="portrait"`.

**گرفتن خطای نوع داده در آگهی‌های جدید:**
مطمئن شو `publishedAt` را به‌صورت عدد (میلی‌ثانیه) ذخیره می‌کنی، نه Timestamp.

---

## #29 — پسندیده‌ها و درخواست‌های ارسالی روی سرور (دور جدید)

| چه چیزی | جزئیات |
|---|---|
| فایل تازه | `data/repo/UserActivityRepository.kt` — `ActivityItem` + `like/unlike/apply/myLikedJobIds/myAppliedJobIds/observeLikes/observeApplications/mergeIds` |
| کلکسیون‌ها | `Config.COL_LIKES = "likes"` و `Config.COL_APPLICATIONS = "applications"` با شناسه‌ی سند `{applicantId}_{jobId}` |
| نگاه‌داشتن آفلاین | `AppPrefs.setLikedIds/setAppliedIds` + (`liked_jobs`/`applied_jobs`)؛ هر تغییر هم محلی ذخیره می‌شود هم روی سرور |
| هم‌گام‌سازی پس از ورود | `AppViewModel.syncUserActivity()` — اتحاد پسندیده/درخواست‌های گوشی با سرور؛ در `init` (اگر کاربر قبلاً وارد شده) و در `openSession` صدا زده می‌شود |
| نمایش به مدیر | `ApplicantDetailScreen(likedItems, appliedItems, activityLive)` + `ActivityCard`؛ جریان زنده با `AppViewModel.watchApplicantActivity(id)` در `MainActivity` (روت متقاضی) |
| قاعده‌ها | `storage.rules` → `match /jobs/{jobId}/{fileName}` (خواندن: کاربران وارد‌شده، نوشتن: فقط مدیر، ≤۲۵MB، `image/*|video/*`) · `firestore.rules` → `likes/{docId}` و `applications/{docId}` (ساختن فقط برای صاحب سند، پاک‌کردن برای صاحب سند یا مدیر) |
| بدون ایندکس ترکیبی | کوئری‌ها فقط `whereEqualTo("applicantId", …)` دارند و مرتب‌سازی در برنامه انجام می‌شود تا Firebase ایندکس ترکیبی نخواهد |
| راهنما | `Karbama/FIREBASE-SETUP-fa.md` — پنج مرحله: پروژه، `google-services.json`، سه سرویس، حساب مدیر + `admins/{uid}`، Publish قواعد |
| تست | `kb-tools/check-29-activity.js` (۱۸ ✅ / ۰ ❌ در ۴۲۰px و ۳۶۰px) + تصاویر `kb-tools/shots/s29-person-activity-fa.png` و `…-ar.png` |

---

## #33 — پاس دیزاین اندروید (رنگ · فونت · فاصله)

| چه چیزی | جزئیات |
|---|---|
| تنها منبع تم | `ui/theme/Theme.kt` — پالت کامل (برند، متن، لهجه‌ها، وضعیت‌ها، شیشه) + گرادیان‌ها + شبکه‌ی فاصله `S1..S6` (۴·۸·۱۲·۱۶·۲۰·۲۴) + گردی گوشه `RSm/RChip/RField/RCard/RSheet/RPill` |
| بازخوانی متن | `Ink3` از `0xFF8496A6` (۳.۰۵:۱) به `0xFF57697A` (۵.۰:۱)؛ رنگ‌های هشدار `WarnText/WarnDeep/WarnStrong` و `Coral/Pink/Blue` تیره‌تر شدند |
| گرادیان هدر | `BrandBrush`/`HeaderBrush` از فیروزه‌ای روشن به `#088080 → #0A7F91 → #087A8A` تا متن سفید کنتراست ≥ ۴.۵:۱ داشته باشد |
| رنگ‌های ثابت در کد | ۶۲ مورد `Color(0xFF…)` به **نام توکن** تبدیل شد (فقط رنگ لوگوهای تلگرام/واتساپ/اینستاگرام دست‌نخورده ماند) |
| مقیاس تایپ | `Typography` با lineHeight کامل؛ همه‌ی `fontSize`ها روی مقیاس ۱۱…۲۱ (۱۷sp → ۱۸sp) |
| شبکه‌ی فاصله | ۱۸۱ مقدار `padding/spacedBy` روی مضرب ۴ پیکسل (فاصله‌های بزرگ طرح دست‌نخورده) |
| حذف نوار حالت نمایشی | نوار «🟡 حالت نمایشی / 🟢 متصل به Firebase» از `AdminScreens` برده شد؛ کلیدهای `demo_banner`/`live_banner` از چهار دیکشنری i18n پاک شدند |
| دکمه‌ی خروج از پنل | زمینه‌ی تیره‌تر (`#06323A` با آلفا ۰.۴۶) تا متن سفید خوانا بماند |
| اسکریپت | `kb-tools/design-pass-android.py` (idempotent — هر بار اجرا مشکلی ایجاد نمی‌کند) |
| تست | `:app:compileDebugKotlin` ✅ و `assembleDebug assembleRelease` → `BUILD SUCCESSFUL in 4m 52s` · dex: `demo_banner=0`, `live_banner=0`, `seed_data=0` |

---

## #34 — قفل آگهی‌ها تا پرداخت هزینه (تأیید مدیر)

| چه چیزی | جزئیات |
|---|---|
| مدل | `Applicant.payStatus` (`""` پرداخت‌نشده · `"pending"` در انتظار · `"paid"` تأییدشده) + `payAt`؛ در Firestore با فیلدهای **`paymentStatus` / `paymentAt`** ذخیره می‌شود (`FirestoreMapper` + `AppPrefs` برای حالت بدون Firebase) |
| مبلغ | `Config.DEFAULT_ADS_FEE = 6000` و `AppPrefs.adsFee`؛ در تب «تنظیمات» پنل مدیر با کارت **«💳 هزینه نمایش آگهی‌ها»** قابل تغییر است (مستقل از مبلغ تعهدنامه) |
| دروازه‌ی نمایش | `AppViewModel.adsUnlocked` — مدیر همیشه، کارجو فقط با `payStatus == "paid"`؛ در `MainActivity` تب «آگهی‌ها» (برچسب 🔒)، صفحه‌ی کامل آگهی و فهرست «پسندیده» پشت همین دروازه‌اند |
| صفحه‌ی پرداخت | `ui/screens/PaywallScreen.kt` — مبلغ، کارت کارگزار، نام، **کد پیگیری یکتا** (`Applicant.payTrackCode` = `KB-XXXXX`)، دکمه‌ی «واریز کردم» و دکمه‌ی «بررسی مجدد وضعیت» (`refreshMyPayment`) |
| پنل مدیر | `PayBadge` روی کارت هر متقاضی + بخش «وضعیت پرداخت» در `ApplicantDetailScreen` با **«✅ تأیید پرداخت» / «↩︎ لغو تأیید»** (`AppViewModel.setApplicantPayment` → `ApplicantRepository.setPayment`) |
| امنیت | `firestore.rules` → `applicants/{uid}`: کارجو فقط می‌تواند `paymentStatus` را `""`/`"pending"` بگذارد؛ نوشتن `"paid"` تنها با `isAdmin()` مجاز است |
| تست | `kb-tools/check-34-paywall.js` → ۲۸ ✅ / ۰ ❌ · `:app:assembleDebug :app:assembleRelease` → `BUILD SUCCESSFUL in 6m 15s` · dex: `paymentStatus`, `paymentAt`, `pay_confirm`, `pay_ads_fee`, `DangerText` |

---

## #35 — مسیر نهایی پرداخت (صفحه‌ی اصلی دست‌نخورده)

| مرحله | جزئیات |
|---|---|
| ۱) صفحه‌ی اصلی | `HomeContent` همان صفحه‌ی قبلی است (سرتیتر، `search`, چیپ‌ها، «{n} آگهی یافت شد»)؛ فقط بخش آگهی‌ها با `adsLocked` به `AdsLockPanel` تبدیل می‌شود (`ui/screens/PaywallScreen.kt`) |
| ۲) صفحه‌ی پرداخت | روت تازه‌ی `Routes.PAY` → `PaymentScreen(...)`: مبلغ، کارت کارگزار، نام، کد پیگیری، دکمه‌ی «واریز کردم» |
| ۳) تأییدیه/رسید | همان `PaymentScreen` با `payStatus == "pending"\|"paid"`: «🧾 تأییدیه پرداخت»، نشان وضعیت، **مراحل ۱·۲·۳** (`PaySteps`) و دکمه‌های «بررسی مجدد»/«دیدن آگهی‌ها»/«بازگشت به صفحه‌ی اصلی» |
| ۴) مدیر | `AdminScreens`: نشان `PayBadge` روی کارت متقاضی + بخش «وضعیت پرداخت» با «✅ تأیید پرداخت»/«↩︎ لغو تأیید» → `AppViewModel.setApplicantPayment` |
| ۵) باز شدن آگهی‌ها | `AppViewModel.adsUnlocked` (فقط `payStatus == "paid"` یا مدیر) — تا تأیید، پنل قفل و تب 🔒 پابرجاست |
| i18n | کلیدهای `pay_page_t`, `pay_back`, `pay_locked_note`, `pay_view_status`, `pay_open`, `pay_receipt`, `pay_reg_t`, `pay_recheck`, `pay_continue`, `pay_step1..3`, `pay_after_paid` در چهار زبان (fa/tr/en/ar) |
