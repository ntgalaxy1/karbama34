package com.karbama.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.karbama.app.ui.components.FirestoreImageFetcher
import com.karbama.app.data.Service
import com.karbama.app.data.local.LocalPhotoCache

/**
 * کلاس Application برنامه.
 * سرویس‌های Firebase و پوشه‌ی عکس‌های محلی اینجا آماده می‌شوند.
 */
class KarbamaApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        Service.init(this)
        LocalPhotoCache.init(this)
        // نکته: حالت آفلاین Firestore روی اندروید به‌صورت پیش‌فرض فعال است،
        // پس اگر کاربر اینترنت نداشته باشد آخرین آگهی‌ها نمایش داده می‌شوند.
    }

    /** v48: Coil عکس‌های fsimg://… را از Firestore می‌خواند */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(FirestoreImageFetcher.Factory()) }
            .build()
}
