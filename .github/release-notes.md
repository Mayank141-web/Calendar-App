## What's new in v2.1
- **Install from the terminal** on any OS with one command (see below). No admin rights needed.
- **Automatic updates**: MyCalendar checks for a new version each time it starts. Click **Update now** and it downloads, installs and restarts itself, keeping your calendar.
- Every download is verified against `SHA256SUMS.txt` before anything is installed.
- The window title shows which version you're running.

## Install from the terminal
**macOS / Linux**
```bash
curl -fsSL https://raw.githubusercontent.com/Mayank141-web/Calendar-App/main/install.sh | sh
```
**Windows (PowerShell)**
```powershell
irm https://raw.githubusercontent.com/Mayank141-web/Calendar-App/main/install.ps1 | iex
```

## Or download a file
| System | File |
|---|---|
| Windows 10/11 | `MyCalendar-2.1-Windows-x64.msi` (installer) or `…-portable.zip` (no install) |
| macOS (Apple Silicon, M1 or newer) | `MyCalendar-2.1-macOS-AppleSilicon.dmg` |
| macOS (Intel) | `MyCalendar-2.1-macOS-Intel.dmg` |
| Ubuntu / Debian / Mint / Pop!_OS | `MyCalendar-2.1-Linux-amd64.deb` |
| Fedora / RHEL / openSUSE | `MyCalendar-2.1-Linux-x86_64.rpm` |
| Any Linux distro | `MyCalendar-2.1-Linux-x86_64.AppImage` or `.tar.gz` |
| Anything with Java 17+ | `MyCalendar-2.1.jar` |

Copies installed with the terminal command, the `.msi`, the `.dmg`, the AppImage or the `.tar.gz` update themselves. The `.deb`/`.rpm`, portable `.zip` and `.jar` show a notice that links to the new download instead.

**Updating from v2.0:** v2.0 didn't have the updater yet, so install v2.1 once yourself, using the terminal command or a download. Updates are automatic from then on. Your calendar data carries over.
