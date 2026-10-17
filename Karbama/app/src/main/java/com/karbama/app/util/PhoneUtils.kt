package com.karbama.app.util

/**
 * پیش‌شماره و اعتبارسنجی شماره موبایل.
 * پشتیبانی از شماره‌های ترکیه (+90) و ایران (+98).
 * برای اضافه‌کردن کشور دیگر، فقط یک مقدار به enum اضافه کن.
 */
enum class PhoneCountry(
    val code: Int,
    val flag: String,
    val iso: String,
    val localLength: Int,
    val firstDigit: Char,
    val example: String
) {
    TURKIYE(90, "🇹🇷", "TR", 10, '5', "0532 123 45 67"),
    IRAN(98, "🇮🇷", "IR", 10, '9', "0912 345 6789");

    companion object {
        fun ofIso(iso: String?): PhoneCountry =
            values().firstOrNull { it.iso.equals(iso, ignoreCase = true) } ?: TURKIYE

        fun ofCode(code: Int): PhoneCountry =
            values().firstOrNull { it.code == code } ?: TURKIYE
    }
}

object PhoneUtils {

    /** فقط رقم‌های محلی شماره (بدون پیش‌شماره کشور و بدون صفر ابتدایی) */
    fun digitsOf(country: PhoneCountry, input: String): String {
        var d = input.filter { it.isDigit() }
        val cc = country.code.toString()
        if (d.startsWith(cc) && d.length > country.localLength) d = d.removePrefix(cc)
        if (d.startsWith("0")) d = d.removePrefix("0")
        return d
    }

    /** آیا شماره برای این کشور معتبر است؟ */
    fun isValid(country: PhoneCountry, input: String): Boolean {
        val d = digitsOf(country, input)
        return d.length == country.localLength && d.first() == country.firstDigit
    }

    /** خروجی استاندارد بین‌المللی: ‎+90 532 123 45 67 یا ‎+98 912 345 6789 */
    fun format(country: PhoneCountry, input: String): String {
        val d = digitsOf(country, input)
        val grouped = when (country) {
            PhoneCountry.TURKIYE -> d.replace(
                Regex("(\\d{3})(\\d{3})(\\d{2})(\\d{2})"), "$1 $2 $3 $4"
            )
            PhoneCountry.IRAN -> d.replace(
                Regex("(\\d{3})(\\d{3})(\\d{4})"), "$1 $2 $3"
            )
        }
        return "+${country.code} $grouped"
    }
}
