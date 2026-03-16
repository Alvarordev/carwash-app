package com.example.carwash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.carwash.data.local.ThemePreferenceManager
import com.example.carwash.domain.model.ThemePreference
import com.example.carwash.presentation.navigation.RootNavigation
import com.example.carwash.ui.theme.CarwashTheme
import com.example.carwash.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var themePreferenceManager: ThemePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreference by themePreferenceManager.themeFlow
                .collectAsState(initial = ThemePreference.Dark)
            val darkTheme = when (themePreference) {
                ThemePreference.Dark -> true
                ThemePreference.Light -> false
                ThemePreference.System -> isSystemInDarkTheme()
            }
            CarwashTheme(darkTheme = darkTheme) {
                RootNavigation(networkMonitor = networkMonitor)
            }
        }
    }
}
