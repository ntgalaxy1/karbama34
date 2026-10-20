package com.karbama.app.util

import java.text.Collator
import java.util.Locale

/**
 * v45: پیش‌شماره‌ی همه‌ی کشورهای دنیا.
 * نام کشورها از داده‌های زبانی خود اندروید (فارسی/ترکی/انگلیسی/عربی) گرفته می‌شود.
 * ترکیه و ایران اعتبارسنجی دقیق دارند؛ بقیه‌ی کشورها اعتبارسنجی عمومی (۶ تا ۱۳ رقم).
 */
class PhoneCountry private constructor(
    /** کد دو حرفی ISO 3166 مثل TR */
    val iso: String,
    /** پیش‌شماره بدون + مثل 90 یا 1268 */
    val code: String
) {
    val flag: String = iso.uppercase(Locale.ROOT)
        .map { String(Character.toChars(0x1F1E6 + (it - 'A'))) }
        .joinToString("")

    /** نمونه‌ی شماره (فقط برای ترکیه و ایران) */
    val example: String = when (iso) {
        "TR" -> "0532 123 45 67"
        "IR" -> "0912 345 6789"
        else -> ""
    }

    /** نام کشور به زبان رابط کاربری */
    fun name(uiLang: String): String {
        val n = Locale("", iso).getDisplayCountry(Locale(uiLang))
        return if (n.isBlank() || n.equals(iso, ignoreCase = true)) FALLBACK_NAMES[iso] ?: iso else n
    }

    /** برچسب فهرست: «🇹🇷 ترکیه  ‎+90» */
    fun label(uiLang: String): String = "$flag ${name(uiLang)}  \u200E+$code"

    /** متن قابل جست‌وجو: نام به زبان فعلی + انگلیسی + کد */
    fun searchText(uiLang: String): String =
        name(uiLang) + " " + name("en") + " +" + code + " " + code + " " + iso

    override fun equals(other: Any?): Boolean = other is PhoneCountry && other.iso == iso
    override fun hashCode(): Int = iso.hashCode()
    override fun toString(): String = iso

    companion object {
        private const val TABLE =
            "AF93 AX358 AL355 DZ213 AS1684 AD376 AO244 AI1264 AG1268 AR54 AM374 AW297 AU61 AT43 " +
            "AZ994 BS1242 BH973 BD880 BB1246 BY375 BE32 BZ501 BJ229 BM1441 BT975 BO591 BA387 " +
            "BW267 BR55 IO246 VG1284 BN673 BG359 BF226 BI257 KH855 CM237 CA1 CV238 KY1345 CF236 " +
            "TD235 CL56 CN86 CX61 CC61 CO57 KM269 CG242 CD243 CK682 CR506 CI225 HR385 CU53 CW599 " +
            "CY357 CZ420 DK45 DJ253 DM1767 DO1809 EC593 EG20 SV503 GQ240 ER291 EE372 SZ268 ET251 " +
            "FK500 FO298 FJ679 FI358 FR33 GF594 PF689 GA241 GM220 GE995 DE49 GH233 GI350 GR30 " +
            "GL299 GD1473 GP590 GU1671 GT502 GG44 GN224 GW245 GY592 HT509 HN504 HK852 HU36 IS354 " +
            "IN91 ID62 IR98 IQ964 IE353 IM44 IL972 IT39 JM1876 JP81 JE44 JO962 KZ7 KE254 KI686 " +
            "XK383 KW965 KG996 LA856 LV371 LB961 LS266 LR231 LY218 LI423 LT370 LU352 MO853 MG261 " +
            "MW265 MY60 MV960 ML223 MT356 MH692 MQ596 MR222 MU230 YT262 MX52 FM691 MD373 MC377 " +
            "MN976 ME382 MS1664 MA212 MZ258 MM95 NA264 NR674 NP977 NL31 NC687 NZ64 NI505 NE227 " +
            "NG234 NU683 NF672 KP850 MK389 MP1670 NO47 OM968 PK92 PW680 PS970 PA507 PG675 PY595 " +
            "PE51 PH63 PL48 PT351 PR1787 QA974 RE262 RO40 RU7 RW250 BL590 SH290 KN1869 LC1758 " +
            "MF590 PM508 VC1784 WS685 SM378 ST239 SA966 SN221 RS381 SC248 SL232 SG65 SX1721 SK421 " +
            "SI386 SB677 SO252 ZA27 KR82 SS211 ES34 LK94 SD249 SR597 SJ47 SE46 CH41 SY963 TW886 " +
            "TJ992 TZ255 TH66 TL670 TG228 TK690 TO676 TT1868 TN216 TR90 TM993 TC1649 TV688 UG256 " +
            "UA380 AE971 GB44 US1 UY598 UZ998 VU678 VA39 VE58 VN84 VI1340 WF681 EH212 YE967 ZM260 " +
            "ZW263 BQ599"

        private val FALLBACK_NAMES = mapOf("XK" to "Kosovo")

        private val ALL: List<PhoneCountry> = TABLE.split(' ').filter { it.length > 2 }
            .map { PhoneCountry(it.substring(0, 2), it.substring(2)) }

        val TURKIYE: PhoneCountry = ALL.first { it.iso == "TR" }
        val IRAN: PhoneCountry = ALL.first { it.iso == "IR" }

        fun values(): List<PhoneCountry> = ALL

        fun ofIso(iso: String?): PhoneCountry =
            ALL.firstOrNull { it.iso.equals(iso, ignoreCase = true) } ?: TURKIYE

        fun ofCode(code: Int): PhoneCountry =
            ALL.firstOrNull { it.code == code.toString() } ?: TURKIYE

        private val sortedCache = HashMap<String, List<PhoneCountry>>()

        /** ترکیه و ایران اول، بقیه به ترتیب الفبای زبان رابط کاربری */
        fun sortedFor(uiLang: String): List<PhoneCountry> = synchronized(sortedCache) {
            sortedCache.getOrPut(uiLang) {
                val collator = Collator.getInstance(Locale(uiLang))
                listOf(TURKIYE, IRAN) + ALL.filter { it != TURKIYE && it != IRAN }
                    .sortedWith { a, b -> collator.compare(a.name(uiLang), b.name(uiLang)) }
            }
        }
    }
}

object PhoneUtils {

    /** فقط رقم‌های محلی شماره (بدون پیش‌شماره کشور و بدون صفر ابتدایی) */
    fun digitsOf(country: PhoneCountry, input: String): String {
        var d = input.filter { it.isDigit() }
        if (d.startsWith("00")) d = d.removePrefix("00")
        val cc = country.code
        val localMax = if (country.iso == "TR" || country.iso == "IR") 10 else 13
        if (d.startsWith(cc) && d.length > localMax) d = d.removePrefix(cc)
        if (d.startsWith("0")) d = d.removePrefix("0")
        return d
    }

    /** آیا شماره برای این کشور معتبر است؟ */
    fun isValid(country: PhoneCountry, input: String): Boolean {
        val d = digitsOf(country, input)
        return when (country.iso) {
            "TR" -> d.length == 10 && d.first() == '5'
            "IR" -> d.length == 10 && d.first() == '9'
            else -> d.length in 6..13
        }
    }

    /** خروجی استاندارد بین‌المللی: ‎+90 532 123 45 67 یا ‎+98 912 345 6789 */
    fun format(country: PhoneCountry, input: String): String {
        val d = digitsOf(country, input)
        val grouped = when (country.iso) {
            "TR" -> d.replace(Regex("(\\d{3})(\\d{3})(\\d{2})(\\d{2})"), "$1 $2 $3 $4")
            "IR" -> d.replace(Regex("(\\d{3})(\\d{3})(\\d{4})"), "$1 $2 $3")
            else -> d.chunked(3).joinToString(" ")
        }
        return "+${country.code} $grouped"
    }
}
