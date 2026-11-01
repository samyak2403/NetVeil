package com.samyak.netveil.util

import android.net.InetAddresses
import android.os.Build

fun checkIp(ip: String): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            InetAddresses.isNumericAddress(ip)
        } catch (e: Exception) {
            false
        }
    } else {
        ip.isNotBlank() && ip.split(".").let { parts ->
            parts.size == 4 && parts.all { it.toIntOrNull() in 0..255 }
        }
    }

fun checkNotLocalIp(ip: String): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            InetAddresses.isNumericAddress(ip) && InetAddresses.parseNumericAddress(ip).let {
                !it.isAnyLocalAddress && !it.isLoopbackAddress
            }
        } catch (e: Exception) {
            false
        }
    } else {
        checkIp(ip) && !ip.startsWith("127.") && ip != "0.0.0.0"
    }

fun checkPort(portStr: String): Boolean =
    portStr.toIntOrNull()?.let { it in 1..65535 } ?: false

fun checkIntRange(valueStr: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Boolean =
    valueStr.toIntOrNull()?.let { it in min..max } ?: false

