package com.samyak.netveil.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.samyak.netveil.R
import com.samyak.netveil.data.NetVeilPreferences
import com.samyak.netveil.model.AppStatus
import com.samyak.netveil.model.DesyncMethod
import com.samyak.netveil.model.HostsMode
import com.samyak.netveil.service.ConnectionState
import com.samyak.netveil.ui.components.*
import com.samyak.netveil.util.checkIntRange
import com.samyak.netveil.util.checkIp
import com.samyak.netveil.util.checkPort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiConfigScreen(
    preferences: NetVeilPreferences,
    connectionState: ConnectionState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isRunning = connectionState.status == AppStatus.Running

    // State bindings
    var proxyIp by remember { mutableStateOf(preferences.proxyIp) }
    var proxyPort by remember { mutableStateOf(preferences.proxyPort) }
    var maxConnections by remember { mutableStateOf(preferences.maxConnections) }
    var bufferSize by remember { mutableStateOf(preferences.bufferSize) }
    var defaultTtl by remember { mutableStateOf(preferences.defaultTtl) }
    var noDomain by remember { mutableStateOf(preferences.noDomain) }
    var tcpFastOpen by remember { mutableStateOf(preferences.tcpFastOpen) }

    var hostsMode by remember { mutableStateOf(preferences.hostsMode) }
    var hostsBlacklist by remember { mutableStateOf(preferences.hostsBlacklist) }
    var hostsWhitelist by remember { mutableStateOf(preferences.hostsWhitelist) }
    var desyncMethod by remember { mutableStateOf(preferences.desyncMethod) }
    var splitPosition by remember { mutableStateOf(preferences.splitPosition) }
    var splitAtHost by remember { mutableStateOf(preferences.splitAtHost) }
    var dropSack by remember { mutableStateOf(preferences.dropSack) }

    var fakeTtl by remember { mutableStateOf(preferences.fakeTtl) }
    var fakeOffset by remember { mutableStateOf(preferences.fakeOffset) }
    var fakeSni by remember { mutableStateOf(preferences.fakeSni) }
    var oobChar by remember { mutableStateOf(preferences.oobChar) }

    var desyncHttp by remember { mutableStateOf(preferences.desyncHttp) }
    var desyncHttps by remember { mutableStateOf(preferences.desyncHttps) }
    var desyncUdp by remember { mutableStateOf(preferences.desyncUdp) }

    var hostMixedCase by remember { mutableStateOf(preferences.hostMixedCase) }
    var domainMixedCase by remember { mutableStateOf(preferences.domainMixedCase) }
    var hostRemoveSpaces by remember { mutableStateOf(preferences.hostRemoveSpaces) }

    var tlsRecordSplit by remember { mutableStateOf(preferences.tlsRecordSplit) }
    var tlsRecordSplitPosition by remember { mutableStateOf(preferences.tlsRecordSplitPosition) }
    var tlsRecordSplitAtSni by remember { mutableStateOf(preferences.tlsRecordSplitAtSni) }

    var udpFakeCount by remember { mutableStateOf(preferences.udpFakeCount) }

    // Logic constraints mirroring ByeDpiUISettingsFragment
    val desyncEnabled = desyncMethod != DesyncMethod.None
    val isFake = desyncMethod == DesyncMethod.Fake
    val isOob = desyncMethod == DesyncMethod.OOB || desyncMethod == DesyncMethod.DISOOB
    val desyncAllProtocols = !desyncHttp && !desyncHttps && !desyncUdp

    val desyncHttpEnabled = !isRunning && (desyncAllProtocols || desyncHttp)
    val desyncHttpsEnabled = !isRunning && (desyncAllProtocols || desyncHttps)
    val desyncUdpEnabled = !isRunning && (desyncAllProtocols || desyncUdp)
    val tlsRecEnabled = desyncHttpsEnabled && tlsRecordSplit

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.ui_editor), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hufrea/byedpi/blob/v0.13/README.md"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = "Help")
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

            // CARD 1: Proxy Network Settings
            ScreenCard(
                title = stringResource(R.string.byedpi_proxy),
                icon = Icons.Default.Router,
                subtitle = "SOCKS5 proxy address, ports, and connection tuning"
            ) {
                SettingTextRow(
                    title = stringResource(R.string.bye_dpi_proxy_ip_setting),
                    value = proxyIp,
                    subtitle = "Proxy bind address (default 127.0.0.1)",
                    onValueSaved = {
                        proxyIp = it
                        preferences.proxyIp = it
                    },
                    validator = { checkIp(it) },
                    errorMessage = "Invalid IP format",
                    enabled = !isRunning
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingTextRow(
                    title = stringResource(R.string.byedpi_proxy_port_setting),
                    value = proxyPort,
                    subtitle = "Port number (1 - 65535, default 1080)",
                    onValueSaved = {
                        proxyPort = it
                        preferences.proxyPort = it
                    },
                    validator = { checkPort(it) },
                    errorMessage = "Port must be between 1 and 65535",
                    keyboardType = KeyboardType.Number,
                    enabled = !isRunning
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingTextRow(
                    title = stringResource(R.string.byedpi_max_connections_setting),
                    value = maxConnections,
                    subtitle = "Max simultaneous connections (1 - 32767, default 512)",
                    onValueSaved = {
                        maxConnections = it
                        preferences.maxConnections = it
                    },
                    validator = { checkIntRange(it, 1, 32767) },
                    errorMessage = "Must be between 1 and 32767",
                    keyboardType = KeyboardType.Number,
                    enabled = !isRunning
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingTextRow(
                    title = stringResource(R.string.byedpi_buffer_size_setting),
                    value = bufferSize,
                    subtitle = "Buffer size in bytes (default 16384)",
                    onValueSaved = {
                        bufferSize = it
                        preferences.bufferSize = it
                    },
                    validator = { checkIntRange(it, 1, Int.MAX_VALUE / 4) },
                    errorMessage = "Buffer size too large or invalid",
                    keyboardType = KeyboardType.Number,
                    enabled = !isRunning
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingTextRow(
                    title = stringResource(R.string.byedpi_default_ttl_setting),
                    value = defaultTtl,
                    subtitle = "Default TTL for outbound packets (0 = system default)",
                    onValueSaved = {
                        defaultTtl = it
                        preferences.defaultTtl = it
                    },
                    validator = { checkIntRange(it, 0, 255) },
                    errorMessage = "TTL must be between 0 and 255",
                    keyboardType = KeyboardType.Number,
                    enabled = !isRunning
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingSwitchRow(
                    title = stringResource(R.string.byedpi_no_domain_setting),
                    subtitle = "Do not resolve domain names on the proxy client",
                    checked = noDomain,
                    onCheckedChange = {
                        noDomain = it
                        preferences.noDomain = it
                    },
                    enabled = !isRunning
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingSwitchRow(
                    title = stringResource(R.string.byedpi_tcp_fast_open_setting),
                    subtitle = "Enable TCP Fast Open (TFO)",
                    checked = tcpFastOpen,
                    onCheckedChange = {
                        tcpFastOpen = it
                        preferences.tcpFastOpen = it
                    },
                    enabled = !isRunning
                )
            }

            // CARD 2: Desync Strategy
            ScreenCard(
                title = stringResource(R.string.byedpi_desync),
                icon = Icons.AutoMirrored.Filled.AltRoute,
                subtitle = "Methods to fragment and disguise traffic from Deep Packet Inspection"
            ) {
                SettingDropdownRow(
                    title = stringResource(R.string.byedpi_hosts_mode_setting),
                    selectedValue = hostsMode,
                    options = HostsMode.entries,
                    onValueChange = {
                        hostsMode = it
                        preferences.hostsMode = it
                    },
                    labelProvider = { it.displayName },
                    enabled = !isRunning
                )

                if (hostsMode == HostsMode.Blacklist) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingTextRow(
                        title = stringResource(R.string.byedpi_hosts_blacklist_setting),
                        value = hostsBlacklist,
                        subtitle = "Apply desync only to domains matching this list",
                        onValueSaved = {
                            hostsBlacklist = it
                            preferences.hostsBlacklist = it
                        },
                        enabled = !isRunning
                    )
                } else if (hostsMode == HostsMode.Whitelist) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingTextRow(
                        title = stringResource(R.string.byedpi_hosts_whitelist_setting),
                        value = hostsWhitelist,
                        subtitle = "Bypass desync for domains matching this list",
                        onValueSaved = {
                            hostsWhitelist = it
                            preferences.hostsWhitelist = it
                        },
                        enabled = !isRunning
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingDropdownRow(
                    title = stringResource(R.string.byedpi_desync_method_setting),
                    selectedValue = desyncMethod,
                    options = DesyncMethod.entries,
                    onValueChange = {
                        desyncMethod = it
                        preferences.desyncMethod = it
                    },
                    labelProvider = { it.displayName },
                    enabled = !isRunning
                )

                if (desyncEnabled) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingTextRow(
                        title = stringResource(R.string.byedpi_split_position_setting),
                        value = splitPosition,
                        subtitle = "Offset byte to split data packet (default 1)",
                        onValueSaved = {
                            splitPosition = it
                            preferences.splitPosition = it
                        },
                        validator = { it.toIntOrNull() != null },
                        errorMessage = "Must be an integer",
                        keyboardType = KeyboardType.Number,
                        enabled = !isRunning
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingSwitchRow(
                        title = stringResource(R.string.byedpi_split_at_host_setting),
                        subtitle = "Offset split relative to Host header or SNI",
                        checked = splitAtHost,
                        onCheckedChange = {
                            splitAtHost = it
                            preferences.splitAtHost = it
                        },
                        enabled = !isRunning
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingSwitchRow(
                    title = stringResource(R.string.byedpi_drop_sack_setting),
                    subtitle = "Filter out Selective Acknowledgment (SACK) option",
                    checked = dropSack,
                    onCheckedChange = {
                        dropSack = it
                        preferences.dropSack = it
                    },
                    enabled = !isRunning
                )
            }

            // CARD 3: Fake & Out-of-band Settings
            if (isFake || isOob) {
                ScreenCard(
                    title = if (isFake) "Fake Packet Injection" else "Out-of-band (OOB) Data",
                    icon = Icons.Default.Flaky,
                    subtitle = "Configure deceptive packets or OOB flags"
                ) {
                    if (isFake) {
                        SettingTextRow(
                            title = stringResource(R.string.byedpi_fake_ttl_setting),
                            value = fakeTtl,
                            subtitle = "TTL for fake packets (1 - 255, default 8)",
                            onValueSaved = {
                                fakeTtl = it
                                preferences.fakeTtl = it
                            },
                            validator = { checkIntRange(it, 1, 255) },
                            errorMessage = "TTL must be between 1 and 255",
                            keyboardType = KeyboardType.Number,
                            enabled = !isRunning
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        SettingTextRow(
                            title = stringResource(R.string.sni_of_fake_packet),
                            value = fakeSni,
                            subtitle = "SNI domain inserted into fake ClientHello",
                            onValueSaved = {
                                fakeSni = it
                                preferences.fakeSni = it
                            },
                            enabled = !isRunning
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        SettingTextRow(
                            title = stringResource(R.string.byedpi_fake_offset_setting),
                            value = fakeOffset,
                            subtitle = "Byte offset in fake packet",
                            onValueSaved = {
                                fakeOffset = it
                                preferences.fakeOffset = it
                            },
                            validator = { it.toIntOrNull() != null },
                            keyboardType = KeyboardType.Number,
                            enabled = !isRunning
                        )
                    }

                    if (isOob) {
                        if (isFake) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }

                        SettingTextRow(
                            title = stringResource(R.string.oob_data),
                            value = oobChar,
                            subtitle = "Single byte character for out-of-band urgent data",
                            onValueSaved = {
                                val char = if (it.isNotEmpty()) it.substring(0, 1) else "a"
                                oobChar = char
                                preferences.oobChar = char
                            },
                            validator = { it.isNotBlank() },
                            errorMessage = "Must not be empty",
                            enabled = !isRunning
                        )
                    }
                }
            }

            // CARD 4: Target Protocols
            ScreenCard(
                title = stringResource(R.string.byedpi_protocols_category),
                icon = Icons.Default.FilterAlt,
                subtitle = stringResource(R.string.byedpi_protocols_hint)
            ) {
                SettingSwitchRow(
                    title = stringResource(R.string.desync_http),
                    subtitle = "Apply desync techniques to unencrypted HTTP (port 80)",
                    checked = desyncHttp,
                    onCheckedChange = {
                        desyncHttp = it
                        preferences.desyncHttp = it
                    },
                    enabled = !isRunning
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingSwitchRow(
                    title = stringResource(R.string.desync_https),
                    subtitle = "Apply desync techniques to TLS/HTTPS (port 443)",
                    checked = desyncHttps,
                    onCheckedChange = {
                        desyncHttps = it
                        preferences.desyncHttps = it
                    },
                    enabled = !isRunning
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingSwitchRow(
                    title = stringResource(R.string.desync_udp),
                    subtitle = "Apply fake packet desync to UDP traffic (e.g. QUIC/HTTP3)",
                    checked = desyncUdp,
                    onCheckedChange = {
                        desyncUdp = it
                        preferences.desyncUdp = it
                    },
                    enabled = !isRunning
                )
            }

            // CARD 5: HTTP Modifiers
            ScreenCard(
                title = stringResource(R.string.desync_http_category),
                icon = Icons.Default.Http,
                subtitle = "HTTP header line manipulations (active for HTTP)"
            ) {
                SettingSwitchRow(
                    title = stringResource(R.string.byedpi_host_mixed_case_setting),
                    subtitle = "Randomize case of Host header (e.g. hOsT: ...)",
                    checked = hostMixedCase,
                    onCheckedChange = {
                        hostMixedCase = it
                        preferences.hostMixedCase = it
                    },
                    enabled = desyncHttpEnabled
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingSwitchRow(
                    title = stringResource(R.string.byedpi_domain_mixed_case_setting),
                    subtitle = "Randomize domain name case (e.g. eXaMpLe.CoM)",
                    checked = domainMixedCase,
                    onCheckedChange = {
                        domainMixedCase = it
                        preferences.domainMixedCase = it
                    },
                    enabled = desyncHttpEnabled
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                SettingSwitchRow(
                    title = stringResource(R.string.byedpi_host_remove_spaces_setting),
                    subtitle = "Remove space after Host header colon (Host:example.com)",
                    checked = hostRemoveSpaces,
                    onCheckedChange = {
                        hostRemoveSpaces = it
                        preferences.hostRemoveSpaces = it
                    },
                    enabled = desyncHttpEnabled
                )
            }

            // CARD 6: HTTPS TLS Record Split
            ScreenCard(
                title = stringResource(R.string.desync_https_category),
                icon = Icons.Default.Lock,
                subtitle = "Split TLS ClientHello record across multiple fragments"
            ) {
                SettingSwitchRow(
                    title = stringResource(R.string.byedpi_tlsrec_enabled_setting),
                    subtitle = "Split TLS record layer",
                    checked = tlsRecordSplit,
                    onCheckedChange = {
                        tlsRecordSplit = it
                        preferences.tlsRecordSplit = it
                    },
                    enabled = desyncHttpsEnabled
                )

                if (tlsRecordSplit) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingTextRow(
                        title = stringResource(R.string.byedpi_tlsrec_position_setting),
                        value = tlsRecordSplitPosition,
                        subtitle = "Offset in TLS record to place record split",
                        onValueSaved = {
                            tlsRecordSplitPosition = it
                            preferences.tlsRecordSplitPosition = it
                        },
                        validator = { checkIntRange(it, -65535, 65535) },
                        errorMessage = "Must be between -65535 and 65535",
                        keyboardType = KeyboardType.Number,
                        enabled = tlsRecEnabled
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingSwitchRow(
                        title = stringResource(R.string.byedpi_tlsrec_at_sni_setting),
                        subtitle = "Offset TLS record split position relative to SNI",
                        checked = tlsRecordSplitAtSni,
                        onCheckedChange = {
                            tlsRecordSplitAtSni = it
                            preferences.tlsRecordSplitAtSni = it
                        },
                        enabled = tlsRecEnabled
                    )
                }
            }

            // CARD 7: UDP Desync
            ScreenCard(
                title = stringResource(R.string.desync_udp_category),
                icon = Icons.Default.Speed,
                subtitle = "UDP & QUIC packet obfuscation"
            ) {
                SettingTextRow(
                    title = stringResource(R.string.byedpi_udp_fake_count),
                    value = udpFakeCount,
                    subtitle = "Number of fake UDP packets to inject",
                    onValueSaved = {
                        udpFakeCount = it
                        preferences.udpFakeCount = it
                    },
                    validator = { checkIntRange(it, 0, 1000) },
                    errorMessage = "Must be a positive integer",
                    keyboardType = KeyboardType.Number,
                    enabled = desyncUdpEnabled
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

