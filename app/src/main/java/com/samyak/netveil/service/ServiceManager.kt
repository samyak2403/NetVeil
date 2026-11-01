package com.samyak.netveil.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.samyak.netveil.data.START_ACTION
import com.samyak.netveil.data.STOP_ACTION
import com.samyak.netveil.model.Mode

object ServiceManager {
    private val TAG: String = ServiceManager::class.java.simpleName

    fun start(context: Context, mode: Mode) {
        when (mode) {
            Mode.VPN -> {
                Log.i(TAG, "Starting NetVeil VPN service")
                val intent = Intent(context, NetVeilVpnService::class.java).apply {
                    action = START_ACTION
                }
                ContextCompat.startForegroundService(context, intent)
            }

            Mode.Proxy -> {
                Log.i(TAG, "Starting NetVeil Proxy service")
                val intent = Intent(context, NetVeilProxyService::class.java).apply {
                    action = START_ACTION
                }
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    fun stop(context: Context) {
        val (_, mode) = appStatus
        when (mode) {
            Mode.VPN -> {
                Log.i(TAG, "Stopping NetVeil VPN service")
                val intent = Intent(context, NetVeilVpnService::class.java).apply {
                    action = STOP_ACTION
                }
                ContextCompat.startForegroundService(context, intent)
            }

            Mode.Proxy -> {
                Log.i(TAG, "Stopping NetVeil Proxy service")
                val intent = Intent(context, NetVeilProxyService::class.java).apply {
                    action = STOP_ACTION
                }
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }
}
