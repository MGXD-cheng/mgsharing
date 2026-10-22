package com.lingxi.mgsharing.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(name = "mg_history")

/**
 * 分享会话历史记录
 */
data class ShareRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val fileNames: List<String>,
    val totalSize: Long,
    val durationMs: Long = 0
) {
    val displayTime: String
        get() {
            val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }

    val displayDuration: String
        get() {
            val sec = durationMs / 1000
            return when {
                sec < 60 -> "${sec}秒"
                sec < 3600 -> "${sec / 60}分${sec % 60}秒"
                else -> "${sec / 3600}时${(sec % 3600) / 60}分"
            }
        }

    val displayTotalSize: String
        get() = when {
            totalSize < 1024 -> "$totalSize B"
            totalSize < 1024 * 1024 -> "%.1f KB".format(totalSize / 1024.0)
            totalSize < 1024 * 1024 * 1024 -> "%.1f MB".format(totalSize / (1024.0 * 1024))
            else -> "%.2f GB".format(totalSize / (1024.0 * 1024 * 1024))
        }
}

/**
 * 分享历史仓库 —— JSON 序列化存 DataStore
 */
class HistoryRepository(private val context: Context) {

    companion object {
        private val KEY_HISTORY = stringPreferencesKey("history_json")
    }

    val history: Flow<List<ShareRecord>> = context.historyDataStore.data.map { prefs ->
        parseJson(prefs[KEY_HISTORY] ?: "[]")
    }

    suspend fun addRecord(record: ShareRecord) {
        context.historyDataStore.edit { prefs ->
            val current = parseJson(prefs[KEY_HISTORY] ?: "[]").toMutableList()
            current.add(0, record) // 最新的在前面
            // 最多保留 50 条
            val trimmed = if (current.size > 50) current.subList(0, 50) else current
            prefs[KEY_HISTORY] = toJson(trimmed)
        }
    }

    suspend fun clearHistory() {
        context.historyDataStore.edit { it[KEY_HISTORY] = "[]" }
    }

    private fun toJson(records: List<ShareRecord>): String {
        val arr = JSONArray()
        records.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("timestamp", r.timestamp)
                put("totalSize", r.totalSize)
                put("durationMs", r.durationMs)
                put("fileNames", JSONArray(r.fileNames))
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseJson(json: String): List<ShareRecord> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val namesArr = obj.getJSONArray("fileNames")
                val names = (0 until namesArr.length()).map { namesArr.getString(it) }
                ShareRecord(
                    id = obj.getString("id"),
                    timestamp = obj.getLong("timestamp"),
                    fileNames = names,
                    totalSize = obj.getLong("totalSize"),
                    durationMs = obj.getLong("durationMs")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}