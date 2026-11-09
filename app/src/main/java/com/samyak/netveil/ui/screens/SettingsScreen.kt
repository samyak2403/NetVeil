package com.samyak.netveil.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.samyak.netveil.BuildConfig
import com.samyak.netveil.R
import com.samyak.netveil.data.NetVeilPreferences
import com.samyak.netveil.model.AppStatus
import com.samyak.netveil.model.AppThemeMode
import com.samyak.netveil.model.Mode
import com.samyak.netveil.service.ConnectionState
import com.samyak.netveil.ui.components.*
import com.samyak.netveil.util.checkNotLocalIp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: NetVeilPreferences,
    connectionState: ConnectionState,
    onNavigateBack: () -> Unit,
    onNavigateToUiConfig: () -> Unit,
    onNavigateToCmdConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isRunning = connectionState.status == AppStatus.Running

    var showResetDialog by remember { mutableStateOf(false) }

    var theme by remember { mutableStateOf(preferences.appTheme) }
    var currentMode by remember { mutableStateOf(preferences.mode) }
    var dnsIp by remember { mutableStateOf(preferences.dnsIp) }
    var ipv6Enable by remember { mutableStateOf(preferences.ipv6Enable) }
    var enableCmdSettings by remember { mutableStateOf(preferences.enableCmdSettings) }


    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.title_settings), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (!isRunning) {
                                    showResetDialog = true
                                } else {
                                    Toast.makeText(context, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(R.string.reset_settings))
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // General Settings Card
            ScreenCard(
                title = stringResource(R.string.general_category),
                icon = Icons.Default.DisplaySettings,
                subtitle = "Theme, mode, and network routing"
            ) {
                SettingDropdownRow(
                    title = stringResource(R.string.theme_settings),
                    selectedValue = theme,
                    options = AppThemeMode.entries,
                    onValueChange = {
                        theme = it
                        preferences.appTheme = it
                    },
                    labelProvider = { it.displayName }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingDropdownRow(
                    title = stringResource(R.string.mode_setting),
                    selectedValue = currentMode,
                    options = Mode.entries,
                    onValueChange = {
                        if (!isRunning) {
                            currentMode = it
                            preferences.mode = it
                        } else {
                            Toast.makeText(context, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
                        }
                    },
                    labelProvider = { it.title },
                    enabled = !isRunning
                )

                if (currentMode == Mode.VPN) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingTextRow(
                        title = stringResource(R.string.dbs_ip_setting),
                        value = dnsIp,
                        subtitle = "DNS server used for VPN queries (e.g. 1.1.1.1 or 8.8.8.8)",
                        onValueSaved = {
                            dnsIp = it
                            preferences.dnsIp = it
                        },
                        validator = { it.isBlank() || checkNotLocalIp(it) },
                        errorMessage = "Invalid IP address or local address",
                        keyboardType = KeyboardType.Number,
                        enabled = !isRunning
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingSwitchRow(
                        title = stringResource(R.string.ipv6_setting),
                        subtitle = "Enable IPv6 VPN routes",
                        checked = ipv6Enable,
                        onCheckedChange = {
                            if (!isRunning) {
                                ipv6Enable = it
                                preferences.ipv6Enable = it
                            } else {
                                Toast.makeText(context, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isRunning
                    )
                }
            }

            // NetVeil Configuration Card Mode
            ScreenCard(
                title = stringResource(R.string.byedpi_category),
                icon = Icons.Default.Security,
                subtitle = "Select configuration method and tune NetVeil engine options"
            ) {
                SettingSwitchRow(
                    title = stringResource(R.string.use_command_line_settings),
                    subtitle = "Switch between structured card controls and raw NetVeil command line flags",
                    checked = enableCmdSettings,
                    onCheckedChange = {
                        if (!isRunning) {
                            enableCmdSettings = it
                            preferences.enableCmdSettings = it
                        } else {
                            Toast.makeText(context, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isRunning
                )
            }

            // Navigation to UI Card Editor
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(enabled = !enableCmdSettings) { onNavigateToUiConfig() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!enableCmdSettings) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.padding(end = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DashboardCustomize,
                                contentDescription = null,
                                tint = if (!enableCmdSettings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.ui_editor),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (!enableCmdSettings) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Card-based parameters for Split, Disorder, Fake SNI, TTL & protocols",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (!enableCmdSettings) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (!enableCmdSettings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Navigation to Command Line Editor
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(enabled = enableCmdSettings) { onNavigateToCmdConfig() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (enableCmdSettings) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.padding(end = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = if (enableCmdSettings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.command_line_editor),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (enableCmdSettings) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Raw command line arguments passed directly to NetVeil engine",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (enableCmdSettings) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (enableCmdSettings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // About Card
            ScreenCard(
                title = stringResource(R.string.about_category),
                icon = Icons.Default.Info,
                subtitle = "App information and documentation"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.version),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/samyak2403/NetVeil/blob/v0.13/README.md"))
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.byedpi_readme_link),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/samyak2403/NetVeil"))
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.source_code_link),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text(stringResource(R.string.reset_settings), fontWeight = FontWeight.SemiBold) },
            text = { Text("Are you sure you want to reset all NetVeil settings back to their default values?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        preferences.resetToDefaults()
                        theme = preferences.appTheme
                        currentMode = preferences.mode
                        dnsIp = preferences.dnsIp
                        ipv6Enable = preferences.ipv6Enable
                        enableCmdSettings = preferences.enableCmdSettings
                        showResetDialog = false
                        Toast.makeText(context, "Settings reset to default", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

