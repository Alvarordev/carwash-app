package com.example.carwash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

@Composable
fun CarwashTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}
