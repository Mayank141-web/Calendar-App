package Calendar;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

// Checks GitHub for a newer release on startup and, if the user agrees, installs it and restarts.
// Only files attached to the release are downloaded, and each is checked against the release's
// SHA256SUMS.txt before anything is installed.
public final class Updater {

    private static final String REPO = "Mayank141-web/Calendar-App";
    private static final String LATEST_API = "https://api.github.com/repos/" + REPO + "/releases/latest";
    private static final String RELEASES_PAGE = "https://github.com/" + REPO + "/releases/latest";
    private static final Pattern TAG = Pattern.compile("\"tag_name\"\\s*:\\s*\"v?([0-9][0-9.]*)\"");
    private static final String OS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    private static final boolean WINDOWS = OS.contains("win");
    private static final boolean MAC = OS.contains("mac");

    // Runs after the app quits: swaps in the downloaded files, then starts the new version.
    // Paths are passed as arguments ($1..$4), never pasted into the script text.
    private static final String UNIX_INSTALLER = String.join("\n",
        "pid=$1; kind=$2; file=$3; target=$4",
        "while kill -0 \"$pid\" 2>/dev/null; do sleep 1; done",
        "set -e",
        "case \"$kind\" in",
        "  mac)",
        "    mnt=$(mktemp -d)",
        "    hdiutil attach -nobrowse -quiet -mountpoint \"$mnt\" \"$file\"",
        "    rm -rf \"$target.new\"",
        "    ditto \"$mnt/MyCalendar.app\" \"$target.new\" || { hdiutil detach -quiet \"$mnt\"; exit 1; }",
        "    hdiutil detach -quiet \"$mnt\" || true",
        "    rm -rf \"$target\"; mv \"$target.new\" \"$target\"",
        "    xattr -dr com.apple.quarantine \"$target\" 2>/dev/null || true",
        "    open \"$target\" ;;",
        "  linux)",
        "    dir=$(mktemp -d \"$target.update.XXXXXX\")",
        "    tar -xzf \"$file\" -C \"$dir\" || { rm -rf \"$dir\"; exit 1; }",
        "    rm -rf \"$target\"; mv \"$dir/MyCalendar\" \"$target\"; rm -rf \"$dir\"",
        "    nohup \"$target/bin/MyCalendar\" >/dev/null 2>&1 & ;;",
        "  appimage)",
        "    chmod +x \"$file\"; mv -f \"$file\" \"$target\"",
        "    nohup \"$target\" >/dev/null 2>&1 & ;;",
        "esac",
        "rm -f \"$file\"");

    private Updater() {}

    // Set by the native launcher (jpackage) or the JAR manifest; null when run from an IDE
    public static String currentVersion() {
        String version = System.getProperty("jpackage.app-version");
        return version != null ? version : Main.class.getPackage().getImplementationVersion();
    }

    public static void checkInBackground(JFrame frame) {
        String current = currentVersion();
        if (current == null) return;
        Thread thread = new Thread(() -> {
            try {
                String release = new String(fetch(LATEST_API), StandardCharsets.UTF_8);
                Matcher tag = TAG.matcher(release);
                if (tag.find() && compare(tag.group(1), current) > 0) {
                    SwingUtilities.invokeLater(() -> offer(frame, current, tag.group(1), release));
                }
            } catch (IOException | RuntimeException e) {
                // Offline or GitHub unreachable: try again next launch
            }
        }, "update-check");
        thread.setDaemon(true);
        thread.start();
    }

    // Compares dotted versions numerically, so "2.10" > "2.9"
    static int compare(String a, String b) {
        String[] x = a.split("\\.");
        String[] y = b.split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int diff = Integer.compare(part(x, i), part(y, i));
            if (diff != 0) return diff;
        }
        return 0;
    }

    private static int part(String[] parts, int i) {
        try {
            return i < parts.length ? Integer.parseInt(parts[i]) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void offer(JFrame frame, String current, String latest, String release) {
        Install install = detectInstall();
        String message = "MyCalendar " + latest + " is available (you have " + current + ").";
        if (install == null) {
            int choice = JOptionPane.showConfirmDialog(frame,
                    message + "\nThis copy can't update itself (for example a .deb/.rpm, portable or .jar install).\n"
                            + "Open the download page?",
                    "Update available", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) openReleasesPage(frame);
            return;
        }

        Object[] options = {"Update now", "Later"};
        int choice = JOptionPane.showOptionDialog(frame,
                message + "\nUpdate now? MyCalendar will restart, and your calendar is kept.",
                "Update available", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);
        if (choice == 0) download(frame, install, release);
    }

    private static void download(JFrame frame, Install install, String release) {
        JDialog progress = new JDialog(frame, "Updating MyCalendar", false);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        JLabel label = new JLabel("Downloading the update...");
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        progress.getContentPane().setLayout(new BorderLayout());
        ((JComponent) progress.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        progress.add(label, BorderLayout.NORTH);
        progress.add(bar, BorderLayout.CENTER);
        progress.pack();
        progress.setLocationRelativeTo(frame);
        progress.setVisible(true);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String fileUrl = assetUrl(release, install.assetSuffix);
                String sumsUrl = assetUrl(release, "SHA256SUMS.txt");
                if (fileUrl == null || sumsUrl == null) throw new IOException("update files not found in the release");

                String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
                String expected = expectedHash(new String(fetch(sumsUrl), StandardCharsets.UTF_8), fileName);
                if (expected == null) throw new IOException("no checksum published for " + fileName);

                byte[] data = fetch(fileUrl);
                String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
                if (!actual.equalsIgnoreCase(expected)) throw new IOException("checksum mismatch, download discarded");

                Files.write(install.download, data);
                startInstaller(install);
                return null;
            }

            @Override
            protected void done() {
                progress.dispose();
                try {
                    get();
                    System.exit(0); // the shutdown hook saves the calendar; the installer waits for us to quit
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(frame,
                            "The update couldn't be installed: " + cause.getMessage()
                                    + "\nThe download page will open instead.",
                            "Update failed", JOptionPane.WARNING_MESSAGE);
                    openReleasesPage(frame);
                }
            }
        }.execute();
    }

    // Describes how this copy was installed, or null if it can't be replaced in place
    private static final class Install {
        final String kind;        // msi, mac, linux or appimage
        final String assetSuffix; // which release file to download
        final Path target;        // what gets replaced
        final Path download;      // where the verified download is stored

        Install(String kind, String assetSuffix, Path target, Path download) {
            this.kind = kind;
            this.assetSuffix = assetSuffix;
            this.target = target;
            this.download = download;
        }
    }

    private static Install detectInstall() {
        String appPath = System.getProperty("jpackage.app-path");
        if (appPath == null) return null; // running from the .jar
        Path launcher = Paths.get(appPath).toAbsolutePath();
        Path temp = Paths.get(System.getProperty("java.io.tmpdir"));

        if (WINDOWS) {
            Path appDir = launcher.getParent(); // ...\MyCalendar\MyCalendar.exe
            if (Files.exists(appDir.resolve(".portable"))) return null;
            return new Install("msi", "Windows-x64.msi", launcher, temp.resolve("MyCalendar-update.msi"));
        }
        if (MAC) {
            Path bundle = launcher.getParent().getParent().getParent(); // .../MyCalendar.app/Contents/MacOS/MyCalendar
            if (!bundle.toString().endsWith(".app") || !Files.isWritable(bundle.getParent())) return null;
            String chip = System.getProperty("os.arch").contains("aarch64") ? "AppleSilicon" : "Intel";
            return new Install("mac", "macOS-" + chip + ".dmg", bundle, temp.resolve("MyCalendar-update.dmg"));
        }
        String appImage = System.getenv("APPIMAGE");
        if (appImage != null) {
            Path file = Paths.get(appImage);
            if (!Files.isWritable(file.getParent())) return null;
            return new Install("appimage", "Linux-x86_64.AppImage", file, Paths.get(appImage + ".new"));
        }
        Path root = launcher.getParent().getParent(); // .../MyCalendar/bin/MyCalendar
        if (!Files.isWritable(root.getParent()) || !Files.isWritable(root)) return null; // /opt install from .deb/.rpm
        return new Install("linux", "Linux-x86_64.tar.gz", root, temp.resolve("MyCalendar-update.tar.gz"));
    }

    private static void startInstaller(Install install) throws IOException {
        String pid = String.valueOf(ProcessHandle.current().pid());
        ProcessBuilder builder;
        if (WINDOWS) {
            // Waits for MyCalendar to close, upgrades it with the verified MSI, then restarts it
            String script = "Wait-Process -Id " + pid + " -ErrorAction SilentlyContinue; "
                    + "Start-Process msiexec.exe -ArgumentList '/i', '\"" + quote(install.download) + "\"', '/passive', '/norestart' -Wait; "
                    + "Remove-Item -LiteralPath '" + quote(install.download) + "' -ErrorAction SilentlyContinue; "
                    + "if (Test-Path -LiteralPath '" + quote(install.target) + "') { Start-Process -FilePath '" + quote(install.target) + "' }";
            builder = new ProcessBuilder("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass",
                    "-WindowStyle", "Hidden", "-Command", script);
        } else {
            builder = new ProcessBuilder("/bin/sh", "-c", UNIX_INSTALLER, "mycalendar-update",
                    pid, install.kind, install.download.toString(), install.target.toString());
        }
        // The jpackage launcher sets these for itself; a restarted launcher that inherits them fails to start
        Map<String, String> env = builder.environment();
        env.remove("_JPACKAGE_LAUNCHER");
        String libraryPath = env.remove("LD_LIBRARY_PATH");
        if (libraryPath != null) {
            String appRoot = Paths.get(System.getProperty("jpackage.app-path")).getParent().getParent().toString();
            String cleaned = Arrays.stream(libraryPath.split(":"))
                    .filter(entry -> !entry.isEmpty() && !entry.startsWith(appRoot))
                    .collect(Collectors.joining(":"));
            if (!cleaned.isEmpty()) env.put("LD_LIBRARY_PATH", cleaned);
        }

        File log = new File(System.getProperty("java.io.tmpdir"), "mycalendar-update.log");
        builder.redirectErrorStream(true)
               .redirectOutput(log)
               .redirectInput(new File(WINDOWS ? "NUL" : "/dev/null"));
        builder.start();
    }

    // Escapes a path for a single-quoted PowerShell string
    private static String quote(Path path) {
        return path.toString().replace("'", "''");
    }

    static String assetUrl(String release, String suffix) {
        Matcher matcher = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"(https://[^\"]*" + Pattern.quote(suffix) + ")\"")
                .matcher(release);
        return matcher.find() ? matcher.group(1) : null;
    }

    // SHA256SUMS.txt lines look like "<hash>  <file name>"
    static String expectedHash(String sums, String fileName) {
        for (String line : sums.split("\\R")) {
            String[] parts = line.trim().split("\\s+\\*?", 2);
            if (parts.length == 2 && parts[1].equals(fileName) && parts[0].matches("[0-9a-fA-F]{64}")) return parts[0];
        }
        return null;
    }

    private static byte[] fetch(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "MyCalendar");
        if (url.startsWith("https://api.github.com/")) {
            connection.setRequestProperty("Accept", "application/vnd.github+json");
        }
        int status = connection.getResponseCode();
        if (status != 200) throw new IOException("HTTP " + status + " from " + url);
        try (InputStream in = connection.getInputStream()) {
            return in.readAllBytes();
        }
    }

    private static void openReleasesPage(JFrame frame) {
        try {
            Desktop.getDesktop().browse(URI.create(RELEASES_PAGE));
        } catch (IOException | UnsupportedOperationException e) {
            JOptionPane.showMessageDialog(frame, "Download the latest version from:\n" + RELEASES_PAGE);
        }
    }
}
