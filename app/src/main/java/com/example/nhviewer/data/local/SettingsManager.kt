package com.example.nhviewer.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
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
        val IMAGE_SCALE_MODE = stringPreferencesKey("image_scale_mode")
        val READER_BACKGROUND = stringPreferencesKey("reader_background")
        val SECURE_MODE = booleanPreferencesKey("secure_mode")
        val READER_BRIGHTNESS = floatPreferencesKey("reader_brightness")
        val COLOR_FILTER_MODE = stringPreferencesKey("color_filter_mode")
        val COLOR_FILTER_ALPHA = floatPreferencesKey("color_filter_alpha")
        val PAGE_TRANSITION_ANIM = booleanPreferencesKey("page_transition_anim")
        val SHOW_PERSISTENT_PAGE_NUMBER = booleanPreferencesKey("show_persistent_page_number")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val TAG_LANGUAGE = stringPreferencesKey("tag_language")
        val TAG_DISPLAY_MODE = stringPreferencesKey("tag_display_mode")
        val MAX_IMAGE_CACHE_MB = intPreferencesKey("max_image_cache_mb")
    }

    val maxImageCacheMb: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.MAX_IMAGE_CACHE_MB] ?: 250
        }

    val appLanguage: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE] ?: "system"
        }

    val tagLanguage: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.TAG_LANGUAGE] ?: "zh"
        }

    val tagDisplayMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.TAG_DISPLAY_MODE] ?: "only_translation"
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

    val imageScaleMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IMAGE_SCALE_MODE] ?: "fit_screen"
        }

    val readerBackground: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.READER_BACKGROUND] ?: "default"
        }

    val secureMode: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SECURE_MODE] ?: false
        }

    val readerBrightness: Flow<Float> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.READER_BRIGHTNESS] ?: -1f
        }

    val colorFilterMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.COLOR_FILTER_MODE] ?: "none"
        }

    val colorFilterAlpha: Flow<Float> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.COLOR_FILTER_ALPHA] ?: 0.3f
        }

    val pageTransitionAnim: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.PAGE_TRANSITION_ANIM] ?: true
        }

    val showPersistentPageNumber: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_PERSISTENT_PAGE_NUMBER] ?: true
        }

    suspend fun setMaxImageCacheMb(mb: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_IMAGE_CACHE_MB] = mb
        }
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

    suspend fun setImageScaleMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IMAGE_SCALE_MODE] = mode
        }
    }

    suspend fun setReaderBackground(background: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.READER_BACKGROUND] = background
        }
    }

    suspend fun setSecureMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SECURE_MODE] = enabled
        }
    }

    suspend fun setReaderBrightness(brightness: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.READER_BRIGHTNESS] = brightness
        }
    }

    suspend fun setColorFilterMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_FILTER_MODE] = mode
        }
    }

    suspend fun setColorFilterAlpha(alpha: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_FILTER_ALPHA] = alpha
        }
    }

    suspend fun setPageTransitionAnim(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PAGE_TRANSITION_ANIM] = enabled
        }
    }

    suspend fun setShowPersistentPageNumber(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_PERSISTENT_PAGE_NUMBER] = enabled
        }
    }

    suspend fun setAppLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE] = language
        }
    }

    suspend fun setTagLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TAG_LANGUAGE] = language
        }
    }

    suspend fun setTagDisplayMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TAG_DISPLAY_MODE] = mode
        }
    }
}
