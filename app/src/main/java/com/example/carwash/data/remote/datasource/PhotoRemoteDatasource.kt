package com.example.carwash.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import javax.inject.Inject

class PhotoRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    companion object {
        private const val BUCKET = "order-photos"
    }
    suspend fun uploadPhoto(
        orderNumber: String,
        imageBytes: ByteArray,
        fileName: String = "${System.currentTimeMillis()}.jpg"
    ): String {
        val path = "$orderNumber/$fileName"

        client.storage[BUCKET].upload(path, imageBytes) {
            upsert = false
            contentType = ContentType.Image.JPEG
        }

        return client.storage[BUCKET].publicUrl(path)
    }

    suspend fun uploadPhotos(
        orderNumber: String,
        images: List<ByteArray>
    ): List<String> = images.mapIndexed { index, bytes ->
        uploadPhoto(
            orderNumber = orderNumber,
            imageBytes = bytes,
            fileName = "${System.currentTimeMillis()}_$index.jpg"
        )
    }

    suspend fun deletePhoto(url: String) {
        // Extraer el path de la URL pública
        val path = url.substringAfter("$BUCKET/")
        client.storage[BUCKET].delete(listOf(path))
    }
}