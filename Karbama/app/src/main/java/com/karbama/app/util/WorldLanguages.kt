package com.karbama.app.util

import android.icu.util.ULocale
import java.text.Collator
import java.util.Locale

/**
 * v45: فهرست زبان‌های زنده‌ی دنیا برای فیلد «زبان‌هایی که بلدم».
 * نام‌ها از داده‌های ICU خود اندروید به زبان رابط کاربری (fa/tr/en/ar) گرفته می‌شوند؛
 * اگر ICU نامی نداشت، نام انگلیسی جدول زیر نمایش داده می‌شود.
 * مقدار ذخیره‌شده در Firestore فقط کد زبان (ISO 639) است.
 */
object WorldLanguages {

    data class Lang(val code: String, val label: String, val search: String)

    /** ISO 639-1 — همه‌ی زبان‌های زنده (زبان‌های مرده و ساختگیِ بی‌گوینده حذف شده‌اند) */
    private const val ISO1 =
        "aa ab af ak am an ar as av ay az ba be bg bi bm bn bo br bs ca ce ch co cr cs cv cy da de " +
        "dv dz ee el en eo es et eu fa ff fi fj fo fr fy ga gd gl gn gu gv ha he hi ho hr ht hu hy " +
        "hz id ig ii ik is it iu ja jv ka kg ki kj kk kl km kn ko kr ks ku kv kw ky lb lg li ln lo " +
        "lt lu lv mg mh mi mk ml mn mr ms mt my na nb nd ne ng nl nn nr nv ny oc oj om or os pa pl " +
        "ps pt qu rm rn ro ru rw sa sc sd se sg si sk sl sm sn so sq sr ss st su sv sw ta te tg th " +
        "ti tk tl tn to tr ts tt tw ty ug uk ur uz ve vi wa wo xh yi yo za zu"

    /** زبان‌های زنده‌ی دیگر (ISO 639-2/3) با نام انگلیسی پشتیبان */
    private val EXTRA: Map<String, String> = linkedMapOf(
        // ایران، افغانستان، قفقاز، ترکیه
        "ckb" to "Central Kurdish (Sorani)", "kmr" to "Northern Kurdish (Kurmanji)",
        "sdh" to "Southern Kurdish", "lrc" to "Northern Luri", "luz" to "Southern Luri",
        "bqi" to "Bakhtiari", "glk" to "Gilaki", "mzn" to "Mazanderani", "bal" to "Balochi",
        "tly" to "Talysh", "azb" to "South Azerbaijani", "prs" to "Dari", "haz" to "Hazaragi",
        "brh" to "Brahui", "zza" to "Zazaki", "lzz" to "Laz", "crh" to "Crimean Tatar",
        "gag" to "Gagauz", "kbd" to "Kabardian", "ady" to "Adyghe", "lez" to "Lezgian",
        "xmf" to "Mingrelian", "sva" to "Svan", "aii" to "Assyrian Neo-Aramaic",
        "tru" to "Turoyo", "kum" to "Kumyk", "krc" to "Karachay-Balkar", "nog" to "Nogai",
        // عربی محلی
        "arz" to "Egyptian Arabic", "apc" to "Levantine Arabic", "acm" to "Mesopotamian Arabic",
        "afb" to "Gulf Arabic", "ary" to "Moroccan Arabic", "arq" to "Algerian Arabic",
        "aeb" to "Tunisian Arabic", "ayl" to "Libyan Arabic", "apd" to "Sudanese Arabic",
        // شبه‌قاره
        "pnb" to "Western Punjabi", "skr" to "Saraiki", "khw" to "Khowar", "bsk" to "Burushaski",
        "mai" to "Maithili", "bho" to "Bhojpuri", "awa" to "Awadhi", "mag" to "Magahi",
        "raj" to "Rajasthani", "mwr" to "Marwari", "hne" to "Chhattisgarhi", "doi" to "Dogri",
        "kok" to "Konkani", "mni" to "Manipuri", "sat" to "Santali", "brx" to "Bodo",
        "tcy" to "Tulu", "new" to "Newari", "kha" to "Khasi", "lus" to "Mizo",
        "ccp" to "Chakma", "rhg" to "Rohingya", "syl" to "Sylheti", "ctg" to "Chittagonian",
        // شرق و جنوب شرق آسیا
        "yue" to "Cantonese", "nan" to "Min Nan", "wuu" to "Wu Chinese", "hak" to "Hakka",
        "hsn" to "Xiang Chinese", "gan" to "Gan Chinese", "cdo" to "Min Dong", "ain" to "Ainu",
        "shn" to "Shan", "kac" to "Kachin", "hmn" to "Hmong", "fil" to "Filipino",
        "ceb" to "Cebuano", "ilo" to "Iloko", "hil" to "Hiligaynon", "war" to "Waray",
        "pam" to "Pampanga", "bik" to "Bikol", "pag" to "Pangasinan", "iba" to "Iban",
        "bjn" to "Banjar", "ban" to "Balinese", "mad" to "Madurese", "min" to "Minangkabau",
        "bug" to "Buginese", "ace" to "Acehnese", "sas" to "Sasak", "tet" to "Tetum",
        "mnw" to "Mon", "kjg" to "Khmu",
        // اقیانوسیه
        "tpi" to "Tok Pisin", "haw" to "Hawaiian", "rar" to "Rarotongan", "gil" to "Gilbertese",
        "tvl" to "Tuvalu", "niu" to "Niuean", "pau" to "Palauan", "chk" to "Chuukese",
        "pon" to "Pohnpeian", "yap" to "Yapese", "hif" to "Fiji Hindi", "pis" to "Pijin",
        // آفریقا
        "kab" to "Kabyle", "shi" to "Tachelhit", "tzm" to "Central Atlas Tamazight",
        "zgh" to "Standard Moroccan Tamazight", "rif" to "Riffian", "tig" to "Tigre",
        "sid" to "Sidamo", "wal" to "Wolaytta", "kri" to "Krio", "bem" to "Bemba",
        "nso" to "Northern Sotho", "tum" to "Tumbuka", "luo" to "Luo", "kam" to "Kamba",
        "mer" to "Meru", "guz" to "Gusii", "kln" to "Kalenjin", "mas" to "Masai",
        "nyn" to "Nyankole", "xog" to "Soga", "teo" to "Teso", "lua" to "Luba-Lulua",
        "fon" to "Fon", "dyu" to "Dyula", "mos" to "Mossi", "efi" to "Efik", "ibb" to "Ibibio",
        "tiv" to "Tiv", "snk" to "Soninke", "men" to "Mende", "tem" to "Timne",
        "mfe" to "Morisyen", "kea" to "Kabuverdianu", "sus" to "Susu", "dje" to "Zarma",
        "kbp" to "Kabiye", "bas" to "Basaa", "ewo" to "Ewondo", "dua" to "Duala",
        "luy" to "Luyia", "kik" to "Gikuyu", "loz" to "Lozi", "ven" to "Venda",
        "nbl" to "South Ndebele", "din" to "Dinka", "nus" to "Nuer", "fuv" to "Nigerian Fulfulde",
        // قاره‌ی آمریکا
        "pap" to "Papiamento", "srn" to "Sranan Tongo", "jam" to "Jamaican Creole",
        "gcr" to "Guianese Creole", "quc" to "K'iche'", "yua" to "Yucatec Maya", "nah" to "Nahuatl",
        "arn" to "Mapudungun", "chr" to "Cherokee", "moh" to "Mohawk", "oji" to "Ojibwa",
        "cho" to "Choctaw", "lkt" to "Lakota", "mam" to "Mam", "kek" to "Q'eqchi'",
        "cak" to "Kaqchikel", "zap" to "Zapotec", "mix" to "Mixtec", "oto" to "Otomi",
        // روسیه و آسیای میانه
        "sah" to "Yakut", "tyv" to "Tuvinian", "alt" to "Southern Altai", "bua" to "Buriat",
        "xal" to "Kalmyk", "udm" to "Udmurt", "mhr" to "Eastern Mari", "myv" to "Erzya",
        "mdf" to "Moksha", "koi" to "Komi-Permyak", "kaa" to "Kara-Kalpak", "dng" to "Dungan",
        "evn" to "Evenki", "ckt" to "Chukchi",
        // اروپا
        "smn" to "Inari Sami", "sms" to "Skolt Sami", "sma" to "Southern Sami", "smj" to "Lule Sami",
        "vro" to "Võro", "csb" to "Kashubian", "hsb" to "Upper Sorbian", "dsb" to "Lower Sorbian",
        "rue" to "Rusyn", "szl" to "Silesian", "fur" to "Friulian", "lld" to "Ladin",
        "lij" to "Ligurian", "lmo" to "Lombard", "pms" to "Piedmontese", "vec" to "Venetian",
        "scn" to "Sicilian", "nap" to "Neapolitan", "ast" to "Asturian", "mwl" to "Mirandese",
        "frp" to "Arpitan", "gsw" to "Swiss German", "bar" to "Bavarian", "ksh" to "Colognian",
        "nds" to "Low German", "stq" to "Saterland Frisian", "frr" to "Northern Frisian",
        "lad" to "Ladino", "rom" to "Romany", "ltg" to "Latgalian", "sgs" to "Samogitian",
        "wae" to "Walser", "rgn" to "Romagnol", "egl" to "Emilian",
        "aln" to "Gheg Albanian", "aae" to "Arbëresh", "sli" to "Lower Silesian",
        // زبان اشاره
        "tsm" to "Turkish Sign Language", "psc" to "Persian Sign Language",
        "ase" to "American Sign Language", "bfi" to "British Sign Language"
    )

    private val CODES: List<String> by lazy {
        (ISO1.split(' ') + EXTRA.keys).filter { it.isNotBlank() }.distinct()
    }

    private fun icuName(code: String, ui: String): String? = try {
        val n = ULocale.getDisplayLanguage(code, ULocale(ui))
        if (n.isNullOrBlank() || n.equals(code, ignoreCase = true)) null else n
    } catch (_: Throwable) {
        null
    }

    private fun english(code: String): String =
        icuName(code, "en") ?: EXTRA[code] ?: code

    /** نام زبان به زبان رابط کاربری (حرف اول بزرگ برای tr/en) */
    fun label(code: String, ui: String): String {
        val n = icuName(code, ui) ?: english(code)
        return n.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale(ui)) else it.toString() }
    }

    /** چند زبان، جداشده با «، » در فارسی/عربی و «, » در ترکی/انگلیسی */
    fun labels(codes: List<String>, ui: String): String =
        codes.joinToString(if (ui == "fa" || ui == "ar") "، " else ", ") { label(it, ui) }

    private val cache = HashMap<String, List<Lang>>()

    /** همه‌ی زبان‌ها؛ فارسی، ترکی، انگلیسی و عربی اول، بقیه به ترتیب الفبا */
    fun all(ui: String): List<Lang> = synchronized(cache) {
        cache.getOrPut(ui) {
            val collator = Collator.getInstance(Locale(ui))
            val top = listOf("fa", "tr", "en", "ar")
            val list = CODES.map { c ->
                val native = icuName(c, c) ?: ""
                Lang(c, label(c, ui), listOf(label(c, ui), english(c), native, c).joinToString(" "))
            }
            list.filter { it.code in top }.sortedBy { top.indexOf(it.code) } +
                list.filter { it.code !in top }.sortedWith { a, b -> collator.compare(a.label, b.label) }
        }
    }
}
