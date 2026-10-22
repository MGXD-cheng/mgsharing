package com.lingxi.mgsharing.server

import android.content.Context
import com.lingxi.mgsharing.data.SharedFile
import fi.iki.elonen.NanoHTTPD
import java.net.URLEncoder
import java.util.UUID

/**
 * 基于 NanoHTTPD 的局域网文件分享服务器
 *
 * 路由：
 *  GET  /                文件列表页（设置了密码时先显示登录页）
 *  POST /login           密码验证，成功后下发 mg_session Cookie（30 分钟）
 *  GET  /logout          清除会话
 *  GET  /download/{id}   流式下载文件
 */
class FileServer(
    private val context: Context,
    port: Int
) : NanoHTTPD(port) {

    /** 会话 token -> 过期时间戳 */
    private val sessions = HashMap<String, Long>()

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri ?: "/"
        return when {
            uri == "/" -> serveHome(session)
            uri == "/login" -> serveLogin(session)
            uri == "/logout" -> serveLogout()
            uri.startsWith("/download/") -> serveDownload(session, uri.substring("/download/".length))
            else -> newFixedLengthResponse(
                Response.Status.NOT_FOUND, MIME_HTML, WebPageBuilder.buildErrorPage()
            )
        }
    }

    // ---------- 页面 ----------

    private fun serveHome(session: IHTTPSession): Response {
        if (!isAuthorized(session)) {
            return html(WebPageBuilder.buildLoginPage(error = false))
        }
        return html(
            WebPageBuilder.buildFileListPage(
                files = ServerState.sharedFiles.value,
                protected = isProtected()
            )
        )
    }

    private fun serveLogin(session: IHTTPSession): Response {
        if (session.method != Method.POST) {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "Method Not Allowed")
        }
        val password = try {
            val body = HashMap<String, String>()
            session.parseBody(body)
            session.parms["password"] ?: ""
        } catch (_: Exception) {
            ""
        }
        return if (password.isNotEmpty() && password == ServerState.password.value) {
            val token = UUID.randomUUID().toString()
            sessions[token] = System.currentTimeMillis() + SESSION_TTL_MS
            newFixedLengthResponse(Response.Status.FOUND, MIME_PLAINTEXT, "").apply {
                addHeader("Location", "/")
                addHeader("Set-Cookie", "mg_session=$token; Path=/; HttpOnly; SameSite=Strict; Max-Age=1800")
            }
        } else {
            html(WebPageBuilder.buildLoginPage(error = true))
        }
    }

    private fun serveLogout(): Response {
        sessions.clear()
        return newFixedLengthResponse(Response.Status.FOUND, MIME_PLAINTEXT, "").apply {
            addHeader("Location", "/")
            addHeader("Set-Cookie", "mg_session=; Path=/; Max-Age=0")
        }
    }

    // ---------- 下载 ----------

    private fun serveDownload(session: IHTTPSession, id: String): Response {
        if (!isAuthorized(session)) {
            return newFixedLengthResponse(Response.Status.FOUND, MIME_PLAINTEXT, "").apply {
                addHeader("Location", "/")
            }
        }
        val file: SharedFile = ServerState.sharedFiles.value.firstOrNull { it.id == id }
            ?: return newFixedLengthResponse(
                Response.Status.NOT_FOUND, MIME_PLAINTEXT, "文件不存在或已被移除"
            )

        val stream = try {
            context.contentResolver.openInputStream(file.uri)
                ?: return newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "无法读取文件"
                )
        } catch (_: Exception) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "读取文件失败"
            )
        }

        val mime = file.mimeType.ifEmpty { "application/octet-stream" }
        val encoded = URLEncoder.encode(file.name, "UTF-8").replace("+", "%20")
        // 流式返回，避免大文件占用内存
        val response = if (file.size > 0) {
            newFixedLengthResponse(Response.Status.OK, mime, stream, file.size)
        } else {
            newChunkedResponse(Response.Status.OK, mime, stream)
        }
        return response.apply {
            addHeader("Content-Disposition", "attachment; filename*=UTF-8''$encoded")
            addHeader("Cache-Control", "no-store")
        }
    }

    // ---------- 鉴权 ----------

    private fun isProtected(): Boolean = ServerState.password.value.isNotEmpty()

    private fun isAuthorized(session: IHTTPSession): Boolean {
        if (!isProtected()) return true
        val cookie = session.headers["cookie"] ?: return false
        val token = cookie.split(";")
            .map { it.trim() }
            .firstOrNull { it.startsWith("$SESSION_COOKIE=") }
            ?.removePrefix("$SESSION_COOKIE=")
            ?: return false
        val expiry = sessions[token] ?: return false
        if (System.currentTimeMillis() > expiry) {
            sessions.remove(token)
            return false
        }
        return true
    }

    private fun html(content: String): Response =
        newFixedLengthResponse(Response.Status.OK, MIME_HTML, content)

    companion object {
        private const val SESSION_COOKIE = "mg_session"
        private const val SESSION_TTL_MS = 30L * 60L * 1000L
    }
}