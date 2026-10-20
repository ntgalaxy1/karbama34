package com.karbama.app.data.sample

import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Undertaking
import com.karbama.app.data.model.Condition
import com.karbama.app.data.model.Contract
import com.karbama.app.data.model.Job
import com.karbama.app.data.model.JobMedia
import com.karbama.app.data.model.MediaType
import com.karbama.app.data.model.Status

/**
 * کاتالوگ داده‌های پایه: شهرها، مناطق، عناوین شغلی و داده‌های نمونه.
 */

data class JobTitle(val key: String, val icon: String, val names: Map<String, String>) {
    fun name(lang: String): String = names[lang] ?: names["fa"] ?: key
}

object Catalog {

    val CITIES = listOf(
        "İstanbul", "Ankara", "İzmir", "Bursa", "Antalya", "Kocaeli",
        "Adana", "Mersin", "Gaziantep", "Konya", "Kayseri", "Samsun", "Tekirdağ"
    )

    /** مناطق استانبول و چند استان مهم */
    val DISTRICTS: Map<String, List<String>> = mapOf(
        "İstanbul" to listOf(
            "Fatih", "Esenyurt", "Bağcılar", "Bahçelievler", "Küçükçekmece", "Beyoğlu",
            "Şişli", "Beşiktaş", "Kadıköy", "Üsküdar", "Ataşehir", "Maltepe", "Pendik",
            "Kartal", "Sultangazi", "Zeytinburnu", "Başakşehir", "Avcılar", "Beylikdüzü",
            "Ümraniye", "Kağıthane", "Eyüpsultan", "Sarıyer", "Bakırköy", "Gaziosmanpaşa",
            "Esenler", "Arnavutköy", "Tuzla", "Şirinevler", "Bayrampaşa", "Çekmeköy",
            "Sancaktepe", "Sultanbeyli", "Büyükçekmece", "Silivri", "Çatalca"
        ),
        "Ankara" to listOf("Çankaya", "Keçiören", "Mamak", "Yenimahalle", "Etimesgut", "Sincan", "Altındağ", "Pursaklar"),
        "İzmir" to listOf("Bornova", "Konak", "Karşıyaka", "Buca", "Gaziemir", "Çiğli", "Bayraklı", "Torbalı"),
        "Bursa" to listOf("Nilüfer", "Osmangazi", "Yıldırım", "İnegöl", "Gemlik"),
        "Antalya" to listOf("Muratpaşa", "Kepez", "Konyaaltı", "Alanya", "Manavgat", "Kumluca"),
        "Kocaeli" to listOf("Gebze", "İzmit", "Darıca", "Körfez", "Çayırova", "Kartepe")
    )

    fun districtsOf(city: String): List<String> = DISTRICTS[city] ?: emptyList()

    // ==========================================================
    // محل سکونت متقاضی — ۳۱ استان ایران و شهرستان‌های هر استان
    // (نام‌ها فارسی ثابت است، چون آدرس و سند رسمی فارسی است)
    // ==========================================================
    val IR_PROVINCES: List<Pair<String, List<String>>> = listOf(
        "آذربایجان شرقی" to listOf("تبریز", "مراغه", "مرند", "اهر", "میانه", "شبستر", "سراب", "بناب", "جلفا", "اسکو", "هریس", "آذرشهر"),
        "آذربایجان غربی" to listOf("ارومیه", "خوی", "میاندوآب", "مهاباد", "بوکان", "نقده", "سلماس", "پیرانشهر", "ماکو", "سردشت", "تکاب", "اشنویه"),
        "اردبیل" to listOf("اردبیل", "پارس‌آباد", "مشگین‌شهر", "خلخال", "گرمی", "بیله‌سوار", "نمین", "نیر", "کوثر"),
        "اصفهان" to listOf("اصفهان", "کاشان", "خمینی‌شهر", "نجف‌آباد", "شهرضا", "شاهین‌شهر", "فلاورجان", "برخوار", "لنجان", "نطنز", "آران و بیدگل", "اردستان", "سمیرم"),
        "البرز" to listOf("کرج", "فردیس", "نظرآباد", "ساوجبلاغ", "اشتهارد", "طالقان", "چهارباغ"),
        "ایلام" to listOf("ایلام", "دهلران", "آبدانان", "ایوان", "مهران", "دره‌شهر", "چرداول", "ملکشاهی"),
        "بوشهر" to listOf("بوشهر", "دشتستان (برازجان)", "گناوه", "کنگان", "دیلم", "دیر", "تنگستان", "جم", "عسلویه"),
        "تهران" to listOf("تهران", "اسلامشهر", "شهریار", "قدس", "ورامین", "پاکدشت", "رباط‌کریم", "پیشوا", "پردیس", "دماوند", "فیروزکوه", "بهارستان", "ملارد", "قرچک"),
        "چهارمحال و بختیاری" to listOf("شهرکرد", "بروجن", "فارسان", "لردگان", "اردل", "کوهرنگ", "سامان", "بن"),
        "خراسان جنوبی" to listOf("بیرجند", "قائن", "فردوس", "نهبندان", "طبس", "سربیشه", "درمیان", "خوسف"),
        "خراسان رضوی" to listOf("مشهد", "نیشابور", "سبزوار", "تربت حیدریه", "قوچان", "کاشمر", "گناباد", "چناران", "خواف", "تایباد", "تربت جام", "درگز", "سرخس", "بردسکن"),
        "خراسان شمالی" to listOf("بجنورد", "شیروان", "اسفراین", "مانه و سملقان (آشخانه)", "گرمه", "جاجرم", "راز و جرگلان", "فاروج"),
        "خوزستان" to listOf("اهواز", "آبادان", "خرمشهر", "دزفول", "ماهشهر", "بهبهان", "شوشتر", "شوش", "ایذه", "مسجدسلیمان", "رامهرمز", "اندیمشک", "امیدیه", "هندیجان"),
        "زنجان" to listOf("زنجان", "ابهر", "خرمدره", "خدابنده (قیدار)", "ماهنشان", "ایجرود", "طارم", "سلطانیه"),
        "سمنان" to listOf("سمنان", "شاهرود", "دامغان", "گرمسار", "مهدی‌شهر", "میامی", "آرادان"),
        "سیستان و بلوچستان" to listOf("زاهدان", "زابل", "چابهار", "ایرانشهر", "سراوان", "خاش", "نیک‌شهر", "کنارک", "میرجاوه", "قصرقند"),
        "فارس" to listOf("شیراز", "مرودشت", "کازرون", "جهرم", "فسا", "داراب", "لار", "آباده", "نی‌ریز", "فیروزآباد", "لامرد", "اقلید", "سپیدان", "استهبان"),
        "قزوین" to listOf("قزوین", "تاکستان", "آبیک", "البرز (الوند)", "بوئین‌زهرا", "آوج"),
        "قم" to listOf("قم", "کهک", "جعفریه", "دستجرد", "سلفچگان"),
        "کردستان" to listOf("سنندج", "سقز", "مریوان", "بانه", "قروه", "بیجار", "کامیاران", "دیواندره", "دهگلان", "سروآباد"),
        "کرمان" to listOf("کرمان", "سیرجان", "رفسنجان", "جیرفت", "بم", "زرند", "کهنوج", "بردسیر", "شهربابک", "راور", "عنبرآباد", "منوجان"),
        "کرمانشاه" to listOf("کرمانشاه", "اسلام‌آباد غرب", "هرسین", "سنقر", "پاوه", "جوانرود", "کنگاور", "صحنه", "گیلانغرب", "قصر شیرین", "روانسر"),
        "کهگیلویه و بویراحمد" to listOf("یاسوج", "گچساران (دوگنبدان)", "دهدشت", "سی‌سخت", "باشت", "چرام", "لنده", "مارگون"),
        "گلستان" to listOf("گرگان", "گنبدکاووس", "علی‌آباد کتول", "بندر ترکمن", "آق‌قلا", "کردکوی", "آزادشهر", "مینودشت", "رامیان", "گمیشان"),
        "گیلان" to listOf("رشت", "بندر انزلی", "لاهیجان", "آستارا", "تالش (هشتپر)", "لنگرود", "رودسر", "آستانه اشرفیه", "صومعه‌سرا", "فومن", "رودبار", "سیاهکل"),
        "لرستان" to listOf("خرم‌آباد", "بروجرد", "دورود", "الیگودرز", "کوهدشت", "نورآباد (دلفان)", "پلدختر", "ازنا", "سلسله (الشتر)"),
        "مازندران" to listOf("ساری", "بابل", "آمل", "قائم‌شهر", "نوشهر", "چالوس", "بهشهر", "تنکابن", "رامسر", "نور", "فریدونکنار", "بابلسر", "جویبار", "سوادکوه"),
        "مرکزی" to listOf("اراک", "ساوه", "خمین", "محلات", "دلیجان", "زرندیه", "شازند", "آشتیان", "تفرش", "فراهان"),
        "هرمزگان" to listOf("بندرعباس", "میناب", "قشم", "بندرلنگه", "رودان", "حاجی‌آباد", "پارسیان", "جاسک", "بستک", "ابوموسی"),
        "همدان" to listOf("همدان", "ملایر", "نهاوند", "تویسرکان", "اسدآباد", "بهار", "کبودرآهنگ", "رزن", "فامنین"),
        "یزد" to listOf("یزد", "میبد", "اردکان", "بافق", "مهریز", "تفت", "ابرکوه", "خاتم", "اشکذر")
    )

    fun countiesOf(province: String): List<String> =
        IR_PROVINCES.firstOrNull { it.first == province }?.second ?: emptyList()

    val JOB_TITLES: List<JobTitle> = listOf(
        JobTitle("prod_worker", "🏭", mapOf("fa" to "کارگر خط تولید", "tr" to "Üretim işçisi", "en" to "Production worker", "ar" to "عامل خط إنتاج")),
        JobTitle("sales", "🛍️", mapOf("fa" to "فروشنده فروشگاه", "tr" to "Mağaza satış danışmanı", "en" to "Retail salesperson", "ar" to "بائع متجر")),
        JobTitle("cashier", "🧾", mapOf("fa" to "صندوق‌دار", "tr" to "Kasiyer", "en" to "Cashier", "ar" to "أمين صندوق")),
        JobTitle("secretary", "🗂️", mapOf("fa" to "منشی دفتر", "tr" to "Ofis sekreteri", "en" to "Office secretary", "ar" to "سكرتير مكتب")),
        JobTitle("admin_staff", "💼", mapOf("fa" to "کارمند اداری", "tr" to "İdari personel", "en" to "Administrative staff", "ar" to "موظف إداري")),
        JobTitle("chef", "👨‍🍳", mapOf("fa" to "آشپز", "tr" to "Aşçı", "en" to "Chef", "ar" to "طاهٍ (شيف)")),
        JobTitle("sous_chef", "🥘", mapOf("fa" to "کمک آشپز", "tr" to "Aşçı yardımcısı", "en" to "Kitchen helper", "ar" to "مساعد طاهٍ")),
        JobTitle("barista", "☕", mapOf("fa" to "باریستا", "tr" to "Barista", "en" to "Barista", "ar" to "باريستا")),
        JobTitle("waiter", "🍽️", mapOf("fa" to "گارسون", "tr" to "Garson", "en" to "Waiter", "ar" to "نادل")),
        JobTitle("warehouse", "📦", mapOf("fa" to "انباردار", "tr" to "Depo görevlisi", "en" to "Warehouse worker", "ar" to "أمين مستودع")),
        JobTitle("driver", "🚚", mapOf("fa" to "راننده", "tr" to "Şoför", "en" to "Driver", "ar" to "سائق")),
        JobTitle("courier", "🛵", mapOf("fa" to "پیک موتوری", "tr" to "Moto kurye", "en" to "Moto courier", "ar" to "مندوب توصيل")),
        JobTitle("guard", "🛡️", mapOf("fa" to "نگهبان", "tr" to "Güvenlik görevlisi", "en" to "Security guard", "ar" to "حارس أمن")),
        JobTitle("cleaner", "🧹", mapOf("fa" to "نظافتچی", "tr" to "Temizlik görevlisi", "en" to "Cleaner", "ar" to "عامل تنظيف")),
        JobTitle("tailor", "🧵", mapOf("fa" to "خیاط", "tr" to "Terzi", "en" to "Tailor", "ar" to "خيّاط")),
        JobTitle("hairdresser", "💇", mapOf("fa" to "آرایشگر", "tr" to "Kuaför", "en" to "Hairdresser", "ar" to "مصفّف شعر")),
        JobTitle("callcenter", "🎧", mapOf("fa" to "اپراتور تماس (فارسی/ترکی)", "tr" to "Çağrı merkezi (Farsça/Türkçe)", "en" to "Call center (fa/tr)", "ar" to "موظف مركز اتصال (فارسية/تركية)")),
        JobTitle("accountant", "🧮", mapOf("fa" to "حسابدار", "tr" to "Muhasebeci", "en" to "Accountant", "ar" to "محاسب")),
        JobTitle("welder", "🔧", mapOf("fa" to "جوشکار", "tr" to "Kaynakçı", "en" to "Welder", "ar" to "لحّام")),
        JobTitle("cnc", "⚙️", mapOf("fa" to "اپراتور CNC", "tr" to "CNC operatörü", "en" to "CNC operator", "ar" to "مشغّل آلة CNC")),
        JobTitle("electrician", "⚡", mapOf("fa" to "برقکار", "tr" to "Elektrikçi", "en" to "Electrician", "ar" to "كهربائي")),
        JobTitle("plumber", "🚿", mapOf("fa" to "لوله‌کش", "tr" to "Tesisatçı", "en" to "Plumber", "ar" to "سبّاك")),
        JobTitle("nurse", "🩺", mapOf("fa" to "پرستار / مراقب سالمند", "tr" to "Hemşire / yaşlı bakıcısı", "en" to "Nurse / elderly care", "ar" to "ممرض / مرافق مسنّين")),
        JobTitle("babysitter", "🧸", mapOf("fa" to "مراقب کودک", "tr" to "Çocuk bakıcısı", "en" to "Babysitter", "ar" to "مرافقة أطفال")),
        JobTitle("construction", "🏗️", mapOf("fa" to "کارگر ساختمانی", "tr" to "İnşaat işçisi", "en" to "Construction worker", "ar" to "عامل بناء")),
        JobTitle("gardener", "🌱", mapOf("fa" to "کارگر گلخانه", "tr" to "Sera işçisi", "en" to "Greenhouse worker", "ar" to "عامل بيوت محمية")),
        JobTitle("it_dev", "💻", mapOf("fa" to "برنامه‌نویس", "tr" to "Yazılım geliştirici", "en" to "Software developer", "ar" to "مبرمج")),
        JobTitle("packer", "📮", mapOf("fa" to "بسته‌بندی", "tr" to "Paketleme", "en" to "Packer", "ar" to "عامل تغليف")),
        JobTitle("sales_manager", "📈", mapOf("fa" to "مدیر فروش", "tr" to "Satış müdürü", "en" to "Sales manager")),
        JobTitle("translator", "🌐", mapOf("fa" to "مترجم فارسی/ترکی", "tr" to "Farsça/Türkçe tercüman", "en" to "fa/tr translator"))
    )

    private val titleMap: Map<String, JobTitle> = JOB_TITLES.associateBy { it.key }

    fun title(key: String): JobTitle? = titleMap[key]

    fun titleName(key: String, lang: String): String = titleMap[key]?.name(lang) ?: key

    fun titleIcon(key: String): String = titleMap[key]?.icon ?: "💼"

    /**
     * داده‌های نمونه‌ی آگهی‌ها — همان‌هایی که در حالت نمایشی (بدون Firebase) نمایش داده می‌شوند.
     * تا وقتی فایل google-services.json را اضافه نکرده‌ای، اپ با همین داده‌ها کار می‌کند.
     */
    fun sampleJobs(): List<Job> {
        val now = System.currentTimeMillis()
        val h = 60L * 60L * 1000L
        // عکس‌ها و ویدیوهای نمونه — داخل خود برنامه ذخیره شده‌اند (res:drawable/… و res:raw/…)
        fun pic(name: String) = JobMedia(MediaType.IMAGE, "res:drawable/job_photo_$name")
        fun vid(video: String, posterName: String) = JobMedia(
            MediaType.VIDEO,
            "res:raw/job_video_$video",
            poster = "res:drawable/job_photo_$posterName"
        )

        fun j(
            id: String, key: String, company: String, city: String, district: String,
            salary: Long, contract: String, cond: List<String>, hoursAgo: Long,
            desc: String, reqs: List<String>,
            media: List<JobMedia> = emptyList()
        ) = Job(
            id = id, titleKey = key, company = company, city = city, district = district,
            salary = salary, contract = contract, conditions = cond,
            description = desc, requirements = reqs,
            contactPhone = "+90 212 000 00 00",
            publishedAt = now - hoursAgo * h, active = true,
            media = media
        )

        return listOf(
            j(
                "j1", "prod_worker", "Arma Tekstil", "İstanbul", "Bağcılar", 22000, Contract.FULL,
                listOf(Condition.INSURANCE, Condition.MEAL), 2,
                "کارگر خط تولید دوخت، ۴۵ ساعت در هفته، شیفت ثابت روز. آموزش اولیه رایگان و سرویس ایاب‌وذهاب.",
                listOf("سن ۱۸ تا ۴۵ سال", "توانایی کار ایستاده", "سابقه مرتبط مزیت است", "آشنایی مقدماتی با ترکی"),
                media = listOf(pic("factory"), vid("factory", "factory"), pic("warehouse"))
            ),
            j(
                "j2", "sales", "Moda Center", "İstanbul", "Fatih", 24500, Contract.FULL,
                listOf(Condition.INSURANCE, Condition.MEAL), 5,
                "فروشنده فروشگاه پوشاک در منطقه فاتح؛ برخورد خوب با مشتری و توانایی کار تیمی.",
                listOf("تجربه فروش", "ترکی مکالمه‌ای (حداقل B1)", "ظاهر مرتب", "قابل انعطاف در شیفت"),
                media = listOf(pic("store"))
            ),
            j(
                "j3", "callcenter", "Boğaz Call Center", "İstanbul", "Ataşehir", 26500, Contract.FULL,
                listOf(Condition.INSURANCE), 8,
                "اپراتور تلفن فارسی و ترکی برای پاسخگویی به مشتریان ایرانی. محیط اداری و شیفت ۸ ساعته.",
                listOf("فارسی روان (اجباری)", "ترکی در سطح متوسط", "کار با کامپیوتر", "صدای رسا")
            ),
            j(
                "j4", "barista", "Melat Cafe", "İstanbul", "Kadıköy", 23500, Contract.PART,
                listOf(Condition.MEAL), 11,
                "باریستا با تجربه کار با اسپرسو و لاته آرت. شیفت عصرها و آخر هفته‌ها.",
                listOf("سابقه کار در کافه", "ترکی مکالمه‌ای", "سرعت عمل", "انرژی مثبت"),
                media = listOf(pic("kitchen"), pic("store"))
            ),
            j(
                "j5", "warehouse", "Bessa Lojistik", "İstanbul", "Esenyurt", 25000, Contract.SHIFT,
                listOf(Condition.INSURANCE, Condition.MEAL), 14,
                "انباردار برای بارگیری و چیدمان کالا، کار با اسکنر بارکد. شیفت ۱۲ ساعته چرخشی.",
                listOf("توان بدنی مناسب", "آشنایی با انبارداری", "ترکی حداقل A2", "مقاوم در برابر سرما و گرما"),
                media = listOf(pic("warehouse"), pic("factory"))
            ),
            j(
                "j6", "chef", "Su Food Restaurant", "İstanbul", "Beşiktaş", 32000, Contract.FULL,
                listOf(Condition.INSURANCE, Condition.MEAL), 18,
                "آشپز حرفه‌ای با تسلط بر غذاهای ایرانی و مدیترانه‌ای برای رستوران ۹۰ نفره.",
                listOf("حداقل ۳ سال سابقه", "مدرک بهداشت (Hijyen belgesi)", "کنترل کیفیت و سبد خرید", "ترکی B1")
            ),
            j(
                "j7", "driver", "Anadolu Dağıtım", "İstanbul", "Ümraniye", 28000, Contract.FULL,
                listOf(Condition.INSURANCE, Condition.MEAL), 22,
                "راننده وانت توزیع محصولات غذایی در محدوده استانبول اروپا. گواهینامه B معتبر.",
                listOf("گواهینامه ترکیه (B)", "دفترچه اقامت معتبر", "آشنایی با مسیرهای استانبول", "سن بالای ۲۵")
            ),
            j(
                "j8", "nurse", "Mermer Yaşlı Bakım Evi", "İstanbul", "Maltepe", 29000, Contract.FULL,
                listOf(Condition.INSURANCE, Condition.MEAL), 26,
                "پرستار یا مراقب سالمند برای شیفت شب، نگهداری و همراهی سالمندان فارسی‌زبان.",
                listOf("تجربه مراقبت", "صبر و مسئولیت‌پذیری", "ترکی مکالمه‌ای", "دسترسی آسان به مالتپه")
            ),
            j(
                "j9", "tailor", "Atölye Tekstil", "İstanbul", "Küçükçekmece", 27000, Contract.FULL,
                listOf(Condition.INSURANCE), 29,
                "خیاط مسلط به الگوسازی و چرخ صنعتی برای تولید لباس زنانه.",
                listOf("حداقل ۲ سال خیاطی", "کار با چرخ صنعتی", "دقت در دوخت", "تحمل کار در کارگاه")
            ),
            j(
                "j10", "guard", "Securitas TR", "İstanbul", "Zeytinburnu", 24000, Contract.SHIFT,
                listOf(Condition.INSURANCE, Condition.MEAL), 34,
                "نگهبان ساختمان اداری، شیفت ۱۲ ساعته؛ دو روز کار و دو روز تعطیل.",
                listOf("کارت امنیتی (Kimlik)", "ترکی سطح پایه", "قد بالای ۱۷۰ سانتی‌متر", "سابقه نظامی مزیت است")
            ),
            j(
                "j11", "cleaner", "Mermer Palace Hotel", "İstanbul", "Bakırköy", 20000, Contract.FULL,
                listOf(Condition.INSURANCE, Condition.MEAL), 40,
                "نظافتچی اتاق‌های هتل، آموزش داده می‌شود. محیط گرم و منظم.",
                listOf("توان بدنی خوب", "تمیزکاری دقیق", "ترکی ابتدایی", "دسترسی به بکرکوی")
            ),
            j(
                "j12", "admin_staff", "Ana İhracat", "Ankara", "Çankaya", 30000, Contract.FULL,
                listOf(Condition.INSURANCE, Condition.WEEKEND), 48,
                "کارمند اداری دفتر صادرات، کار با اکسل و مکاتبات فارسی/ترکی/انگلیسی.",
                listOf("اکسل و آفیس", "ترکی و انگلیسی متوسط", "دقت بالا", "حداقل دیپلم")
            ),
            j(
                "j13", "sales", "Carpi AVM", "İzmir", "Bornova", 23000, Contract.FULL,
                listOf(Condition.INSURANCE), 62,
                "فروشنده کفش و کیف، شیفت ثابت روز، پورسانت فروش ماهانه.",
                listOf("تجربه فروش", "ترکی مکالمه‌ای", "خوش‌برخورد", "ساکن ازمیر")
            ),
            j(
                "j14", "prod_worker", "Nilüfer Otomotiv", "Bursa", "Nilüfer", 24500, Contract.SHIFT,
                listOf(Condition.INSURANCE, Condition.MEAL), 78,
                "کارگر خط مونتاژ قطعات خودرو، خدمات ایاب‌وذهاب و سرویس ناهار.",
                listOf("سن ۲۰ تا ۴۰", "توان کار شیفتی", "سابقه در خط تولید", "ترکی ابتدایی")
            ),
            j(
                "j15", "gardener", "Kumluca Seracılık", "Antalya", "Kumluca", 22000, Contract.FULL,
                listOf(Condition.MEAL), 96,
                "کارگر گلخانه گوجه‌فرنگی، کار تمام فصل، محل اسکان رایگان برای کارگران شهرهای دیگر.",
                listOf("تجربه کشاورزی", "کار زیر آفتاب", "ترکی ابتدایی", "انعطاف در زمان کار")
            ),
            j(
                "j16", "welder", "Gebze Industrial", "Kocaeli", "Gebze", 35000, Contract.FULL,
                listOf(Condition.INSURANCE, Condition.MEAL), 120,
                "جوشکار آرگون و CO2 برای قطعات صنعتی. تست مهارت در روز مصاحبه.",
                listOf("حداقل ۳ سال جوشکاری", "مدرک MIG/TIG", "رعایت ایمنی", "ترکی A2")
            ),
            j(
                "j17", "secretary", "Ana Sigorta", "İstanbul", "Şişli", 26000, Contract.FULL,
                listOf(Condition.INSURANCE, Condition.WEEKEND), 150,
                "منشی دفتر بیمه؛ پاسخگویی تلفن، ثبت پرونده و هماهنگی قرارها.",
                listOf("ترکی B2 و فارسی روان", "کار با کامپیوتر", "ظاهر رسمی", "توانایی چندکاره")
            ),
            j(
                "j18", "packer", "Bilsu Gıda", "İstanbul", "Arnavutköy", 21500, Contract.SHIFT,
                listOf(Condition.MEAL), 180,
                "بسته‌بندی مواد غذایی، شیفت ۸ ساعته با اضافه‌کاری اختیاری.",
                listOf("سرعت دست", "بهداشت فردی", "ترکی ابتدایی", "آماده کار با ماسک و دستکش")
            )
        )
    }

    /** متقاضیان نمونه برای حالت نمایشی پنل مدیریت */
    fun sampleApplicants(): List<Applicant> {
        val now = System.currentTimeMillis()
        val h = 60L * 60L * 1000L
        fun p(
            id: String, first: String, last: String,
            phone: String, local: String, cc: String,
            city: String, district: String, jobs: List<String>,
            level: Int, gender: String, status: String, hoursAgo: Long,
            und: Undertaking? = null
        ) = Applicant(
            id = id, uid = "demo_$id", firstName = first, lastName = last,
            fatherName = if (gender == "f") "رضا" else "حسین",
            phone = phone, email = "", city = city, district = district,
            jobKeys = jobs, trLevel = level, gender = gender,
            status = status, consent = true,
            createdAt = now - hoursAgo * h, lang = "fa",
            phoneCountry = cc, phoneLocal = local, undertaking = und,
            spokenLangs = when (id) {      // v45: نمونه‌ی «زبان‌هایی که بلدم»
                "p1" -> listOf("fa", "tr")
                "p2" -> listOf("fa", "azb", "tr", "en")
                "p3" -> listOf("tr", "en", "de")
                "p4" -> listOf("fa", "en", "ar")
                "p5" -> listOf("fa", "bqi")
                "p6" -> listOf("fa", "ar")
                else -> listOf("fa")
            }
        )

        // تعهدنامه‌های نمونه (متن امضا در اپ واقعی با انگشت کشیده می‌شود)
        fun und(nid: String, card: String, date: String, daysAgo: Long) = Undertaking(
            signed = true,
            date = date,
            signedAt = now - daysAgo * 24 * h,
            nationalId = nid,
            payerCard = card,
            fee = 6000L,
            brokerCard = "TR33 0006 1005 1978 6457 8413 26".replace(" ", ""),
            signature = ""
        )

        return listOf(
            // شماره‌های ترکیه (‎+90)
            p("p1", "علی", "رضایی", "+90 532 145 88 21", "5321458821", "TR",
                "تهران", "تهران", listOf("prod_worker", "warehouse"), 45, "m", Status.NEW, 3),
            p("p2", "زهرا", "محمدی", "+90 538 902 11 47", "5389021147", "TR",
                "آذربایجان شرقی", "تبریز", listOf("hairdresser", "sales"), 80, "f", Status.REFERRED, 20,
                und("0079123488", "6037991122334455", "2026/09/28", 20)),
            p("p3", "Mustafa", "Yıldız", "+90 505 337 08 90", "5053370890", "TR",
                "تهران", "شهریار", listOf("it_dev"), 95, "m", Status.REVIEWED, 30,
                und("0012345678", "6104337788991122", "2026/09/30", 30)),
            p("p6", "فاطمه", "حسینی", "+90 537 664 29 55", "5376642955", "TR",
                "خوزستان", "اهواز", listOf("cleaner", "babysitter"), 20, "f", Status.NEW, 160),

            // شماره‌های ایران (‎+98)
            p("p4", "سمیرا", "کاظمی", "+98 912 210 6613", "9122106613", "IR",
                "فارس", "شیراز", listOf("callcenter", "secretary"), 70, "f", Status.NEW, 52),
            p("p5", "حسن", "مرادی", "+98 913 780 4102", "9137804102", "IR",
                "اصفهان", "اصفهان", listOf("chef", "sous_chef"), 35, "m", Status.REVIEWED, 90)
        )
    }
}
