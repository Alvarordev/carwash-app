package com.example.carwash.domain.model

enum class ThemePreference {
    Dark, Light, System;

    val displayName: String
        get() = when (this) {
            Dark -> "Oscuro"
            Light -> "Claro"
            System -> "Sistema"
        }
}
