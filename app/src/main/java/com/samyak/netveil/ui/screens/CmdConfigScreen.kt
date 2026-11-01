package com.samyak.netveil.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samyak.netveil.R
import com.samyak.netveil.data.NetVeilPreferences
import com.samyak.netveil.model.AppStatus
import com.samyak.netveil.service.ConnectionState
import com.samyak.netveil.ui.components.ScreenCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmdConfigScreen(
    preferences: NetVeilPreferences,
    connectionState: ConnectionState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isRunning = connectionState.status == AppStatus.Running

    var cmdText by remember { mutableStateOf(preferences.cmdArgs) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.command_line_editor), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/samyak2403/NetVeil/blob/v0.13/README.md"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = "Docs")
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
            Spacer(modifier = Modifier.height(8.dp))

            // Presets & Guide Card
            ScreenCard(
                title = "Popular Presets",
                icon = Icons.Default.Terminal,
                subtitle = "Tap a preset to populate command line arguments"
            ) {
                val presets = listOf(
                    "Standard Disorder" to "--disorder 1 --proto http,https",
                    "Split at Host/SNI" to "--split 1+s --proto http,https",
                    "Fake Packet + TTL" to "--fake -1 --ttl 8 --proto https",
                    "Discord / YouTube" to "-s 1 -d 3 -t 8 -o 1 -K t,h",
                    "OOB Urgent Byte" to "--oob 1 -e a --proto http,https"
                )

                presets.forEach { (name, args) ->
                    AssistChip(
                        onClick = {
                            if (!isRunning) {
                                cmdText = args
                                preferences.cmdArgs = args
                                Toast.makeText(context, "Loaded preset: $name", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = { Text(name, fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.padding(vertical = 2.dp),
                        enabled = !isRunning
                    )
                }
            }

            // Command Line Arguments Input Card
            ScreenCard(
                title = stringResource(R.string.command_line_arguments),
                icon = Icons.Default.Terminal,
                subtitle = "Passed directly as argv to NetVeil engine"
            ) {
                OutlinedTextField(
                    value = cmdText,
                    onValueChange = {
                        if (!isRunning) {
                            cmdText = it
                            preferences.cmdArgs = it
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("e.g. --split 1 --disorder 3 --proto https") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    enabled = !isRunning
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            if (!isRunning) {
                                cmdText = ""
                                preferences.cmdArgs = ""
                            }
                        },
                        enabled = !isRunning && cmdText.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

