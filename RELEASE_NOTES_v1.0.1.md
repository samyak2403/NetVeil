# NetVeil v1.0.1 — Hotfix & Stability Release

**NetVeil** is a modern Android application designed to bypass Deep Packet Inspection (DPI) and Internet censorship using native packet desynchronization without routing your traffic through third-party VPN servers.

---

## 🌟 What's New in v1.0.1

### 🐛 Critical Fixes
- **Fixed Release APK Startup Crash**:
  - Resolved `java.lang.IllegalAccessError: Class kotlin.collections.d extended by class zj is inaccessible` thrown by `androidx.startup.InitializationProvider` on release builds.
  - Removed experimental AGP 9.0+ `optimization { packageScope }` block which caused synthetic class repackaging issues on Android ART.
- **Hardened ProGuard / R8 Safety Rules**:
  - Added `-dontrepackage` and `-dontflattenpackagehierarchy` to preserve package visibility boundaries for Kotlin stdlib collections and AndroidX App Startup.

### 🛡️ Core Engine & Compatibility
- **Dual Proxy Mode on Port 1080**:
  - Automatically handles both **SOCKS4/SOCKS5** connections and standard **HTTP `CONNECT`** tunneling requests on `127.0.0.1:1080`.
- **F-Droid & IzzyOnDroid Ready**:
  - Google Play dependency metadata tracking explicitly disabled (`includeInApk = false`).
  - Added complete Fastlane metadata and localized changelogs (`en-US` and `ru-RU`).
- **Clean Epoll Teardown**:
  - Fixed native event loop shutdown hang, preventing port 1080 `EADDRINUSE` rebind failures upon reconnect.

---

## 📦 Downloads & Verification

| Asset | Size | Description |
| :--- | :--- | :--- |
| **`app-release.apk`** | ~14.3 MB | Production Release APK (All ABIs: ARM64, ARMv7, x86, x86_64) |
| **`native-debug-symbols.zip`** | ~150 KB | Native symbols for stack trace de-obfuscation |

---

## 🤝 Open Source & Contributing
- **Source Code**: [https://github.com/samyak2403/NetVeil](https://github.com/samyak2403/NetVeil)
- **License**: GNU General Public License v3.0 (GPLv3)

