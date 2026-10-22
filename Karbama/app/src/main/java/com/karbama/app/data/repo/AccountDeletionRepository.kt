package com.karbama.app.data.repo

import com.karbama.app.data.Service
import kotlinx.coroutines.tasks.await

/**
 * Account/data-deletion intake for the live Firebase app.
 *
 * This deliberately records a deletion *request*, rather than making a client
 * claim that it can instantly erase every possible legal/payment record or the
 * Firebase Auth identity. A main administrator must review and fulfil it under
 * the published retention policy. The request contains no copied profile or
 * document data: its document id is the already-authenticated applicant UID.
 */
class AccountDeletionRepository {
    sealed interface RequestResult {
        data object Submitted : RequestResult
        data object AlreadySubmitted : RequestResult
        data class Failed(val error: Throwable) : RequestResult
    }

    suspend fun requestCurrentApplicantDeletion(): RequestResult {
        val db = Service.db ?: return RequestResult.Failed(IllegalStateException("Firebase is unavailable"))
        val uid = Service.currentUid ?: return RequestResult.Failed(IllegalStateException("Sign-in is required"))
        val request = db.collection(COLLECTION).document(uid)
        return try {
            // Android's Firestore SDK has set(), not DocumentReference.create().
            // Rules still permit only a create, so an existing request is never
            // overwritten; checking first gives the user an accurate pending state.
            if (request.get().await().exists()) {
                RequestResult.AlreadySubmitted
            } else {
                request.set(
                    mapOf(
                        "uid" to uid,
                        "status" to STATUS_REQUESTED,
                        "requestedAt" to System.currentTimeMillis()
                    )
                ).await()
                RequestResult.Submitted
            }
        } catch (error: Throwable) {
            RequestResult.Failed(error)
        }
    }

    companion object {
        const val COLLECTION = "accountDeletionRequests"
        const val STATUS_REQUESTED = "requested"
    }
}
