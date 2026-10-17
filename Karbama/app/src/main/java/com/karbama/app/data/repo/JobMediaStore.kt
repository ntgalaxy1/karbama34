package com.karbama.app.data.repo

import android.net.Uri
import com.karbama.app.data.Service
import com.karbama.app.data.local.LocalPhotoCache
import com.karbama.app.data.model.JobMedia
import com.karbama.app.util.MediaUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * رسانه‌های آگهی (عکس و ویدیو) را قبل از ذخیره آماده می‌کند:
 *
 *  - فایلی که مدیر از گوشی انتخاب کرده (`content://…`):
 *      · اگر Firebase وصل باشد → در Storage آپلود می‌شود و لینک آن ذخیره می‌گردد
 *        (تا کارجو هم همان عکس/ویدیو را ببیند)
 *      · در حالت نمایشی → داخل حافظه‌ی خود برنامه کپی می‌شود
 *  - لینک‌های اینترنتی و مسیرهای `res:` دست‌نخورده می‌مانند.
 *
 * اگر خواندن یا آپلود یک فایل شکست بخورد، همان مورد حذف می‌شود تا ثبت آگهی متوقف نشود.
 */
object JobMediaStore {

    suspend fun prepare(jobKey: String, items: List<JobMedia>): List<JobMedia> =
        withContext(Dispatchers.IO) {
            items.mapNotNull { item ->
                if (item.url.isBlank()) return@mapNotNull null
                if (!MediaUtil.isDeviceFile(item.url)) return@mapNotNull item
                runCatching { uploadOrCopy(jobKey, item) }.getOrNull()
            }
        }

    private suspend fun uploadOrCopy(jobKey: String, item: JobMedia): JobMedia {
        val ctx = Service.app
        val bytes = ctx.contentResolver.openInputStream(Uri.parse(item.url))?.use { it.readBytes() }
            ?: return item                          // نتوانستیم بخوانیم → همان مسیر قبلی بماند
        val video = item.type == com.karbama.app.data.model.MediaType.VIDEO
        val ext = if (video) "mp4" else "jpg"
        val name = "job_${jobKey}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$ext"

        val storage = Service.storage
        if (storage != null) {
            val ref = storage.reference.child("jobs/$jobKey/$name")
            ref.putBytes(bytes).await()
            val url = ref.downloadUrl.await().toString()
            // پوستر ویدیو (اگر از گوشی انتخاب شده) هم باید آپلود شود
            val poster = if (video) uploadPoster(jobKey, item.poster, name) else item.poster
            return item.copy(url = url, poster = poster)
        }

        val path = LocalPhotoCache.saveFile(name, bytes)   // حالت نمایشی
        val poster = if (video) copyPoster(item.poster, name) else item.poster
        return item.copy(url = path.ifBlank { item.url }, poster = poster)
    }

    private suspend fun uploadPoster(jobKey: String, poster: String, base: String): String {
        if (!MediaUtil.isDeviceFile(poster)) return poster
        return runCatching {
            val bytes = Service.app.contentResolver.openInputStream(Uri.parse(poster))
                ?.use { it.readBytes() } ?: return poster
            val ref = Service.storage!!.reference.child("jobs/$jobKey/poster_$base.jpg")
            ref.putBytes(bytes).await()
            ref.downloadUrl.await().toString()
        }.getOrDefault(poster)
    }

    private fun copyPoster(poster: String, base: String): String {
        if (!MediaUtil.isDeviceFile(poster)) return poster
        return runCatching {
            val bytes = Service.app.contentResolver.openInputStream(Uri.parse(poster))
                ?.use { it.readBytes() } ?: return poster
            LocalPhotoCache.saveFile("poster_$base", bytes)
        }.getOrDefault(poster)
    }

}
