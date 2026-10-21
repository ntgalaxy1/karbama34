package com.karbama.app.util

/** ابزارهای ساده‌ی امنیتی و اعتبارسنجی ورود */
object AuthUtils {

    /** Firebase username mapping accepts English lowercase identifiers only. */
    fun usernameIsValid(u: String): Boolean = Regex("^[a-z][a-z0-9._-]{2,31}$").matches(u.trim().lowercase())

    /** رمز عبور: حداقل ۴ کاراکتر */
    fun passwordIsValid(p: String): Boolean = p.length >= 4

    /** فقط حروف و اعداد انگلیسی + . _ - (بقیه حذف می‌شود) */
    fun sanitizeUsername(v: String): String =
        v.lowercase().filter { it.code in 33..126 && (it.isLetterOrDigit() || it == '.' || it == '_' || it == '-') }
            .take(32)

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
