package com.karbama.app.data.repo

import com.google.firebase.firestore.Query
import com.karbama.app.data.Config
import com.karbama.app.data.Service
import com.karbama.app.data.model.Hotel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

/**
 * هتل‌های بخش مسیریابی.
 * خواندن برای کاربر واردشده است؛ نوشتن در Firestore با قانون isAdmin محدود می‌شود.
 * ذخیرهٔ محلی فقط برای حالت نمایشی است و با دادهٔ واقعی سرور قاطی نمی‌شود.
 */
class HotelRepository {
    private companion object {
        val demoHotels = MutableStateFlow<List<Hotel>>(emptyList())
    }

    fun observeHotels(): Flow<List<Hotel>> {
        val db = Service.db ?: return demoHotels
        return kotlinx.coroutines.flow.callbackFlow {
            val registration = db.collection(Config.COL_HOTELS)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(500)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.map { it.toHotel() } ?: emptyList())
                }
            awaitClose { registration.remove() }
        }
    }

    suspend fun save(hotel: Hotel): Result<Unit> {
        val clean = hotel.copy(
            countryCode = hotel.countryCode.trim().uppercase().take(2),
            cityId = hotel.cityId.trim().lowercase(),
            name = hotel.name.trim(),
            address = hotel.address.trim(),
            ownerUid = hotel.ownerUid.ifBlank { Service.currentUid.orEmpty() },
            active = hotel.active,
            updatedAt = System.currentTimeMillis()
        )
        if (clean.name.isBlank() || clean.cityId.isBlank() ||
            clean.lat !in -90.0..90.0 || clean.lon !in -180.0..180.0
        ) return Result.failure(IllegalArgumentException("invalid hotel"))

        val db = Service.db
        if (db == null) {
            val id = clean.id.ifBlank { "local_hotel_" + System.currentTimeMillis() }
            val list = demoHotels.value.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index >= 0) list[index] = clean.copy(id = id) else list.add(clean.copy(id = id))
            demoHotels.value = list
            return Result.success(Unit)
        }
        return runCatching {
            val col = db.collection(Config.COL_HOTELS)
            if (clean.id.isBlank()) col.add(clean.toMap()).await()
            else col.document(clean.id).set(clean.toMap()).await()
            Unit
        }
    }

    suspend fun delete(hotelId: String): Result<Unit> {
        val db = Service.db
        if (db == null) {
            demoHotels.value = demoHotels.value.filterNot { it.id == hotelId }
            return Result.success(Unit)
        }
        return runCatching { db.collection(Config.COL_HOTELS).document(hotelId).delete().await() }
    }
}
