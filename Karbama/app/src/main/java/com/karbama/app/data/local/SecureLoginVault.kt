package com.karbama.app.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Remembered login for this device only.
 *
 * The password is never put in ordinary SharedPreferences or Firestore. The combined
 * username/password payload is encrypted using an AES-GCM key held by Android Keystore;
 * only the ciphertext and IV live in private app preferences. Saving happens only after a
 * successful sign-in, so a mistyped password is never remembered.
 */
data class SavedLogin(val username: String, val password: String)

class SecureLoginVault(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) return
        runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key())
            val plain = JSONObject().apply {
                put("username", username.trim())
                put("password", password)
            }.toString().toByteArray(StandardCharsets.UTF_8)
            val encrypted = cipher.doFinal(plain)
            prefs.edit()
                .putString(K_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .putString(K_DATA, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .apply()
        }
    }

    fun read(): SavedLogin? {
        val iv = prefs.getString(K_IV, null) ?: return null
        val encrypted = prefs.getString(K_DATA, null) ?: return null
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                key(),
                GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
            )
            val text = String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)), StandardCharsets.UTF_8)
            val data = JSONObject(text)
            SavedLogin(data.optString("username"), data.optString("password"))
                .takeIf { it.username.isNotBlank() && it.password.isNotBlank() }
        }.getOrElse {
            // Key invalidation, app-data restoration to a different device, or corrupt data
            // must fail closed rather than leaving undecryptable data behind.
            clear()
            null
        }
    }

    fun clear() {
        prefs.edit().remove(K_IV).remove(K_DATA).apply()
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }.generateKey()
    }

    private companion object {
        const val PREFS = "karbama_login_vault"
        const val K_IV = "iv"
        const val K_DATA = "data"
        const val KEY_ALIAS = "karbama.saved_login.aes_gcm.v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
