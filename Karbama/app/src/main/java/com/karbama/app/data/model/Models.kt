package com.karbama.app.data.model

/**
 * مدل داده‌ها — Karbama
 * همه‌ی مدل‌ها مقدار پیش‌فرض دارند تا نگاشت (mapping) امن باشد.
 */

/** آگهی استخدام */
data class Job(
    val id: String = "",
    val titleKey: String = "",          // کلید عنوان شغلی از Catalog.JOB_TITLES
    val titleCustom: String = "",       // اگر عنوان دلخواه وارد شده باشد
    val company: String = "",
    val city: String = "",
    val district: String = "",
    val salary: Long = 0L,              // حقوق ماهانه به لیر
    val contract: String = "fullTime",  // fullTime | partTime | shift | contract
    val conditions: List<String> = emptyList(),   // ins | meal | weekend
    val description: String = "",
    val requirements: List<String> = emptyList(),
    val contactPhone: String = "",
    val active: Boolean = true,
    val publishedAt: Long = 0L,         // زمان انتشار (میلی‌ثانیه)
    val createdBy: String = "",
    val media: List<JobMedia> = emptyList()   // عکس‌ها و ویدیوهای آگهی
)

/** یک عکس یا ویدیوی آگهی */
data class JobMedia(
    val type: String = MediaType.IMAGE,   // image | video
    val url: String = "",                 // مسیر یا لینک عکس/ویدیو
    val poster: String = ""               // تصویر پیش‌نمایش ویدیو (اختیاری)
)

object MediaType {
    const val IMAGE = "image"
    const val VIDEO = "video"
}

/** متقاضی کار */
data class Applicant(
    val id: String = "",
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val fatherName: String = "",     // نام پدر (برای متن تعهدنامه)
    val username: String = "",       // نام کاربری ورود
    val passwordHash: String = "",   // هش رمز عبور (SHA-256)
    val nationalId: String = "",     // کد ملی (در مرحله‌ی ۱ ثبت‌نام گرفته می‌شود)
    val otherJobs: String = "",      // حرفه‌های دیگر (متن آزاد)
    val phone: String = "",
    val email: String = "",
    val city: String = "",              // استان (محل سکونت)
    val district: String = "",          // شهرستان
    val address: String = "",           // آدرس کامل
    val jobKeys: List<String> = emptyList(),
    val spokenLangs: List<String> = emptyList(),   // v45: «زبان‌هایی که بلدم» (کدهای ISO 639)
    val trLevel: Int = 0,               // ۰ تا ۱۰۰
    val gender: String = "",            // m | f
    val idPhotoUrl: String = "",
    val bodyPhotoUrl: String = "",
    val status: String = "new",         // new | reviewed | referred
    val consent: Boolean = false,
    val createdAt: Long = 0L,
    val lang: String = "fa",
    val phoneCountry: String = "TR",    // ISO2 هر کشور (v45)
    val phoneLocal: String = "",        // رقم‌های محلی بدون پیش‌شماره
    val undertaking: Undertaking? = null,
    // #34: وضعیت پرداخت «هزینه نمایش آگهی‌ها» — "" پرداخت‌نشده | pending در انتظار تأیید | paid تأییدشده
    val payStatus: String = "",
    val payAt: Long = 0L,
    // v38 manual payment evidence. Never unlocks ads by itself; manager confirmation is still required.
    val payMethod: String = "",          // manual | zarinpal | iyzico
    val payReference: String = "",       // bank / card-to-card transaction reference
    val payReceiptUrl: String = ""       // Storage URL (or local demo file path) for the receipt image
) {
    val fullName: String get() = "$firstName $lastName".trim()

    /** #34: آیا مدیر پرداخت این کارجو را تأیید کرده؟ (تنها راه باز شدن آگهی‌ها) */
    val adsPaid: Boolean get() = payStatus == "paid"

    /** #34: کد پیگیری پرداخت این کارجو (کاربر به مدیر می‌گوید، مدیر با آن تأیید می‌کند) */
    val payTrackCode: String get() {
        val base = username.ifEmpty { id } .ifEmpty { uid }
        var h = 7
        base.forEach { h = (h * 31 + it.code) % 90000 }
        return "KB-" + (10000 + h)
    }
}

/**
 * تعهدنامه تسویه حساب هزینه کارگزار (سند A4 با امضای الکترونیکی)
 * متن سند فارسی و رسمی است؛ عنوان‌ها و برچسب‌ها چندزبانه‌اند.
 */
data class Undertaking(
    val signed: Boolean = false,
    val date: String = "",            // تاریخ عقد قرارداد — yyyy/MM/dd
    val signedAt: Long = 0L,          // زمان دقیق امضا
    val nationalId: String = "",      // شماره ملی
    val payerCard: String = "",       // شماره کارت واریزکننده
    val fee: Long = 6000,             // مبلغ هزینه کارگزار (لیر)
    val brokerCard: String = "",      // شماره کارت بانکی کارگزار (در زمان امضا)
    val signature: String = ""        // امضای الکترونیکی — data:image/png;base64,...
)

/** وضعیت‌های متقاضی */
object Status {
    const val NEW = "new"
    const val REVIEWED = "reviewed"
    const val REFERRED = "referred"
    val all = listOf(NEW, REVIEWED, REFERRED)
}

/** نوع قرارداد */
object Contract {
    const val FULL = "fullTime"
    const val PART = "partTime"
    const val SHIFT = "shift"
    const val CONTRACT = "contract"
    val all = listOf(FULL, PART, SHIFT, CONTRACT)
}

/** مزایا / شرایط */
object Condition {
    const val INSURANCE = "ins"
    const val MEAL = "meal"
    const val WEEKEND = "weekend"
    val all = listOf(INSURANCE, MEAL, WEEKEND)
}

/** دسترسی مدیر */
object AdminMode {
    /** حالت واقعی: متصل به Firebase و حساب مدیر */
    const val LIVE = "live"

    /** حالت نمایشی: داده‌های نمونه روی خود گوشی (بدون Firebase) */
    const val DEMO = "demo"

    /** بدون دسترسی */
    const val NONE = "none"
}

/** فیلترهای صفحه اصلی */
data class JobFilter(
    val query: String = "",
    val city: String = "",
    val district: String = "",
    val contract: String = "",
    val sort: String = "new"            // new | salary | salaryLow
)
