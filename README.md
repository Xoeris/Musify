# Musify

**A fast, native-feeling music player and library suite** — audiophile playback, seamless phone-to-desktop sync, and comprehensive audio management from one lightweight app.

Built on **XIME** (Xoeris Interactive Modular Ecosystem). Runs on **Windows, Linux and macOS**.

> Status: `1.01.01` — active development. Closed source.

---

## Table of contents

1. [Features](#features)
2. [How it works](#how-it-works)
3. [Getting started](#getting-started)
4. [Platform support](#platform-support)
5. [Configuration and data](#configuration-and-data)
6. [Feedback](#feedback)
7. [Acknowledgements and license](#acknowledgements)

---

## Features

Musify is a **desktop music player and audio workstation** with a companion **Android app** (Xoeris Musify for Android).

### Desktop app — pages and panels

| Page / Component | What it does |
|---|---|
| **Home (Overview)** | Recently played tracks, recommended mixes, quick library stats, and instant resume. |
| **Library** | Track collection browser with fast sorting and grouping by Title, Artist, Album, Genre, and Year. |
| **Playlists** | Custom playlist manager: create, reorder, and import/export `.xmpx` playlists compatible with the mobile app. |
| **Now Playing** | Immersive player view with album artwork, real-time waveform progress seeker, queue drawer, and volume controls. |
| **Search** | Instant multi-field library search across titles, artists, albums, and folder paths. |
| **Settings** | Audio tuning (crossfade, volume boost), watched folders, system tray behaviors, and theme preferences. |

---

### Audio engine
- **High-Fidelity Playback:** Powered by LibVLC audio core with hardware acceleration and bit-perfect rendering.
- **Optimized Footprint:** Stripped of unnecessary video/disc modules (~500 unused plugins removed), providing instant startup and low RAM overhead without triggering antivirus scanning delays.
- **Crossfade & Gapless:** Customizable track crossfading (`CrossfadeLane`) with configurable duration (1–10 seconds) for seamless transitions.
- **Volume Normalization & Boost:** Smart gain normalization with volume expansion up to 200%, matching the mobile audio experience.
- **Context-Aware Playback Modes:** Independent shuffle and repeat memory preserved per playlist or global library.

### Library & `.xmpx` playlist sync
- **Cross-Platform Playlist Format (`.xmpx`):** Native JSON-based playlist structure with schema parity between desktop and Android, allowing zero-friction playlist migration.
- **Fast Metadata Scanner:** Scans local directories, extracts ID3/Vorbis/FLAC metadata tags, and caches album artwork.
- **Local SQLite Store:** Managed library persistence with thread-safe queries, fast warm-up, and stable track identifiers.
- **Format Support:** MP3, FLAC, AAC, M4A, OGG, Opus, WAV, ALAC, AIFF, and WMA.

### System integration
- **Native Media Session Controls:**
  - **Windows:** System Media Transport Controls (SMTC) integration with taskbar controls, volume overlay, and hardware multimedia keys.
  - **Linux:** MPRIS v2 D-Bus interface for desktop environment shell integration (GNOME, KDE, etc.).
  - **macOS:** Now Playing transport controls integration.
- **System Tray Operations:** Minimize or close to system tray with persistent background playback, tray tooltips, and playback hotkeys.
- **Single Instance Guard:** Named system mutex preventing duplicate instances, passing folder imports to the running instance.

### Android companion app (Xoeris Musify for Android)
Package `com.xoeris.android.app.musify` · minSdk 24 · targetSdk 36 · ABIs `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`.
- On-device music library browser, background audio playback service, and notification controls.
- Volume boost up to 200% with normalization.
- Interchangeable `.xmpx` playlist import and export.
- Offline playback and lightweight storage footprint.

---

## How it works

Musify pairs a responsive **XIME** desktop frontend with a dedicated audio streaming pipeline:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Desktop UI Layer (XIME)                            │
│   Fluent Theme (Gold #FFC94A) · Responsive Panels · Vector Waveform Seeker  │
│   Home · Library Grid · Playlist Manager · Now Playing Bar · Settings       │
├──────────────────────────────────────┬──────────────────────────────────────┤
│         System Integrations          │           Managed Services           │
│        (OS Media Transports)         │        (CommunityToolkit.Mvvm)       │
├──────────────────────────────────────┼──────────────────────────────────────┤
│  Windows SMTC / Linux MPRIS / macOS  │  Library Scanner (Background Tasks)  │
│  System Tray Service & Autostart     │  SQLite / EF Core (Library Store)    │
│  Single-Instance IPC Mutex           │  PlaybackSession & CrossfadeLane     │
├──────────────────────────────────────┴──────────────────────────────────────┤
│                        XIME.Media (LibVLC Core)                             │
│       Audio-Only Decoders · DirectSound / WASAPI / PulseAudio / CoreAudio   │
└─────────────────────────────────────────────────────────────────────────────┘
```

```
 User Play Action / Multimedia Key
        │
        ▼
 Desktop UI Shell ──▶ Playback Controller (MVVM)
        │
 ┌──────┴──────────────────────────┬─────────────────────────────┐
 │                                 │                             │
 ▼                                 ▼                             ▼
OS Transport Session (SMTC/MPRIS)  XIME.Media Audio Core     Library Store (SQLite)
 ├─ Metadata & Track Info          ├─ LibVLC Audio Pipeline   ├─ Fast Indexed Query
 ├─ Play / Pause / Seek / Skip     ├─ Crossfade & Gain Normal ├─ Track Metadata Cache
 └─ Hardware Media Key Listener    └─ Hardware Audio Output   └─ .xmpx Playlist Sync
```

- **Audio-Only VLC Pipeline:** By omitting video filters and stream muxers, memory footprint is halved and first-launch disk I/O is reduced by over 70%.
- **Dual-Lane Crossfade:** Crossfading runs two concurrent LibVLC media players modulated by a software volume ramp to prevent silence gaps or audio pops between tracks.
- **Thread-Safe Data Access:** Single DbContext gated through an asynchronous semaphore prevents database lockups during concurrent background scanning and UI reads.

---

## Getting started

### Install

Download the installer for your platform from the [Releases](https://github.com/Xoeris/Musify/releases) page and run it. The installer offers a per-user install (no elevation) or an all-users install, and can create desktop and Start menu shortcuts.

| Platform | Artifact |
|---|---|
| Windows | `Musify-Installer-win-x64.exe`, `Musify-Installer-win-arm64.exe` |
| Linux | `Musify-Installer-linux-<arch>`, or the `musify` `.deb`, `.tar.gz` and AppImage packages |
| macOS | `Musify-Installer-osx-<arch>`, or the `Musify.app` zip / `.dmg` |

| Install type | Location (Windows) |
|---|---|
| Just for me | `%LOCALAPPDATA%\Musify` |
| All users | `C:\Program Files\Musify` |

> **macOS:** builds are not notarized, so Gatekeeper will warn on first launch (right-click -> Open).

### First run

1. Launch Musify.
2. Open **Settings -> Watched Folders** and choose your music directories (or let Musify scan your system Music folder automatically).
3. The library will populate in the background. Double-click any track to begin playback.

---

## Platform support

| Capability | Windows | Linux | macOS |
|---|---|---|---|
| **Desktop UI & Themes** | ✅ Fluent theme, custom decorations | ✅ Fluent theme, X11/Wayland | ✅ Fluent theme, macOS menu bar |
| **Audio Core (LibVLC)** | ✅ Bundled audio-only LibVLC | ✅ System libvlc | ✅ App bundle libvlc |
| **System Media Controls** | ✅ SMTC (WinRT) | ✅ MPRIS (D-Bus) | ✅ Now Playing |
| **Crossfade & Normalization** | ✅ | ✅ | ✅ |
| **System Tray & Autostart** | ✅ | ✅ | ✅ |
| **`.xmpx` Playlist Import/Export** | ✅ | ✅ | ✅ |

---

## Configuration and data

| What | Where (Windows) |
|---|---|
| Settings & preferences | `%LOCALAPPDATA%\Musify\settings.json` |
| Music library database | `%LOCALAPPDATA%\Musify\musify.db` |
| Playlist archives | User-chosen or exported as `.xmpx` |
| Startup and playback logs | `%LOCALAPPDATA%\Musify\logs\` |

---

## Feedback

Musify is closed-source software by Xoeris; this repository hosts releases, the license and documentation. Bug reports and feature requests are welcome in [Issues](https://github.com/Xoeris/Musify/issues).

---

## Acknowledgements

Musify builds on outstanding open-source work:
- [VideoLAN LibVLC](https://www.videolan.org/vlc/libvlc.html) & [LibVLCSharp](https://github.com/videolan/libvlcsharp)
- [Microsoft Entity Framework Core (SQLite)](https://github.com/dotnet/efcore)
- [CommunityToolkit.Mvvm](https://github.com/CommunityToolkit/dotnet)
- [Tmds.DBus.Protocol](https://github.com/tmds/Tmds.DBus)
- [Inter](https://rsms.me/inter/) typeface

---

## License

Musify is proprietary software by Xoeris, distributed in binary form under the [Xoeris Proprietary Software License](LICENSE). You may install and use it, but may not redistribute, modify or reverse engineer it. Third-party components keep their own licenses.

© 2018 - 2026 Xoeris. All rights reserved.
