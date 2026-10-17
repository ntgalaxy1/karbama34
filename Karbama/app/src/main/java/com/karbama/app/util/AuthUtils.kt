package com.karbama.app.util

import java.security.MessageDigest

/** ابزارهای ساده‌ی امنیتی و اعتبارسنجی ورود */
object AuthUtils {

    /** نام کاربری معتبر: حداقل ۳ حرف، بدون فاصله */
    fun usernameIsValid(u: String): Boolean {
        val t = u.trim()
        return t.length >= 3 && !t.contains(" ") && t.none { it == '@' }
    }

    /** رمز عبور: حداقل ۴ کاراکتر */
    fun passwordIsValid(p: String): Boolean = p.length >= 4

    /** فقط حروف و اعداد انگلیسی + . _ - (بقیه حذف می‌شود) */
    fun sanitizeUsername(v: String): String =
        v.filter { it.code in 33..126 && (it.isLetterOrDigit() || it == '.' || it == '_' || it == '-') }
            .take(24)

    /** هش SHA-256 — رمز خام هرگز ذخیره نمی‌شود */
    fun hash(password: String, salt: String = "karbama"): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(("$salt::$password").toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /** ایمیل است؟ (برای تشخیص ورود مدیر با حساب Firebase) */
    fun looksLikeEmail(v: String): Boolean =
        v.contains("@") && v.substringAfter("@").contains(".")

    /** فقط رقم‌ها یکسان‌سازی شماره برای ورود با تلفن */
    fun digits(v: String): String = v.filter { it.isDigit() }
}
