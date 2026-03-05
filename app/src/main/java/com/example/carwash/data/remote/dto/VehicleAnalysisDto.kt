package com.example.carwash.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleAnalysisResponse(
    val plate: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val color: String? = null,
    val partial: Boolean = false,
    @SerialName("processing_time_ms") val processingTimeMs: Long = 0
)
