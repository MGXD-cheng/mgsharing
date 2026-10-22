package com.lingxi.mgsharing.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.lingxi.mgsharing.MGsharingApp
import com.lingxi.mgsharing.MainActivity
import com.lingxi.mgsharing.R
import com.lingxi.mgsharing.data.ShareRecord
import com.lingxi.mgsharing.data.SharedFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * 服务器运行时状态 —— UI 与服务共享的单一数据源
 */
object ServerState {
    val isRunning = MutableStateFlow(false)
    val sharedFiles = MutableStateFlow<List<SharedFile>>(emptyList())
    val password = MutableStateFlow("")
    val port = MutableStateFlow(8080)
    val url = MutableStateFlow("")
    val startedAt = MutableStateFlow(0L)
}

/**
 * 前台服务 —— 托管 FileServer 的生命周期
 */
class ServerService : Service() {

    private var server: FileServer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val ACTION_START = "com.lingxi.mgsharing.action.START"
        const val ACTION_STOP = "com.lingxi.mgsharing.action.STOP"
        private const val EXTRA_PORT = "extra_port"
        private const val CHANNEL_ID = "mg_server_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context, port: Int) {
            val intent = Intent(context, ServerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PORT, port)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ServerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): Nothing? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startServer(intent.getIntExtra(EXTRA_PORT, 8080))
            ACTION_STOP -> shutdown()
            else -> if (server == null) shutdown()
        }
        return START_NOT_STICKY
    }

    private fun startServer(port: Int) {
        if (server != null) return
        createChannel()
        val ip = NetworkUtils.getLocalIpAddress()
        val urlText = if (ip.isEmpty()) "http://:$port/" else "http://$ip:$port/"

        // 先进入前台，满足 startForegroundService 的 5 秒约束
        // （即使随后端口绑定失败也不会触发 RemoteServiceException）
        val notification = buildNotification(urlText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val fileServer = FileServer(this, port)
        try {
            fileServer.start(2000)
        } catch (_: Exception) {
            shutdown()
            return
        }
        server = fileServer
        ServerState.port.value = port
        ServerState.url.value = urlText
        ServerState.startedAt.value = System.currentTimeMillis()
        ServerState.isRunning.value = true
    }

    private fun shutdown() {
        if (server != null) {
            val files = ServerState.sharedFiles.value
            if (files.isNotEmpty()) {
                val record = ShareRecord(
                    fileNames = files.map { it.name },
                    totalSize = files.sumOf { it.size },
                    durationMs = System.currentTimeMillis() - ServerState.startedAt.value
                )
                serviceScope.launch {
                    runCatching {
                        (application as MGsharingApp).historyRepository.addRecord(record)
                    }
                }
            }
            runCatching { server?.stop() }
            server = null
        }
        ServerState.isRunning.value = false
        ServerState.url.value = ""
        ServerState.startedAt.value = 0L
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        runCatching { server?.stop() }
        server = null
        ServerState.isRunning.value = false
        serviceScope.cancel()
        super.onDestroy()
    }

    // ---------- 通知 ----------

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "MGsharing 文件分享服务运行状态" }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(url: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, ServerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(url)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(0, "停止分享", stopIntent)
            .build()
    }
}