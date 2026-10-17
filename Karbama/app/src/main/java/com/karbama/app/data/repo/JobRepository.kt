package com.karbama.app.data.repo

import com.google.firebase.firestore.Query
import com.karbama.app.data.Config
import com.karbama.app.data.Service
import com.karbama.app.data.local.LocalStore
import com.karbama.app.data.model.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * منبع آگهی‌های استخدام.
 * - اگر Firebase تنظیم شده باشد: Firestore (هم‌زمان‌سازی زنده)
 * - وگرنه: داده‌های نمونه روی گوشی
 */
class JobRepository {

    fun observeJobs(): Flow<List<Job>> {
        val db = Service.db ?: return LocalStore.jobs

        return callbackFlow {
            val registration = db.collection(Config.COL_JOBS)
                .orderBy("publishedAt", Query.Direction.DESCENDING)
                .limit(400)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents?.map { it.toJob() } ?: emptyList()
                    trySend(list)
                }
            awaitClose { registration.remove() }
        }
    }

    /** ثبت یا ویرایش آگهی (فقط مدیر) */
    suspend fun saveJob(job: Job): Result<Unit> {
        val db = Service.db
        if (db == null) {
            val id = job.id.ifEmpty { "local_" + System.currentTimeMillis() }
            LocalStore.upsertJob(
                job.copy(
                    id = id,
                    publishedAt = if (job.publishedAt == 0L) System.currentTimeMillis() else job.publishedAt,
                    createdBy = Service.currentUid ?: "demo"
                )
            )
            return Result.success(Unit)
        }
        return runCatching {
            val col = db.collection(Config.COL_JOBS)
            if (job.id.isNotEmpty()) {
                col.document(job.id).set(job.toMap()).await()
            } else {
                col.add(
                    job.copy(
                        publishedAt = System.currentTimeMillis(),
                        createdBy = Service.currentUid ?: ""
                    ).toMap()
                ).await()
            }
            Unit
        }
    }

    /** حذف آگهی (فقط مدیر) */
    suspend fun deleteJob(id: String): Result<Unit> {
        val db = Service.db
        if (db == null) {
            LocalStore.deleteJob(id)
            return Result.success(Unit)
        }
        return runCatching { db.collection(Config.COL_JOBS).document(id).delete().await() }
    }

    /** چند آگهی نمونه داخل Firestore بریزد (برای شروع کار مدیر) */
    suspend fun seedSampleJobs(): Result<Int> {
        val db = Service.db ?: return Result.success(0)
        return runCatching {
            val col = db.collection(Config.COL_JOBS)
            val existing = col.limit(1).get().await()
            if (!existing.isEmpty) return@runCatching 0
            val now = System.currentTimeMillis()
            var count = 0
            com.karbama.app.data.sample.Catalog.sampleJobs().forEachIndexed { i, j ->
                col.add(j.copy(id = "", publishedAt = now - (i + 1) * 3600_000L).toMap()).await()
                count++
            }
            count
        }
    }
}
