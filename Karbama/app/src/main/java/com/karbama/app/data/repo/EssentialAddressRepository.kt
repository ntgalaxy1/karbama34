package com.karbama.app.data.repo

import com.google.firebase.firestore.Query
import com.karbama.app.data.Config
import com.karbama.app.data.Service
import com.karbama.app.data.local.LocalStore
import com.karbama.app.data.model.EssentialAddress
import com.karbama.app.data.model.EssentialAddressCategory
import com.karbama.app.data.model.EssentialAddressSamples
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * نشانی‌های ضروری ترکیه.
 *
 * فهرست رسمیِ اولیه داخل APK است تا صفحه در یک Firebase collection تازه خالی
 * نباشد. رکوردهای Firestore که مدیر اصلی ایجاد می‌کند، با همان ID می‌توانند
 * هر مورد بسته‌بندی‌شده را جایگزین یا غیرفعال کنند؛ رکوردهای جدید مدیر نیز به
 * فهرست افزوده می‌شوند. در demo همین داده‌ها به‌طور محلی و پایدار نگه‌داری
 * می‌شوند.
 */
class EssentialAddressRepository {
    private val bundledAddresses: List<EssentialAddress> = EssentialAddressSamples.default()

    fun observeAddresses(): Flow<List<EssentialAddress>> {
        val db = Service.db ?: return LocalStore.essentialAddresses
        return kotlinx.coroutines.flow.callbackFlow {
            val registration = db.collection(Config.COL_ESSENTIAL_ADDRESSES)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(500)
                .addSnapshotListener { snapshot, error ->
                    // A temporary offline/permission failure must not make emergency
                    // contacts disappear. It never grants write access.
                    if (error != null) {
                        trySend(bundledAddresses)
                        return@addSnapshotListener
                    }
                    val remote = snapshot?.documents?.map { it.toEssentialAddress() } ?: emptyList()
                    trySend(mergeWithBundled(remote))
                }
            awaitClose { registration.remove() }
        }
    }

    /** A Firestore row with the same stable ID replaces the bundled row. */
    private fun mergeWithBundled(remote: List<EssentialAddress>): List<EssentialAddress> {
        val remoteIds = remote.asSequence().map { it.id }.toSet()
        return remote + bundledAddresses.filterNot { it.id in remoteIds }
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
        return runCatching {
            val bundled = bundledAddresses.firstOrNull { it.id == addressId }
            if (bundled == null) {
                db.collection(Config.COL_ESSENTIAL_ADDRESSES).document(addressId).delete().await()
            } else {
                // Keep an inactive override rather than deleting a non-existent
                // bundled document, otherwise the default would immediately return.
                save(bundled.copy(active = false)).getOrThrow()
            }
            Unit
        }
    }
}
