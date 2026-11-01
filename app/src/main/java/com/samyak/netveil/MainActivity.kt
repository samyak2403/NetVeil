package com.samyak.netveil

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.samyak.netveil.data.*
import com.samyak.netveil.model.AppStatus
import com.samyak.netveil.model.AppThemeMode
import com.samyak.netveil.model.Mode
import com.samyak.netveil.model.Sender
import com.samyak.netveil.service.ServiceManager
import com.samyak.netveil.service.appStatus
import com.samyak.netveil.service.statusFlow
import com.samyak.netveil.ui.screens.CmdConfigScreen
import com.samyak.netveil.ui.screens.MainScreen
import com.samyak.netveil.ui.screens.SettingsScreen
import com.samyak.netveil.ui.screens.UiConfigScreen
import com.samyak.netveil.ui.theme.NetVeilTheme
import com.samyak.netveil.util.LogUtils
import kotlinx.coroutines.launch

enum class AppScreen {
    Main,
    Settings,
    UiConfig,
    CmdConfig
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: NetVeilPreferences

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
    }

    private val vpnLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                ServiceManager.start(this, Mode.VPN)
            } else {
                Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Log.w(TAG, "Notification permission denied by user")
            }
        }

    private val saveLogsLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val logs = LogUtils.collectLogs()
                    if (logs != null && LogUtils.saveLogsToUri(this@MainActivity, uri, logs)) {
                        Toast.makeText(this@MainActivity, R.string.logs_saved, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, R.string.logs_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val senderOrd = intent?.getIntExtra(SENDER, -1) ?: -1
            val sender = Sender.entries.getOrNull(senderOrd)

            when (intent?.action) {
                STARTED_BROADCAST, STOPPED_BROADCAST -> {
                    // statusFlow is updated automatically by services
                }

                FAILED_BROADCAST -> {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.failed_to_start, sender?.senderName ?: "Service"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = NetVeilPreferences(this)

        val intentFilter = IntentFilter().apply {
            addAction(STARTED_BROADCAST)
            addAction(STOPPED_BROADCAST)
            addAction(FAILED_BROADCAST)
            addAction(LEGACY_STARTED_BROADCAST)
            addAction(LEGACY_STOPPED_BROADCAST)
            addAction(LEGACY_FAILED_BROADCAST)
        }

        ContextCompat.registerReceiver(
            this,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val themeMode by preferences.themeFlow.collectAsState()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.System -> isSystemInDarkTheme()
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
            }

            val connectionState by statusFlow.collectAsState()
            var currentScreen by remember { mutableStateOf(AppScreen.Main) }

            NetVeilTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        AppScreen.Main -> MainScreen(
                            preferences = preferences,
                            connectionState = connectionState,
                            onToggleConnection = { handleToggleConnection() },
                            onNavigateToSettings = {
                                val (status) = appStatus
                                if (status == AppStatus.Halted) {
                                    currentScreen = AppScreen.Settings
                                } else {
                                    Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onExportLogs = {
                                saveLogsLauncher.launch("netveil_logcat.txt")
                            }
                        )

                        AppScreen.Settings -> SettingsScreen(
                            preferences = preferences,
                            connectionState = connectionState,
                            onNavigateBack = { currentScreen = AppScreen.Main },
                            onNavigateToUiConfig = { currentScreen = AppScreen.UiConfig },
                            onNavigateToCmdConfig = { currentScreen = AppScreen.CmdConfig }
                        )

                        AppScreen.UiConfig -> UiConfigScreen(
                            preferences = preferences,
                            connectionState = connectionState,
                            onNavigateBack = { currentScreen = AppScreen.Settings }
                        )

                        AppScreen.CmdConfig -> CmdConfigScreen(
                            preferences = preferences,
                            connectionState = connectionState,
                            onNavigateBack = { currentScreen = AppScreen.Settings }
                        )
                    }
                }
            }
        }
    }

    private fun handleToggleConnection() {
        val (status) = appStatus
        when (status) {
            AppStatus.Halted -> startService()
            AppStatus.Running -> stopService()
        }
    }

    private fun startService() {
        when (preferences.mode) {
            Mode.VPN -> {
                val prepareIntent = VpnService.prepare(this)
                if (prepareIntent != null) {
                    vpnLauncher.launch(prepareIntent)
                } else {
                    ServiceManager.start(this, Mode.VPN)
                }
            }

            Mode.Proxy -> {
                ServiceManager.start(this, Mode.Proxy)
            }
        }
    }

    private fun stopService() {
        ServiceManager.stop(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.w(TAG, "Receiver unregister error", e)
        }
    }
}
