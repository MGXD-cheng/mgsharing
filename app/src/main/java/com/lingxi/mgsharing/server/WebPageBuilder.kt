package com.lingxi.mgsharing.server

import com.lingxi.mgsharing.data.FileCategory
import com.lingxi.mgsharing.data.SharedFile

/**
 * Web 页面 HTML 生成器 —— Material 风格内嵌样式，支持深浅色自适应
 */
object WebPageBuilder {

    private val ICONS = mapOf(
        FileCategory.IMAGE to "🖼️",
        FileCategory.VIDEO to "🎬",
        FileCategory.AUDIO to "🎵",
        FileCategory.APK to "📱",
        FileCategory.DOCUMENT to "📄",
        FileCategory.ARCHIVE to "📦",
        FileCategory.OTHER to "📁"
    )

    private const val CSS = """
        * { box-sizing: border-box; margin: 0; padding: 0; }
        :root {
            --primary: #00897B; --primary-dark: #00695C; --on-primary: #ffffff;
            --bg: #F4F7F7; --surface: #ffffff; --text: #1A1C1C; --text2: #5A5F5F;
            --border: #E1E5E5; --chip: #E0F2F1; --danger: #C62828;
        }
        @media (prefers-color-scheme: dark) {
            :root {
                --primary: #4DB6AC; --primary-dark: #26A69A; --on-primary: #003330;
                --bg: #101414; --surface: #1B2020; --text: #E1E4E4; --text2: #A8B0B0;
                --border: #2C3333; --chip: #123632; --danger: #EF9A9A;
            }
        }
        body {
            font-family: -apple-system, "PingFang SC", "Microsoft YaHei", "Segoe UI", sans-serif;
            background: var(--bg); color: var(--text); min-height: 100vh;
        }
        .header {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: var(--on-primary); padding: 32px 20px 28px; border-radius: 0 0 24px 24px;
        }
        .header h1 { font-size: 22px; font-weight: 700; letter-spacing: 0.5px; }
        .header p { margin-top: 8px; opacity: 0.92; font-size: 14px; }
        .container { max-width: 720px; margin: 0 auto; padding: 20px 16px 48px; }
        .card {
            background: var(--surface); border: 1px solid var(--border); border-radius: 16px;
            padding: 14px 16px; display: flex; align-items: center; gap: 14px; margin-bottom: 12px;
            transition: transform 0.15s ease, box-shadow 0.15s ease;
        }
        .card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.10); }
        .file-icon {
            width: 46px; height: 46px; border-radius: 12px; background: var(--chip);
            display: flex; align-items: center; justify-content: center; font-size: 22px; flex-shrink: 0;
        }
        .file-info { flex: 1; min-width: 0; }
        .file-name {
            font-size: 15px; font-weight: 600; overflow: hidden;
            text-overflow: ellipsis; white-space: nowrap;
        }
        .file-meta { margin-top: 4px; font-size: 12.5px; color: var(--text2); }
        .btn {
            display: inline-flex; align-items: center; justify-content: center; gap: 6px;
            border: none; border-radius: 12px; padding: 10px 18px; font-size: 14px;
            font-weight: 600; cursor: pointer; text-decoration: none;
            background: var(--primary); color: var(--on-primary); transition: opacity 0.15s ease;
        }
        .btn:hover { opacity: 0.88; }
        .btn-outlined {
            background: transparent; color: var(--primary);
            border: 1.5px solid var(--primary);
        }
        .btn-outlined:hover { background: var(--chip); opacity: 1; }
        .empty {
            text-align: center; padding: 56px 20px; color: var(--text2);
            background: var(--surface); border: 1px dashed var(--border); border-radius: 16px;
        }
        .empty .icon { font-size: 44px; margin-bottom: 12px; }
        .footer {
            text-align: center; color: var(--text2); font-size: 12px;
            padding: 20px 0 8px;
        }
        .footer a { color: var(--primary); text-decoration: none; }
        .login-wrap {
            min-height: 100vh; display: flex; align-items: center;
            justify-content: center; padding: 24px;
        }
        .login-card {
            width: 100%; max-width: 360px; background: var(--surface);
            border: 1px solid var(--border); border-radius: 24px; padding: 32px 28px;
            box-shadow: 0 12px 32px rgba(0,0,0,0.12);
        }
        .login-card h2 { font-size: 20px; margin-bottom: 6px; }
        .login-card .sub { color: var(--text2); font-size: 13.5px; margin-bottom: 22px; }
        .input {
            width: 100%; padding: 13px 15px; border-radius: 12px; font-size: 15px;
            border: 1.5px solid var(--border); background: var(--bg); color: var(--text);
            outline: none; transition: border-color 0.15s ease;
        }
        .input:focus { border-color: var(--primary); }
        .error { color: var(--danger); font-size: 13px; margin: 10px 2px 0; }
        .login-btn { width: 100%; margin-top: 18px; padding: 13px; }
        .lock { font-size: 40px; margin-bottom: 14px; }
    """

    private fun page(title: String, body: String): String = """
        <!DOCTYPE html>
        <html lang="zh-CN">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>$title</title>
            <style>$CSS</style>
        </head>
        <body>$body</body>
        </html>
    """.trimIndent()

    /** 登录页 */
    fun buildLoginPage(error: Boolean): String {
        val errorHtml = if (error) """<p class="error">密码错误，请重试</p>""" else ""
        return page(
            title = "MGsharing - 登录",
            body = """
                <div class="login-wrap">
                    <div class="login-card">
                        <div class="lock">🔒</div>
                        <h2>MGsharing</h2>
                        <p class="sub">此分享受密码保护，请输入密码后继续</p>
                        <form method="POST" action="/login">
                            <input class="input" type="password" name="password"
                                placeholder="请输入访问密码" autofocus autocomplete="current-password">
                            $errorHtml
                            <button class="btn login-btn" type="submit">进入分享</button>
                        </form>
                    </div>
                </div>
            """.trimIndent()
        )
    }

    /** 文件列表页 */
    fun buildFileListPage(files: List<SharedFile>, protected: Boolean): String {
        val totalSize = files.sumOf { it.size }
        val sizeText = formatSize(totalSize)
        val logoutHtml = if (protected) """<a class="btn btn-outlined" href="/logout">退出</a>""" else ""

        val filesHtml = if (files.isEmpty()) {
            """
                <div class="empty">
                    <div class="icon">📂</div>
                    <p>还没有分享任何文件</p>
                </div>
            """.trimIndent()
        } else {
            files.joinToString("\n") { file ->
                val icon = ICONS[file.category] ?: "📁"
                """
                    <div class="card">
                        <div class="file-icon">$icon</div>
                        <div class="file-info">
                            <div class="file-name">${escapeHtml(file.name)}</div>
                            <div class="file-meta">${file.displaySize}</div>
                        </div>
                        <a class="btn" href="/download/${file.id}">下载</a>
                    </div>
                """.trimIndent()
            }
        }

        return page(
            title = "MGsharing - 文件分享",
            body = """
                <div class="header">
                    <h1>MGsharing</h1>
                    <p>共 ${files.size} 个文件 · $sizeText · 仅限同一局域网访问</p>
                </div>
                <div class="container">
                    $filesHtml
                    <div class="footer">
                        <p>由 MGsharing 驱动 · <a href="/">刷新列表</a> · $logoutHtml</p>
                    </div>
                </div>
            """.trimIndent()
        )
    }

    /** 404 页面 */
    fun buildErrorPage(): String = page(
        title = "MGsharing - 页面不存在",
        body = """
            <div class="login-wrap">
                <div class="login-card">
                    <div class="lock">🤔</div>
                    <h2>页面不存在</h2>
                    <p class="sub">你要找的页面可能已被移除</p>
                    <a class="btn login-btn" href="/">返回首页</a>
                </div>
            </div>
        """.trimIndent()
    )

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&" + "quot;")

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
        else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
    }
}