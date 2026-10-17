package com.karbama.app

import android.app.Application
import com.karbama.app.data.Service
import com.karbama.app.data.local.LocalPhotoCache

/**
 * کلاس Application برنامه.
 * سرویس‌های Firebase و پوشه‌ی عکس‌های محلی اینجا آماده می‌شوند.
 */
class KarbamaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Service.init(this)
        LocalPhotoCache.init(this)
        // نکته: حالت آفلاین Firestore روی اندروید به‌صورت پیش‌فرض فعال است،
        // پس اگر کاربر اینترنت نداشته باشد آخرین آگهی‌ها نمایش داده می‌شوند.
    }
}
