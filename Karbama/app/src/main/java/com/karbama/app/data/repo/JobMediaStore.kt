package com.karbama.app.data.repo

import android.net.Uri
import com.karbama.app.data.Service
import com.karbama.app.data.local.LocalPhotoCache
import com.karbama.app.data.model.JobMedia
import com.karbama.app.util.MediaUtil
import com.karbama.app.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * رسانه‌های آگهی (عکس و ویدیو) را قبل از ذخیره آماده می‌کند:
 *
 *  - فایلی که مدیر از گوشی انتخاب کرده (`content://…`):
 *      · اگر Firebase وصل باشد → عکس در Firestore (jobFiles) ذخیره می‌شود (v48)؛
 *        ویدیوی داخل گوشی پشتیبانی نمی‌شود و باید به‌صورت لینک اضافه شود
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
        val video = item.type == com.karbama.app.data.model.MediaType.VIDEO

        if (Service.db != null) {
            // v48: حالت متصل → عکس در Firestore؛ ویدیوی داخل گوشی جا نمی‌شود (باید لینک بگذارید)
            if (video) throw IllegalArgumentException("device video not supported; use a link")
            val bytes = ImageUtils.prepare(ctx, Uri.parse(item.url)) ?: return item
            return item.copy(url = FirestoreFiles.putJobImage(jobKey, bytes))
        }

        // ---------- حالت نمایشی: کپی داخل حافظه‌ی برنامه ----------
        val bytes = ctx.contentResolver.openInputStream(Uri.parse(item.url))?.use { it.readBytes() }
            ?: return item
        val ext = if (video) "mp4" else "jpg"
        val name = "job_${jobKey}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$ext"
        val path = LocalPhotoCache.saveFile(name, bytes)
        val poster = if (video) copyPoster(item.poster, name) else item.poster
        return item.copy(url = path.ifBlank { item.url }, poster = poster)
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
