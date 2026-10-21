package com.karbama.app.data.repo

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.karbama.app.data.Service
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication boundary for the username/password experience.
 *
 * The deterministic address is an implementation detail required by Firebase
 * Email/Password Auth. It is never displayed, collected, or stored as profile
 * data. Passwords are passed only to Firebase Auth/callable Functions and are
 * never serialized to Firestore or local preferences.
 */
class IdentityRepository {
    enum class Role { APPLICANT, COLLABORATOR }

    data class SignedIn(val user: FirebaseUser, val role: Role)
    data class ProvisionedIdentity(val uid: String, val username: String)

    companion object {
        private val USERNAME = Regex("^[a-z][a-z0-9._-]{2,31}$")

        fun username(value: String): String = value.trim().lowercase().also {
            require(USERNAME.matches(it)) { "invalid_username" }
        }

        private fun authEmail(username: String): String =
            "${username(username)}@users.karbama.invalid"

        fun password(value: String) {
            require(value.length >= 8) { "weak_password" }
        }
    }

    private fun auth() = Service.auth ?: error("Auth unavailable")

    /** Creates the jobseeker identity without writing password material anywhere. */
    suspend fun createApplicant(username: String, password: String): FirebaseUser {
        password(password)
        val credential = EmailAuthProvider.getCredential(authEmail(username), password)
        return try {
            val current = auth().currentUser
            val result = if (current?.isAnonymous == true) {
                current.linkWithCredential(credential).await()
            } else {
                auth().createUserWithEmailAndPassword(authEmail(username), password).await()
            }
            result.user ?: error("No Firebase user returned")
        } catch (error: FirebaseAuthUserCollisionException) {
            throw IllegalArgumentException("username_taken", error)
        }
    }

    suspend fun signIn(username: String, password: String): SignedIn {
        password(password)
        val user = auth().signInWithEmailAndPassword(authEmail(username), password).await().user
            ?: error("No Firebase user returned")
        val role = if (user.getIdToken(true).await().claims["karbamaRole"] == "collaborator") {
            Role.COLLABORATOR
        } else {
            Role.APPLICANT
        }
        return SignedIn(user, role)
    }

    /** Main-manager-only callable; server verifies the manager before creating the identity. */
    suspend fun createCollaborator(username: String, password: String): ProvisionedIdentity =
        provision("provisionCollaboratorIdentity", username, password)

    /** Collaborator-only callable; server stamps the collaborator as the immutable creator. */
    suspend fun createApplicantForCollaborator(username: String, password: String): ProvisionedIdentity =
        provision("provisionApplicantIdentity", username, password)

    private suspend fun provision(
        functionName: String,
        username: String,
        password: String
    ): ProvisionedIdentity {
        password(password)
        val normalizedUsername = username(username)
        @Suppress("UNCHECKED_CAST")
        val data = (Service.functions ?: error("Functions unavailable"))
            .getHttpsCallable(functionName)
            .call(mapOf("username" to normalizedUsername, "password" to password))
            .await()
            .data as? Map<String, Any?>
            ?: error("Bad provisioning response")
        return ProvisionedIdentity(
            uid = data["uid"] as? String ?: error("Missing provisioned UID"),
            username = data["username"] as? String ?: normalizedUsername
        )
    }

    fun signOut() {
        runCatching { Service.auth?.signOut() }
    }
}
