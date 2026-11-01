package com.samyak.netveil.core

import android.content.SharedPreferences
import com.samyak.netveil.data.NetVeilPreferences
import com.samyak.netveil.model.DesyncMethod
import com.samyak.netveil.model.HostsMode
import com.samyak.netveil.util.shellSplit

sealed interface NetVeilProxyPreferences {
    companion object {
        fun fromPreferences(preferences: NetVeilPreferences): NetVeilProxyPreferences =
            if (preferences.enableCmdSettings) {
                NetVeilProxyCmdPreferences(preferences.cmdArgs)
            } else {
                NetVeilProxyUIPreferences(preferences)
            }

        fun fromSharedPreferences(prefs: SharedPreferences): NetVeilProxyPreferences {
            val isCmd = prefs.getBoolean(NetVeilPreferences.KEY_ENABLE_CMD_SETTINGS, false)
            return if (isCmd) {
                val cmd = prefs.getString(NetVeilPreferences.KEY_CMD_ARGS, "") ?: ""
                NetVeilProxyCmdPreferences(cmd)
            } else {
                NetVeilProxyUIPreferences(prefs)
            }
        }
    }
}

class NetVeilProxyCmdPreferences(val args: Array<String>) : NetVeilProxyPreferences {
    constructor(cmd: String) : this(cmdToArgs(cmd))

    companion object {
        private fun cmdToArgs(cmd: String): Array<String> {
            val firstArgIndex = cmd.indexOf("-")
            val argsStr = (if (firstArgIndex >= 0) cmd.substring(firstArgIndex) else cmd).trim()
            return (listOf("ciadpi") + shellSplit(argsStr)).toTypedArray()
        }
    }
}

class NetVeilProxyUIPreferences(
    ip: String? = null,
    port: Int? = null,
    maxConnections: Int? = null,
    bufferSize: Int? = null,
    defaultTtl: Int? = null,
    noDomain: Boolean? = null,
    desyncHttp: Boolean? = null,
    desyncHttps: Boolean? = null,
    desyncUdp: Boolean? = null,
    desyncMethod: DesyncMethod? = null,
    splitPosition: Int? = null,
    splitAtHost: Boolean? = null,
    fakeTtl: Int? = null,
    fakeSni: String? = null,
    oobChar: String? = null,
    hostMixedCase: Boolean? = null,
    domainMixedCase: Boolean? = null,
    hostRemoveSpaces: Boolean? = null,
    tlsRecordSplit: Boolean? = null,
    tlsRecordSplitPosition: Int? = null,
    tlsRecordSplitAtSni: Boolean? = null,
    hostsMode: HostsMode? = null,
    hosts: String? = null,
    tcpFastOpen: Boolean? = null,
    udpFakeCount: Int? = null,
    dropSack: Boolean? = null,
    fakeOffset: Int? = null,
) : NetVeilProxyPreferences {
    val ip: String = ip ?: "127.0.0.1"
    val port: Int = port ?: 1080
    val maxConnections: Int = maxConnections ?: 512
    val bufferSize: Int = bufferSize ?: 16384
    val defaultTtl: Int = defaultTtl ?: 0
    val customTtl: Boolean = defaultTtl != null && defaultTtl > 0
    val noDomain: Boolean = noDomain ?: false
    val desyncHttp: Boolean = desyncHttp ?: true
    val desyncHttps: Boolean = desyncHttps ?: true
    val desyncUdp: Boolean = desyncUdp ?: false
    val desyncMethod: DesyncMethod = desyncMethod ?: DesyncMethod.Disorder
    val splitPosition: Int = splitPosition ?: 1
    val splitAtHost: Boolean = splitAtHost ?: false
    val fakeTtl: Int = fakeTtl ?: 8
    val fakeSni: String = fakeSni ?: "www.iana.org"
    val oobChar: Byte = (oobChar?.ifEmpty { "a" } ?: "a")[0].code.toByte()
    val hostMixedCase: Boolean = hostMixedCase ?: false
    val domainMixedCase: Boolean = domainMixedCase ?: false
    val hostRemoveSpaces: Boolean = hostRemoveSpaces ?: false
    val tlsRecordSplit: Boolean = tlsRecordSplit ?: false
    val tlsRecordSplitPosition: Int = tlsRecordSplitPosition ?: 0
    val tlsRecordSplitAtSni: Boolean = tlsRecordSplitAtSni ?: false
    val hostsMode: HostsMode =
        if (hosts?.isBlank() != false) HostsMode.Disable
        else hostsMode ?: HostsMode.Disable
    val hosts: String? =
        if (this.hostsMode == HostsMode.Disable) null
        else hosts?.trim()
    val tcpFastOpen: Boolean = tcpFastOpen ?: false
    val udpFakeCount: Int = udpFakeCount ?: 0
    val dropSack: Boolean = dropSack ?: false
    val fakeOffset: Int = fakeOffset ?: 0

    constructor(netVeilPrefs: NetVeilPreferences) : this(
        ip = netVeilPrefs.proxyIp,
        port = netVeilPrefs.proxyPort.toIntOrNull(),
        maxConnections = netVeilPrefs.maxConnections.toIntOrNull(),
        bufferSize = netVeilPrefs.bufferSize.toIntOrNull(),
        defaultTtl = netVeilPrefs.defaultTtl.toIntOrNull(),
        noDomain = netVeilPrefs.noDomain,
        desyncHttp = netVeilPrefs.desyncHttp,
        desyncHttps = netVeilPrefs.desyncHttps,
        desyncUdp = netVeilPrefs.desyncUdp,
        desyncMethod = netVeilPrefs.desyncMethod,
        splitPosition = netVeilPrefs.splitPosition.toIntOrNull(),
        splitAtHost = netVeilPrefs.splitAtHost,
        fakeTtl = netVeilPrefs.fakeTtl.toIntOrNull(),
        fakeSni = netVeilPrefs.fakeSni,
        oobChar = netVeilPrefs.oobChar,
        hostMixedCase = netVeilPrefs.hostMixedCase,
        domainMixedCase = netVeilPrefs.domainMixedCase,
        hostRemoveSpaces = netVeilPrefs.hostRemoveSpaces,
        tlsRecordSplit = netVeilPrefs.tlsRecordSplit,
        tlsRecordSplitPosition = netVeilPrefs.tlsRecordSplitPosition.toIntOrNull(),
        tlsRecordSplitAtSni = netVeilPrefs.tlsRecordSplitAtSni,
        hostsMode = netVeilPrefs.hostsMode,
        hosts = when (netVeilPrefs.hostsMode) {
            HostsMode.Blacklist -> netVeilPrefs.hostsBlacklist
            HostsMode.Whitelist -> netVeilPrefs.hostsWhitelist
            else -> null
        },
        tcpFastOpen = netVeilPrefs.tcpFastOpen,
        udpFakeCount = netVeilPrefs.udpFakeCount.toIntOrNull(),
        dropSack = netVeilPrefs.dropSack,
        fakeOffset = netVeilPrefs.fakeOffset.toIntOrNull(),
    )

    constructor(preferences: SharedPreferences) : this(
        ip = preferences.getString(NetVeilPreferences.KEY_PROXY_IP, null),
        port = preferences.getString(NetVeilPreferences.KEY_PROXY_PORT, null)?.toIntOrNull(),
        maxConnections = preferences.getString(NetVeilPreferences.KEY_MAX_CONNECTIONS, null)?.toIntOrNull(),
        bufferSize = preferences.getString(NetVeilPreferences.KEY_BUFFER_SIZE, null)?.toIntOrNull(),
        defaultTtl = preferences.getString(NetVeilPreferences.KEY_DEFAULT_TTL, null)?.toIntOrNull(),
        noDomain = preferences.getBoolean(NetVeilPreferences.KEY_NO_DOMAIN, false),
        desyncHttp = preferences.getBoolean(NetVeilPreferences.KEY_DESYNC_HTTP, true),
        desyncHttps = preferences.getBoolean(NetVeilPreferences.KEY_DESYNC_HTTPS, true),
        desyncUdp = preferences.getBoolean(NetVeilPreferences.KEY_DESYNC_UDP, false),
        desyncMethod = preferences.getString(NetVeilPreferences.KEY_DESYNC_METHOD, null)
            ?.let { DesyncMethod.fromName(it) },
        splitPosition = preferences.getString(NetVeilPreferences.KEY_SPLIT_POSITION, null)?.toIntOrNull(),
        splitAtHost = preferences.getBoolean(NetVeilPreferences.KEY_SPLIT_AT_HOST, false),
        fakeTtl = preferences.getString(NetVeilPreferences.KEY_FAKE_TTL, null)?.toIntOrNull(),
        fakeSni = preferences.getString(NetVeilPreferences.KEY_FAKE_SNI, null),
        oobChar = preferences.getString(NetVeilPreferences.KEY_OOB_CHAR, null),
        hostMixedCase = preferences.getBoolean(NetVeilPreferences.KEY_HOST_MIXED_CASE, false),
        domainMixedCase = preferences.getBoolean(NetVeilPreferences.KEY_DOMAIN_MIXED_CASE, false),
        hostRemoveSpaces = preferences.getBoolean(NetVeilPreferences.KEY_HOST_REMOVE_SPACES, false),
        tlsRecordSplit = preferences.getBoolean(NetVeilPreferences.KEY_TLS_RECORD_SPLIT, false),
        tlsRecordSplitPosition = preferences.getString(NetVeilPreferences.KEY_TLS_RECORD_SPLIT_POSITION, null)?.toIntOrNull(),
        tlsRecordSplitAtSni = preferences.getBoolean(NetVeilPreferences.KEY_TLS_RECORD_SPLIT_AT_SNI, false),
        hostsMode = preferences.getString(NetVeilPreferences.KEY_HOSTS_MODE, null)?.let { HostsMode.fromName(it) },
        hosts = preferences.getString(NetVeilPreferences.KEY_HOSTS_MODE, null)?.let {
            when (HostsMode.fromName(it)) {
                HostsMode.Blacklist -> preferences.getString(NetVeilPreferences.KEY_HOSTS_BLACKLIST, null)
                HostsMode.Whitelist -> preferences.getString(NetVeilPreferences.KEY_HOSTS_WHITELIST, null)
                else -> null
            }
        },
        tcpFastOpen = preferences.getBoolean(NetVeilPreferences.KEY_TCP_FAST_OPEN, false),
        udpFakeCount = preferences.getString(NetVeilPreferences.KEY_UDP_FAKE_COUNT, null)?.toIntOrNull(),
        dropSack = preferences.getBoolean(NetVeilPreferences.KEY_DROP_SACK, false),
        fakeOffset = preferences.getString(NetVeilPreferences.KEY_FAKE_OFFSET, null)?.toIntOrNull(),
    )
}

typealias ByeDpiProxyPreferences = NetVeilProxyPreferences
typealias ByeDpiProxyCmdPreferences = NetVeilProxyCmdPreferences
typealias ByeDpiProxyUIPreferences = NetVeilProxyUIPreferences

