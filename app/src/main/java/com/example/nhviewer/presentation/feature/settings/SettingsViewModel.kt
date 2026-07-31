package com.example.nhviewer.presentation.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.data.local.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    val maxImageCacheMb: StateFlow<Int> = settingsManager.maxImageCacheMb
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 250
        )

    val themeMode: StateFlow<String> = settingsManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    val readerDirection: StateFlow<String> = settingsManager.readerDirection
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "rtl"
        )

    val defaultDownloadFormat: StateFlow<String> = settingsManager.defaultDownloadFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "zip"
        )

    val dynamicColor: StateFlow<Boolean> = settingsManager.dynamicColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val keepScreenOn: StateFlow<Boolean> = settingsManager.keepScreenOn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val imageScaleMode: StateFlow<String> = settingsManager.imageScaleMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "fit_screen"
        )

    val readerBackground: StateFlow<String> = settingsManager.readerBackground
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "default"
        )

    val secureMode: StateFlow<Boolean> = settingsManager.secureMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val readerBrightness: StateFlow<Float> = settingsManager.readerBrightness
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = -1f
        )

    val colorFilterMode: StateFlow<String> = settingsManager.colorFilterMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "none"
        )

    val colorFilterAlpha: StateFlow<Float> = settingsManager.colorFilterAlpha
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.3f
        )

    val pageTransitionAnim: StateFlow<Boolean> = settingsManager.pageTransitionAnim
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val showPersistentPageNumber: StateFlow<Boolean> = settingsManager.showPersistentPageNumber
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val appLanguage: StateFlow<String> = settingsManager.appLanguage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    val tagLanguage: StateFlow<String> = settingsManager.tagLanguage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "zh"
        )

    val tagDisplayMode: StateFlow<String> = settingsManager.tagDisplayMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "only_translation"
        )

    val logLevel: StateFlow<String> = settingsManager.logLevel
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "info"
        )

    fun setMaxImageCacheMb(mb: Int) {
        viewModelScope.launch {
            settingsManager.setMaxImageCacheMb(mb)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setThemeMode(mode)
        }
    }

    fun setReaderDirection(direction: String) {
        viewModelScope.launch {
            settingsManager.setReaderDirection(direction)
        }
    }

    fun setDefaultDownloadFormat(format: String) {
        viewModelScope.launch {
            settingsManager.setDefaultDownloadFormat(format)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setDynamicColor(enabled)
        }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setKeepScreenOn(enabled)
        }
    }

    fun setImageScaleMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setImageScaleMode(mode)
        }
    }

    fun setReaderBackground(background: String) {
        viewModelScope.launch {
            settingsManager.setReaderBackground(background)
        }
    }

    fun setSecureMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setSecureMode(enabled)
        }
    }

    fun setReaderBrightness(brightness: Float) {
        viewModelScope.launch {
            settingsManager.setReaderBrightness(brightness)
        }
    }

    fun setColorFilterMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setColorFilterMode(mode)
        }
    }

    fun setColorFilterAlpha(alpha: Float) {
        viewModelScope.launch {
            settingsManager.setColorFilterAlpha(alpha)
        }
    }

    fun setPageTransitionAnim(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setPageTransitionAnim(enabled)
        }
    }

    fun setShowPersistentPageNumber(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setShowPersistentPageNumber(enabled)
        }
    }

    fun setAppLanguage(language: String) {
        viewModelScope.launch {
            settingsManager.setAppLanguage(language)
        }
    }

    fun setTagLanguage(language: String) {
        viewModelScope.launch {
            settingsManager.setTagLanguage(language)
        }
    }

    fun setTagDisplayMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setTagDisplayMode(mode)
        }
    }

    fun setLogLevel(level: String) {
        viewModelScope.launch {
            settingsManager.setLogLevel(level)
        }
    }
}
