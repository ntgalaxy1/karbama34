package com.karbama.app.data.local

import android.content.Context
import java.io.File

/**
 * در «حالت نمایشی» عکس‌ها به‌جای Firebase Storage داخل حافظه‌ی خود برنامه ذخیره می‌شوند
 * تا پنل مدیریت بتواند آن‌ها را نشان دهد.
 */
object LocalPhotoCache {

    private var dir: File? = null

    fun init(context: Context) {
        dir = File(context.filesDir, "karbama_photos").apply { if (!exists()) mkdirs() }
    }

    /** عکس را ذخیره می‌کند و مسیر فایل (file path) را برمی‌گرداند */
    fun save(id: String, kind: String, bytes: ByteArray): String {
        val d = dir ?: return ""
        val f = File(d, "${id}_$kind.jpg")
        return runCatching {
            f.writeBytes(bytes)
            f.absolutePath
        }.getOrDefault("")
    }

    /** ذخیره‌ی یک فایل رسانه‌ی آگهی (عکس یا ویدیو) در حالت نمایشی */
    fun saveFile(name: String, bytes: ByteArray): String {
        val d = dir ?: return ""
        return runCatching {
            val f = File(d, name)
            f.writeBytes(bytes)
            f.absolutePath
        }.getOrDefault("")
    }

    fun delete(id: String) {
        val d = dir ?: return
        listOf("id", "body").forEach { k ->
            runCatching { File(d, "${id}_$k.jpg").delete() }
        }
    }

    /** مسیر فایل عکس (اگر وجود داشته باشد) */
    fun path(id: String, kind: String): String? {
        val d = dir ?: return null
        val f = File(d, "${id}_$kind.jpg")
        return if (f.exists()) f.absolutePath else null
    }
}
