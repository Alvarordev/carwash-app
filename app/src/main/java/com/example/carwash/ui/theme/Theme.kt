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
                onTertiary = Color.White,
                background = BackgroundDark,
                onBackground = OnBackgroundDark,
                surface = SurfaceDark,
                onSurface = OnSurfaceDark,
                surfaceVariant = SurfaceCardDark,
                onSurfaceVariant = OnSurfaceVariantDark,
                outline = Color(0xFF424242),
                error = StatusCancelled,
                onError = Color.White,
        )

private val LightColorScheme =
        lightColorScheme(
                primary = OrangePrimary,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFDBD0),
                onPrimaryContainer = OrangeDark,
                secondary = OrangeLight,
                onSecondary = Color.White,
                tertiary = StatusInProgress,
                onTertiary = Color.White,
                background = Color(0xFFFFFBFF),
                onBackground = Color(0xFF1C1B1F),
                surface = Color(0xFFFFFBFF),
                onSurface = Color(0xFF1C1B1F),
                surfaceVariant = Color(0xFFF3EFED),
                onSurfaceVariant = Color(0xFF49454F),
                outline = Color(0xFFCAC4D0),
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
