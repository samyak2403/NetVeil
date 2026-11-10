package com.samyak.netveil.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samyak.netveil.R
import com.samyak.netveil.data.NetVeilPreferences
import com.samyak.netveil.model.AppStatus
import com.samyak.netveil.model.Mode
import com.samyak.netveil.service.ConnectionState
import com.samyak.netveil.ui.theme.*

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
    var showMenu by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val isRunning = connectionState.status == AppStatus.Running
    val currentMode by preferences.modeFlow.collectAsState()
    val proxyIp = preferences.proxyIp
    val proxyPort = preferences.proxyPort

    val bottomGlowAlpha by animateFloatAsState(
        targetValue = if (isRunning) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "BottomGlowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VeilDarkBg)
    ) {
        // Subtle ambient bottom glow when connected
        if (bottomGlowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer(alpha = bottomGlowAlpha)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                VeilBottomGlow.copy(alpha = 0.45f),
                                VeilBottomGlow.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Help button (?)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(VeilCircleBtn)
                        .clickable { showHelpDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        color = VeilHeaderIcon,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Center logo: 6-node network mesh icon


                // Menu button (Hamburger)
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = VeilHeaderIcon,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = Color(0xFF1C1E24)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.settings),
                                    color = Color.White
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = VeilHeaderIcon
                                )
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToSettings()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.save_logs),
                                    color = Color.White
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Article,
                                    contentDescription = null,
                                    tint = VeilHeaderIcon
                                )
                            },
                            onClick = {
                                showMenu = false
                                onExportLogs()
                            }
                        )

                        if (currentMode == Mode.Proxy) {
                            HorizontalDivider(
                                color = Color(0xFF2C2E36),
                                thickness = 1.dp
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Proxy: $proxyIp:$proxyPort",
                                        color = VeilCoralPink,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val address = "$proxyIp:$proxyPort"
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Proxy Address", address))
                                    Toast.makeText(context, "Copied $address", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Brand Title: VeilVPN Pro
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "VeilVPN",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pro",
                    color = VeilCoralPink,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.weight(1.3f))

            // Main Power Toggle Switch with Concentric Glow Rings
            VeilPowerSwitch(
                isConnected = isRunning,
                onToggle = onToggleConnection
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Connection Status Title
            Text(
                text = if (isRunning) "Connected" else "Disconnected",
                color = if (isRunning) VeilCoralPink else Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Privacy Subtitle
            val subtitleText = when {
                currentMode == Mode.Proxy && isRunning -> "SOCKS5 proxy active on $proxyIp:$proxyPort"
                currentMode == Mode.Proxy && !isRunning -> "SOCKS5 proxy server is stopped"
                isRunning -> "Your DNS queries are private and faster"
                else -> "Your DNS queries are not private"
            }

            Text(
                text = subtitleText,
                color = if (isRunning) VeilTextSubtle else VeilTextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            // Optional clickable proxy badge in proxy mode
            if (currentMode == Mode.Proxy) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1A1B22),
                    border = BorderStroke(1.dp, Color(0xFF282A33)),
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val address = "$proxyIp:$proxyPort"
                        clipboard.setPrimaryClip(ClipData.newPlainText("Proxy Address", address))
                        Toast.makeText(context, "Copied $address", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = "Proxy: $proxyIp:$proxyPort",
                        color = if (isRunning) VeilCoralPink else VeilTextSubtle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.9f))
        }

        // Help / About Dialog
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NetworkMeshLogo(modifier = Modifier.size(24.dp), tint = VeilCoralPink)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "VeilVPN Pro",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isRunning) {
                                "✓ Secure Connection Active\nYour internet traffic and DNS queries are obfuscated, bypassing DPI censorship and tracking."
                            } else {
                                "⚠ Connection Not Protected\nYour internet traffic and DNS queries are currently visible to local network operators and ISPs."
                            },
                            color = if (isRunning) VeilCoralPink else VeilTextSubtle,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        HorizontalDivider(color = Color(0xFF2C2E36))
                        Text(
                            text = "Engine: NetVeil v1.0.2\nMode: ${currentMode.name}",
                            color = VeilTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("Got it", color = VeilCoralPink)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xFF1A1C22)
            )
        }
    }
}

/**
 * 6-node network mesh icon matching the top-center logo.
 */
@Composable
fun NetworkMeshLogo(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val nodeRadius = w * 0.11f
        val strokeWidth = nodeRadius * 2f

        // 6 interconnected node positions
        val p1 = Offset(w * 0.40f, h * 0.22f)
        val p2 = Offset(w * 0.68f, h * 0.22f)
        val p3 = Offset(w * 0.32f, h * 0.50f)
        val p4 = Offset(w * 0.60f, h * 0.50f)
        val p5 = Offset(w * 0.40f, h * 0.78f)
        val p6 = Offset(w * 0.68f, h * 0.78f)

        // Draw connecting bridges
        drawLine(
            color = tint,
            start = p1,
            end = p2,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = p3,
            end = p4,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = p5,
            end = p6,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = p1,
            end = p3,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = p4,
            end = p6,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw circular nodes
        val nodes = listOf(p1, p2, p3, p4, p5, p6)
        nodes.forEach { pt ->
            drawCircle(
                color = tint,
                radius = nodeRadius,
                center = pt
            )
        }
    }
}

/**
 * Custom large power toggle switch with concentric outer glowing rings when connected.
 */
@Composable
fun VeilPowerSwitch(
    isConnected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val switchWidth = 136.dp
    val switchHeight = 74.dp
    val thumbWidth = 36.dp
    val thumbHeight = 58.dp
    val padding = 8.dp

    // Smooth thumb animation between left and right
    val maxOffset = switchWidth - thumbWidth - padding
    val thumbOffset by animateDpAsState(
        targetValue = if (isConnected) maxOffset else padding,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 400f
        ),
        label = "ThumbOffset"
    )

    // Pulse animation for outer rings when connected
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val connectedAlpha by animateFloatAsState(
        targetValue = if (isConnected) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "ConnectedAlpha"
    )

    Box(
        modifier = modifier
            .size(210.dp, 150.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer concentric rings when connected
        if (connectedAlpha > 0f) {
            // Ring 3 (outermost)
            Box(
                modifier = Modifier
                    .size(202.dp, 140.dp)
                    .border(
                        width = 1.5.dp,
                        color = VeilCoralPink.copy(alpha = 0.10f * connectedAlpha * pulseAlpha),
                        shape = RoundedCornerShape(50.dp)
                    )
            )

            // Ring 2 (middle)
            Box(
                modifier = Modifier
                    .size(174.dp, 114.dp)
                    .border(
                        width = 1.5.dp,
                        color = VeilCoralPink.copy(alpha = 0.22f * connectedAlpha * pulseAlpha),
                        shape = RoundedCornerShape(40.dp)
                    )
            )

            // Ring 1 (innermost)
            Box(
                modifier = Modifier
                    .size(150.dp, 90.dp)
                    .border(
                        width = 1.5.dp,
                        color = VeilCoralPink.copy(alpha = 0.38f * connectedAlpha * pulseAlpha),
                        shape = RoundedCornerShape(32.dp)
                    )
            )
        }

        // Switch Track
        Box(
            modifier = Modifier
                .size(switchWidth, switchHeight)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    if (isConnected) {
                        Brush.horizontalGradient(
                            colors = listOf(VeilPeach, VeilRose)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(VeilTrackDark, Color(0xFF181A1E))
                        )
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = if (isConnected) Color.Transparent else VeilTrackBorder,
                    shape = RoundedCornerShape(26.dp)
                )
        ) {
            // Switch Thumb (Pill shape)
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .align(Alignment.CenterStart)
                    .size(thumbWidth, thumbHeight)
                    .shadow(
                        elevation = if (isConnected) 6.dp else 2.dp,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isConnected) Color.White else VeilThumbDark)
                    .border(
                        width = if (isConnected) 0.dp else 1.dp,
                        color = if (isConnected) Color.Transparent else Color(0xFF3C3E47),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }
    }
}
