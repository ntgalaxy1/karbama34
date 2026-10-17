package com.karbama.app.data.repo

import com.karbama.app.data.Config
import com.karbama.app.data.Service
import com.karbama.app.data.model.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * یک فعالیت کاربر روی یک آگهی (پسندیدن یا ارسال درخواست) — برای نمایش در پنل مدیر.
 */
data class ActivityItem(
    val applicantId: String,
    val jobId: String,
    val titleKey: String,
    val titleCustom: String,
    val company: String,
    val city: String,
    val district: String,
    val createdAt: Long
)

/**
 * پسندیده‌ها و درخواست‌های ارسالی کاربر در Firestore.
 *
 * طراحی:
 *  · کلکسیون‌های `likes` و `applications` — شناسه‌ی هر سند «{شناسه‌ی متقاضی}_{شناسه‌ی آگهی}» است
 *    تا نوشتن دوباره رکورد تکراری نسازد.
 *  · هر سند هم «شناسه‌ی متقاضی» و هم «uid ورود» را نگه می‌دارد (برای قواعد امنیتی).
 *  · اگر Firebase وصل نباشد (حالت نمایشی) همه‌ی این توابع امن و بی‌اثرند و برنامه
 *    محلی (روی خود گوشی) ادامه می‌دهد — بدون خطا و بدون کرش.
 */
class UserActivityRepository {

    private val db get() = Service.db

    private fun docId(applicantId: String, jobId: String) = "${applicantId}_$jobId"

    private fun payload(applicantId: String, job: Job?, jobId: String): Map<String, Any?> = mapOf(
        "applicantId" to applicantId,
        "uid" to (Service.currentUid ?: applicantId),
        "jobId" to jobId,
        "titleKey" to (job?.titleKey ?: ""),
        "titleCustom" to (job?.titleCustom ?: ""),
        "company" to (job?.company ?: ""),
        "city" to (job?.city ?: ""),
        "district" to (job?.district ?: ""),
        "createdAt" to System.currentTimeMillis()
    )

    private fun toItem(applicantId: String, doc: com.google.firebase.firestore.DocumentSnapshot): ActivityItem? {
        val jobId = doc.getString("jobId") ?: return null
        return ActivityItem(
            applicantId = doc.getString("applicantId") ?: applicantId,
            jobId = jobId,
            titleKey = doc.getString("titleKey") ?: "",
            titleCustom = doc.getString("titleCustom") ?: "",
            company = doc.getString("company") ?: "",
            city = doc.getString("city") ?: "",
            district = doc.getString("district") ?: "",
            createdAt = doc.getLong("createdAt") ?: 0L
        )
    }

    // ---------- نوشتن ----------

    /** پسندیدن یک آگهی (سرور) */
    suspend fun like(applicantId: String, job: Job): Result<Unit> {
        val d = db ?: return Result.success(Unit)
        if (applicantId.isBlank()) return Result.success(Unit)
        return runCatching {
            d.collection(Config.COL_LIKES).document(docId(applicantId, job.id))
                .set(payload(applicantId, job, job.id)).await()
            Unit
        }
    }

    /** برداشتن آگهی از پسندیده‌ها (سرور) */
    suspend fun unlike(applicantId: String, jobId: String): Result<Unit> {
        val d = db ?: return Result.success(Unit)
        if (applicantId.isBlank()) return Result.success(Unit)
        return runCatching {
            d.collection(Config.COL_LIKES).document(docId(applicantId, jobId)).delete().await()
            Unit
        }
    }

    /** ثبت «درخواست ارسال شد» (سرور) */
    suspend fun apply(applicantId: String, job: Job): Result<Unit> {
        val d = db ?: return Result.success(Unit)
        if (applicantId.isBlank()) return Result.success(Unit)
        return runCatching {
            d.collection(Config.COL_APPLICATIONS).document(docId(applicantId, job.id))
                .set(payload(applicantId, job, job.id)).await()
            Unit
        }
    }

    // ---------- خواندن ----------

    private suspend fun idsOf(collection: String, applicantId: String): Result<Set<String>> {
        val d = db ?: return Result.success(emptySet())
        if (applicantId.isBlank()) return Result.success(emptySet())
        return runCatching {
            d.collection(collection)
                .whereEqualTo("applicantId", applicantId)
                .limit(500)
                .get().await()
                .documents
                .mapNotNull { it.getString("jobId") }
                .toSet()
        }
    }

    suspend fun myLikedJobIds(applicantId: String): Result<Set<String>> =
        idsOf(Config.COL_LIKES, applicantId)

    suspend fun myAppliedJobIds(applicantId: String): Result<Set<String>> =
        idsOf(Config.COL_APPLICATIONS, applicantId)

    /** جریان زنده‌ی پسندیده‌های یک متقاضی (برای پنل مدیر) */
    fun observeLikes(applicantId: String): Flow<List<ActivityItem>> =
        observeCollection(Config.COL_LIKES, applicantId)

    /** جریان زنده‌ی درخواست‌های یک متقاضی (برای پنل مدیر) */
    fun observeApplications(applicantId: String): Flow<List<ActivityItem>> =
        observeCollection(Config.COL_APPLICATIONS, applicantId)

    private fun observeCollection(collection: String, applicantId: String): Flow<List<ActivityItem>> {
        val d = db ?: return flowOf(emptyList())
        if (applicantId.isBlank()) return flowOf(emptyList())
        return callbackFlow {
            // ⚠️ بدون orderBy در کوئری: «برابری + مرتب‌سازی» به ایندکس ترکیبی Firebase نیاز دارد
            // و بدون آن کوئری خطا می‌دهد؛ پس مرتب‌سازی را همین‌جا در برنامه انجام می‌دهیم.
            val reg = d.collection(collection)
                .whereEqualTo("applicantId", applicantId)
                .limit(200)
                .addSnapshotListener { snap, _ ->
                    val items = (snap?.documents ?: emptyList())
                        .mapNotNull { toItem(applicantId, it) }
                        .sortedByDescending { it.createdAt }
                    trySend(items)
                }
            awaitClose { reg.remove() }
        }
    }

    /** ادغام پسندیده‌ها/درخواست‌های محلی با سرور (برای برگرداندن داده‌ها بعد از ورود روی دستگاه تازه) */
    suspend fun mergeIds(
        applicantId: String,
        localLiked: Set<String>,
        localApplied: Set<String>
    ): Pair<Set<String>, Set<String>> {
        if (db == null || applicantId.isBlank()) return localLiked to localApplied
        val cloudLiked = myLikedJobIds(applicantId).getOrDefault(emptySet())
        val cloudApplied = myAppliedJobIds(applicantId).getOrDefault(emptySet())
        return (localLiked + cloudLiked) to (localApplied + cloudApplied)
    }
}
