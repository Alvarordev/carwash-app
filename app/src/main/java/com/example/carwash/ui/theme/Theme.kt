package com.example.carwash.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
        darkColorScheme(
                primary = OrangePrimary,
                onPrimary = Color.White,
                primaryContainer = OrangeDark,
                onPrimaryContainer = Color.White,
                secondary = OrangeLight,
                onSecondary = Color.White,
                tertiary = StatusInProgress,
                onTertiary = OnBackgroundLight,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceCardDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = BorderDark,
                error = StatusCancelled,
                onError = Color.White,
        )

private val LightColorScheme =
        lightColorScheme(
                primary = OrangePrimary,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFE4DA),
                onPrimaryContainer = OrangeDark,
                secondary = OrangeLight,
                onSecondary = Color.White,
                tertiary = StatusInProgress,
                onTertiary = OnBackgroundLight,
                background = BackgroundLight,
                onBackground = OnBackgroundLight,
                surface = SurfaceLight,
                onSurface = OnSurfaceLight,
                surfaceVariant = SurfaceMutedLight,
                onSurfaceVariant = OnSurfaceVariantLight,
                outline = BorderLight,
                error = StatusCancelled,
                onError = Color.White,
        )

@Composable
fun CarwashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
