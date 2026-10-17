package com.karbama.app.data.repo

import com.karbama.app.data.Config
import com.karbama.app.data.Service
import com.karbama.app.data.model.AdminMode
import kotlinx.coroutines.tasks.await

/**
 * کنترل دسترسی پنل مدیریت.
 *
 * دو مسیر ورود وجود دارد:
 *  ۱) رمز عبور داخل برنامه  →  حالت نمایشی (داده‌های نمونه روی گوشی)
 *  ۲) ورود با «حساب مدیر» (ایمیل و رمز Firebase)  →  حالت واقعی (Firestore)
 *      برای این کار در Firebase Console یک کاربر بساز و در مجموعه‌ی admins
 *      یک سند با شناسه‌ی همان کاربر (uid) ایجاد کن.
 */
object AdminGate {

    fun passwordIsCorrect(input: String): Boolean =
        input.trim() == Config.ADMIN_PASSWORD

    /** آیا کاربر وارد‌شده در فهرست مدیران است؟ */
    suspend fun isAdminAccount(): Boolean {
        val db = Service.db ?: return false
        val uid = Service.currentUid ?: return false
        return runCatching {
            db.collection(Config.COL_ADMINS).document(uid).get().await().exists()
        }.getOrDefault(false)
    }

    /** ورود با ایمیل و رمز مدیر */
    suspend fun signIn(email: String, password: String): Result<Boolean> {
        val auth = Service.auth ?: return Result.failure(
            IllegalStateException("Firebase تنظیم نشده است")
        )
        return runCatching {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            isAdminAccount()
        }
    }

    fun signOut() {
        runCatching { Service.auth?.signOut() }
    }

    /** تعیین حالت پنل بر اساس وضعیت فعلی */
    suspend fun currentMode(): String = when {
        isAdminAccount() -> AdminMode.LIVE
        else -> AdminMode.DEMO
    }
}
