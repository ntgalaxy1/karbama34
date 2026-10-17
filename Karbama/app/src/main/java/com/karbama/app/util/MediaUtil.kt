package com.karbama.app.util

import com.karbama.app.data.model.JobMedia
import com.karbama.app.data.model.MediaType

/**
 * آدرس‌دهی رسانه‌های آگهی.
 *
 * نمونه‌های داخل برنامه (حالت نمایشی) با پیشوند `res:` ذخیره می‌شوند، مثل:
 *   res:drawable/job_photo_factory   → یک عکس داخل خود اپ
 *   res:raw/job_video_factory        → یک ویدیو داخل خود اپ
 *
 * بقیه‌ی حالت‌ها همان مسیر واقعی هستند: لینک اینترنتی (https)، مسیر فایل گوشی (/data/…)
 * یا آدرس گالری (content://). [mediaAddress] همه را به آدرسی تبدیل می‌کند که
 * Coil و پخش‌کننده‌ی ویدیو هر دو می‌فهمند.
 */
object MediaUtil {

    const val PACKAGE = "com.karbama.app"

    private const val RES = "res:"

    fun isVideo(m: JobMedia): Boolean = m.type == MediaType.VIDEO

    /** آدرس قابل نمایش (تصویر/ویدیو) برای Coil و VideoView */
    fun mediaAddress(path: String): String =
        if (path.startsWith(RES)) "android.resource://$PACKAGE/" + path.removePrefix(RES) else path

    /** آدرس تصویر پیش‌نمایش — برای ویدیو، پوستر آن (اگر نباشد، خودِ آدرس ویدیو) */
    fun previewAddress(m: JobMedia): String {
        val p = when {
            m.type == MediaType.VIDEO && m.poster.isNotBlank() -> m.poster
            else -> m.url
        }
        return mediaAddress(p)
    }

    /** آیا این مسیر به فایلی روی خود گوشی اشاره می‌کند که باید آپلود/کپی شود؟ */
    fun isDeviceFile(url: String): Boolean =
        url.startsWith("content://") || url.startsWith("file://") || url.startsWith("/")
}
