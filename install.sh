#!/bin/sh
# Installs (or updates to) the latest MyCalendar on macOS or Linux.
#   curl -fsSL https://raw.githubusercontent.com/Mayank141-web/Calendar-App/main/install.sh | sh
# No admin rights needed. Every download is checked against the release's SHA256SUMS.txt.
set -eu

REPO="Mayank141-web/Calendar-App"
API="https://api.github.com/repos/$REPO/releases/latest"

say() { printf '%s\n' "$*"; }
fail() { printf 'Error: %s\n' "$*" >&2; exit 1; }

command -v curl >/dev/null 2>&1 || fail "curl is required"

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

curl -fsSL -H "Accept: application/vnd.github+json" "$API" -o "$TMP/release.json" \
    || fail "couldn't reach GitHub"
VERSION=$(grep -o '"tag_name": *"[^"]*"' "$TMP/release.json" | head -n 1 | sed 's/.*"v\{0,1\}\([^"]*\)"$/\1/')

# Prints the download URL of the release file whose name ends with $1
asset_url() {
    grep -o '"browser_download_url": *"[^"]*'"$1"'"' "$TMP/release.json" | head -n 1 | sed 's/.*"\(https[^"]*\)"$/\1/'
}

# Downloads the file ending with $1 to $2 and verifies its checksum
download() {
    url=$(asset_url "$1")
    sums=$(asset_url "SHA256SUMS.txt")
    [ -n "$url" ] && [ -n "$sums" ] || fail "no $1 download in the latest release"
    say "Downloading MyCalendar $VERSION..."
    curl -fL --progress-bar -o "$2" "$url"
    curl -fsSL -o "$TMP/SHA256SUMS.txt" "$sums"
    expected=$(grep " \*\{0,1\}${url##*/}\$" "$TMP/SHA256SUMS.txt" | cut -d ' ' -f 1)
    if command -v sha256sum >/dev/null 2>&1; then
        actual=$(sha256sum "$2" | cut -d ' ' -f 1)
    else
        actual=$(shasum -a 256 "$2" | cut -d ' ' -f 1)
    fi
    [ -n "$expected" ] && [ "$expected" = "$actual" ] || fail "checksum mismatch, download discarded"
}

case "$(uname -s)" in
Darwin)
    # Apple Silicon reports x86_64 when the terminal runs under Rosetta, so ask the hardware
    if [ "$(sysctl -n hw.optional.arm64 2>/dev/null || echo 0)" = 1 ]; then CHIP=AppleSilicon; else CHIP=Intel; fi
    download "macOS-$CHIP.dmg" "$TMP/MyCalendar.dmg"

    if [ -d "$HOME/Applications/MyCalendar.app" ] && [ ! -d /Applications/MyCalendar.app ]; then
        TARGET="$HOME/Applications/MyCalendar.app"
    elif [ -w /Applications ]; then
        TARGET=/Applications/MyCalendar.app
    else
        mkdir -p "$HOME/Applications"
        TARGET="$HOME/Applications/MyCalendar.app"
    fi

    if pgrep -x MyCalendar >/dev/null 2>&1; then osascript -e 'quit app "MyCalendar"' >/dev/null 2>&1 || true; sleep 2; fi
    hdiutil attach -nobrowse -quiet -mountpoint "$TMP/mnt" "$TMP/MyCalendar.dmg"
    rm -rf "$TARGET.new"
    ditto "$TMP/mnt/MyCalendar.app" "$TARGET.new" || { hdiutil detach -quiet "$TMP/mnt"; fail "copy failed"; }
    hdiutil detach -quiet "$TMP/mnt" || true
    rm -rf "$TARGET"
    mv "$TARGET.new" "$TARGET"
    xattr -dr com.apple.quarantine "$TARGET" 2>/dev/null || true

    say "MyCalendar $VERSION is installed in $(dirname "$TARGET")."
    say "Open it from Launchpad or Spotlight, or run: open -a MyCalendar"
    ;;

Linux)
    [ "$(uname -m)" = x86_64 ] || fail "the Linux app is built for x86_64 only. On other CPUs install Java 17+ and run the .jar from the Releases page"
    download "Linux-x86_64.tar.gz" "$TMP/MyCalendar.tar.gz"

    DATA="${XDG_DATA_HOME:-$HOME/.local/share}"
    ROOT="$DATA/MyCalendar"
    if [ -e "$ROOT" ] && [ ! -x "$ROOT/bin/MyCalendar" ]; then
        fail "$ROOT exists but isn't a MyCalendar install; move it away and try again"
    fi
    mkdir -p "$DATA" "$HOME/.local/bin" "$DATA/applications"
    tar -xzf "$TMP/MyCalendar.tar.gz" -C "$TMP"
    pkill -f "$ROOT/bin/MyCalendar" 2>/dev/null || true
    rm -rf "$ROOT"
    mv "$TMP/MyCalendar" "$ROOT"
    ln -sf "$ROOT/bin/MyCalendar" "$HOME/.local/bin/mycalendar"

    cat > "$DATA/applications/mycalendar.desktop" <<EOF
[Desktop Entry]
Type=Application
Name=MyCalendar
Comment=Calendar and task manager
Exec="$ROOT/bin/MyCalendar"
Icon=$ROOT/lib/MyCalendar.png
Categories=Office;Calendar;
Terminal=false
EOF
    update-desktop-database "$DATA/applications" >/dev/null 2>&1 || true

    say "MyCalendar $VERSION is installed in $ROOT."
    say "Open it from your applications menu, or run: mycalendar"
    case ":$PATH:" in
        *":$HOME/.local/bin:"*) ;;
        *) say "(Add ~/.local/bin to your PATH to use the 'mycalendar' command.)" ;;
    esac
    ;;

*)
    fail "unsupported system. On Windows, run this in PowerShell instead:
  irm https://raw.githubusercontent.com/$REPO/main/install.ps1 | iex"
    ;;
esac

say "MyCalendar checks for updates each time it starts and installs them with one click."
