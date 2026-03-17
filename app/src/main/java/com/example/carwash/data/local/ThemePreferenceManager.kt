package com.example.carwash.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.carwash.domain.model.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class ThemePreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey("theme_preference")

    val themeFlow: Flow<ThemePreference> = context.dataStore.data.map { prefs ->
        when (prefs[themeKey]) {
            "dark" -> ThemePreference.Dark
            "light" -> ThemePreference.Light
            "system" -> ThemePreference.System
            else -> ThemePreference.System
        }
    }

    suspend fun setTheme(preference: ThemePreference) {
        context.dataStore.edit { prefs ->
            prefs[themeKey] = when (preference) {
                ThemePreference.Dark -> "dark"
                ThemePreference.Light -> "light"
                ThemePreference.System -> "system"
            }
        }
    }
}
