# 🤖 F-Droid Submission Guide for NetVeil

This guide follows the official [F-Droid Quick Start Guide](https://f-droid.org/docs/Submitting_to_F-Droid_Quick_Start_Guide/) to submit **NetVeil** (`com.samyak.netveil`) to the official F-Droid repository (`fdroiddata`).

---

## 1. Prerequisites (on GitHub)

Before submitting to F-Droid, ensure the Git tag for the release exists on your GitHub repository:

```bash
# Ensure master is up to date
git checkout master
git pull origin master

# Create release tag v1.0.1 and push to GitHub
git tag -a v1.0.1 -m "NetVeil Release v1.0.1"
git push origin v1.0.1
```

---

## 2. Forking and Adding the Metadata Recipe

1. **Fork the F-Droid Data repository** on GitLab:
   Go to [https://gitlab.com/fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata) and click **Fork**.

2. **Clone your fork locally**:
   ```bash
   git clone https://gitlab.com/<YOUR_GITLAB_USERNAME>/fdroiddata.git
   cd fdroiddata
   git checkout -b add-netveil
   ```

3. **Copy the metadata recipe**:
   Copy [`metadata/com.samyak.netveil.yml`](metadata/com.samyak.netveil.yml) (or [`com.samyak.netveil.yml`](com.samyak.netveil.yml)) into the `metadata/` folder of your `fdroiddata` clone:
   ```bash
   cp /path/to/NetVeil/metadata/com.samyak.netveil.yml metadata/com.samyak.netveil.yml
   ```

---

## 3. The Metadata File (`metadata/com.samyak.netveil.yml`)

```yaml
Categories:
  - Internet
  - Security
License: GPL-3.0-or-later
AuthorName: Samyak Kamble
AuthorEmail: samyakkamble6659@gmail.com
AuthorWebSite: https://github.com/samyak2403
WebSite: https://github.com/samyak2403/NetVeil
SourceCode: https://github.com/samyak2403/NetVeil
IssueTracker: https://github.com/samyak2403/NetVeil/issues
Changelog: https://github.com/samyak2403/NetVeil/blob/master/RELEASE_NOTES.md

AutoName: NetVeil
Summary: Modern Android DPI circumvention engine & SOCKS5/VPN client
Description: |-
  NetVeil is a modern, lightweight, privacy-respecting Android application designed
  to bypass Deep Packet Inspection (DPI) and Internet censorship without routing traffic
  through external VPN servers.

  All packet desynchronization occurs locally on your device:
  * Dual Operating Modes: System-wide VPN Mode (tun2socks) & Local Proxy Mode (127.0.0.1:1080)
  * Dual-Protocol Proxy on Port 1080: SOCKS4, SOCKS5, and HTTP CONNECT tunneling
  * Advanced DPI Desynchronization: Split, Disorder, Fake SNI, TTL hop manipulation
  * Jetpack Compose Material 3 UI with dynamic colors and dark mode
  * Quick Settings Tile for one-tap status bar toggling
  * Zero analytics, zero ad trackers, zero telemetry

RepoType: git
Repo: https://github.com/samyak2403/NetVeil.git

Builds:
  - versionName: 1.0.1
    versionCode: 2
    commit: v1.0.1
    subdir: app
    gradle:
      - yes
    ndk: r26b

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.1
CurrentVersionCode: 2
```

---

## 4. Lint & Verify (Optional but Recommended)

If you have `fdroidserver` installed:
```bash
fdroid lint com.samyak.netveil
fdroid checkupdates com.samyak.netveil
```

---

## 5. Commit and Create Merge Request (MR)

```bash
git add metadata/com.samyak.netveil.yml
git commit -m "Add com.samyak.netveil"
git push -u origin add-netveil
```

1. Go to your fork on GitLab and click **Create Merge Request**.
2. **Title**: `Add com.samyak.netveil`
3. Check the submission checklist in the description template and submit!
