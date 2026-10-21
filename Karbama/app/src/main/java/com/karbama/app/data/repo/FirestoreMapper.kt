package com.karbama.app.data.repo

import com.google.firebase.firestore.DocumentSnapshot
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Job
import com.karbama.app.data.model.JobMedia
import com.karbama.app.data.model.Hotel
import com.karbama.app.data.model.MediaType
import com.karbama.app.data.model.Undertaking

/**
 * نگاشت امن سندهای Firestore به مدل‌های Kotlin.
 * (نگاشت دستی نوشته شده تا هیچ وابستگی به رفلکشن نداشته باشیم.)
 */

private fun DocumentSnapshot.strList(field: String): List<String> =
    (get(field) as? List<*>)?.filterIsInstance<String>() ?: emptyList()

fun DocumentSnapshot.toJob(): Job = Job(
    id = id,
    titleKey = getString("titleKey") ?: "",
    titleCustom = getString("titleCustom") ?: "",
    company = getString("company") ?: "",
    city = getString("city") ?: "",
    district = getString("district") ?: "",
    salary = getLong("salary") ?: 0L,
    contract = getString("contract") ?: "fullTime",
    conditions = strList("conditions"),
    description = getString("description") ?: "",
    requirements = strList("requirements"),
    contactPhone = getString("contactPhone") ?: "",
    active = getBoolean("active") ?: true,
    publishedAt = getLong("publishedAt") ?: 0L,
    createdBy = getString("createdBy") ?: "",
    media = (get("media") as? List<*>)?.mapNotNull { raw ->
        val m = raw as? Map<*, *> ?: return@mapNotNull null
        JobMedia(
            type = (m["type"] as? String) ?: MediaType.IMAGE,
            url = (m["url"] as? String) ?: "",
            poster = (m["poster"] as? String) ?: ""
        )
    }?.filter { it.url.isNotBlank() } ?: emptyList()
)

fun Hotel.toMap(): Map<String, Any> = mapOf(
    "countryCode" to countryCode,
    "cityId" to cityId,
    "name" to name,
    "address" to address,
    "lat" to lat,
    "lon" to lon,
    "ownerUid" to ownerUid,
    "active" to active,
    "updatedAt" to updatedAt
)

fun DocumentSnapshot.toHotel(): Hotel = Hotel(
    id = id,
    countryCode = getString("countryCode") ?: "TR",
    cityId = getString("cityId") ?: "",
    name = getString("name") ?: "",
    address = getString("address") ?: "",
    lat = getDouble("lat") ?: 0.0,
    lon = getDouble("lon") ?: 0.0,
    ownerUid = getString("ownerUid") ?: "",
    active = getBoolean("active") ?: true,
    updatedAt = getLong("updatedAt") ?: 0L
)

fun Job.toMap(): Map<String, Any> = mapOf(
    "titleKey" to titleKey,
    "titleCustom" to titleCustom,
    "company" to company,
    "city" to city,
    "district" to district,
    "salary" to salary,
    "contract" to contract,
    "conditions" to conditions,
    "description" to description,
    "requirements" to requirements,
    "contactPhone" to contactPhone,
    "active" to active,
    "publishedAt" to publishedAt,
    "createdBy" to createdBy,
    "media" to media.map { mapOf("type" to it.type, "url" to it.url, "poster" to it.poster) }
)

fun DocumentSnapshot.toApplicant(): Applicant = Applicant(
    id = id,
    uid = getString("uid") ?: id,
    firstName = getString("firstName") ?: "",
    lastName = getString("lastName") ?: "",
    fatherName = getString("fatherName") ?: "",
    username = getString("username") ?: "",
    createdBy = getString("createdBy") ?: "",
    nationalId = getString("nationalId") ?: "",
    otherJobs = getString("otherJobs") ?: "",
    phone = getString("phone") ?: "",
    email = getString("email") ?: "",
    city = getString("city") ?: "",
    district = getString("district") ?: "",
    address = getString("address") ?: "",
    destinationCountry = getString("destinationCountry") ?: "TR",
    jobKeys = strList("jobKeys"),
    spokenLangs = strList("spokenLangs"),
    trLevel = (getLong("trLevel") ?: 0L).toInt(),
    gender = getString("gender") ?: "",
    idPhotoUrl = getString("idPhotoUrl") ?: "",
    bodyPhotoUrl = getString("bodyPhotoUrl") ?: "",
    status = getString("status") ?: "new",
    consent = getBoolean("consent") ?: false,
    createdAt = getLong("createdAt") ?: 0L,
    lang = getString("lang") ?: "fa",
    phoneCountry = getString("phoneCountry") ?: "TR",
    phoneLocal = getString("phoneLocal") ?: "",
    payStatus = getString("paymentStatus") ?: "",   // #34
    payAt = getLong("paymentAt") ?: 0L,             // #34
    payMethod = getString("paymentMethod") ?: "",  // v38
    payReference = getString("paymentReference") ?: "",
    payReceiptUrl = getString("paymentReceiptUrl") ?: "",
    undertaking = (get("undertaking") as? Map<*, *>)?.let { m ->
        Undertaking(
            signed = m["signed"] as? Boolean ?: false,
            date = m["date"] as? String ?: "",
            signedAt = (m["signedAt"] as? Number)?.toLong() ?: 0L,
            nationalId = m["nationalId"] as? String ?: "",
            payerCard = m["payerCard"] as? String ?: "",
            fee = (m["fee"] as? Number)?.toLong() ?: 6000L,
            brokerCard = m["brokerCard"] as? String ?: "",
            signature = m["signature"] as? String ?: ""
        )
    }
)

/** فقط تعهدنامه — برای به‌روزرسانی مبلغ یک متقاضی از پنل مدیریت */
fun Undertaking.toMap(): Map<String, Any> = mapOf(
    "signed" to signed,
    "date" to date,
    "signedAt" to signedAt,
    "nationalId" to nationalId,
    "payerCard" to payerCard,
    "fee" to fee,
    "brokerCard" to brokerCard,
    "signature" to signature
)

fun Applicant.toMap(): Map<String, Any> = mapOf(
    "uid" to uid,
    "firstName" to firstName,
    "lastName" to lastName,
    "fatherName" to fatherName,
    "username" to username,
    "createdBy" to createdBy,
    "nationalId" to nationalId,
    "otherJobs" to otherJobs,
    "phone" to phone,
    "email" to email,
    "city" to city,
    "district" to district,
    "address" to address,
    "destinationCountry" to destinationCountry,
    "jobKeys" to jobKeys,
    "spokenLangs" to spokenLangs,
    "trLevel" to trLevel,
    "gender" to gender,
    "idPhotoUrl" to idPhotoUrl,
    "bodyPhotoUrl" to bodyPhotoUrl,
    "status" to status,
    "consent" to consent,
    "createdAt" to createdAt,
    "lang" to lang,
    "phoneCountry" to phoneCountry,
    "phoneLocal" to phoneLocal,
    "paymentStatus" to payStatus,      // #34
    "paymentAt" to payAt,              // #34
    "paymentMethod" to payMethod,      // v38
    "paymentReference" to payReference,
    "paymentReceiptUrl" to payReceiptUrl,
    "undertaking" to (undertaking?.toMap() ?: emptyMap<String, Any>())
)
