param(
    [Parameter(Mandatory = $false)]
    [string]$ProjectRoot = ".",

    [Parameter(Mandatory = $true)]
    [string]$KeystorePath,

    [Parameter(Mandatory = $false)]
    [string]$KeyAlias = ""
)

$ErrorActionPreference = "Stop"
$ProjectRoot = (Resolve-Path $ProjectRoot).Path
$KeystorePath = (Resolve-Path $KeystorePath).Path

function Resolve-AndroidSdk {
    param([string]$Root)

    $localProperties = Join-Path $Root "local.properties"
    if (Test-Path $localProperties) {
        $line = Get-Content $localProperties | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($line) {
            $value = $line.Substring("sdk.dir=".Length)
            $value = $value -replace '\\\\', '\'
            $value = $value -replace '\\:', ':'
            if (Test-Path $value) { return (Resolve-Path $value).Path }
        }
    }

    foreach ($candidate in @(
        $env:ANDROID_SDK_ROOT,
        $env:ANDROID_HOME,
        (Join-Path $env:LOCALAPPDATA "Android\Sdk")
    )) {
        if ($candidate -and (Test-Path $candidate)) {
            return (Resolve-Path $candidate).Path
        }
    }

    throw "Android SDK not found. Check local.properties, ANDROID_SDK_ROOT/ANDROID_HOME or %LOCALAPPDATA%\Android\Sdk."
}

function Resolve-BuildTool {
    param(
        [string]$SdkRoot,
        [string]$ToolName
    )

    $buildToolsRoot = Join-Path $SdkRoot "build-tools"
    if (-not (Test-Path $buildToolsRoot)) {
        throw "Android build-tools missing under $buildToolsRoot"
    }

    $dirs = Get-ChildItem $buildToolsRoot -Directory | Sort-Object {
        try { [version]$_.Name } catch { [version]'0.0' }
    } -Descending

    foreach ($dir in $dirs) {
        $candidate = Join-Path $dir.FullName $ToolName
        if (Test-Path $candidate) { return $candidate }
    }

    throw "$ToolName not found in Android build-tools."
}

function Read-AppVersion {
    param([string]$Root)

    $buildFile = Join-Path $Root "app\build.gradle.kts"
    if (-not (Test-Path $buildFile)) {
        throw "app/build.gradle.kts not found."
    }

    $text = Get-Content -LiteralPath $buildFile -Raw -Encoding UTF8
    $versionMatch = [regex]::Match($text, 'versionName\s*=\s*"([^"]+)"')
    $codeMatch = [regex]::Match($text, 'versionCode\s*=\s*(\d+)')

    if (-not $versionMatch.Success -or -not $codeMatch.Success) {
        throw "Could not read versionName/versionCode from app/build.gradle.kts."
    }

    $versionName = $versionMatch.Groups[1].Value
    $versionCode = [int]$codeMatch.Groups[1].Value

    if ($versionName -notmatch '^[0-9]+\.[0-9]+\.[0-9]+(?:[-+][0-9A-Za-z.-]+)?$') {
        throw "Unsupported versionName for release artifact naming: $versionName"
    }

    return [PSCustomObject]@{
        Name = $versionName
        Code = $versionCode
    }
}

if (-not (Get-Command java.exe -ErrorAction SilentlyContinue)) {
    $jbr = "C:\Program Files\Android\Android Studio\jbr"
    if (Test-Path (Join-Path $jbr "bin\java.exe")) {
        $env:JAVA_HOME = $jbr
        $env:Path = "$jbr\bin;$env:Path"
    } else {
        throw "Java not found. Provide Java 25 / Android Studio JBR through JAVA_HOME or PATH."
    }
}

if ([string]::IsNullOrWhiteSpace($KeyAlias)) {
    $KeyAlias = Read-Host "Keystore alias"
}
if ([string]::IsNullOrWhiteSpace($KeyAlias)) {
    throw "Keystore alias must not be empty."
}

$gradlew = Join-Path $ProjectRoot "gradlew.bat"
if (-not (Test-Path $gradlew)) { throw "gradlew.bat missing under $ProjectRoot" }

$sdkRoot = Resolve-AndroidSdk -Root $ProjectRoot
$zipalign = Resolve-BuildTool -SdkRoot $sdkRoot -ToolName "zipalign.exe"
$apksigner = Resolve-BuildTool -SdkRoot $sdkRoot -ToolName "apksigner.bat"
$appVersion = Read-AppVersion -Root $ProjectRoot

Push-Location $ProjectRoot
try {
    $gitHead = (& git rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($gitHead)) {
        throw "Could not determine Git HEAD."
    }

    $gitStatus = & git status --porcelain --untracked-files=all
    if ($LASTEXITCODE -ne 0) {
        throw "Could not determine Git working-tree state."
    }
    if ($gitStatus) {
        throw "Working tree is not clean. Commit/stash/restore all changes before building a release artifact."
    }

    Write-Host "== STR Remote $($appVersion.Name) final build =="
    Write-Host "Version code: $($appVersion.Code)"
    Write-Host "Git commit:   $gitHead"
    Write-Host "Project:      $ProjectRoot"
    Write-Host "SDK:          $sdkRoot"
    Write-Host "Keystore:     $KeystorePath"
    Write-Host "Alias:        $KeyAlias"
    Write-Host ""

    & $gradlew --version
    if ($LASTEXITCODE -ne 0) { throw "Gradle --version failed ($LASTEXITCODE)." }

    & $gradlew clean :app:assembleDebug :app:lintDebug :app:assembleRelease --stacktrace
    if ($LASTEXITCODE -ne 0) { throw "Final Gradle build failed ($LASTEXITCODE)." }

    $releaseDir = Join-Path $ProjectRoot "app\build\outputs\apk\release"
    $unsigned = Get-ChildItem $releaseDir -Filter "*.apk" -File |
        Sort-Object `
            @{ Expression = { if ($_.Name -match 'unsigned') { 0 } else { 1 } }; Ascending = $true }, `
            @{ Expression = { $_.LastWriteTime }; Descending = $true } |
        Select-Object -First 1

    if (-not $unsigned) { throw "No release APK found under $releaseDir." }

    $distDir = Join-Path $ProjectRoot "dist"
    New-Item -ItemType Directory -Path $distDir -Force | Out-Null

    $baseName = "STR-Remote-$($appVersion.Name)"
    $alignedApk = Join-Path $distDir "$baseName-aligned-unsigned.apk"
    $signedApk = Join-Path $distDir "$baseName.apk"
    $hashFile = Join-Path $distDir "$baseName.apk.sha256.txt"

    Remove-Item $alignedApk, $signedApk, $hashFile -Force -ErrorAction SilentlyContinue

    Write-Host ""
    Write-Host "== zipalign =="
    & $zipalign -P 16 -f -v 4 $unsigned.FullName $alignedApk
    if ($LASTEXITCODE -ne 0) { throw "zipalign failed ($LASTEXITCODE)." }

    Write-Host ""
    Write-Host "== APK signing =="
    Write-Host "apksigner requests the keystore/key password interactively; nothing is stored."
    & $apksigner sign --ks $KeystorePath --ks-key-alias $KeyAlias --out $signedApk $alignedApk
    if ($LASTEXITCODE -ne 0) { throw "apksigner sign failed ($LASTEXITCODE)." }

    Write-Host ""
    Write-Host "== Signature verification =="
    & $apksigner verify --verbose --print-certs $signedApk
    if ($LASTEXITCODE -ne 0) { throw "apksigner verify failed ($LASTEXITCODE)." }

    & $zipalign -c -P 16 -v 4 $signedApk
    if ($LASTEXITCODE -ne 0) { throw "zipalign verification failed ($LASTEXITCODE)." }

    $hash = (Get-FileHash $signedApk -Algorithm SHA256).Hash.ToLowerInvariant()
    "$hash  $baseName.apk" | Set-Content -Path $hashFile -Encoding ascii

    Write-Host ""
    Write-Host "== Final artifact =="
    Write-Host $signedApk
    Write-Host "SHA-256: $hash"
    Write-Host "Hash file: $hashFile"
    Write-Host "Source commit: $gitHead"
    Write-Host ""
    Write-Host "Next gate: install exactly this APK on the real device and run the final release smoke test."
}
finally {
    Pop-Location
}
