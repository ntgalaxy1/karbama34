package com.karbama.app.data.local

import com.karbama.app.data.Config

import android.content.Context
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.EssentialAddress
import com.karbama.app.data.model.Job
import com.karbama.app.data.model.Undertaking
import org.json.JSONArray
import org.json.JSONObject

/**
 * ذخیره‌سازی محلی (SharedPreferences + JSON)
 * برای نگه‌داشتن زبان، پروفایل کاربر و داده‌های حالت نمایشی.
 */
class AppPrefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("karbama_prefs", Context.MODE_PRIVATE)

    init {
        migratePaymentV38Default()
        // نسخه‌های قدیمی نام/رمز مدیر را در SharedPreferences داشتند.
        // مسیر مدیریت اکنون فقط Firebase Authentication است؛ داده‌های قدیمی پاک می‌شوند.
        sp.edit().remove(K_LEGACY_ADMIN_USER).remove(K_LEGACY_ADMIN_PASS).apply()
    }

    /** Removes legacy client-verifiable password hashes from private JSON caches. */
    private fun purgeLegacyPasswordHashes() {
        fun objectJson(raw: String?): String? = runCatching {
            JSONObject(raw ?: return null).apply { remove("passwordHash") }.toString()
        }.getOrNull()
        fun arrayJson(raw: String?): String? = runCatching {
            JSONArray(raw ?: return null).apply { for (i in 0 until length()) optJSONObject(i)?.remove("passwordHash") }.toString()
        }.getOrNull()
        val edit = sp.edit()
        objectJson(sp.getString(K_MY_PROFILE, null))?.let { edit.putString(K_MY_PROFILE, it) }
        arrayJson(sp.getString(K_LOCAL_APPLICANTS, null))?.let { edit.putString(K_LOCAL_APPLICANTS, it) }
        edit.apply()
    }

    /**
     * v38 price migration: the former built-in ads default was ₺6000; the new
     * launch price is ₺500.  This runs just once so a later explicit manager
     * update (including a deliberate ₺6000) is never overwritten again.
     */
    private fun migratePaymentV38Default() {
        if (sp.getBoolean(K_PAYMENT_V38_MIGRATED, false)) return
        if (sp.contains(K_ADS_FEE) && sp.getLong(K_ADS_FEE, 0L) == 6000L) {
            sp.edit().putLong(K_ADS_FEE, Config.DEFAULT_ADS_FEE).apply()
        }
        sp.edit().putBoolean(K_PAYMENT_V38_MIGRATED, true).apply()
    }

    var lang: String?
        get() = sp.getString(K_LANG, null)
        set(v) = sp.edit().putString(K_LANG, v).apply()

    /** system (پیش‌فرض) | light | dark — انتخاب رنگ هر کاربر روی همین گوشی */
    var themeMode: String
        get() = sp.getString(K_THEME_MODE, "system") ?: "system"
        set(v) = sp.edit().putString(K_THEME_MODE, v).apply()

    var registered: Boolean
        get() = sp.getBoolean(K_REGISTERED, false)
        set(v) = sp.edit().putBoolean(K_REGISTERED, v).apply()

    var adminMode: String
        get() = sp.getString(K_ADMIN_MODE, "none") ?: "none"
        set(v) = sp.edit().putString(K_ADMIN_MODE, v).apply()

    var myApplicantId: String
        get() = sp.getString(K_MY_ID, "") ?: ""
        set(v) = sp.edit().putString(K_MY_ID, v).apply()

    // ---------- تنظیمات کارگزار (برای متن تعهدنامه) ----------

    var brokerFee: Long
        get() = sp.getLong(K_BROKER_FEE, 6000L)
        set(v) = sp.edit().putLong(K_BROKER_FEE, v).apply()

    var brokerCard: String
        get() = sp.getString(K_BROKER_CARD, Config.DEFAULT_BROKER_CARD) ?: Config.DEFAULT_BROKER_CARD
        set(v) = sp.edit().putString(K_BROKER_CARD, v).apply()

    var brokerHolder: String
        get() = sp.getString(K_BROKER_HOLDER, "Karbama") ?: "Karbama"
        set(v) = sp.edit().putString(K_BROKER_HOLDER, v).apply()

    // ---------- #34: هزینه‌ی نمایش آگهی‌ها (تنظیمات مدیر) ----------

    var adsFee: Long
        get() = sp.getLong(K_ADS_FEE, Config.DEFAULT_ADS_FEE)
        set(v) = sp.edit().putLong(K_ADS_FEE, v).apply()

    // ---------- ورود دومرحله‌ای (2FA / TOTP) ----------

    var twoFactorEnabled: Boolean
        get() = sp.getBoolean(K_2FA_ON, false)
        set(v) = sp.edit().putBoolean(K_2FA_ON, v).apply()

    var twoFactorSecret: String
        get() = sp.getString(K_2FA_SECRET, "") ?: ""
        set(v) = sp.edit().putString(K_2FA_SECRET, v).apply()

    var twoFactorRecovery: String
        get() = sp.getString(K_2FA_RECOVERY, "") ?: ""
        set(v) = sp.edit().putString(K_2FA_RECOVERY, v).apply()

    // ---------- پروفایل خودم ----------

    fun saveMyProfile(a: Applicant) = sp.edit().putString(K_MY_PROFILE, a.toJson().toString()).apply()

    fun clearMyProfile() = sp.edit().remove(K_MY_PROFILE).remove(K_MY_ID).apply()

    /** Password persistence was removed; Firebase owns credentials and refresh tokens. */
    fun clearRememberedLogin() = Unit

    fun myProfile(): Applicant? {
        val raw = sp.getString(K_MY_PROFILE, null) ?: return null
        return runCatching { jsonToApplicant(JSONObject(raw)) }.getOrNull()
    }

    // ---------- درخواست‌های ارسال‌شده ----------

    fun appliedIds(): MutableSet<String> =
        (sp.getStringSet(K_APPLIED, emptySet()) ?: emptySet()).toMutableSet()

    /** #29: جای‌گذاری کل مجموعه (برای هم‌گام‌سازی با سرور) */
    fun setAppliedIds(ids: Set<String>) {
        sp.edit().putStringSet(K_APPLIED, ids).apply()
    }

    fun setLikedIds(ids: Set<String>) {
        sp.edit().putStringSet(K_LIKED, ids).apply()
    }

    fun addApplied(jobId: String) {
        val s = appliedIds()
        s.add(jobId)
        sp.edit().putStringSet(K_APPLIED, s).apply()
    }

    // ---------- #28: آگهی‌های پسندیده ----------

    fun likedIds(): MutableSet<String> =
        (sp.getStringSet(K_LIKED, emptySet()) ?: emptySet()).toMutableSet()

    fun toggleLiked(jobId: String): Boolean {
        val s = likedIds()
        val nowLiked = if (s.contains(jobId)) { s.remove(jobId); false } else { s.add(jobId); true }
        sp.edit().putStringSet(K_LIKED, s).apply()
        return nowLiked
    }

    // ---------- داده‌های حالت نمایشی ----------

    fun saveLocalJobs(list: List<Job>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        sp.edit().putString(K_LOCAL_JOBS, arr.toString()).apply()
    }

    fun localJobs(): List<Job>? {
        val raw = sp.getString(K_LOCAL_JOBS, null) ?: return null
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { jsonToJob(arr.getJSONObject(it)) }
        }.getOrNull()
    }

    fun saveLocalApplicants(list: List<Applicant>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        sp.edit().putString(K_LOCAL_APPLICANTS, arr.toString()).apply()
    }

    fun localApplicants(): List<Applicant>? {
        val raw = sp.getString(K_LOCAL_APPLICANTS, null) ?: return null
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { jsonToApplicant(arr.getJSONObject(it)) }
        }.getOrNull()
    }

    fun saveLocalEssentialAddresses(list: List<EssentialAddress>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        sp.edit().putString(K_LOCAL_ESSENTIAL_ADDRESSES, arr.toString()).apply()
    }

    fun localEssentialAddresses(): List<EssentialAddress>? {
        val raw = sp.getString(K_LOCAL_ESSENTIAL_ADDRESSES, null) ?: return null
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { jsonToEssentialAddress(arr.getJSONObject(it)) }
        }.getOrNull()
    }

    fun clearAll() {
        sp.edit().clear().apply()
    }

    private companion object {
        const val K_LANG = "lang"
        const val K_THEME_MODE = "theme_mode"
        const val K_REGISTERED = "registered"
        const val K_ADMIN_MODE = "admin_mode"
        const val K_MY_ID = "my_applicant_id"
        const val K_MY_PROFILE = "my_profile"
        const val K_APPLIED = "applied_jobs"
        const val K_LIKED = "liked_jobs"
        const val K_LOCAL_JOBS = "local_jobs"
        const val K_LOCAL_APPLICANTS = "local_applicants"
        const val K_LOCAL_ESSENTIAL_ADDRESSES = "local_essential_addresses"
        const val K_BROKER_FEE = "broker_fee"
        const val K_BROKER_CARD = "broker_card"
        const val K_ADS_FEE = "ads_fee"            // #34
        const val K_PAYMENT_V38_MIGRATED = "payment_v38_migrated"
        const val K_BROKER_HOLDER = "broker_holder"
        const val K_LEGACY_ADMIN_USER = "admin_user"
        const val K_LEGACY_ADMIN_PASS = "admin_pass"
        const val K_2FA_ON = "two_factor_on"
        const val K_2FA_SECRET = "two_factor_secret"
        const val K_2FA_RECOVERY = "two_factor_recovery"
    }
}

// ==================== JSON ====================

fun Job.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("titleKey", titleKey)
    put("titleCustom", titleCustom)
    put("company", company)
    put("city", city)
    put("district", district)
    put("salary", salary)
    put("contract", contract)
    put("conditions", JSONArray(conditions))
    put("description", description)
    put("requirements", JSONArray(requirements))
    put("contactPhone", contactPhone)
    put("active", active)
    put("publishedAt", publishedAt)
    put("createdBy", createdBy)
    put("media", JSONArray().apply {
        media.forEach { m ->
            put(JSONObject().apply {
                put("type", m.type)
                put("url", m.url)
                put("poster", m.poster)
            })
        }
    })
}

fun jsonToJob(o: JSONObject): Job = Job(
    id = o.optString("id"),
    titleKey = o.optString("titleKey"),
    titleCustom = o.optString("titleCustom"),
    company = o.optString("company"),
    city = o.optString("city"),
    district = o.optString("district"),
    salary = o.optLong("salary"),
    contract = o.optString("contract", "fullTime"),
    conditions = o.optJSONArray("conditions").toStringList(),
    description = o.optString("description"),
    requirements = o.optJSONArray("requirements").toStringList(),
    contactPhone = o.optString("contactPhone"),
    active = o.optBoolean("active", true),
    publishedAt = o.optLong("publishedAt"),
    createdBy = o.optString("createdBy"),
    media = o.optJSONArray("media")?.let { arr ->
        (0 until arr.length()).mapNotNull { i ->
            val m = arr.optJSONObject(i) ?: return@mapNotNull null
            com.karbama.app.data.model.JobMedia(
                type = m.optString("type", com.karbama.app.data.model.MediaType.IMAGE),
                url = m.optString("url"),
                poster = m.optString("poster")
            )
        }
    } ?: emptyList()
)

fun Applicant.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("uid", uid)
    put("firstName", firstName)
    put("lastName", lastName)
    put("fatherName", fatherName)
    put("username", username)
    put("nationalId", nationalId)
    put("otherJobs", otherJobs)
    put("phone", phone)
    put("email", email)
    put("city", city)
    put("district", district)
    put("address", address)
    put("destinationCountry", destinationCountry)
    put("jobKeys", JSONArray(jobKeys))
    put("spokenLangs", JSONArray(spokenLangs))
    put("trLevel", trLevel)
    put("gender", gender)
    put("idPhotoUrl", idPhotoUrl)
    put("bodyPhotoUrl", bodyPhotoUrl)
    put("status", status)
    put("consent", consent)
    put("createdAt", createdAt)
    put("lang", lang)
    put("phoneCountry", phoneCountry)
    put("phoneLocal", phoneLocal)
    put("payStatus", payStatus)          // #34
    put("payAt", payAt)                  // #34
    put("payMethod", payMethod)          // v38
    put("payReference", payReference)
    put("payReceiptUrl", payReceiptUrl)
    put("undertaking", undertaking?.toJson())
}

fun Undertaking.toJson(): JSONObject = JSONObject().apply {
    put("signed", signed)
    put("date", date)
    put("signedAt", signedAt)
    put("nationalId", nationalId)
    put("payerCard", payerCard)
    put("fee", fee)
    put("brokerCard", brokerCard)
    put("signature", signature)
}

fun jsonToUndertaking(o: JSONObject): Undertaking = Undertaking(
    signed = o.optBoolean("signed"),
    date = o.optString("date"),
    signedAt = o.optLong("signedAt"),
    nationalId = o.optString("nationalId"),
    payerCard = o.optString("payerCard"),
    fee = o.optLong("fee", 6000L),
    brokerCard = o.optString("brokerCard"),
    signature = o.optString("signature")
)

fun jsonToApplicant(o: JSONObject): Applicant = Applicant(
    id = o.optString("id"),
    uid = o.optString("uid"),
    firstName = o.optString("firstName"),
    lastName = o.optString("lastName"),
    fatherName = o.optString("fatherName"),
    username = o.optString("username"),
    createdBy = o.optString("createdBy"),
    nationalId = o.optString("nationalId"),
    otherJobs = o.optString("otherJobs"),
    phone = o.optString("phone"),
    email = o.optString("email"),
    city = o.optString("city"),
    district = o.optString("district"),
    address = o.optString("address"),
    destinationCountry = o.optString("destinationCountry", "TR"),
    jobKeys = o.optJSONArray("jobKeys").toStringList(),
    spokenLangs = o.optJSONArray("spokenLangs").toStringList(),
    trLevel = o.optInt("trLevel"),
    gender = o.optString("gender"),
    idPhotoUrl = o.optString("idPhotoUrl"),
    bodyPhotoUrl = o.optString("bodyPhotoUrl"),
    status = o.optString("status", "new"),
    consent = o.optBoolean("consent"),
    createdAt = o.optLong("createdAt"),
    lang = o.optString("lang", "fa"),
    phoneCountry = o.optString("phoneCountry", "TR"),
    phoneLocal = o.optString("phoneLocal", ""),
    payStatus = o.optString("payStatus", ""),      // #34
    payAt = o.optLong("payAt", 0L),                // #34
    payMethod = o.optString("payMethod", ""),      // v38
    payReference = o.optString("payReference", ""),
    payReceiptUrl = o.optString("payReceiptUrl", ""),
    undertaking = o.optJSONObject("undertaking")?.let { jsonToUndertaking(it) }
)

fun EssentialAddress.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("countryCode", countryCode); put("cityId", cityId); put("category", category)
    put("title", title); put("titleTr", titleTr); put("address", address); put("phone", phone)
    put("hours", hours); put("description", description)
    if (lat != null) put("lat", lat); if (lon != null) put("lon", lon)
    put("active", active); put("ownerUid", ownerUid); put("updatedAt", updatedAt)
}

fun jsonToEssentialAddress(o: JSONObject): EssentialAddress = EssentialAddress(
    id = o.optString("id"), countryCode = o.optString("countryCode", "TR"),
    cityId = o.optString("cityId", "nationwide"), category = o.optString("category", "other"),
    title = o.optString("title"), titleTr = o.optString("titleTr"), address = o.optString("address"),
    phone = o.optString("phone"), hours = o.optString("hours"), description = o.optString("description"),
    lat = if (o.has("lat") && !o.isNull("lat")) o.optDouble("lat") else null,
    lon = if (o.has("lon") && !o.isNull("lon")) o.optDouble("lon") else null,
    active = o.optBoolean("active", true), ownerUid = o.optString("ownerUid"), updatedAt = o.optLong("updatedAt")
)

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    val out = ArrayList<String>(length())
    for (i in 0 until length()) out.add(optString(i))
    return out
}
