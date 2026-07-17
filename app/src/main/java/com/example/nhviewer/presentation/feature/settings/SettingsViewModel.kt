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

    val themeMode: StateFlow<String> = settingsManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    val gridBaseWidth: StateFlow<Int> = settingsManager.gridBaseWidth
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 160
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

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setThemeMode(mode)
        }
    }

    fun setGridBaseWidth(width: Int) {
        viewModelScope.launch {
            settingsManager.setGridBaseWidth(width)
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
}
