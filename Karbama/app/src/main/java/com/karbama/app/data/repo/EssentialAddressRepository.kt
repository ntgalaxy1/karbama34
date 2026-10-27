package com.karbama.app.data.repo

import com.google.firebase.firestore.Query
import com.karbama.app.data.Config
import com.karbama.app.data.Service
import com.karbama.app.data.local.LocalStore
import com.karbama.app.data.model.EssentialAddress
import com.karbama.app.data.model.EssentialAddressCategory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * نشانی‌های ضروری ترکیه.
 *
 * در Firebase، فهرست عمومی فقط خواندنی است و نوشتن آن با Firestore Rules به
 * مدیر اصلی محدود شده است. در demo همان contract با داده‌های نمونهٔ محلی اجرا
 * می‌شود تا UI هیچ مسیر جداگانه‌ای نداشته باشد.
 */
class EssentialAddressRepository {
    fun observeAddresses(): Flow<List<EssentialAddress>> {
        val db = Service.db ?: return LocalStore.essentialAddresses
        return kotlinx.coroutines.flow.callbackFlow {
            val registration = db.collection(Config.COL_ESSENTIAL_ADDRESSES)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(500)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.map { it.toEssentialAddress() } ?: emptyList())
                }
            awaitClose { registration.remove() }
        }
    }

    suspend fun save(address: EssentialAddress): Result<Unit> {
        val city = address.cityId.trim().lowercase().ifBlank { "nationwide" }
        val category = address.category.takeIf { it in EssentialAddressCategory.all }
            ?: EssentialAddressCategory.OTHER
        val latitude = address.lat
        val longitude = address.lon
        val hasCoordinates = latitude != null || longitude != null
        val coordinatesValid = !hasCoordinates || (
            latitude != null && longitude != null &&
                latitude in -90.0..90.0 && longitude in -180.0..180.0
            )
        val clean = address.copy(
            countryCode = address.countryCode.trim().uppercase().take(2).ifBlank { "TR" },
            cityId = city,
            category = category,
            title = address.title.trim(),
            titleTr = address.titleTr.trim(),
            address = address.address.trim(),
            phone = address.phone.trim(),
            hours = address.hours.trim(),
            description = address.description.trim(),
            ownerUid = address.ownerUid.ifBlank { Service.currentUid.orEmpty() },
            updatedAt = System.currentTimeMillis()
        )
        val textValid = clean.title.length in 2..160 &&
            clean.titleTr.length <= 160 &&
            clean.cityId.length in 2..80 &&
            clean.address.length <= 500 &&
            clean.phone.length <= 64 &&
            clean.hours.length <= 160 &&
            clean.description.length <= 1200
        if (!textValid || !coordinatesValid) {
            return Result.failure(IllegalArgumentException("invalid essential address"))
        }

        val db = Service.db
        if (db == null) {
            val id = clean.id.ifBlank { "demo_address_${System.currentTimeMillis()}" }
            LocalStore.upsertEssentialAddress(clean.copy(id = id))
            return Result.success(Unit)
        }
        return runCatching {
            val collection = db.collection(Config.COL_ESSENTIAL_ADDRESSES)
            if (clean.id.isBlank()) collection.add(clean.toMap()).await()
            else collection.document(clean.id).set(clean.toMap()).await()
            Unit
        }
    }

    suspend fun delete(addressId: String): Result<Unit> {
        if (addressId.isBlank()) return Result.failure(IllegalArgumentException("missing address id"))
        val db = Service.db
        if (db == null) {
            LocalStore.deleteEssentialAddress(addressId)
            return Result.success(Unit)
        }
        return runCatching { db.collection(Config.COL_ESSENTIAL_ADDRESSES).document(addressId).delete().await() }
    }
}
