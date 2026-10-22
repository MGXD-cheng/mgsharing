package com.lingxi.mgsharing.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mg_settings")

/**
 * 应用设置仓库 —— DataStore 持久化
 */
class SettingsRepository(private val context: Context) {

    companion object {
        private val KEY_PASSWORD = stringPreferencesKey("password")
        private val KEY_PORT = intPreferencesKey("port")
        private val KEY_DARK_MODE = intPreferencesKey("dark_mode") // 0=系统 1=浅色 2=深色
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    /** 密码：空字符串表示未设置 */
    val password: Flow<String> = context.dataStore.data.map { it[KEY_PASSWORD] ?: "" }

    /** 端口，默认 8080 */
    val port: Flow<Int> = context.dataStore.data.map { it[KEY_PORT] ?: 8080 }

    /** 深色模式 0=跟随系统 1=浅色 2=深色 */
    val darkMode: Flow<Int> = context.dataStore.data.map { it[KEY_DARK_MODE] ?: 0 }

    /** Material You 动态取色 */
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[KEY_DYNAMIC_COLOR] ?: true }

    suspend fun setPassword(password: String) {
        context.dataStore.edit { it[KEY_PASSWORD] = password }
    }

    suspend fun setPort(port: Int) {
        context.dataStore.edit { it[KEY_PORT] = port }
    }

    suspend fun setDarkMode(mode: Int) {
        context.dataStore.edit { it[KEY_DARK_MODE] = mode }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }
}