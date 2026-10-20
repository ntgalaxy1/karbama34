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
     * برگرداندن کاربر معتبر برای «ثبت‌نام تازه».
     * هیچ رمز یا شناسه‌ای ذخیره/گزارش نمی‌شود؛ فقط Firebase Auth را از سرور refresh می‌کند.
     */
    private suspend fun freshRegistrationUser(auth: com.google.firebase.auth.FirebaseAuth): com.google.firebase.auth.FirebaseUser {
        auth.currentUser?.let { current ->
            try {
                // forceRefresh=true → اگر نشست حذف/منقضی شده، اینجا به‌جای Firestore معلوم می‌شود.
                current.getIdToken(true).await()
                return current
            } catch (_: Exception) {
                // نشست منقضی/حذف‌شده را پاک می‌کنیم؛ سپس پایین یک نشست تازه می‌سازیم.
                runCatching { auth.signOut() }
            }
        }
        return auth.signInAnonymously().await().user
            ?: throw IllegalStateException("anonymous auth failed")
    }

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
            // v52.0.3: یک نشست ناشناسِ باقی‌مانده روی گوشی ممکن است بعد از حذف/انقضای
            // حساب Firebase نامعتبر باشد. در آن وضعیت `currentUser` هنوز غیرnull است،
            // اما Firestore با PERMISSION_DENIED جواب می‌دهد. پیش از هر آپلود و نوشتن،
            // توکن را از سرور تازه می‌کنیم؛ اگر نامعتبر بود، نشست قبلی را کنار می‌گذاریم
            // و یک مالک ناشناسِ تازه می‌گیریم. بنابراین uid فایل‌ها و پرونده یکی می‌ماند.
            val user = freshRegistrationUser(auth)
            val uid = user.uid

            // v50: بررسی دوباره‌ی تکراری‌بودن، پیش از آپلود عکس‌ها
            val (nidTaken, phoneTaken) = checkUnique(applicant.nationalId, applicant.phone)
            if (nidTaken) throw DuplicateException("nid")
            if (phoneTaken) throw DuplicateException("phone")

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
            runCatching { claimKeys(saved) }   // v50: ثبت کد ملی/تلفن (best-effort)
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

    // ================= v50: یکتا بودن کد ملی و شماره تلفن =================
    // uniqueKeys/{nid_<sha256> | ph_<sha256>} = { owner: uid, kind, createdAt }
    // فقط هش ذخیره می‌شود؛ هر کاربرِ واردشده فقط می‌تواند «وجود داشتن» یک کلید را بپرسد.

    class DuplicateException(val kind: String) : Exception("duplicate:$kind")

    private fun sha256(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun nidKey(nid: String): String? =
        nid.filter { it.isDigit() }.takeIf { it.length == 10 }?.let { "nid_" + sha256("karbama|nid|$it") }

    /** phone = شماره‌ی کامل بین‌المللی (هر قالبی؛ فقط رقم‌ها استفاده می‌شوند) */
    fun phoneKey(phone: String): String? =
        phone.filter { it.isDigit() }.takeIf { it.length >= 7 }?.let { "ph_" + sha256("karbama|ph|$it") }

    /**
     * (nidTaken, phoneTaken). کلیدی «تکراری» است که مال شخص دیگری باشد —
     * یا مال همین حساب باشد ولی این حساب از قبل پروفایل داشته باشد.
     * اگر سرور در دسترس نباشد یا قانون هنوز منتشر نشده باشد → false (ثبت‌نام مسدود نمی‌شود).
     */
    suspend fun checkUnique(nid: String, phone: String): Pair<Boolean, Boolean> {
        val db = Service.db
        val auth = Service.auth
        if (db == null || auth == null) {
            val pd = phone.filter { it.isDigit() }
            val nd = nid.filter { it.isDigit() }
            val all = LocalStore.applicants.value
            return Pair(
                nd.isNotEmpty() && all.any { it.nationalId.filter { c -> c.isDigit() } == nd },
                pd.isNotEmpty() && all.any { it.phone.filter { c -> c.isDigit() } == pd }
            )
        }
        val uid = runCatching {
            (auth.currentUser ?: auth.signInAnonymously().await().user)?.uid
        }.getOrNull() ?: return Pair(false, false)
        val iHaveProfile = runCatching {
            db.collection(Config.COL_APPLICANTS).document(uid).get().await().exists()
        }.getOrDefault(false)
        suspend fun taken(key: String?): Boolean {
            if (key == null) return false
            val doc = runCatching {
                db.collection(COL_UNIQUE).document(key).get().await()
            }.getOrNull() ?: return false
            return doc.exists() && (doc.getString("owner") != uid || iHaveProfile)
        }
        return Pair(taken(nidKey(nid)), taken(phoneKey(phone)))
    }

    /** ثبت کلیدهای کد ملی/تلفن برای این متقاضی (فقط اگر هنوز نیست) */
    suspend fun claimKeys(applicant: Applicant) {
        val db = Service.db ?: return
        val uid = Service.currentUid ?: return
        if (uid != applicant.id) return
        listOf("nid" to nidKey(applicant.nationalId), "phone" to phoneKey(applicant.phone)).forEach { (kind, key) ->
            if (key == null) return@forEach
            runCatching {
                val ref = db.collection(COL_UNIQUE).document(key)
                if (!ref.get().await().exists()) {
                    ref.set(
                        mapOf(
                            "owner" to uid,
                            "kind" to kind,
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
        }
    }

    /** آزاد کردن کلید شماره‌ی قبلی بعد از تغییر شماره (فقط اگر مال خودم باشد — قانون بررسی می‌کند) */
    suspend fun releasePhoneKey(oldPhone: String) {
        val db = Service.db ?: return
        val key = phoneKey(oldPhone) ?: return
        runCatching { db.collection(COL_UNIQUE).document(key).delete().await() }
    }

    private val COL_UNIQUE = "uniqueKeys"

    private val ADMIN_OR_PAYMENT_KEYS = setOf(
        "status", "paymentStatus", "paymentAt", "paymentMethod", "paymentReference", "paymentReceiptUrl"
    )
}
