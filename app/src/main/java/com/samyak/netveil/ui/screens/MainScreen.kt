package com.samyak.netveil.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samyak.netveil.R
import com.samyak.netveil.data.NetVeilPreferences
import com.samyak.netveil.model.AppStatus
import com.samyak.netveil.model.Mode
import com.samyak.netveil.service.ConnectionState
import com.samyak.netveil.ui.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    preferences: NetVeilPreferences,
    connectionState: ConnectionState,
    onToggleConnection: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onExportLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var showMenu by remember { mutableStateOf(false) }

    val isRunning = connectionState.status == AppStatus.Running
    val currentMode by preferences.modeFlow.collectAsState()
    val proxyIp = preferences.proxyIp
    val proxyPort = preferences.proxyPort

    val buttonTextRes = when (connectionState.status) {
        AppStatus.Halted -> when (currentMode) {
            Mode.VPN -> R.string.vpn_connect
            Mode.Proxy -> R.string.proxy_start
        }
        AppStatus.Running -> when (currentMode) {
            Mode.VPN -> R.string.vpn_disconnect
            Mode.Proxy -> R.string.proxy_stop
        }
    }

    val statusTextRes = when (connectionState.status) {
        AppStatus.Halted -> when (currentMode) {
            Mode.VPN -> R.string.vpn_disconnected
            Mode.Proxy -> R.string.proxy_down
        }
        AppStatus.Running -> when (currentMode) {
            Mode.VPN -> R.string.vpn_connected
            Mode.Proxy -> R.string.proxy_up
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings)
                            )
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.save_logs)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Article,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onExportLogs()
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = onToggleConnection,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFC4CBD4)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
                        contentColor = if (isRunning) Color(0xFFDC2626) else BrandBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 14.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    Text(
                        text = stringResource(buttonTextRes),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isRunning) Color(0xFFDC2626) else BrandBlue
                    )
                }

                Text(
                    text = stringResource(statusTextRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                val address = "$proxyIp:$proxyPort"
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Proxy Address", address))
                        Toast.makeText(context, "Copied $address", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

