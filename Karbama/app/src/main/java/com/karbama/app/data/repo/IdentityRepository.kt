package com.karbama.app.data.repo

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.karbama.app.data.Service
import kotlinx.coroutines.tasks.await

/** Firebase Authentication is the sole password authority. */
class IdentityRepository {
    enum class Role { APPLICANT, COLLABORATOR }
    data class SignedIn(val user: FirebaseUser, val role: Role)
    data class ProvisionedIdentity(val uid: String, val username: String)

    companion object {
        private val USERNAME = Regex("^[a-z][a-z0-9._-]{2,31}$")
        private const val INTERNAL_DOMAIN = "users.karbama.invalid"
        fun normalizedUsername(value: String): String = value.trim().lowercase()
        fun requireUsername(value: String): String {
            val username = normalizedUsername(value)
            require(USERNAME.matches(username)) { "Username must be 3–32 lowercase English letters, numbers, dot, underscore, or hyphen." }
            return username
        }
        fun internalEmail(username: String): String = "${requireUsername(username)}@$INTERNAL_DOMAIN"
        fun requirePassword(password: String) { require(password.length >= 8) { "Password must have at least 8 characters." } }
    }

    private fun auth(): FirebaseAuth = Service.auth ?: throw IllegalStateException("Firebase Authentication is not configured.")

    /** Creates a self-service applicant credential without storing the password anywhere else. */
    suspend fun createApplicant(username: String, password: String): FirebaseUser {
        val auth = auth()
        val credential = EmailAuthProvider.getCredential(internalEmail(username), password.also(::requirePassword))
        val current = auth.currentUser
        val user = try {
            if (current?.isAnonymous == true) current.linkWithCredential(credential).await().user
            else auth.createUserWithEmailAndPassword(internalEmail(username), password).await().user
        } catch (collision: FirebaseAuthUserCollisionException) {
            throw IllegalArgumentException("username_taken", collision)
        } ?: throw IllegalStateException("Firebase did not return a new applicant account.")
        user.getIdToken(true).await()
        return user
    }

    /** Signs in username-only UI users and refreshes custom claims before role selection. */
    suspend fun signIn(username: String, password: String): SignedIn {
        val user = auth().signInWithEmailAndPassword(internalEmail(username), password).await().user
            ?: throw IllegalStateException("Firebase did not return a signed-in account.")
        val token = user.getIdToken(true).await()
        val role = if (token.claims["karbamaRole"] == "collaborator") Role.COLLABORATOR else Role.APPLICANT
        return SignedIn(user, role)
    }

    suspend fun createCollaborator(username: String, password: String): ProvisionedIdentity = callProvision("provisionCollaboratorIdentity", username, password)
    suspend fun createApplicantForCollaborator(username: String, password: String): ProvisionedIdentity = callProvision("provisionApplicantIdentity", username, password)

    private suspend fun callProvision(functionName: String, username: String, password: String): ProvisionedIdentity {
        val functions = Service.functions ?: throw IllegalStateException("Firebase Functions is not configured.")
        val safeUsername = requireUsername(username)
        requirePassword(password)
        val result = functions.getHttpsCallable(functionName).call(mapOf("username" to safeUsername, "password" to password)).await()
        @Suppress("UNCHECKED_CAST")
        val data = result.data as? Map<String, Any?> ?: throw IllegalStateException("Malformed provisioning response.")
        val uid = data["uid"] as? String ?: throw IllegalStateException("Provisioning response had no UID.")
        return ProvisionedIdentity(uid, safeUsername)
    }

    fun signOut() { runCatching { Service.auth?.signOut() } }
}
