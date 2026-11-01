package com.samyak.netveil.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.samyak.netveil.MainActivity
import com.samyak.netveil.R
import com.samyak.netveil.data.*
import com.samyak.netveil.model.AppStatus
import com.samyak.netveil.model.Mode
import com.samyak.netveil.model.Sender

@RequiresApi(Build.VERSION_CODES.N)
class QuickTileService : TileService() {

    companion object {
        private val TAG: String = QuickTileService::class.java.simpleName
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val senderOrd = intent.getIntExtra(SENDER, -1)
            val sender = Sender.entries.getOrNull(senderOrd)

            when (intent.action) {
                STARTED_BROADCAST,
                STOPPED_BROADCAST -> updateStatus()

                FAILED_BROADCAST -> {
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_start, sender?.senderName ?: "Service"),
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateStatus()
                }

                else -> Log.w(TAG, "Unknown action: ${intent.action}")
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateStatus()
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter().apply {
                addAction(STARTED_BROADCAST)
                addAction(STOPPED_BROADCAST)
                addAction(FAILED_BROADCAST)
                addAction(LEGACY_STARTED_BROADCAST)
                addAction(LEGACY_STOPPED_BROADCAST)
                addAction(LEGACY_FAILED_BROADCAST)
            },
            ContextCompat.RECEIVER_EXPORTED,
        )
    }

    override fun onStopListening() {
        super.onStopListening()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.w(TAG, "Receiver not registered", e)
        }
    }

    private fun launchActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        if (tile.state == Tile.STATE_UNAVAILABLE) {
            return
        }

        unlockAndRun {
            handleClick()
        }
    }

    private fun setState(newState: Int) {
        val tile = qsTile ?: return
        tile.state = newState
        tile.updateTile()
    }

    private fun updateStatus() {
        val (status) = appStatus
        setState(if (status == AppStatus.Halted) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE)
    }

    private fun handleClick() {
        setState(Tile.STATE_UNAVAILABLE)

        val (status) = appStatus
        when (status) {
            AppStatus.Halted -> {
                val mode = NetVeilPreferences(this).mode
                if (mode == Mode.VPN && VpnService.prepare(this) != null) {
                    updateStatus()
                    launchActivity()
                    return
                }

                ServiceManager.start(this, mode)
            }

            AppStatus.Running -> ServiceManager.stop(this)
        }
    }
}

