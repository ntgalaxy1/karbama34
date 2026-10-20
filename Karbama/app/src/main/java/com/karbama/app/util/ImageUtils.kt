package com.karbama.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.karbama.app.data.Config
import java.io.ByteArrayOutputStream

/**
 * آماده‌سازی عکس‌ها: کوچک‌سازی + اصلاح چرخش (EXIF) + فشرده‌سازی
 * تا آپلود روی اینترنت همراه سریع و کم‌حجم باشد.
 */
object ImageUtils {

    fun prepare(
        context: Context,
        uri: Uri,
        maxDim: Int = Config.MAX_PHOTO_DIM,
        quality: Int = Config.PHOTO_QUALITY
    ): ByteArray? = runCatching {
        val resolver = context.contentResolver

        // ۱) اندازه‌ی اصلی
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (bounds.outWidth / sample > maxDim * 2 || bounds.outHeight / sample > maxDim * 2) {
            sample *= 2
        }

        // ۲) خواندن تصویر
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null

        // ۳) اصلاح چرخش
        val rotation = resolver.openInputStream(uri)?.use { stream ->
            when (ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        val rotated = if (rotation != 0f) {
            Bitmap.createBitmap(
                decoded, 0, 0, decoded.width, decoded.height,
                Matrix().apply { postRotate(rotation) }, true
            )
        } else decoded

        // ۴) تغییر اندازه‌ی نهایی
        val scale = maxDim.toFloat() / maxOf(rotated.width, rotated.height)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                rotated,
                (rotated.width * scale).toInt().coerceAtLeast(1),
                (rotated.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else rotated

        // ۵) فشرده‌سازی
        ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }
    }.getOrNull()

    /**
     * v48: JPEG را آن‌قدر فشرده/کوچک می‌کند که زیر [maxBytes] شود
     * (برای ذخیره در یک سند Firestore). اگر نشد → null.
     */
    fun fitUnder(bytes: ByteArray, maxBytes: Int): ByteArray? = runCatching {
        if (bytes.size <= maxBytes) return@runCatching bytes
        var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@runCatching null
        repeat(8) {
            for (q in intArrayOf(80, 70, 60, 50, 40)) {
                val out = ByteArrayOutputStream().use { o ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, q, o); o.toByteArray()
                }
                if (out.size <= maxBytes) return@runCatching out
            }
            val w = (bmp.width * 0.8f).toInt().coerceAtLeast(1)
            val h = (bmp.height * 0.8f).toInt().coerceAtLeast(1)
            bmp = Bitmap.createScaledBitmap(bmp, w, h, true)
        }
        null
    }.getOrNull()
}
