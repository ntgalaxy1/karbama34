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

    /** v51: ایمیل حساب Firebase مدیرِ واردشده (برای «یوزرنیم مدیر» در تنظیمات) */
    fun currentEmail(): String = Service.auth?.currentUser?.email.orEmpty()

    enum class PwResult { OK, WEAK, NEED_CURRENT, WRONG_CURRENT, NETWORK, ERROR }

    /**
     * v51: تغییر رمز مدیر در Firebase Auth. اگر رمز به‌خاطرسپرده‌ی همین حساب موجود باشد،
     * اول با آن دوباره احراز هویت می‌کند تا خطای «ورود اخیر لازم است» پیش نیاید.
     */
    suspend fun changePassword(
        newPassword: String,
        rememberedPassword: String?,
        currentPassword: String? = null          // v52: پسوردی که مدیر همین الان در پنجره‌ی تأیید وارد کرد
    ): PwResult {
        val auth = Service.auth ?: return PwResult.ERROR
        val user = auth.currentUser ?: return PwResult.NEED_CURRENT
        val email = user.email ?: return PwResult.ERROR
        return try {
            if (!currentPassword.isNullOrEmpty()) {
                // تأیید صریح: اگر پسورد فعلی غلط باشد همین‌جا خطای WRONG_CURRENT
                try {
                    user.reauthenticate(
                        com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
                    ).await()
                } catch (e: FirebaseNetworkException) {
                    return PwResult.NETWORK
                } catch (e: com.google.firebase.FirebaseTooManyRequestsException) {
                    lastDetail = "auth:too_many"
                    return PwResult.ERROR
                } catch (e: Exception) {
                    return PwResult.WRONG_CURRENT
                }
            } else if (!rememberedPassword.isNullOrEmpty()) {
                runCatching {
                    user.reauthenticate(
                        com.google.firebase.auth.EmailAuthProvider.getCredential(email, rememberedPassword)
                    ).await()
                }
            }
            user.updatePassword(newPassword).await()
            PwResult.OK
        } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
            PwResult.WEAK
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            PwResult.NEED_CURRENT                 // v52: به‌جای «خارج و دوباره وارد شوید» → پنجره‌ی پسورد فعلی
        } catch (e: FirebaseNetworkException) {
            PwResult.NETWORK
        } catch (e: Exception) {
            lastDetail = "auth:update_password"
            PwResult.ERROR
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
