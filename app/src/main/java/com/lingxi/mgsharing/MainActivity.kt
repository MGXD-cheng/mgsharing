package com.lingxi.mgsharing

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingxi.mgsharing.ui.AppNavigation
import com.lingxi.mgsharing.ui.theme.MGsharingTheme
import com.lingxi.mgsharing.viewmodel.HistoryViewModel
import com.lingxi.mgsharing.viewmodel.SettingsViewModel
import com.lingxi.mgsharing.viewmodel.ShareViewModel

/**
 * 应用入口 Activity —— 承载导航骨架，处理外部分享与通知权限
 */
class MainActivity : ComponentActivity() {

    private val shareViewModel: ShareViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        handleShareIntent(intent)

        setContent {
            val darkMode by settingsViewModel.darkMode.collectAsStateWithLifecycle()
            val dynamicColor by settingsViewModel.dynamicColor.collectAsStateWithLifecycle()
            val darkTheme = when (darkMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }
            MGsharingTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor
            ) {
                AppNavigation(
                    shareViewModel = shareViewModel,
                    historyViewModel = historyViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    /** Android 13+ 通知运行时权限（前台服务通知需要） */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /** 处理其他应用"分享进来"的文件（SEND / SEND_MULTIPLE） */
    private fun handleShareIntent(intent: Intent?) {
        val uris = extractSharedUris(intent)
        if (uris.isNotEmpty()) {
            shareViewModel.addUris(uris)
        }
    }

    private fun extractSharedUris(intent: Intent?): List<Uri> {
        if (intent == null) return emptyList()
        val action = intent.action ?: return emptyList()
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) return emptyList()

        val uris = mutableListOf<Uri>()
        if (action == Intent.ACTION_SEND) {
            IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                ?.let { uris.add(it) }
        } else {
            IntentCompat.getParcelableArrayListExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                ?.let { uris.addAll(it) }
        }
        // 兜底：部分应用只塞 ClipData 不填 EXTRA_STREAM
        if (uris.isEmpty()) {
            val clip = intent.clipData
            if (clip != null) {
                for (i in 0 until clip.itemCount) {
                    clip.getItemAt(i).uri?.let { uris.add(it) }
                }
            }
        }
        return uris.filter { it.scheme == "content" || it.scheme == "file" }
    }
}