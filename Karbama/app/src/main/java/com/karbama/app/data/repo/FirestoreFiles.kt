package com.karbama.app.data.repo

import com.google.firebase.firestore.Blob
import com.karbama.app.data.Service
import com.karbama.app.util.ImageUtils
import kotlinx.coroutines.tasks.await
import java.io.FileNotFoundException
import java.util.UUID

/**
 * v48: ذخیره‌ی عکس‌ها داخل خود Firestore (به‌جای Firebase Storage که طرح Blaze می‌خواهد).
 *
 * هر عکس یک سند جداست با فیلد بایتی `data` (حداکثر ~۹۰۰ کیلوبایت، زیر سقف ۱ مگابایتی سند):
 *   applicantFiles/{uid}/files/{kind}   ← پاسپورت، عکس تمام‌قد، رسید پرداخت (فقط صاحب پرونده و مدیر)
 *   jobFiles/{fileId}                   ← عکس‌ها و پوسترهای آگهی (فقط مدیر می‌نویسد)
 *
 * در مدل‌ها فقط «آدرس» عکس ذخیره می‌شود، مثل:  fsimg://applicantFiles/UID/files/id_card
 * Coil این آدرس را با [com.karbama.app.ui.components.FirestoreImageFetcher] می‌خواند.
 * امنیت با قوانین Firestore است (firestore.rules).
 */
object FirestoreFiles {

    const val SCHEME = "fsimg"
    private const val PREFIX = "$SCHEME://"

    /** سقف حجم هر عکس (بایت) — سند Firestore حداکثر ۱٬۰۴۸٬۵۷۶ بایت است */
    const val MAX_BYTES = 900_000

    const val COL_APPLICANT_FILES = "applicantFiles"
    const val SUB_FILES = "files"
    const val COL_JOB_FILES = "jobFiles"

    /** انواع مجاز فایل متقاضی (باید با firestore.rules یکی باشد) */
    const val KIND_ID_CARD = "id_card"
    const val KIND_BODY = "body_photo"
    const val KIND_RECEIPT = "payment_receipt"

    fun isFsImage(address: String): Boolean = address.startsWith(PREFIX)

    /** آپلود عکس متقاضی؛ خروجی آدرس fsimg:// است */
    suspend fun putApplicantImage(uid: String, kind: String, bytes: ByteArray): String =
        put("$COL_APPLICANT_FILES/$uid/$SUB_FILES/$kind", owner = uid, bytes = bytes)

    /** آپلود عکس/پوستر آگهی (فقط مدیر) */
    suspend fun putJobImage(jobKey: String, bytes: ByteArray): String {
        val safeKey = jobKey.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.take(40)
        val id = "${safeKey}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val owner = Service.currentUid ?: ""
        return put("$COL_JOB_FILES/$id", owner = owner, bytes = bytes)
    }

    private suspend fun put(path: String, owner: String, bytes: ByteArray): String {
        val db = Service.db ?: throw IllegalStateException("firestore not configured")
        val fitted = ImageUtils.fitUnder(bytes, MAX_BYTES)
            ?: throw IllegalArgumentException("image too large")
        db.document(path).set(
            mapOf(
                "owner" to owner,
                "contentType" to "image/jpeg",
                "size" to fitted.size,
                "data" to Blob.fromBytes(fitted),
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
        return PREFIX + path
    }

    /** خواندن بایت‌های عکس از آدرس fsimg:// */
    suspend fun read(address: String): ByteArray {
        val db = Service.db ?: throw IllegalStateException("firestore not configured")
        val path = address.removePrefix(PREFIX).trim('/')
        require(path.isNotEmpty() && path.split('/').size % 2 == 0) { "bad image address" }
        val snap = db.document(path).get().await()
        return snap.getBlob("data")?.toBytes()
            ?: throw FileNotFoundException("image not found")
    }
}
