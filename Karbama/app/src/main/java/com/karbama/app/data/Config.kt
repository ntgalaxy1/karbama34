package com.karbama.app.data

/**
 * تنظیمات ثابت برنامه — اینجا را می‌توانی راحت تغییر بدهی.
 */
object Config {

    /**
     * رمز ورود پنل مدیریت (حالت نمایشی / محافظت داخل برنامه).
     * ⚠️ این رمز فقط درِ پنل را داخل اپ می‌بندد؛ محافظت واقعی داده‌ها با
     * قواعد امنیتی Firestore (فایل firestore.rules) و «حساب مدیر» انجام می‌شود.
     */
    const val ADMIN_PASSWORD = "karbama2026"

    /**
     * نام کاربری مدیر (در حالت نمایشی / بدون Firebase).
     * در حالت متصل، ورود مدیر با «ایمیل و رمز حساب Firebase» انجام می‌شود و
     * نام کاربری لازم نیست.
     */
    const val ADMIN_USERNAME = "admin"

    /** مبلغ پیش‌فرض تعهدنامه (در تنظیمات پنل تغییر نمی‌کند؛ متقاضی آن را روی برگه وارد می‌کند
     *  و مدیر هم می‌تواند برای هر شخص جداگانه تغییرش بدهد) */
    const val DEFAULT_FEE = 6000L

    /** شماره کارت بانکی پیش‌فرض کارگزار — در تنظیمات پنل مدیریت قابل تغییر است */
    const val DEFAULT_BROKER_CARD = "1234 1234 1234 1234"

    /** #34: مبلغ پیش‌فرض «هزینه نمایش آگهی‌ها» — کارجو تا پرداخت و تأیید مدیر، آگهی نمی‌بیند */
    const val DEFAULT_ADS_FEE = 6000L

    /** نام مجموعه‌ها در Firestore */
    const val COL_JOBS = "jobs"
    const val COL_APPLICANTS = "applicants"
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
