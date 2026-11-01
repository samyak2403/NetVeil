package com.samyak.netveil.service

import android.app.Notification
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.samyak.netveil.R
import com.samyak.netveil.core.NetVeilProxy
import com.samyak.netveil.core.NetVeilProxyPreferences
import com.samyak.netveil.data.*
import com.samyak.netveil.model.AppStatus
import com.samyak.netveil.model.Mode
import com.samyak.netveil.model.Sender
import com.samyak.netveil.model.ServiceStatus
import com.samyak.netveil.util.createConnectionNotification
import com.samyak.netveil.util.registerNotificationChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class NetVeilProxyService : LifecycleService() {
    private var proxy = NetVeilProxy()
    private var proxyJob: Job? = null
    private val mutex = Mutex()

    companion object {
        private val TAG: String = NetVeilProxyService::class.java.simpleName
        private const val FOREGROUND_SERVICE_ID: Int = 2
        private const val NOTIFICATION_CHANNEL_ID: String = "NetVeilProxy"

        private var status: ServiceStatus = ServiceStatus.Disconnected
    }

    override fun onCreate() {
        super.onCreate()
        registerNotificationChannel(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.proxy_channel_name,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return when (val action = intent?.action) {
            START_ACTION, LEGACY_START_ACTION -> {
                lifecycleScope.launch { start() }
                START_STICKY
            }

            STOP_ACTION, LEGACY_STOP_ACTION -> {
                lifecycleScope.launch { stop() }
                START_NOT_STICKY
            }

            else -> {
                Log.w(TAG, "Unknown action: $action")
                START_NOT_STICKY
            }
        }
    }

    private suspend fun start() {
        Log.i(TAG, "Starting NetVeil Proxy Service")

        if (status == ServiceStatus.Connected) {
            Log.w(TAG, "NetVeil Proxy already connected")
            return
        }

        try {
            startForeground()
            mutex.withLock {
                startProxy()
            }
            updateStatus(ServiceStatus.Connected)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start NetVeil proxy", e)
            updateStatus(ServiceStatus.Failed, e.localizedMessage)
            stop()
        }
    }

    private fun startForeground() {
        val notification: Notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_SERVICE_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(FOREGROUND_SERVICE_ID, notification)
        }
    }

    private suspend fun stop() {
        Log.i(TAG, "Stopping NetVeil Proxy Service")

        mutex.withLock {
            try {
                stopProxy()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping proxy", e)
            }
        }
        updateStatus(ServiceStatus.Disconnected)
        stopSelf()
    }

    private suspend fun startProxy() {
        Log.i(TAG, "Starting NetVeil proxy socket")

        if (proxyJob != null) {
            throw IllegalStateException("Proxy job is already active")
        }

        proxy = NetVeilProxy()
        val preferences = NetVeilProxyPreferences.fromPreferences(NetVeilPreferences(this))

        proxyJob = lifecycleScope.launch(Dispatchers.IO) {
            val code = proxy.startProxy(preferences)

            withContext(Dispatchers.Main) {
                if (code != 0) {
                    Log.e(TAG, "Proxy stopped with code $code")
                    updateStatus(ServiceStatus.Failed, "Proxy process terminated with code $code")
                } else {
                    updateStatus(ServiceStatus.Disconnected)
                }
            }
        }

        Log.i(TAG, "NetVeil proxy started successfully")
    }

    private suspend fun stopProxy() {
        Log.i(TAG, "Stopping NetVeil proxy socket")

        if (status == ServiceStatus.Disconnected) {
            return
        }

        try {
            proxy.stopProxy()
        } catch (e: Exception) {
            Log.w(TAG, "Exception during stopProxy: ${e.message}")
        }
        try {
            withTimeout(2000L) {
                proxyJob?.join()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Timed out waiting for proxyJob to finish, cancelling", e)
            proxyJob?.cancel()
        }
        proxyJob = null

        Log.i(TAG, "NetVeil proxy stopped")
    }

    private fun updateStatus(newStatus: ServiceStatus, errorMessage: String? = null) {
        Log.d(TAG, "NetVeil proxy status changed to $newStatus")

        status = newStatus

        setStatus(
            when (newStatus) {
                ServiceStatus.Connected -> AppStatus.Running
                ServiceStatus.Disconnected,
                ServiceStatus.Failed -> {
                    proxyJob = null
                    AppStatus.Halted
                }
            },
            Mode.Proxy,
            errorMessage
        )

        val intent = Intent(
            when (newStatus) {
                ServiceStatus.Connected -> STARTED_BROADCAST
                ServiceStatus.Disconnected -> STOPPED_BROADCAST
                ServiceStatus.Failed -> FAILED_BROADCAST
            }
        ).apply {
            putExtra(SENDER, Sender.Proxy.ordinal)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun createNotification(): Notification =
        createConnectionNotification(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_title,
            R.string.proxy_notification_content,
            NetVeilProxyService::class.java,
        )
}

typealias ByeDpiProxyService = NetVeilProxyService

