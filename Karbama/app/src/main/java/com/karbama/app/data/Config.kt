package com.karbama.app.data

/**
 * تنظیمات ثابت برنامه — اینجا را می‌توانی راحت تغییر بدهی.
 */
object Config {

    /** مبلغ پیش‌فرض تعهدنامه (در تنظیمات پنل تغییر نمی‌کند؛ متقاضی آن را روی برگه وارد می‌کند
     *  و مدیر هم می‌تواند برای هر شخص جداگانه تغییرش بدهد) */
    const val DEFAULT_FEE = 6000L

    /** شماره کارت بانکی پیش‌فرض کارگزار — در تنظیمات پنل مدیریت قابل تغییر است */
    const val DEFAULT_BROKER_CARD = "1234 1234 1234 1234"

    /** مبلغ پایهٔ نمایش آگهی‌ها به لیر ترکیه — مدیر می‌تواند آن را از پنل تغییر دهد. */
    const val DEFAULT_ADS_FEE = 500L

    /**
     * #38: نرخ معادل ریالی فقط سمت Cloud Functions خوانده می‌شود؛ هرگز در APK کلید API نیست.
     * `try_hav` = نرخ حواله‌ای لیر ترکیه در منبع نرخ انتخاب‌شده.
     */
    const val IRR_RATE_ITEM = "try_hav"
    const val PAYMENT_QUOTE_TTL_MINUTES = 15L

    /** نام مجموعه‌ها در Firestore */
    const val COL_JOBS = "jobs"
    const val COL_APPLICANTS = "applicants"
    /** هتل‌های مسیریابی؛ فقط مدیر می‌نویسد، کاربران واردشده می‌خوانند. */
    const val COL_HOTELS = "hotels"
    const val COL_ADMINS = "admins"

    /** #29: پسندیده‌ها و درخواست‌های ارسالی کاربران (برای دیدن مدیر در پنل) */
    const val COL_LIKES = "likes"
    const val COL_APPLICATIONS = "applications"

    /** مسیر آپلود عکس‌ها در Firebase Storage */
    const val STORAGE_ROOT = "applicants"

    /** حداکثر اندازه‌ی طول عکس بعد از فشرده‌سازی (پیکسل) */
    const val MAX_PHOTO_DIM = 1280

    /** کیفیت فشرده‌سازی JPEG (۰ تا ۱۰۰) */
    const val PHOTO_QUALITY = 82

    const val CONTACT_PHONE = "+90 212 000 00 00"
}
