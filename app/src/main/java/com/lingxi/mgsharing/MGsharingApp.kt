package com.lingxi.mgsharing

import android.app.Application
import com.lingxi.mgsharing.data.HistoryRepository
import com.lingxi.mgsharing.data.SettingsRepository

/**
 * 应用入口 —— 持有全局仓库单例
 */
class MGsharingApp : Application() {

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val historyRepository: HistoryRepository by lazy { HistoryRepository(this) }
}