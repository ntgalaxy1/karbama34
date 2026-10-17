package com.karbama.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * کمک‌تابع‌های قالب‌بندی: شماره ملی، شماره کارت، تاریخ.
 */
object FormatUtils {

    /** فقط رقم‌ها، با محدودیت طول */
    fun digitsOnly(input: String, max: Int = 16): String =
        input.filter { it.isDigit() }.take(max)

    /** ‎6037 9911 2233 4455 — گروه‌بندی چهاررقمی */
    fun formatCard(input: String): String =
        digitsOnly(input, 16).chunked(4).joinToString(" ")

    fun cardIsValid(input: String): Boolean = digitsOnly(input, 16).length == 16

    fun nationalIdIsValid(input: String): Boolean {
        val d = digitsOnly(input, 10)
        return d.length in 8..10
    }

    /** ۲۰۲۶/۱۰/۱۱ */
    fun todayIso(): String =
        SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())

    /** ۱۱ اکتبر ۲۰۲۶ — شنبه */
    fun todayPersian(): String {
        val fa = Locale("fa", "IR")
        val date = try {
            SimpleDateFormat("d MMMM yyyy", fa).format(Date())
        } catch (e: Exception) {
            SimpleDateFormat("d MMMM yyyy", Locale.US).format(Date())
        }
        val day = try {
            SimpleDateFormat("EEEE", fa).format(Date())
        } catch (e: Exception) {
            SimpleDateFormat("EEEE", Locale.US).format(Date())
        }
        return "$day، $date"
    }
}
