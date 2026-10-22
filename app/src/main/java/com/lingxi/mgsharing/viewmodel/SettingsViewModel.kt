package com.lingxi.mgsharing.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lingxi.mgsharing.MGsharingApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 设置页 ViewModel —— 密码 / 端口 / 深色模式 / 动态取色
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as MGsharingApp).settingsRepository

    val password: StateFlow<String> =
        repo.password.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val port: StateFlow<Int> =
        repo.port.stateIn(viewModelScope, SharingStarted.Eagerly, 8080)

    val darkMode: StateFlow<Int> =
        repo.darkMode.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val dynamicColor: StateFlow<Boolean> =
        repo.dynamicColor.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setPassword(value: String) = viewModelScope.launch { repo.setPassword(value) }

    fun setPort(value: Int) = viewModelScope.launch { repo.setPort(value) }

    fun setDarkMode(mode: Int) = viewModelScope.launch { repo.setDarkMode(mode) }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { repo.setDynamicColor(enabled) }
}