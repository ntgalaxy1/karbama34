# پیش‌پرواز Google Play — Karbama v10.0.3 (پیش‌نویس غیرمنتشرشده)

**وضعیت این سند در ۲۳ اکتبر ۲۰۲۶:** این یک کاربرگ فنی/عملیاتی است، نه سیاست حریم خصوصی نهایی یا مشاورهٔ حقوقی. نسخهٔ `10.0.3` هنوز commit، push، tag، build ابری، آپلود Play Console یا منتشر نشده است. تا زمانی که موارد «مسدودکننده» پایین بسته نشده‌اند، هیچ ادعایی مبنی بر آمادگی کامل برای انتشار نباید شود.

## ۱) نتیجهٔ فنی این patch

| مورد | وضعیت در patch محلی | نکتهٔ عملیاتی |
|---|---|---|
| `compileSdk` و `targetSdk` | ۳۶ | برای الزام Android 16/API 36 Google Play در ۳۱ اوت ۲۰۲۶ آماده شده است. |
| نسخهٔ اندروید | `versionCode=10003` / `versionName=10.0.3` | از `10002 / 10.0.2` یکتا و صعودی است. |
| APK امضاشده | حفظ شده | همان مسیر بررسی signer دائمی، `assembleRelease` و GitHub Release فقط برای APK باقی می‌ماند. |
| AAB امضاشده | اضافه شده | workflow اکنون `bundleRelease` را با **همان signer دائمی GitHub** می‌سازد، certificate آن را با SHA-256 متغیر GitHub دوباره تطبیق می‌دهد و AAB را فقط به‌صورت private Actions artifact نگه می‌دارد. |
| انتشار AAB در GitHub Release | عمداً انجام نمی‌شود | AAB نه به Git اضافه می‌شود و نه GitHub Release asset خواهد بود؛ فقط APK آن‌جا منتشر می‌شود. |
| ماندگاری AAB | ۷ روز | نام artifact: `Karbama-v10.0.3-play-bundle` در یک اجرای موفق workflow. پیش از انقضا آن را فقط از Actions دانلود و در Play Console آپلود کنید. |
| درخواست حذف داخل اپ | اضافه شده، ولی «درخواست» است نه حذف خودکار | کاربرِ واردشده در Firebase از Profile پس از تأیید آگاهانه یک سند حداقلی `accountDeletionRequests/{uid}` ایجاد می‌کند. هیچ پروفایل، عکس یا مدرکی در آن سند کپی نمی‌شود. |

### مسیر AAB پیشنهادی (فقط پس از اجازهٔ جداگانه)

1. اجرای workflow با `tag=v10.0.3` و `dry_run=true`؛ خروجی test/APK/AAB/signer را بررسی کنید.
2. در اجرای dry-run موفق، artifact خصوصی AAB را دانلود و `SHA-256` آن را ثبت کنید؛ هرگز کلید یا keystore را دانلود/نمایش ندهید.
3. **قبل از نخستین upload به Play**، موجودبودن یک backup بازیابی‌پذیر و خارج از GitHub از keystore/alias/password signer دائمی را مالک حساب به‌طور مستقل تأیید کند. این patch نه کلید را می‌خواند و نه backup می‌سازد.
4. در اولین release Play، Play App Signing را با همین AAB/upload key راه‌اندازی کنید. تغییر کلید یا ایجاد signer تازه، مسیر update مستقیم برای نصب‌های v10.0.1/v10.0.2 را به خطر می‌اندازد.
5. ابتدا track مناسب (برای حساب شخصی جدید: closed testing، با شرایط فعلی Play) را انتخاب کنید؛ production را تا تمام تأییدهای این سند باز نکنید.

> Workflow پیش‌نیازهای موجود `KARBAMA_RELEASE_KEYSTORE_BASE64`، `KARBAMA_RELEASE_KEYSTORE_SHA256`، `KARBAMA_RELEASE_STORE_PASSWORD`، `KARBAMA_RELEASE_KEY_ALIAS`، `KARBAMA_RELEASE_KEY_PASSWORD` و variable `KARBAMA_RELEASE_CERT_SHA256` را مصرف می‌کند. هیچ مقدار یا secret جدیدی در این patch افزوده/تغییر/چاپ نمی‌شود.

## ۲) مسدودکننده‌های مالک Play Console و امضا

| پرسش که هنوز پاسخ قطعی ندارد | اقدام لازم پیش از upload | چرا مهم است |
|---|---|---|
| حساب توسعه‌دهنده شخصی است یا سازمانی، و identity/device verification آن کامل است؟ | در Play Console، Account details و verification status را بررسی و روش پرداخت ثبت‌نام یک‌باره را تکمیل کنید. | نوع حساب و شرایط تست/دسترسی production را تعیین می‌کند. |
| آیا backup خارج از GitHub از signer دائمی قابل بازگردانی است؟ | مالک کلید، محل نگهداری رمزگذاری‌شده، alias و روش بازگردانی را **بدون ارسال secret در chat/Git** آزمایش و ثبت داخلی کند. | پس از enrollment در Play، از دست دادن upload key مسیر بازیابی و انتشارهای بعدی را دشوار می‌کند. |
| آیا package name `com.ntgalaxy.karbama` در حساب صحیح Play Console ثبت شده است؟ | بله؛ پیش از upload از همان app record استفاده کنید. | package name پس از ثبت قابل انتقال/تغییر ساده نیست. |

## ۳) کاربرگ Privacy Policy و Data Safety — مبنای بازبینی، نه پاسخ نهایی Console

بررسی زیر از کد Android و قواعد Firestore این نسخه است. مالک محصول باید با عملیات واقعی Firebase، پرداخت، مدیران، hosting و تصمیم حقوقی/KVKK آن را بازبینی کند. در Console گزینه‌ها را حدس نزنید و فقط همان چیزهایی را انتخاب کنید که واقعاً در production فعال‌اند.

### ۳-۱. داده‌هایی که در source دیده می‌شوند

| گروه Data Safety احتمالی | نمونهٔ فیلد/جریان در source | محل/هدف ظاهری | پرسش‌هایی که مالک باید قبل از اعلام پاسخ دهد |
|---|---|---|---|
| اطلاعات هویتی و شخصی | نام/نام خانوادگی، نام پدر، نام کاربری، کد ملی، جنسیت، شماره تلفن، زبان رابط | `applicants/{uid}`؛ تشکیل پرونده و کاریابی | مبنای حقوقی و مدت نگهداری هر فیلد چیست؟ چه کسی به پرونده دسترسی دارد؟ |
| آدرس و محل سکونت | استان، شهرستان و آدرس کامل | `applicants/{uid}`؛ پروفایل/معرفی شغلی | آیا همهٔ اجزا واقعاً لازم‌اند و به کارفرما منتقل می‌شوند؟ |
| مدارک و عکس‌ها | تصویر مدرک هویتی، تصویر تمام‌قد، تصویر رسید پرداخت | `applicantFiles/{uid}/files/{id_card,body_photo,payment_receipt}` در Firestore | سیاست دسترسی، retention و حذف برای مدارک حساس چیست؟ |
| اطلاعات مالی/قراردادی | شماره کارت پرداخت‌کننده در تعهدنامه، مبلغ/کارت کارگزار، امضای الکترونیکی، کد رهگیری پرداخت، رسید | undertaking داخل پروفایل و فیش دستی | کدام سوابق را قانون/قرارداد باید نگه دارد و برای چه مدت؟ |
| فعالیت در اپ | آگهی‌های پسندیده، درخواست‌های شغلی، زمان ایجاد | `likes` و `applications` | آیا این داده به کارفرما/مدیر نمایش داده می‌شود و تا چه مدت؟ |
| شناسهٔ حساب | Firebase UID؛ نام کاربری به‌صورت email فنی `username@users.karbama.invalid` فقط برای Firebase Email/Password Auth | Firebase Authentication و Firestore | این email واقعی از کاربر گرفته یا نمایش داده نمی‌شود، اما تصمیم Data Safety دربارهٔ شناسهٔ حساب باید با واقعیت Firebase تطبیق داده شود. |
| سوابق پرداخت سروری در صورت فعال‌سازی آینده | `paymentSessions` با UID، روش/مبلغ، زمان، وضعیت و provider token | Cloud Functions / Firestore؛ اکنون پرداخت live در source عمداً safety-latched است | اگر این قابلیت واقعاً فعال شد، providerها/انتقال/retention و disclosure را جداگانه بازبینی کنید. |

### ۳-۲. نکات قابل اثبات از source، با محدودیت

- Manifest فقط `INTERNET` و `ACCESS_NETWORK_STATE` درخواست می‌کند؛ permission مکان دستگاه در آن دیده نمی‌شود. نقشهٔ OSM برای انتخاب/نمایش مکان هتل از دادهٔ ذخیره‌شده استفاده می‌کند، نه GPS کاربر.
- email واقعی در ثبت‌نام یا ویرایش پروفایل پرسیده نمی‌شود؛ آدرس فنی Firebase Auth نمایش یا به‌عنوان فیلد پروفایل پر نمی‌شود.
- SharedPreferences حاوی profile محلی از cloud backup و device transfer خارج شده است؛ این به معنی نبودن دادهٔ محلی نیست.
- قواعد و source به‌تنهایی جای متن عمومیِ قابل‌دسترس، نام/نشانی data controller، فهرست processorها، زمان نگهداری، مبنای قانونی، یا پاسخ قطعی «encrypted in transit / shared» در Data Safety را نمی‌گیرند.

### ۳-۳. متن/اطلاعاتی که برای سیاست عمومی باید از مالک گرفته شود

قبل از ساخت و host کردن URL، این جای‌خالی‌ها باید با حقیقت کامل شوند:

1. نام حقوقی data controller، کشور/نشانی ثبت، ایمیل و راه تماس قابل‌پاسخ برای حریم خصوصی/KVKK.
2. هدف هر دسته داده، مبنای قانونی/رضایت، دریافت‌کنندگان (مدیران، کارفرمایان، Firebase/Google و هر payment provider)، محل پردازش و انتقال بین‌المللی احتمالی.
3. retention دقیق: پرونده، مدرک، تعهدنامه/امضا، رسید/سوابق مالی، گزارش امنیتی و backupها؛ همچنین فرایند استثناهای قانونی.
4. دستور روشنِ درخواست دسترسی/اصلاح/حذف، روش احراز هویت درخواست‌کننده، زمان پاسخ، اعتراض و مرجع مرتبط.
5. URL عمومی HTTPS و بدون نیاز به login. همان URL باید در Play Console و داخل خود اپ نمایش داده شود. در source فعلی URL نهایی یا لینک in-app قرار نگرفته، زیرا مالک هنوز URL/متن تأییدشده نداده است.

**نتیجه:** این سند را در Play Console به‌عنوان Privacy Policy paste نکنید و تا URL عمومی واقعی و متن بازبینی‌شده ندارید، پاسخ‌های Data Safety را final نکنید.

## ۴) حذف حساب و داده

### آنچه v10.0.3 انجام می‌دهد

- مسیر Profile → «حذف حساب و داده‌ها» فقط وقتی Firebase متصل باشد نمایش داده می‌شود.
- با AlertDialog آگاهانه، یک درخواست حداقلی به `accountDeletionRequests/{uid}` با `uid`، وضعیت `requested` و زمان درخواست ثبت می‌شود.
- Firestore rules اجازهٔ ایجاد را فقط به همان کاربر واردشده و فقط اگر پروفایل او وجود داشته باشد می‌دهد؛ کاربر نمی‌تواند وضعیت را تغییر دهد یا دادهٔ پروفایل/مدرک را در ticket کپی کند. فقط مدیر می‌تواند ticketها را فهرست/بررسی/تکمیل کند.
- متن UI صریحاً می‌گوید درخواست فوری همهٔ داده‌ها را پاک نمی‌کند و retention مجاز/الزامی طبق privacy policy بررسی خواهد شد.

### چیزی که هنوز برای انطباق کامل لازم است

این flow، **راه آغاز in-app** است؛ اما نه لینک حذف بیرونی دارد و نه fulfilment خودکار/قابل‌ممیزی. بنابراین به‌تنهایی جایگزین الزامات Play نیست.

| کار لازم | مسئول/پیش‌نیاز | وضعیت |
|---|---|---|
| یک صفحهٔ HTTPS عمومی برای «Account deletion request» بسازید، بدون الزام به نصب یا ورود به اپ | مالک محصول + hosting + متن حقوقی | مسدودکننده؛ URL هنوز داده نشده است. |
| URL صفحهٔ حذف را در Play Console ثبت و در Privacy Policy/in-app link اعلام کنید | مالک Play Console | مسدودکننده |
| Firestore rules این patch را deploy کنید | مدیر Firebase مجاز | هنوز deploy نشده؛ بدون آن دکمهٔ درخواست در live app کار نخواهد کرد. |
| SOP مدیر برای ticketها تعیین کنید: احراز هویت، ثبت تصمیم retention، زمان پاسخ، اطلاع‌رسانی نتیجه | مالک حقوقی/عملیاتی | مسدودکننده؛ این patch هنوز فهرست/پردازش ticket در UI پنل مدیریت ندارد، پس فقط ابزار مجاز Firebase/Admin SDK نباید به‌عنوان فرایند نهایی بدون SOP استفاده شود. |
| پس از تصمیم retention، داده‌های مشمول حذف را با ابزار مدیرِ دارای Admin SDK/Console به‌شکل ثبت‌شده حذف کنید: applicant profile، سه applicant file شناخته‌شده، likes، applications، `uniqueKeys` متعلق به UID و در صورت مجازبودن Firebase Auth identity | مدیر Firebase مجاز | به‌صورت خودکار در patch انجام نمی‌شود. |
| payment sessions / رسیدها / undertakingها را طبق تصمیم حقوقی بررسی کنید | مالک حقوقی + مدیر Firebase | `paymentSessions` client-write/delete ندارد و ممکن است retention مالی/قراردادی داشته باشد؛ حذف کور و ادعای «همه چیز حذف شد» مجاز نیست. |
| cache/local data روی دستگاه درخواست‌کننده را در فرایند پاسخ مدیریت کنید | SOP و UX بعدی | logout به‌تنهایی پاک‌سازی همه cacheها نیست؛ در نسخهٔ بعدی می‌توان کنترل «clear local data after fulfilment» افزود. |

## ۵) چک‌لیست Console، تست و listing

### هویت و track

- [ ] نوع حساب (personal / organization)، identity verification و device verification در Play Console تأیید شده است.
- [ ] اگر حساب personal جدید است، الزام closed test فعلی Play (تعداد tester و مدت continuous opt-in) را از Console همان حساب تأیید و انجام داده‌اید.
- [ ] گروه tester، ایمیل testerها و دستور تست بدون افشای admin credential آماده است.
- [ ] Play App Signing و همان certificate/signing lineage پس از build dry-run تأیید شده است.

### App access و reviewer notes

- [ ] برای reviewer یک حساب کارجوی آزمایشی یا دستور ایجاد حساب فراهم شود؛ credential مدیر در UI عمومی نمایش داده نشود.
- [ ] توضیح دهید که مشاهدهٔ آگهی‌ها به ثبت‌نام، ارسال رسید/درخواست پرداخت و تأیید مدیر وابسته است؛ اگر امکان تست بدون پرداخت واقعی می‌دهید، آن روش را دقیق و زمان‌دار بیان کنید.
- [ ] reviewer notes شامل مسیر Profile → درخواست حذف حساب و URL عمومی حذف (پس از آماده‌شدن) باشد.
- [ ] اگر Firebase/شبکه در review لازم است، دسترسی rules و project production را پیش از submission با یک account غیرمدیر تست کنید.

### Store listing و content

- [ ] نام برنامه، short description، full description، category و email/contact واقعی را از مالک دریافت کنید.
- [ ] icon 512×512، feature graphic 1024×500 و screenshots واقعی از build v10.0.3 آماده شوند؛ screenshotهای قدیمی را فقط در صورت تطابق کامل UI/flow استفاده کنید.
- [ ] Content rating questionnaire را با قابلیت‌های واقعاً live تکمیل کنید؛ به payment، user-generated/employment content و دادهٔ حساس با دقت پاسخ دهید.
- [ ] «Contains ads» را صرفاً با تعریف Play و SDK/نمایش تبلیغاتی واقعی تعیین کنید؛ آگهی استخدامیِ هستهٔ محصول را بدون بررسی به‌عنوان advertisement یا non-ad علامت نزنید.
- [ ] Privacy Policy URL، Data Safety، App access، target audience، content declarations و countries/pricing همگی با build/عملیات production منطبق بازبینی شده‌اند.

## ۶) اثباتی که باید قبل از push/release ثبت شود

1. `git diff --check`، بررسی نسخه/API و بررسی ساختار YAML.
2. build/test معتبر: ترجیحاً hosted Actions dry-run با signer دائمی، `testDebugUnitTest`، `assembleRelease` و `bundleRelease` موفق.
3. verify APK با `apksigner` و AAB با `jarsigner`/`keytool` در workflow؛ fingerprint هر دو با `KARBAMA_RELEASE_CERT_SHA256` برابر است.
4. click-through دستی روی Profile → درخواست حذف → تأیید → ایجاد ticket با یک applicant test؛ سپس unauthorized-create/update/list با user دیگر را طبق rules آزمون کنید.
5. قبل از هر production track، privacy/deletion URLهای عمومی را از یک مرورگر بدون login باز و نتیجه را نگهداری کنید.

## ۷) چیزهایی که این patch عمداً انجام نمی‌دهد

- ورود به Play Console، ساخت developer account، پرداخت fee یا تغییر هویت/تنظیمات آن.
- خواندن، export یا جایگزینی keystore/signing secret؛ ساخت backup کلید.
- push/tag/release/Actions dispatch یا upload AAB به Play.
- deploy Firebase rules/Functions یا دسترسی به Firebase/secretها.
- ادعای final دربارهٔ Data Safety، encryption، sharing، retention یا حذف فوری کامل.

---

**مالک باید پیش از اجازهٔ انتشار، دو اطلاعات را تأیید کند:**

1. نام/نوع/وضعیت verification حساب Play Console و اینکه آیا شرایط test-track حساب انجام شده است.
2. محل backup امن signer دائمی و اینکه یک روش بازگردانی آن خارج از GitHub به‌طور مستقل آزموده شده است.
