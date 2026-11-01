package com.samyak.netveil.model

enum class AppStatus {
    Halted,
    Running,
}

enum class Mode(val key: String, val title: String) {
    Proxy("proxy", "Proxy"),
    VPN("vpn", "VPN");

    companion object {
        fun fromSender(sender: Sender): Mode = when (sender) {
            Sender.Proxy -> Proxy
            Sender.VPN -> VPN
        }

        fun fromString(name: String?): Mode = when (name?.lowercase()) {
            "proxy" -> Proxy
            "vpn" -> VPN
            else -> VPN
        }
    }
}

enum class ServiceStatus {
    Disconnected,
    Connected,
    Failed,
}

enum class Sender(val senderName: String) {
    Proxy("Proxy"),
    VPN("VPN")
}

enum class DesyncMethod(val value: String, val displayName: String) {
    None("none", "None"),
    Split("split", "Split"),
    Disorder("disorder", "Disorder"),
    Fake("fake", "Fake"),
    OOB("oob", "Out-of-band (OOB)"),
    DISOOB("disoob", "Disordered Out-of-band (DISOOB)");

    companion object {
        fun fromName(name: String?): DesyncMethod = when (name?.lowercase()) {
            "none" -> None
            "split" -> Split
            "disorder" -> Disorder
            "fake" -> Fake
            "oob" -> OOB
            "disoob" -> DISOOB
            else -> Disorder
        }
    }
}

enum class HostsMode(val value: String, val displayName: String) {
    Disable("disable", "Disable"),
    Blacklist("blacklist", "Blacklist"),
    Whitelist("whitelist", "Whitelist");

    companion object {
        fun fromName(name: String?): HostsMode = when (name?.lowercase()) {
            "disable" -> Disable
            "blacklist" -> Blacklist
            "whitelist" -> Whitelist
            else -> Disable
        }
    }
}

enum class AppThemeMode(val value: String, val displayName: String) {
    System("system", "Follow System"),
    Light("light", "Light"),
    Dark("dark", "Dark");

    companion object {
        fun fromName(name: String?): AppThemeMode = when (name?.lowercase()) {
            "light" -> Light
            "dark" -> Dark
            else -> System
        }
    }
}

