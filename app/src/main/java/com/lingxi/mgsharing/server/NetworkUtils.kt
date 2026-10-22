package com.lingxi.mgsharing.server

import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * 网络工具 —— 获取局域网 IPv4 地址
 */
object NetworkUtils {

    /** 获取当前设备的局域网 IPv4 地址，失败返回空字符串 */
    fun getLocalIpAddress(): String {
        runCatching {
            NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.toList() }
                .filterIsInstance<Inet4Address>()
                .filter { !it.isLoopbackAddress }
                .forEach { return it.hostAddress ?: "" }
        }
        return ""
    }
}