package com.karbama.app.data

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

/**
 * دسترسی سراسری به سرویس‌های Firebase.
 *
 * نکته مهم: اگر فایل google-services.json را اضافه نکرده باشی، این مقادیر null می‌شوند
 * و برنامه به‌صورت خودکار در «حالت نمایشی» با داده‌های نمونه کار می‌کند
 * (بدون خطا و بدون کرش).
 */
object Service {

    lateinit var app: Application
        private set

    private var resolved = false
    private var _auth: FirebaseAuth? = null
    private var _db: FirebaseFirestore? = null
    private var _functions: FirebaseFunctions? = null
    private var _storage: FirebaseStorage? = null

    fun init(application: Application) {
        app = application
    }

    /** یک‌بار تلاش می‌کند Firebase را بسازد؛ نتیجه ذخیره می‌شود. */
    private fun resolve() {
        if (resolved) return
        resolved = true
        _auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
        _db = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
        _functions = runCatching { FirebaseFunctions.getInstance("europe-west1") }.getOrNull()
        _storage = runCatching { FirebaseStorage.getInstance() }.getOrNull()
    }

    val auth: FirebaseAuth?
        get() { resolve(); return _auth }

    val db: FirebaseFirestore?
        get() { resolve(); return _db }

    val functions: FirebaseFunctions?
        get() { resolve(); return _functions }

    val storage: FirebaseStorage?
        get() { resolve(); return _storage }

    /** آیا Firebase درست تنظیم شده است؟ */
    val ready: Boolean
        get() { resolve(); return _auth != null && _db != null }

    val currentUid: String?
        get() = runCatching { auth?.currentUser?.uid }.getOrNull()
}
