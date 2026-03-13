package com.example.carwash.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object ImageCompressor {

    fun compress(
        contentResolver: ContentResolver,
        uri: Uri,
        maxDimension: Int = 1920,
        quality: Int = 80
    ): ByteArray? {
        // Read EXIF orientation before decoding
        val rotation = contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        // First pass: read dimensions only
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        val width = options.outWidth
        val height = options.outHeight
        if (width <= 0 || height <= 0) return null

        // Calculate inSampleSize (power of 2) so decoded bitmap fits within maxDimension
        var inSampleSize = 1
        if (width > maxDimension || height > maxDimension) {
            val halfW = width / 2
            val halfH = height / 2
            while (halfW / inSampleSize >= maxDimension && halfH / inSampleSize >= maxDimension) {
                inSampleSize *= 2
            }
        }

        // Second pass: decode sampled bitmap
        val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        val sampled = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        } ?: return null

        // Scale down further if still larger than maxDimension
        val scaled = if (sampled.width > maxDimension || sampled.height > maxDimension) {
            val scale = maxDimension.toFloat() / max(sampled.width, sampled.height)
            val newW = (sampled.width * scale).roundToInt()
            val newH = (sampled.height * scale).roundToInt()
            Bitmap.createScaledBitmap(sampled, newW, newH, true).also {
                if (it !== sampled) sampled.recycle()
            }
        } else {
            sampled
        }

        // Apply EXIF rotation
        val bitmap = if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, matrix, true).also {
                if (it !== scaled) scaled.recycle()
            }
        } else {
            scaled
        }

        // Compress to JPEG
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        bitmap.recycle()
        return out.toByteArray()
    }
}
