package com.samyak.netveil.service

import com.samyak.netveil.model.AppStatus
import com.samyak.netveil.model.Mode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TrafficStats(
    val txPackets: Long = 0L,
    val txBytes: Long = 0L,
    val rxPackets: Long = 0L,
    val rxBytes: Long = 0L,
)

data class ConnectionState(
    val status: AppStatus = AppStatus.Halted,
    val mode: Mode = Mode.VPN,
    val errorMessage: String? = null,
    val stats: TrafficStats = TrafficStats()
)

private val _statusFlow = MutableStateFlow(ConnectionState())
val statusFlow: StateFlow<ConnectionState> = _statusFlow.asStateFlow()

var appStatus: Pair<AppStatus, Mode>
    get() = _statusFlow.value.status to _statusFlow.value.mode
    private set(value) {
        _statusFlow.value = _statusFlow.value.copy(status = value.first, mode = value.second)
    }

fun setStatus(status: AppStatus, mode: Mode, error: String? = null) {
    _statusFlow.value = ConnectionState(
        status = status,
        mode = mode,
        errorMessage = error,
        stats = if (status == AppStatus.Halted) TrafficStats() else _statusFlow.value.stats
    )
}

fun updateTrafficStats(stats: TrafficStats) {
    if (_statusFlow.value.status == AppStatus.Running) {
        _statusFlow.value = _statusFlow.value.copy(stats = stats)
    }
}

