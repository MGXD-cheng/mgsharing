package com.lingxi.mgsharing.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lingxi.mgsharing.MGsharingApp
import com.lingxi.mgsharing.data.SharedFile
import com.lingxi.mgsharing.server.ServerService
import com.lingxi.mgsharing.server.ServerState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 分享主页 ViewModel —— 管理共享文件列表与服务器启停
 */
class ShareViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = (application as MGsharingApp).settingsRepository

    val isRunning: StateFlow<Boolean> = ServerState.isRunning.asStateFlow()
    val sharedFiles: StateFlow<List<SharedFile>> = ServerState.sharedFiles.asStateFlow()
    val url: StateFlow<String> = ServerState.url.asStateFlow()
    val startedAt: StateFlow<Long> = ServerState.startedAt.asStateFlow()

    /** 添加文件（去重） */
    fun addUris(uris: List<Uri>) {
        val existing = ServerState.sharedFiles.value.map { it.uri.toString() }.toSet()
        val resolved = uris
            .distinctBy { it.toString() }
            .filterNot { it.toString() in existing }
            .mapNotNull { resolveFile(it) }
        if (resolved.isNotEmpty()) {
            ServerState.sharedFiles.value = ServerState.sharedFiles.value + resolved
        }
    }

    /** 从 Uri 解析文件元数据 */
    private fun resolveFile(uri: Uri): SharedFile? {
        val resolver = getApplication<Application>().contentResolver
        var name = uri.lastPathSegment ?: "file"
        var size = 0L
        runCatching {
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0 && !cursor.isNull(nameIdx)) name = cursor.getString(nameIdx)
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
                }
            }
        }
        if (size <= 0L) {
            runCatching { resolver.openFileDescriptor(uri, "r")?.use { size = it.statSize } }
        }
        if (size < 0L) size = 0L
        val mime = resolver.getType(uri) ?: "application/octet-stream"
        // 尽力持久化读权限，保证服务运行期间随时可读
        runCatching {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return SharedFile(name = name, size = size, mimeType = mime, uri = uri)
    }

    fun removeFile(id: String) {
        ServerState.sharedFiles.value = ServerState.sharedFiles.value.filterNot { it.id == id }
    }

    fun clearFiles() {
        ServerState.sharedFiles.value = emptyList()
    }

    /** 启动分享服务（同步设置里的密码与端口） */
    fun startServer(context: Context) {
        viewModelScope.launch {
            ServerState.password.value = settingsRepo.password.first()
            val port = settingsRepo.port.first()
            ServerState.port.value = port
            ServerService.start(context, port)
        }
    }

    fun stopServer(context: Context) {
        ServerService.stop(context)
    }
}