package com.example.carwash.data.remote.datasource

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.carwash.data.remote.dto.VehicleAnalysisResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import javax.inject.Inject
import kotlinx.serialization.json.Json
import io.ktor.client.engine.okhttp.OkHttp
import kotlin.getValue

private val json = Json { ignoreUnknownKeys = true }

class VehicleAnalysisDatasource @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val contentResolver: ContentResolver
) {
    private val httpClient by lazy { HttpClient(OkHttp) }

    suspend fun analyze(photos: List<Uri>): Result<VehicleAnalysisResponse> = runCatching {
        val accessToken = supabaseClient.auth.currentSessionOrNull()?.accessToken
            ?: error("No active session")

        val response = httpClient.post("http://178.156.230.233/analyze-vehicle") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(MultiPartFormDataContent(formData {
                photos.firstOrNull()?.let { uri ->
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: return@let
                    append(
                        key = "image",
                        value = bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "form-data; name=\"image\"; filename=\"photo_0.jpg\"")
                        }
                    )
                }
            }))
        }

        val body = response.bodyAsText()
        Log.d(TAG, "Vehicle analysis response (${response.status}): $body")
        json.decodeFromString<VehicleAnalysisResponse>(body)
    }.onFailure { Log.e(TAG, "Vehicle analysis failed", it) }

    companion object {
        private const val TAG = "VehicleAnalysis"
    }
}
