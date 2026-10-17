package com.karbama.app.util

import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * کد یکبارمصرفِ زمان‌محور (TOTP / RFC 6238) برای «ورود دومرحله‌ای».
 *
 * با هر برنامه‌ی Authenticator سازگار است (Google Authenticator، Microsoft Authenticator، Authy …).
 * کلید به شکل Base32 ذخیره می‌شود و هر ۳۰ ثانیه یک کد ۶ رقمی از آن ساخته می‌شود؛
 * بررسی کد با پنجره‌ی ±۱ گام انجام می‌شود تا اختلاف چند ثانیه‌ای ساعت‌ها مشکلی ایجاد نکند.
 */
object TotpUtil {

    private const val B32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private const val STEP_SEC = 30L
    private const val DIGITS = 6

    /** کلید تصادفی (پیش‌فرض ۲۰ بایت) به شکل Base32 بدون padding */
    fun newSecret(bytes: Int = 20): String {
        val raw = ByteArray(bytes)
        SecureRandom().nextBytes(raw)
        return base32Encode(raw)
    }

    /** کلید را خوانا نشان می‌دهد: ABCD EFGH IJKL … */
    fun pretty(secret: String): String =
        secret.trim().uppercase().chunked(4).joinToString(" ")

    /** کد ۶ رقمی این کلید در این زمان (offsetSteps برای پنجره‌ی زمانی) */
    fun code(
        secret: String,
        timeMs: Long = System.currentTimeMillis(),
        offsetSteps: Long = 0L
    ): String {
        val counter = timeMs / 1000L / STEP_SEC + offsetSteps
        val msg = ByteArray(8)
        var v = counter
        for (i in 7 downTo 0) {
            msg[i] = (v and 0xFF).toByte()
            v = v shr 8
        }
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(base32Decode(secret), "HmacSHA1"))
        val hash = mac.doFinal(msg)
        val off = hash[19].toInt() and 0x0F
        val bin = ((hash[off].toInt() and 0x7F) shl 24) or
            ((hash[off + 1].toInt() and 0xFF) shl 16) or
            ((hash[off + 2].toInt() and 0xFF) shl 8) or
            (hash[off + 3].toInt() and 0xFF)
        val mod = bin % pow10(DIGITS)
        return mod.toString().padStart(DIGITS, '0')
    }

    /** آیا این کد درست است؟ (پنجره‌ی ±۱ گام = حدود یک دقیقه) */
    fun verify(secret: String, input: String, timeMs: Long = System.currentTimeMillis()): Boolean {
        val clean = input.filter { it.isDigit() }
        if (secret.isBlank() || clean.length != DIGITS) return false
        return (-1L..1L).any { code(secret, timeMs, it) == clean }
    }

    /** کد بازیابی یک‌بارمصرف (دو گروه ۴ حرفی) */
    fun newRecoveryCode(): String {
        val raw = ByteArray(5)
        SecureRandom().nextBytes(raw)
        return base32Encode(raw).chunked(4).joinToString("-")
    }

    /** لینک استاندارد برای افزودن به برنامه‌ی Authenticator */
    fun otpauthUri(secret: String, label: String = "Karbama:admin"): String =
        "otpauth://totp/$label?secret=$secret&issuer=Karbama&digits=$DIGITS&period=$STEP_SEC"

    private fun pow10(n: Int): Int {
        var r = 1
        repeat(n) { r *= 10 }
        return r
    }

    fun base32Encode(data: ByteArray): String {
        val sb = StringBuilder()
        var buffer = 0
        var bits = 0
        for (b in data) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bits += 8
            while (bits >= 5) {
                sb.append(B32[(buffer shr (bits - 5)) and 31])
                bits -= 5
            }
        }
        if (bits > 0) sb.append(B32[(buffer shl (5 - bits)) and 31])
        return sb.toString()
    }

    fun base32Decode(s: String): ByteArray {
        val clean = s.trim().uppercase().replace(" ", "").replace("-", "").trimEnd('=')
        val out = ArrayList<Byte>(clean.length * 5 / 8 + 1)
        var buffer = 0
        var bits = 0
        for (c in clean) {
            val v = B32.indexOf(c)
            if (v < 0) continue
            buffer = (buffer shl 5) or v
            bits += 5
            if (bits >= 8) {
                out.add(((buffer shr (bits - 8)) and 0xFF).toByte())
                bits -= 8
            }
        }
        return out.toByteArray()
    }
}
