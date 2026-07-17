package com.example.nhviewer.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GRID_BASE_WIDTH = intPreferencesKey("grid_base_width")
        val READER_DIRECTION = stringPreferencesKey("reader_direction")
        val DEFAULT_DOWNLOAD_FORMAT = stringPreferencesKey("default_download_format")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    val themeMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "system"
        }

    val gridBaseWidth: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.GRID_BASE_WIDTH] ?: 160
        }

    val readerDirection: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.READER_DIRECTION] ?: "rtl"
        }

    val defaultDownloadFormat: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_DOWNLOAD_FORMAT] ?: "zip"
        }

    val dynamicColor: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
        }

    val keepScreenOn: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_ON] ?: false
        }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setGridBaseWidth(width: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRID_BASE_WIDTH] = width
        }
    }

    suspend fun setReaderDirection(direction: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.READER_DIRECTION] = direction
        }
    }

    suspend fun setDefaultDownloadFormat(format: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_DOWNLOAD_FORMAT] = format
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_ON] = enabled
        }
    }
}
