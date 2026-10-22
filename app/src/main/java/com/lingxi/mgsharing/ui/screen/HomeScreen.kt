package com.lingxi.mgsharing.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingxi.mgsharing.ui.components.FileListItem
import com.lingxi.mgsharing.ui.components.QrCodeDialog
import com.lingxi.mgsharing.ui.components.ServerControlCard
import com.lingxi.mgsharing.viewmodel.ShareViewModel

/**
 * 分享主页 —— 服务器控制 + 文件列表
 */
@Composable
fun HomeScreen(
    viewModel: ShareViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val files by viewModel.sharedFiles.collectAsStateWithLifecycle()
    val url by viewModel.url.collectAsStateWithLifecycle()
    var showQr by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (!uris.isNullOrEmpty()) viewModel.addUris(uris)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ServerControlCard(
                isRunning = isRunning,
                url = url,
                fileCount = files.size,
                onStart = { viewModel.startServer(context) },
                onStop = { viewModel.stopServer(context) },
                onShowQr = { if (isRunning) showQr = true }
            )
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "共享文件",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { picker.launch("*/*") }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("添加")
                }
                if (files.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearFiles() }) {
                        Text("清空")
                    }
                }
            }
        }

        if (files.isEmpty()) {
            item { EmptyFileState() }
        } else {
            items(files, key = { it.id }) { file ->
                FileListItem(
                    file = file,
                    onRemove = { viewModel.removeFile(file.id) }
                )
            }
        }
    }

    if (showQr) {
        QrCodeDialog(url = url, onDismiss = { showQr = false })
    }
}

@Composable
private fun EmptyFileState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            text = "还没有共享文件",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "点击「添加」选择文件，\n或从其他应用分享文件到 MGsharing",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}