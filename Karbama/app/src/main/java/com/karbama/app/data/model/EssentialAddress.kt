package com.karbama.app.data.model

/**
 * یک نشانی یا راه تماس ضروری برای کاربران کارباما در ترکیه.
 *
 * این مدل عمداً از پروفایل کارجو جداست: داده‌های عمومیِ نشانی هیچ شناسه، مدرک
 * یا اطلاعات شخصی کاربر را نگه نمی‌دارند. فقط مدیر اصلی می‌تواند رکوردهای
 * Firestore را ایجاد، ویرایش یا غیرفعال کند.
 */
data class EssentialAddress(
    val id: String = "",
    val countryCode: String = "TR",
    val cityId: String = "nationwide",
    val category: String = EssentialAddressCategory.OTHER,
    val title: String = "",
    val titleTr: String = "",
    val address: String = "",
    val phone: String = "",
    val hours: String = "",
    val description: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val active: Boolean = true,
    val ownerUid: String = "",
    val updatedAt: Long = 0L
)

object EssentialAddressCategory {
    const val ALL = "all"
    const val IMMIGRATION = "immigration"
    const val HEALTH = "health"
    const val LEGAL = "legal"
    const val TRANSPORT = "transport"
    const val OTHER = "other"

    val all = listOf(IMMIGRATION, HEALTH, LEGAL, TRANSPORT, OTHER)
}

/**
 * فهرست اولیهٔ عمومی و تأییدشدهٔ ترکیه که داخل APK قرار می‌گیرد.
 *
 * این داده‌ها به‌عنوان fallback نمایش داده می‌شوند تا خالی‌بودن یک collection
 * تازه در Firestore، کاربران را بدون شماره‌ها و نشانی‌های ضروری نگذارد. مدیر
 * اصلی می‌تواند هر سطر را با همان ID در Firestore ویرایش یا غیرفعال کند؛ آن
 * نسخه جای مورد بسته‌بندی‌شده را می‌گیرد. منبع هر مورد در توضیح آمده و کاربر
 * باید پیش از مراجعه، اطلاعات را از مرجع رسمی دوباره تأیید کند.
 */
object EssentialAddressSamples {
    fun default(): List<EssentialAddress> = listOf(
        EssentialAddress(
            id = "official_istanbul_migration_office",
            category = EssentialAddressCategory.IMMIGRATION,
            title = "اداره مهاجرت استان استانبول",
            titleTr = "İstanbul İl Göç İdaresi Müdürlüğü",
            cityId = "istanbul",
            address = "Hırka-i Şerif Mahallesi, Adnan Menderes Bulvarı No:64, 34091 Fatih/İstanbul",
            phone = "0212 499 40 00",
            hours = "پیش از مراجعه، نوبت و ساعت را از مرجع رسمی بررسی کنید.",
            description = "مرجع رسمی امور مهاجرت استان استانبول. منبع: istanbul.goc.gov.tr/iletisim",
            lat = 41.020687,
            lon = 28.935617,
            updatedAt = 8L
        ),
        EssentialAddress(
            id = "official_yimer_157",
            category = EssentialAddressCategory.IMMIGRATION,
            title = "مرکز ارتباط خارجیان YİMER 157",
            titleTr = "Yabancılar İletişim Merkezi YİMER 157",
            cityId = "nationwide",
            address = "Türkiye geneli · yimer.gov.tr",
            phone = "157",
            hours = "اطلاعات: روزهای کاری 08:00–18:00؛ کمک اضطراری و گزارش: 24 ساعته",
            description = "راهنمای رسمی ویزا، اقامت، حمایت بین‌المللی و امور مهاجرت؛ خدمات فارسی نیز ارائه می‌شود. منبع: goc.gov.tr",
            updatedAt = 7L
        ),
        EssentialAddress(
            id = "official_emergency_112",
            category = EssentialAddressCategory.HEALTH,
            title = "۱۱۲ تماس اضطراری یکپارچه",
            titleTr = "112 Acil Çağrı Merkezi",
            cityId = "nationwide",
            address = "Türkiye geneli",
            phone = "112",
            hours = "24 ساعته",
            description = "شماره واحد اضطرار ترکیه برای هماهنگی آمبولانس، پلیس، آتش‌نشانی و AFAD. فقط برای وضعیت اضطراری تماس بگیرید. منبع: saglik.gov.tr",
            updatedAt = 6L
        ),
        EssentialAddress(
            id = "official_mhrs_182",
            category = EssentialAddressCategory.HEALTH,
            title = "سامانه نوبت درمان MHRS",
            titleTr = "Merkezi Hekim Randevu Sistemi (MHRS)",
            cityId = "nationwide",
            address = "Türkiye geneli · mhrs.gov.tr",
            phone = "182",
            description = "دریافت نوبت مراکز درمانی وابسته به وزارت بهداشت ترکیه از طریق ALO 182، وب‌سایت یا برنامهٔ MHRS.",
            updatedAt = 5L
        ),
        EssentialAddress(
            id = "official_alo_170",
            category = EssentialAddressCategory.LEGAL,
            title = "ALO 170 — راهنمای کار و تأمین اجتماعی",
            titleTr = "Çalışma Hayatı İletişim Merkezi ALO 170",
            cityId = "nationwide",
            address = "Türkiye geneli · alo170.gov.tr",
            phone = "170",
            hours = "برای کانال‌ها و ساعات پاسخ‌گویی، وب‌سایت رسمی را بررسی کنید.",
            description = "مرکز ارتباطی رسمی در حوزه کار، تأمین اجتماعی و خدمات مرتبط با وزارت کار، SGK و İŞKUR.",
            updatedAt = 4L
        ),
        EssentialAddress(
            id = "official_iett_istanbul",
            category = EssentialAddressCategory.TRANSPORT,
            title = "راهنمای حمل‌ونقل عمومی استانبول — İETT",
            titleTr = "İETT Genel Müdürlüğü",
            cityId = "istanbul",
            address = "Şahkulu Mahallesi, Erkan-ı Harp Sokak No:2, Tünel, 34420 Beyoğlu/İstanbul",
            phone = "444 18 71",
            description = "اطلاعات و پشتیبانی حمل‌ونقل عمومی استانبول. منبع: iett.istanbul",
            updatedAt = 3L
        ),
        EssentialAddress(
            id = "official_tcdd_4448233",
            category = EssentialAddressCategory.TRANSPORT,
            title = "TCDD Taşımacılık — راهنمای قطار و بلیت",
            titleTr = "TCDD Taşımacılık Çağrı Merkezi",
            cityId = "ankara",
            address = "Hacı Bayram Mahallesi, Hipodrom Caddesi No:3/C, 06050 Altındağ/Ankara",
            phone = "444 82 33",
            hours = "24 ساعته",
            description = "اطلاعات سفر قطار، رزرو، فروش، تغییر و بازپرداخت بلیت. منبع: tcddtasimacilik.gov.tr",
            updatedAt = 2L
        ),
        EssentialAddress(
            id = "official_alo_183",
            category = EssentialAddressCategory.OTHER,
            title = "ALO 183 — حمایت اجتماعی",
            titleTr = "ALO 183 Sosyal Destek Hattı",
            cityId = "nationwide",
            address = "Türkiye geneli",
            phone = "183",
            hours = "24 ساعته",
            description = "راهنمایی و حمایت اجتماعی درباره خانواده، زنان، کودکان، افراد دارای معلولیت و سالمندان. منبع: aile.gov.tr",
            updatedAt = 1L
        )
    )
}
