package com.karbama.app.data.repo

import com.google.firebase.firestore.Query
import com.karbama.app.data.Config
import com.karbama.app.data.Service
import com.karbama.app.data.local.LocalPhotoCache
import com.karbama.app.data.local.LocalStore
import com.karbama.app.data.model.Applicant
import com.karbama.app.util.AuthUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * منبع متقاضیان کار (پروفایل، مدارک، وضعیت).
 */
class ApplicantRepository {

    /**
     * ثبت‌نام متقاضی.
     * در حالت متصل: ورود ناشناس + ذخیره‌ی دو عکس در Firestore (v48) + ثبت سند در Firestore
     * در حالت نمایشی: ذخیره روی خود گوشی
     */
    suspend fun submit(
        applicant: Applicant,
        idCardBytes: ByteArray?,
        bodyPhotoBytes: ByteArray?
    ): Result<Applicant> {
        val db = Service.db
        val auth = Service.auth

        if (db == null || auth == null) {
            // ---------- حالت نمایشی ----------
            val id = applicant.id.ifEmpty { "local_" + System.currentTimeMillis() }
            val saved = applicant.copy(
                id = id,
                uid = id,
                createdAt = System.currentTimeMillis(),
                idPhotoUrl = idCardBytes?.let { LocalPhotoCache.save(id, "id", it) } ?: "",
                bodyPhotoUrl = bodyPhotoBytes?.let { LocalPhotoCache.save(id, "body", it) } ?: ""
            )
            LocalStore.upsertApplicant(saved)
            return Result.success(saved)
        }

        // ---------- حالت متصل ----------
        return runCatching {
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
            val uid = user?.uid ?: throw IllegalStateException("auth failed")

            val idUrl = idCardBytes?.let {
                FirestoreFiles.putApplicantImage(uid, FirestoreFiles.KIND_ID_CARD, it)
            } ?: ""
            val bodyUrl = bodyPhotoBytes?.let {
                FirestoreFiles.putApplicantImage(uid, FirestoreFiles.KIND_BODY, it)
            } ?: ""

            val saved = applicant.copy(
                id = uid,
                uid = uid,
                createdAt = System.currentTimeMillis(),
                idPhotoUrl = idUrl,
                bodyPhotoUrl = bodyUrl
            )
            // v49: «paymentAt» را فقط مدیر می‌نویسد (قوانین Firestore) → هنگام ساخت فرستاده نمی‌شود
            db.collection(Config.COL_APPLICANTS).document(uid).set(saved.toMap() - "paymentAt").await()
            saved
        }
    }

    /** فهرست متقاضیان برای پنل مدیریت */
    fun observeApplicants(): Flow<List<Applicant>> {
        val db = Service.db ?: return LocalStore.applicants
        return callbackFlow {
            val registration = db.collection(Config.COL_APPLICANTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(500)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.map { it.toApplicant() } ?: emptyList())
                }
            awaitClose { registration.remove() }
        }
    }

    /** تغییر وضعیت متقاضی: new / reviewed / referred */
    suspend fun setStatus(id: String, status: String): Result<Unit> {
        val db = Service.db
        if (db == null) {
            LocalStore.setStatus(id, status)
            return Result.success(Unit)
        }
        return runCatching {
            db.collection(Config.COL_APPLICANTS).document(id).update("status", status).await()
        }
    }

    /**
     * v38: Submit a bounded bank/card-to-card receipt for manager review.
     * The client can only create a `pending` request. It never sets `paid`.
     * In connected mode the compressed JPEG goes to applicantFiles/{uid}/files/payment_receipt (Firestore, v48);
     * in demo mode it remains private app storage so the manager screen can still inspect it.
     */
    suspend fun submitManualPayment(
        applicant: Applicant,
        reference: String,
        receiptBytes: ByteArray
    ): Result<Applicant> {
        val safeReference = reference.trim().filter { it.isLetterOrDigit() || it == '-' }.take(32)
        if (safeReference.length < 3) return Result.failure(IllegalArgumentException("payment reference is required"))
        if (receiptBytes.isEmpty() || receiptBytes.size > 5 * 1024 * 1024) {
            return Result.failure(IllegalArgumentException("receipt image must be at most 5 MB"))
        }
        val at = System.currentTimeMillis()
        val db = Service.db
        val auth = Service.auth
        if (db == null || auth == null) {
            val receiptPath = LocalPhotoCache.save(applicant.id, "payment_receipt", receiptBytes)
            val saved = applicant.copy(
                payStatus = "pending", payAt = at, payMethod = "manual",
                payReference = safeReference, payReceiptUrl = receiptPath
            )
            LocalStore.upsertApplicant(saved)
            return Result.success(saved)
        }
        return runCatching {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("auth required")
            if (uid != applicant.id && uid != applicant.uid) throw SecurityException("payment owner mismatch")
            val receiptUrl = FirestoreFiles.putApplicantImage(uid, FirestoreFiles.KIND_RECEIPT, receiptBytes)
            val saved = applicant.copy(
                payStatus = "pending", payAt = at, payMethod = "manual",
                payReference = safeReference, payReceiptUrl = receiptUrl
            )
            // v49: بدون paymentAt — زمان تأیید را مدیر ثبت می‌کند
            db.collection(Config.COL_APPLICANTS).document(applicant.id).update(
                mapOf(
                    "paymentStatus" to saved.payStatus,
                    "paymentMethod" to saved.payMethod,
                    "paymentReference" to saved.payReference,
                    "paymentReceiptUrl" to saved.payReceiptUrl
                )
            ).await()
            saved
        }
    }

    /**
     * #34: ثبت وضعیت پرداخت «هزینه نمایش آگهی‌ها».
     * کارجو فقط «pending» می‌گذارد؛ تأیید («paid») کارِ مدیر است.
     */
    suspend fun setPayment(applicant: Applicant, status: String): Result<Unit> {
        val at = if (status.isEmpty()) 0L else System.currentTimeMillis()
        val db = Service.db
        if (db == null) {
            LocalStore.setPayment(applicant.id, status, at)
            return Result.success(Unit)
        }
        return runCatching {
            db.collection(Config.COL_APPLICANTS).document(applicant.id)
                .update(mapOf("paymentStatus" to status, "paymentAt" to at)).await()
        }
    }

    /**
     * تغییر مبلغ تعهدنامه‌ی یک متقاضی خاص — از پنل مدیریت.
     * مبلغ پیش‌فرض هم در تنظیمات کارگزار می‌ماند؛ این یکی فقط برای همین فرد است.
     */
    suspend fun setFee(applicant: Applicant, fee: Long): Result<Unit> {
        val db = Service.db
        if (db == null) {
            LocalStore.setFee(applicant.id, fee)
            return Result.success(Unit)
        }
        val old = applicant.undertaking
        val und = (old ?: com.karbama.app.data.model.Undertaking(
            signed = true,
            date = com.karbama.app.util.FormatUtils.todayIso()
        )).copy(fee = fee)
        return runCatching {
            db.collection(Config.COL_APPLICANTS).document(applicant.id)
                .update("undertaking", und.toMap()).await()
        }
    }

    /** پروفایل خودم (بر اساس شناسه‌ی کاربر) */
    /** جست‌وجوی متقاضی بر اساس نام کاربری یا شماره تلفن — برای ورود در حالت متصل */
    suspend fun findByUsername(user: String): Applicant? {
        val db = Service.db ?: return null
        val u = user.trim()
        val snap = db.collection(Config.COL_APPLICANTS)
            .whereEqualTo("username", u)
            .limit(1)
            .get()
            .await()
        val byName = snap.documents.firstOrNull()?.toApplicant()
        if (byName != null) return byName
        val dg = AuthUtils.digits(u)
        if (dg.length < 7) return null
        // جست‌وجو با شماره تلفن (قالب‌های مختلف)
        return try {
            val s2 = db.collection(Config.COL_APPLICANTS).get().await()
            s2.documents.mapNotNull { it.toApplicant() }
                .firstOrNull { AuthUtils.digits(it.phone).endsWith(dg) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun myProfile(uid: String): Applicant? {
        val db = Service.db ?: return LocalStore.applicants.value.firstOrNull { it.uid == uid }
        return runCatching {
            val doc = db.collection(Config.COL_APPLICANTS).document(uid).get().await()
            if (doc.exists()) doc.toApplicant() else null
        }.getOrNull()
    }

    /** ویرایش پروفایل خودم */
    suspend fun updateMyProfile(applicant: Applicant): Result<Unit> {
        val db = Service.db
        if (db == null) {
            LocalStore.upsertApplicant(applicant)
            return Result.success(Unit)
        }
        return runCatching {
            // v49: فقط فیلدهای پروفایل، به‌صورت ادغام (merge):
            //  · فیلدهای پرداخت را فقط مسیر پرداخت/مدیر تغییر می‌دهد (قوانین Firestore)
            //  · «status» مال مدیر است و نباید با نسخه‌ی قدیمی گوشی بازنویسی شود
            db.collection(Config.COL_APPLICANTS).document(applicant.id)
                .set(applicant.toMap() - ADMIN_OR_PAYMENT_KEYS, com.google.firebase.firestore.SetOptions.merge())
                .await()
        }
    }

    private val ADMIN_OR_PAYMENT_KEYS = setOf(
        "status", "paymentStatus", "paymentAt", "paymentMethod", "paymentReference", "paymentReceiptUrl"
    )
}
