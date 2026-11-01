package com.samyak.netveil.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.samyak.netveil.MainActivity
import com.samyak.netveil.R
import com.samyak.netveil.core.NetVeilProxy
import com.samyak.netveil.core.NetVeilProxyPreferences
import com.samyak.netveil.core.TProxyService
import com.samyak.netveil.data.*
import com.samyak.netveil.model.AppStatus
import com.samyak.netveil.model.Mode
import com.samyak.netveil.model.Sender
import com.samyak.netveil.model.ServiceStatus
import com.samyak.netveil.util.createConnectionNotification
import com.samyak.netveil.util.registerNotificationChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

class NetVeilVpnService : LifecycleVpnService() {
    private var proxy = NetVeilProxy()
    private var proxyJob: Job? = null
    private var statsJob: Job? = null
    private var tunFd: ParcelFileDescriptor? = null
    private val mutex = Mutex()
    private var stopping: Boolean = false

    companion object {
        private val TAG: String = NetVeilVpnService::class.java.simpleName
        private const val FOREGROUND_SERVICE_ID: Int = 1
        private const val NOTIFICATION_CHANNEL_ID: String = "NetVeilVpn"

        private var status: ServiceStatus = ServiceStatus.Disconnected
    }

    override fun onCreate() {
        super.onCreate()
        registerNotificationChannel(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.vpn_channel_name,
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

    override fun onRevoke() {
        Log.i(TAG, "VPN permission revoked by user or system")
        lifecycleScope.launch { stop() }
    }

    private suspend fun start() {
        Log.i(TAG, "Starting NetVeil VPN Service")

        if (status == ServiceStatus.Connected) {
            Log.w(TAG, "VPN is already running")
            return
        }

        try {
            startForeground()
            mutex.withLock {
                startProxy()
                startTun2Socks()
            }
            updateStatus(ServiceStatus.Connected)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start NetVeil VPN", e)
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
        Log.i(TAG, "Stopping NetVeil VPN Service")

        mutex.withLock {
            stopping = true
            try {
                stopTun2Socks()
                stopProxy()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping VPN components", e)
            } finally {
                stopping = false
            }
        }

        updateStatus(ServiceStatus.Disconnected)
        stopSelf()
    }

    private suspend fun startProxy() {
        Log.i(TAG, "Starting NetVeil DPI proxy engine")

        if (proxyJob != null) {
            throw IllegalStateException("Proxy job is already active")
        }

        proxy = NetVeilProxy()
        val preferences = NetVeilProxyPreferences.fromPreferences(NetVeilPreferences(this))

        proxyJob = lifecycleScope.launch(Dispatchers.IO) {
            val code = proxy.startProxy(preferences)

            withContext(Dispatchers.Main) {
                if (code != 0) {
                    Log.e(TAG, "NetVeil proxy terminated with code $code")
                    updateStatus(ServiceStatus.Failed, "Proxy process terminated with code $code")
                } else {
                    if (!stopping) {
                        stop()
                        updateStatus(ServiceStatus.Disconnected)
                    }
                }
            }
        }

        Log.i(TAG, "NetVeil proxy launched")
    }

    private suspend fun stopProxy() {
        Log.i(TAG, "Stopping NetVeil DPI proxy engine")

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

    private fun startTun2Socks() {
        Log.i(TAG, "Starting tun2socks engine")

        if (tunFd != null) {
            throw IllegalStateException("Tun2socks fd already exists")
        }

        val prefs = NetVeilPreferences(this)
        val port = prefs.proxyPort.toIntOrNull() ?: 1080
        val dns = prefs.dnsIp.ifBlank { "1.1.1.1" }
        val ipv6 = prefs.ipv6Enable

        val tun2socksConfig = """
        | misc:
        |   task-stack-size: 81920
        | socks5:
        |   mtu: 8500
        |   address: 127.0.0.1
        |   port: $port
        |   udp: udp
        """.trimMargin("| ")

        val configFile = try {
            File.createTempFile("config", ".tmp", cacheDir).apply {
                writeText(tun2socksConfig)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create tun2socks config file", e)
            throw e
        }

        val fd = createBuilder(dns, ipv6).establish()
            ?: throw IllegalStateException("VPN establish() returned null. Check VPN permissions.")

        this.tunFd = fd

        TProxyService.TProxyStartService(configFile.absolutePath, fd.fd)

        statsJob?.cancel()
        statsJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val stats = TProxyService.TProxyGetStats()
                    if (stats.size >= 4) {
                        updateTrafficStats(
                            TrafficStats(
                                txPackets = stats[0],
                                txBytes = stats[1],
                                rxPackets = stats[2],
                                rxBytes = stats[3],
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Stats reading: ${e.message}")
                }
                delay(1500)
            }
        }

        Log.i(TAG, "Tun2Socks started successfully")
    }

    private fun stopTun2Socks() {
        Log.i(TAG, "Stopping tun2socks engine")

        statsJob?.cancel()
        statsJob = null

        try {
            TProxyService.TProxyStopService()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping TProxyService: ${e.message}")
        }

        try {
            cacheDir.listFiles { file -> file.name.startsWith("config") && file.name.endsWith(".tmp") }
                ?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clean up temp configs", e)
        }

        try {
            tunFd?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing tunFd", e)
        }
        tunFd = null

        Log.i(TAG, "Tun2socks stopped")
    }

    private fun updateStatus(newStatus: ServiceStatus, errorMessage: String? = null) {
        Log.d(TAG, "NetVeil VPN status changed to $newStatus")

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
            Mode.VPN,
            errorMessage
        )

        val intent = Intent(
            when (newStatus) {
                ServiceStatus.Connected -> STARTED_BROADCAST
                ServiceStatus.Disconnected -> STOPPED_BROADCAST
                ServiceStatus.Failed -> FAILED_BROADCAST
            }
        ).apply {
            putExtra(SENDER, Sender.VPN.ordinal)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun createNotification(): Notification =
        createConnectionNotification(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_title,
            R.string.vpn_notification_content,
            NetVeilVpnService::class.java,
        )

    private fun createBuilder(dns: String, ipv6: Boolean): Builder {
        val builder = Builder()
        builder.setSession(getString(R.string.app_name))
        builder.setConfigureIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE,
            )
        )

        builder.addAddress("10.10.10.10", 32)
            .addRoute("0.0.0.0", 0)

        if (ipv6) {
            builder.addAddress("fd00::1", 128)
                .addRoute("::", 0)
        }

        if (dns.isNotBlank()) {
            try {
                builder.addDnsServer(dns)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to add DNS server $dns, fallback to 1.1.1.1", e)
                builder.addDnsServer("1.1.1.1")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        try {
            builder.addDisallowedApplication(applicationContext.packageName)
        } catch (e: Exception) {
            Log.w(TAG, "Could not disallow app package", e)
        }

        return builder
    }
}

typealias ByeDpiVpnService = NetVeilVpnService

