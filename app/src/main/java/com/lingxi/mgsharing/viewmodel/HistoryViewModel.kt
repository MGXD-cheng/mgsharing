package com.lingxi.mgsharing.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lingxi.mgsharing.MGsharingApp
import com.lingxi.mgsharing.data.ShareRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 分享历史 ViewModel
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as MGsharingApp).historyRepository

    val history: StateFlow<List<ShareRecord>> =
        repo.history.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun clear() = viewModelScope.launch { repo.clearHistory() }
}