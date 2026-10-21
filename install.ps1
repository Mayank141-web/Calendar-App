# Installs (or updates to) the latest MyCalendar on Windows.
#   irm https://raw.githubusercontent.com/Mayank141-web/Calendar-App/main/install.ps1 | iex
# No admin rights needed. The download is checked against the release's SHA256SUMS.txt.
& {
    $ErrorActionPreference = 'Stop'
    $ProgressPreference = 'SilentlyContinue' # makes downloads much faster in Windows PowerShell 5.1
    [Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12

    $repo = 'Mayank141-web/Calendar-App'
    $headers = @{ 'User-Agent' = 'MyCalendar-installer' }
    $release = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/releases/latest" -Headers $headers
    $version = $release.tag_name -replace '^v', ''

    $msiAsset = $release.assets | Where-Object { $_.name -like '*Windows-x64.msi' } | Select-Object -First 1
    $sumsAsset = $release.assets | Where-Object { $_.name -eq 'SHA256SUMS.txt' } | Select-Object -First 1
    if (-not $msiAsset -or -not $sumsAsset) { throw 'The latest release has no Windows installer.' }

    $temp = Join-Path ([IO.Path]::GetTempPath()) ('mycalendar-' + [guid]::NewGuid())
    New-Item -ItemType Directory -Path $temp | Out-Null
    try {
        Write-Host "Downloading MyCalendar $version..."
        $msi = Join-Path $temp $msiAsset.name
        Invoke-WebRequest -Uri $msiAsset.browser_download_url -OutFile $msi -Headers $headers -UseBasicParsing
        $sums = (Invoke-WebRequest -Uri $sumsAsset.browser_download_url -Headers $headers -UseBasicParsing).Content
        if ($sums -is [byte[]]) { $sums = [Text.Encoding]::UTF8.GetString($sums) }

        $expected = $null
        foreach ($line in ($sums -split "`r?`n")) {
            $parts = $line.Trim() -split '\s+\*?', 2
            if ($parts.Count -eq 2 -and $parts[1] -eq $msiAsset.name) { $expected = $parts[0] }
        }
        $actual = (Get-FileHash -Path $msi -Algorithm SHA256).Hash
        if (-not $expected -or $actual -ne $expected.ToUpperInvariant()) { throw 'Checksum mismatch, download discarded.' }

        Get-Process -Name MyCalendar -ErrorAction SilentlyContinue | Stop-Process -ErrorAction SilentlyContinue
        Write-Host 'Installing...'
        $install = Start-Process -FilePath msiexec.exe -ArgumentList '/i', "`"$msi`"", '/passive', '/norestart' -Wait -PassThru
        if ($install.ExitCode -ne 0 -and $install.ExitCode -ne 3010) { throw "The installer failed (code $($install.ExitCode))." }

        Write-Host "MyCalendar $version is installed. Open it from the Start menu or the desktop shortcut."
        Write-Host 'MyCalendar checks for updates each time it starts and installs them with one click.'
    }
    finally {
        Remove-Item -LiteralPath $temp -Recurse -Force -ErrorAction SilentlyContinue
    }
}
