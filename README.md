<p align="center">
  <img src="app/src/main/res/drawable/ic_notification.xml" width="96" height="96" alt="NetVeil Logo" />
</p>

<h1 align="center">NetVeil</h1>

<p align="center">
  <strong>Modern Android Deep Packet Inspection (DPI) Circumvention Engine & SOCKS5/VPN Client</strong>
</p>

<p align="center">
  <a href="https://github.com/samyak2403/NetVeil/releases"><img src="https://img.shields.io/badge/Release-v1.0.0-blue.svg?style=flat-square" alt="Release"></a>
  <a href="https://developer.android.com/about/versions/nougat"><img src="https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-green.svg?style=flat-square" alt="Android Version"></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-purple.svg?style=flat-square" alt="Compose"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Language-Kotlin%202.x%20%7C%20C99-orange.svg?style=flat-square" alt="Language"></a>
  <a href="https://github.com/samyak2403/NetVeil/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-GPLv3-lightgrey.svg?style=flat-square" alt="License"></a>
</p>

---

## 📖 Overview

**NetVeil** is a high-performance Android application designed to bypass Internet censorship powered by **Deep Packet Inspection (DPI)** firewalls and middleboxes. 

Unlike traditional VPNs, NetVeil **does not route your traffic through an external remote proxy server or VPN host**. Instead, all desynchronization and packet manipulations occur **locally on your device**. This provides:
- **Maximum Privacy**: Your traffic flows directly between your device and the destination server without passing through any third-party VPN provider.
- **Full ISP Bandwidth**: No latency penalty or bandwidth throttling from shared remote proxies.
- **Zero Account / Subscription Requirements**: Operates entirely standalone and decentralized.

---

## ✨ Features

### 🚀 Dual Operation Modes
- **VPN Mode (System-wide)**: Creates an Android `VpnService` tun interface integrated with `hev-socks5-tunnel` (`tun2socks`). Routes all device network traffic transparently through the local desync engine. Includes customizable DNS resolution (default `1.1.1.1`) and IPv6 routing control.
- **Proxy Mode (Local SOCKS5)**: Runs a lightweight standalone SOCKS5 proxy on `127.0.0.1:1080`. Perfect for manual configuration in specific applications (browsers, Telegram, etc.) with minimal battery usage.

### 🛡️ Advanced DPI Circumvention Techniques
Powered by the battle-tested native C engine (`byedpi` / `ciadpi`):
- **TCP Splitting (`--split`)**: Fragments HTTP request headers or TLS `ClientHello` into multiple TCP segments to evade pattern matching.
- **Out-of-Order Disorder (`--disorder`)**: Transmits TCP segments out of order with crafted TTLs, confusing stateful DPI inspection middleboxes.
- **Fake Packets (`--fake`)**: Injects spoofed `ClientHello` packets with low TTL so censorship firewalls close their inspection window while origin servers ignore the expired frames.
- **Out-of-Band Data (`--oob` / `--disoob`)**: Inserts TCP Urgent pointer bytes to scramble SNI signatures.
- **TLS Record Splitting (`--tlsrec`)**: Breaks TLS `ClientHello` across distinct TLS record frames or directly at the SNI offset.
- **HTTP Header Normalization**: Host header mixed-case randomization, space stripping, and domain case variation.
- **Host List Filtering**: Blacklist and Whitelist modes for fine-grained per-domain desync targeting.

### 🎨 Modern Material 3 User Experience
- **Minimalist ByeDPI Aesthetic**: Centered one-tap `Connect` / `Disconnect` button with clean status indicators and tap-to-copy proxy address (`127.0.0.1:1080`).
- **Brand Blue Palette**: `#5976DF` primary accent with seamless Light and Dark theme adaptation.
- **Visual Switch Anatomy**: Crisp emerald green switches (`#10B981`) with embedded checkmark handle icons.
- **Structured UI Card Editor**: 7 card-based visual configuration panels with dynamic dependency validation.
- **Raw Command Line Editor**: For advanced users with 5 one-tap popular presets (Disorder, Split+Host, Fake+TTL, Discord/YouTube, OOB).
- **Quick Settings Tile (`QuickTileService`)**: Toggle connection directly from the Android quick settings drawer.
- **Diagnostic Log Export**: Direct `logcat` export via Android Storage Access Framework (SAF).

---

## 📱 Screenshots

| Main Screen (Disconnected) | Main Screen (Connected) | Settings Screen | UI Card Editor |
| :---: | :---: | :---: | :---: |
| Centered Outlined Connect button with Brand Blue toolbar | Instant Disconnect button with live proxy status | Pure white cards with theme and mode switches | 7 parameter tuning cards for desync methods |

---

## ⚡ Popular Command Line Presets

NetVeil provides built-in presets for common censorship scenarios:

| Preset Name | Command Line Arguments | Use Case |
| :--- | :--- | :--- |
| **Standard Disorder** | `--disorder 1 --proto http,https` | General web browsing bypass |
| **Split at Host / SNI**| `--split 1+s --proto http,https` | Target middleboxes sensitive to split SNI |
| **Fake Packet + TTL** | `--fake -1 --ttl 8 --proto https` | Strict DPI with low TTL drop thresholds |
| **Discord / YouTube** | `-s 1 -d 3 -t 8 -o 1 -K t,h` | Unthrottling streaming media and voice gateways |
| **OOB Urgent Byte** | `--oob 1 -e a --proto http,https` | Advanced stateful inspection firewalls |

---

## 🏗️ Architecture & Technical Stack

```
com.samyak.netveil
├── core/
│   ├── NetVeilProxy.kt              # JNI lifecycle wrapper for libnetveil.so
│   ├── NetVeilProxyPreferences.kt  # Mappings between UI preferences & CLI args
│   └── TProxyService.kt            # JNI bindings for libhev-socks5-tunnel.so
├── data/
│   ├── Constants.kt                # Broadcast actions and intent extras
│   └── NetVeilPreferences.kt       # Persistent SharedPreferences & StateFlows
├── model/
│   └── Models.kt                   # AppStatus, Mode, DesyncMethod, HostsMode
├── service/
│   ├── NetVeilVpnService.kt        # Android VpnService & tun2socks management
│   ├── NetVeilProxyService.kt      # Foreground SOCKS5 proxy service
│   ├── NetVeilStatus.kt            # Global connection StateFlow & traffic stats
│   ├── QuickTileService.kt         # Android Quick Settings tile implementation
│   └── ServiceManager.kt           # Lifecycle dispatcher for start/stop intents
├── ui/
│   ├── components/Components.kt    # ScreenCard, SettingSwitchRow, SettingDropdownRow
│   ├── screens/
│   │   ├── MainScreen.kt           # Simple OutlinedButton ByeDPI main interface
│   │   ├── SettingsScreen.kt       # General, Engine, and Navigation cards
│   │   ├── UiConfigScreen.kt       # Structured 7-card parameter editor
│   │   └── CmdConfigScreen.kt      # CLI terminal & one-tap presets
│   └── theme/                      # BrandBlue, Theme, Type, Color
└── util/
    ├── ArgumentsUtils.kt           # Shell tokenizer with quote/escape support
    ├── LogUtils.kt                 # Diagnostic logcat collector & SAF exporter
    ├── NotificationUtils.kt        # Foreground notification builder with Stop action
    └── ValidateUtils.kt            # IP, port, and integer range validators
```

- **Android SDK**: `minSdk = 24` (Android 7.0), `compileSdk = 37`, `targetSdk = 37`
- **Native C Layer**: CMake 3.22.1 + C99 (`libnetveil.so`, `libhev-socks5-tunnel.so`)
- **Native ABIs Supported**: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`

---

## 🛠️ Building & Developing

### Prerequisites
- **Android Studio**: Ladybug (2024.2) / Meerkat or later
- **JDK**: Java 17 or Java 21
- **Android NDK**: Version 25+
- **CMake**: Version 3.22.1+

### Build Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/samyak2403/NetVeil.git
   cd NetVeil
   ```

2. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

3. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The resulting APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 🧪 Testing & Verification

NetVeil features a comprehensive automated and manual verification suite:
- **18 Unit Tests Passing**: Covering IP validation, port boundaries, shell argument splitting, C enum ordinals, and StateFlow transitions.
- **F-Droid / IzzyOnDroid Ready**: Complete `fastlane` metadata in `fastlane/metadata/android/` (en-US and ru-RU), with zero Google Play dependency metadata embedded (`dependenciesInfo.includeInApk = false`).
- See [`TESTING_CHECKLIST.md`](TESTING_CHECKLIST.md) for full test cases and verification criteria.

---

## 🤝 Acknowledgements & Credits

- [**hufrea/byedpi**](https://github.com/hufrea/byedpi) — Core local DPI bypass engine written in C.
- [**dovecoteescapee/ByeDPIAndroid**](https://github.com/dovecoteescapee/ByeDPIAndroid) — Original Android application inspiration and architecture reference.
- [**hev-socks5-tunnel**](https://github.com/heiher/hev-socks5-tunnel) — High-throughput tun2socks tunnel engine.

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0 (GPLv3)**. See the [LICENSE](LICENSE) file for details.

