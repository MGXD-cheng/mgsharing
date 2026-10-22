package com.lingxi.mgsharing.data

import android.net.Uri

/**
 * 共享文件数据模型
 */
data class SharedFile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val size: Long,
    val mimeType: String,
    val uri: Uri
) {
    /** 人类可读的文件大小 */
    val displaySize: String
        get() = when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "%.1f KB".format(size / 1024.0)
            size < 1024 * 1024 * 1024 -> "%.1f MB".format(size / (1024.0 * 1024))
            else -> "%.2f GB".format(size / (1024.0 * 1024 * 1024))
        }

    /** 根据 MIME 类型获取文件分类图标 key */
    val category: FileCategory
        get() = when {
            mimeType.startsWith("image/") -> FileCategory.IMAGE
            mimeType.startsWith("video/") -> FileCategory.VIDEO
            mimeType.startsWith("audio/") -> FileCategory.AUDIO
            mimeType == "application/vnd.android.package-archive" || name.endsWith(".apk") -> FileCategory.APK
            mimeType.startsWith("text/") || mimeType.contains("pdf") || mimeType.contains("document") ||
                mimeType.contains("sheet") || mimeType.contains("presentation") -> FileCategory.DOCUMENT
            mimeType.startsWith("application/zip") || mimeType.startsWith("application/x-rar") ||
                name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".7z") -> FileCategory.ARCHIVE
            else -> FileCategory.OTHER
        }
}

enum class FileCategory {
    IMAGE, VIDEO, AUDIO, APK, DOCUMENT, ARCHIVE, OTHER
}