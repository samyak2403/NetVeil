package com.samyak.netveil.data

import android.content.Context
import android.content.SharedPreferences
import com.samyak.netveil.model.AppThemeMode
import com.samyak.netveil.model.DesyncMethod
import com.samyak.netveil.model.HostsMode
import com.samyak.netveil.model.Mode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetVeilPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("netveil_preferences", Context.MODE_PRIVATE)

    private val _themeFlow = MutableStateFlow(appTheme)
    val themeFlow: StateFlow<AppThemeMode> = _themeFlow.asStateFlow()

    private val _modeFlow = MutableStateFlow(mode)
    val modeFlow: StateFlow<Mode> = _modeFlow.asStateFlow()

    private val _cmdModeFlow = MutableStateFlow(enableCmdSettings)
    val cmdModeFlow: StateFlow<Boolean> = _cmdModeFlow.asStateFlow()

    // General
    var appTheme: AppThemeMode
        get() = AppThemeMode.fromName(prefs.getString(KEY_APP_THEME, "system"))
        set(value) {
            prefs.edit().putString(KEY_APP_THEME, value.value).apply()
            _themeFlow.value = value
        }

    var mode: Mode
        get() = Mode.fromString(prefs.getString(KEY_MODE, "vpn"))
        set(value) {
            prefs.edit().putString(KEY_MODE, value.key).apply()
            _modeFlow.value = value
        }

    var dnsIp: String
        get() = prefs.getString(KEY_DNS_IP, "1.1.1.1") ?: "1.1.1.1"
        set(value) = prefs.edit().putString(KEY_DNS_IP, value).apply()

    var ipv6Enable: Boolean
        get() = prefs.getBoolean(KEY_IPV6_ENABLE, false)
        set(value) = prefs.edit().putBoolean(KEY_IPV6_ENABLE, value).apply()

    var enableCmdSettings: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_CMD_SETTINGS, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ENABLE_CMD_SETTINGS, value).apply()
            _cmdModeFlow.value = value
        }

    var cmdArgs: String
        get() = prefs.getString(KEY_CMD_ARGS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CMD_ARGS, value).apply()

    // Proxy Settings
    var proxyIp: String
        get() = prefs.getString(KEY_PROXY_IP, "127.0.0.1") ?: "127.0.0.1"
        set(value) = prefs.edit().putString(KEY_PROXY_IP, value).apply()

    var proxyPort: String
        get() = prefs.getString(KEY_PROXY_PORT, "1080") ?: "1080"
        set(value) = prefs.edit().putString(KEY_PROXY_PORT, value).apply()

    var maxConnections: String
        get() = prefs.getString(KEY_MAX_CONNECTIONS, "512") ?: "512"
        set(value) = prefs.edit().putString(KEY_MAX_CONNECTIONS, value).apply()

    var bufferSize: String
        get() = prefs.getString(KEY_BUFFER_SIZE, "16384") ?: "16384"
        set(value) = prefs.edit().putString(KEY_BUFFER_SIZE, value).apply()

    var defaultTtl: String
        get() = prefs.getString(KEY_DEFAULT_TTL, "0") ?: "0"
        set(value) = prefs.edit().putString(KEY_DEFAULT_TTL, value).apply()

    var noDomain: Boolean
        get() = prefs.getBoolean(KEY_NO_DOMAIN, false)
        set(value) = prefs.edit().putBoolean(KEY_NO_DOMAIN, value).apply()

    var tcpFastOpen: Boolean
        get() = prefs.getBoolean(KEY_TCP_FAST_OPEN, false)
        set(value) = prefs.edit().putBoolean(KEY_TCP_FAST_OPEN, value).apply()

    // Desync Settings
    var hostsMode: HostsMode
        get() = HostsMode.fromName(prefs.getString(KEY_HOSTS_MODE, "disable"))
        set(value) = prefs.edit().putString(KEY_HOSTS_MODE, value.value).apply()

    var hostsBlacklist: String
        get() = prefs.getString(KEY_HOSTS_BLACKLIST, "") ?: ""
        set(value) = prefs.edit().putString(KEY_HOSTS_BLACKLIST, value).apply()

    var hostsWhitelist: String
        get() = prefs.getString(KEY_HOSTS_WHITELIST, "") ?: ""
        set(value) = prefs.edit().putString(KEY_HOSTS_WHITELIST, value).apply()

    var desyncMethod: DesyncMethod
        get() = DesyncMethod.fromName(prefs.getString(KEY_DESYNC_METHOD, "disorder"))
        set(value) = prefs.edit().putString(KEY_DESYNC_METHOD, value.value).apply()

    var splitPosition: String
        get() = prefs.getString(KEY_SPLIT_POSITION, "1") ?: "1"
        set(value) = prefs.edit().putString(KEY_SPLIT_POSITION, value).apply()

    var splitAtHost: Boolean
        get() = prefs.getBoolean(KEY_SPLIT_AT_HOST, false)
        set(value) = prefs.edit().putBoolean(KEY_SPLIT_AT_HOST, value).apply()

    var dropSack: Boolean
        get() = prefs.getBoolean(KEY_DROP_SACK, false)
        set(value) = prefs.edit().putBoolean(KEY_DROP_SACK, value).apply()

    var fakeTtl: String
        get() = prefs.getString(KEY_FAKE_TTL, "8") ?: "8"
        set(value) = prefs.edit().putString(KEY_FAKE_TTL, value).apply()

    var fakeOffset: String
        get() = prefs.getString(KEY_FAKE_OFFSET, "0") ?: "0"
        set(value) = prefs.edit().putString(KEY_FAKE_OFFSET, value).apply()

    var fakeSni: String
        get() = prefs.getString(KEY_FAKE_SNI, "www.iana.org") ?: "www.iana.org"
        set(value) = prefs.edit().putString(KEY_FAKE_SNI, value).apply()

    var oobChar: String
        get() = prefs.getString(KEY_OOB_CHAR, "a") ?: "a"
        set(value) = prefs.edit().putString(KEY_OOB_CHAR, value).apply()

    // Protocols
    var desyncHttp: Boolean
        get() = prefs.getBoolean(KEY_DESYNC_HTTP, true)
        set(value) = prefs.edit().putBoolean(KEY_DESYNC_HTTP, value).apply()

    var desyncHttps: Boolean
        get() = prefs.getBoolean(KEY_DESYNC_HTTPS, true)
        set(value) = prefs.edit().putBoolean(KEY_DESYNC_HTTPS, value).apply()

    var desyncUdp: Boolean
        get() = prefs.getBoolean(KEY_DESYNC_UDP, false)
        set(value) = prefs.edit().putBoolean(KEY_DESYNC_UDP, value).apply()

    // HTTP Modifiers
    var hostMixedCase: Boolean
        get() = prefs.getBoolean(KEY_HOST_MIXED_CASE, false)
        set(value) = prefs.edit().putBoolean(KEY_HOST_MIXED_CASE, value).apply()

    var domainMixedCase: Boolean
        get() = prefs.getBoolean(KEY_DOMAIN_MIXED_CASE, false)
        set(value) = prefs.edit().putBoolean(KEY_DOMAIN_MIXED_CASE, value).apply()

    var hostRemoveSpaces: Boolean
        get() = prefs.getBoolean(KEY_HOST_REMOVE_SPACES, false)
        set(value) = prefs.edit().putBoolean(KEY_HOST_REMOVE_SPACES, value).apply()

    // HTTPS TLS Record
    var tlsRecordSplit: Boolean
        get() = prefs.getBoolean(KEY_TLS_RECORD_SPLIT, false)
        set(value) = prefs.edit().putBoolean(KEY_TLS_RECORD_SPLIT, value).apply()

    var tlsRecordSplitPosition: String
        get() = prefs.getString(KEY_TLS_RECORD_SPLIT_POSITION, "0") ?: "0"
        set(value) = prefs.edit().putString(KEY_TLS_RECORD_SPLIT_POSITION, value).apply()

    var tlsRecordSplitAtSni: Boolean
        get() = prefs.getBoolean(KEY_TLS_RECORD_SPLIT_AT_SNI, false)
        set(value) = prefs.edit().putBoolean(KEY_TLS_RECORD_SPLIT_AT_SNI, value).apply()

    // UDP
    var udpFakeCount: String
        get() = prefs.getString(KEY_UDP_FAKE_COUNT, "0") ?: "0"
        set(value) = prefs.edit().putString(KEY_UDP_FAKE_COUNT, value).apply()

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _themeFlow.value = appTheme
        _modeFlow.value = mode
        _cmdModeFlow.value = enableCmdSettings
    }

    val rawPreferences: SharedPreferences
        get() = prefs

    companion object {
        const val KEY_APP_THEME = "app_theme"
        const val KEY_MODE = "byedpi_mode"
        const val KEY_DNS_IP = "dns_ip"
        const val KEY_IPV6_ENABLE = "ipv6_enable"
        const val KEY_ENABLE_CMD_SETTINGS = "byedpi_enable_cmd_settings"
        const val KEY_CMD_ARGS = "byedpi_cmd_args"

        const val KEY_PROXY_IP = "byedpi_proxy_ip"
        const val KEY_PROXY_PORT = "byedpi_proxy_port"
        const val KEY_MAX_CONNECTIONS = "byedpi_max_connections"
        const val KEY_BUFFER_SIZE = "byedpi_buffer_size"
        const val KEY_DEFAULT_TTL = "byedpi_default_ttl"
        const val KEY_NO_DOMAIN = "byedpi_no_domain"
        const val KEY_TCP_FAST_OPEN = "byedpi_tcp_fast_open"

        const val KEY_HOSTS_MODE = "byedpi_hosts_mode"
        const val KEY_HOSTS_BLACKLIST = "byedpi_hosts_blacklist"
        const val KEY_HOSTS_WHITELIST = "byedpi_hosts_whitelist"
        const val KEY_DESYNC_METHOD = "byedpi_desync_method"
        const val KEY_SPLIT_POSITION = "byedpi_split_position"
        const val KEY_SPLIT_AT_HOST = "byedpi_split_at_host"
        const val KEY_DROP_SACK = "byedpi_drop_sack"
        const val KEY_FAKE_TTL = "byedpi_fake_ttl"
        const val KEY_FAKE_OFFSET = "byedpi_fake_offset"
        const val KEY_FAKE_SNI = "byedpi_fake_sni"
        const val KEY_OOB_CHAR = "byedpi_oob_data"

        const val KEY_DESYNC_HTTP = "byedpi_desync_http"
        const val KEY_DESYNC_HTTPS = "byedpi_desync_https"
        const val KEY_DESYNC_UDP = "byedpi_desync_udp"

        const val KEY_HOST_MIXED_CASE = "byedpi_host_mixed_case"
        const val KEY_DOMAIN_MIXED_CASE = "byedpi_domain_mixed_case"
        const val KEY_HOST_REMOVE_SPACES = "byedpi_host_remove_spaces"

        const val KEY_TLS_RECORD_SPLIT = "byedpi_tlsrec_enabled"
        const val KEY_TLS_RECORD_SPLIT_POSITION = "byedpi_tlsrec_position"
        const val KEY_TLS_RECORD_SPLIT_AT_SNI = "byedpi_tlsrec_at_sni"

        const val KEY_UDP_FAKE_COUNT = "byedpi_udp_fake_count"
    }
}

