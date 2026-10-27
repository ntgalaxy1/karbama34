package com.karbama.app.data.model

/**
 * v10.0.4 — یک نشانی یا راه تماس ضروری برای کاربران کارباما در ترکیه.
 *
 * این مدل عمداً از پروفایل کارجو جداست: داده‌های عمومیِ نشانی هیچ شناسه، مدرک
 * یا اطلاعات شخصی کاربر را نگه نمی‌دارند. فقط مدیر اصلی می‌تواند رکوردها را
 * در Firebase ایجاد، ویرایش یا غیرفعال کند.
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

/** داده‌های اولیهٔ demo؛ هیچ‌کدام آدرس حضوریِ رسمی نیستند. */
object EssentialAddressSamples {
    fun default(): List<EssentialAddress> = listOf(
        EssentialAddress(
            id = "demo_migration_info",
            category = EssentialAddressCategory.IMMIGRATION,
            title = "راهنمای امور اقامت ترکیه",
            titleTr = "İkamet bilgi hattı",
            cityId = "nationwide",
            phone = "157",
            hours = "سراسری ترکیه",
            description = "نمونهٔ حالت نمایشی؛ مدیر باید اطلاعات و نشانی رسمی و به‌روز را در پنل ثبت کند.",
            updatedAt = 1L
        ),
        EssentialAddress(
            id = "demo_emergency",
            category = EssentialAddressCategory.HEALTH,
            title = "راهنمای تماس اضطراری",
            titleTr = "Acil durum hattı",
            cityId = "nationwide",
            phone = "112",
            hours = "۲۴ ساعته",
            description = "نمونهٔ حالت نمایشی برای نمایش ساختار کارت‌های ضروری.",
            updatedAt = 2L
        ),
        EssentialAddress(
            id = "demo_labour",
            category = EssentialAddressCategory.LEGAL,
            title = "راهنمای مسائل کار و اشتغال",
            titleTr = "Çalışma bilgi hattı",
            cityId = "nationwide",
            phone = "170",
            hours = "سراسری ترکیه",
            description = "نمونهٔ حالت نمایشی؛ پیش از استفاده، منبع رسمی و اطلاعات به‌روز را بررسی کنید.",
            updatedAt = 3L
        )
    )
}
