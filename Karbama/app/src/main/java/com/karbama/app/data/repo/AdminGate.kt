package com.karbama.app.data.repo

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.karbama.app.data.Config
import com.karbama.app.data.Service
import com.karbama.app.data.model.AdminMode
import kotlinx.coroutines.tasks.await

/**
 * کنترل دسترسی پنل مدیریت.
 *
 * پنل واقعی فقط با Firebase Authentication و وجود سند admins/{UID} باز می‌شود.
 * هیچ رمز محلی، پیش‌فرض یا قابل‌خواندن در برنامه وجود ندارد.
 */
object AdminGate {

    enum class LoginResult {
        SUCCESS,
        AUTH_REJECTED,
        NOT_ADMIN,
        NETWORK_ERROR,
        FIREBASE_ERROR,
        NOT_CONFIGURED
    }

    /**
     * v42: کد کوتاه و امن آخرین خطا برای نمایش در صفحه‌ی ورود
     * (مثلاً ERROR_WRONG_PASSWORD یا firestore:PERMISSION_DENIED).
     * هرگز متن خام خطا، ایمیل، رمز یا کلید در آن قرار نمی‌گیرد.
     */
    @Volatile
    var lastDetail: String = ""
        private set

    /** آیا کاربر واردشده در فهرست مدیران است؟ */
    suspend fun isAdminAccount(): Boolean {
        val db = Service.db ?: return false
        val uid = Service.currentUid ?: return false
        return runCatching {
            db.collection(Config.COL_ADMINS).document(uid).get(Source.SERVER).await().exists()
        }.getOrDefault(false)
    }

    /** ورود واقعی با Firebase و بررسی سند مدیر از سرور، نه از کش محلی. */
    suspend fun signIn(email: String, password: String): LoginResult {
        lastDetail = ""
        val auth = Service.auth ?: run { lastDetail = "auth:not_configured"; return LoginResult.NOT_CONFIGURED }
        val user = try {
            auth.signInWithEmailAndPassword(email.trim(), password).await().user
                ?: run { lastDetail = "auth:no_user"; return LoginResult.FIREBASE_ERROR }
        } catch (e: Exception) {
            lastDetail = when (e) {
                is FirebaseAuthException -> e.errorCode
                else -> e.javaClass.simpleName
            }
            return when (e) {
                is FirebaseAuthInvalidCredentialsException,
                is FirebaseAuthInvalidUserException -> LoginResult.AUTH_REJECTED
                is FirebaseNetworkException -> LoginResult.NETWORK_ERROR
                else -> LoginResult.FIREBASE_ERROR
            }
        }

        return try {
            // اولین خواندن Firestore باید با توکن تازهٔ همین کاربر انجام شود.
            user.getIdToken(true).await()
            val db = Service.db ?: run { lastDetail = "firestore:not_configured"; return LoginResult.NOT_CONFIGURED }
            val isAdmin = db.collection(Config.COL_ADMINS).document(user.uid)
                .get(Source.SERVER)
                .await()
                .exists()
            if (!isAdmin) lastDetail = "admins/{uid}:missing"
            if (isAdmin) LoginResult.SUCCESS else LoginResult.NOT_ADMIN
        } catch (e: Exception) {
            lastDetail = when (e) {
                is FirebaseFirestoreException -> "firestore:" + e.code.name
                is FirebaseAuthException -> e.errorCode
                else -> e.javaClass.simpleName
            }
            when {
                e is FirebaseNetworkException -> LoginResult.NETWORK_ERROR
                e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
                    LoginResult.NETWORK_ERROR
                else -> LoginResult.FIREBASE_ERROR
            }
        }
    }

    fun signOut() {
        runCatching { Service.auth?.signOut() }
    }

    /** تعیین حالت پنل بر اساس وضعیت فعلی */
    suspend fun currentMode(): String = when {
        isAdminAccount() -> AdminMode.LIVE
        else -> AdminMode.NONE
    }
}
