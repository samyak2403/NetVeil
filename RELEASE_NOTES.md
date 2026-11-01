# 🚀 NetVeil Release Notes

All notable changes, architectural milestones, and version releases of **NetVeil** (`com.samyak.netveil`) are documented in this file.

---

## [v1.0.0] - 2026-11-01 (Initial Public Release)

> **Initial production release of NetVeil:** A modern, high-performance, and privacy-respecting Android application for Deep Packet Inspection (DPI) circumvention, rebuilt from the ground up with Jetpack Compose and Material 3.

### 🌟 Release Highlights

- **Modern Material 3 Interface**: Completely reimagined UI built with Android's modern Jetpack Compose framework, featuring dynamic theme color harmonization, system light/dark mode support, and edge-to-edge rendering.
- **Dual Operating Modes**:
  - **VPN Mode**: System-wide transparent proxying using Android's native `VpnService` paired with the high-performance `tun2socks` (`hev-socks5-tunnel`) core.
  - **Proxy Mode**: Standalone local proxy on `127.0.0.1:1080` without requiring device-wide VPN permissions.
- **Dual-Protocol Engine on Port 1080**:
  - Automatically negotiates both **SOCKS4/SOCKS5** connections and standard **HTTP `CONNECT`** tunneling requests on the same port, ensuring out-of-the-box compatibility with Wi-Fi manual proxy settings and browsers.
- **Full DPI Desynchronization Suite**:
  - **Packet Splitting** (`--split`): Divides TCP packets at custom offsets (SNI, Host, or numeric markers).
  - **Disorder Tactics** (`--disorder`): Sends split TCP packets out-of-order to confound stateful inspection middleboxes.
  - **Fake SNI Injection** (`--fake`, `--fake-sni`): Transmits decoy SNI headers with customizable TTL values.
  - **Hop Manipulation** (`--ttl`, `--auto-ttl`): Injects fake packets destined to expire right before reaching DPI probes.
  - **HTTP Obfuscation** (`--host-case`, `--mod-http`): Modifies HTTP header casing and dot padding.
- **Visual Card Editor & Raw CLI Support**:
  - Dual-mode configuration editor allowing users to tune engine parameters via intuitive toggle cards or raw ByeDPI command-line flags.
- **Quick Settings Tile**:
  - Fast status-drawer tile (`QuickTileService`) to connect or disconnect NetVeil with one tap.
- **F-Droid & IzzyOnDroid Ready**:
  - Complete `fastlane` metadata directory structure (`en-US` and `ru-RU`).
  - Google Play dependency metadata tracking explicitly disabled (`dependenciesInfo { includeInApk = false; includeInBundle = false }`).
  - Zero external tracking, zero ads, zero telemetry, and zero remote proxy relays.

---

### 🛠️ Detailed Changes & Improvements

#### 1. Native Engine & CMake Layer
- Ported the upstream ByeDPI C99 core into a modernized CMake 3.22.1 build system.
- Cross-compiled and verified binaries for all 4 primary Android ABIs:
  - `arm64-v8a` (Modern 64-bit ARM devices)
  - `armeabi-v7a` (32-bit ARM devices)
  - `x86_64` (64-bit Android emulators and x86 devices)
  - `x86` (32-bit Android emulators)
- **Native Epoll Unblock Fix**: Implemented loopback dummy trigger socket mechanism in `internal_stop_proxy()` to unblock `epoll_wait` instantly during service termination, eliminating port 1080 `EADDRINUSE` rebind failures.
- **HTTP CONNECT Tunneling**: Added native protocol detection in `proxy.c` (`FLAG_HTTP`) to parse HTTP `CONNECT host:port HTTP/1.1` requests and return `HTTP/1.1 200 Connection Established`.

#### 2. Android Services & Reliability
- Declared Android 14+ foreground service types with `specialUse` (`vpn` and `proxy`).
- Promoted `startForeground()` calls to immediate entry points in both `NetVeilVpnService` and `NetVeilProxyService` to eliminate `ForegroundServiceDidNotStartInTimeException`.
- Added coroutine timeout guards (`withTimeout(2000L)`) around native pthread lifecycle joins to prevent UI thread lockups during shutdown.
- Implemented ongoing system notification with direct **"Stop"** action button and deep linking back to `MainActivity`.

#### 3. Jetpack Compose UI
- Brand Blue primary theme (`#5976DF`) top app bar with adaptive contrast and dark mode elevation dividers.
- Simple, centered connection trigger button matching the clean ByeDPI reference style.
- Clickable IP/port text with instant one-tap clipboard copy toast notification.
- Settings screen with General Configuration, Engine Selection, Card Editor, CLI Editor, and About & Info documentation links.

---

### 🧪 Quality Assurance & Test Verification

| Category | Details | Status |
| :--- | :--- | :---: |
| **Unit Test Suite** | 18 automated unit tests covering argument building, shell splitting, and preferences | ✅ **PASS (18/18)** |
| **Native C Engine** | 4 ABIs compiled with `-Wall -Wextra -Werror` zero warning threshold | ✅ **PASS** |
| **Static Analysis** | Android Gradle Plugin lint and compiler checks | ✅ **0 ERRORS** |
| **Packaging** | Debug APK built and tested (`app/build/outputs/apk/debug/app-debug.apk`) | ✅ **PASS (21.1 MB)** |

---

### 🔒 Privacy & Permissions

NetVeil operates entirely on your local device:
- **`android.permission.INTERNET`**: Required to create local loopback sockets.
- **`android.permission.FOREGROUND_SERVICE`**: Required to run background proxy/VPN tunnels.
- **`android.permission.POST_NOTIFICATIONS`**: Required for persistent service notifications on Android 13+.
- **`android.permission.BIND_VPN_SERVICE`**: Required for transparent system routing.
- **No Analytics / Telemetry**: No tracking SDKs (Firebase, Google Play Services, etc.) are included in the codebase.
- **No Remote Servers**: Your network packets travel directly from your device to the target destination without third-party intermediate proxies.

---

### 📦 Installation & Verification

1. Download the latest APK from the [GitHub Releases](https://github.com/samyak2403/NetVeil/releases) page.
2. Verify the SHA-256 checksum of your downloaded APK against the published release hash.
3. Install the APK on any device running **Android 7.0 (API 24)** or newer.

---

*Maintained by [@samyak2403](https://github.com/samyak2403) and the NetVeil open-source community.*

