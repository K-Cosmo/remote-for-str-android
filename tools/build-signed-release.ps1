param(
    [Parameter(Mandatory = $false)]
    [string]$ProjectRoot = ".",

    [Parameter(Mandatory = $true)]
    [string]$KeystorePath,

    [Parameter(Mandatory = $false)]
    [string]$KeyAlias = ""
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "apksigner-certificate.ps1")
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

    Write-Host "== Remote for STR $($appVersion.Name) final build =="
    Write-Host "Version code: $($appVersion.Code)"
    Write-Host "Git commit:   $gitHead"
    Write-Host "Project:      $ProjectRoot"
    Write-Host "SDK:          $sdkRoot"
    Write-Host "Keystore:     $KeystorePath"
    Write-Host "Alias:        $KeyAlias"
    Write-Host ""

    & $gradlew --version
    if ($LASTEXITCODE -ne 0) { throw "Gradle --version failed ($LASTEXITCODE)." }

    & $gradlew clean :app:assembleDebug :app:lintDebug :app:testDebugUnitTest :app:assembleRelease :app:lintRelease --stacktrace
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

    $baseName = "Remote-for-STR-$($appVersion.Name)"
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
    # Windows PowerShell 5.1 turns native stderr warnings into NativeCommandError
    # records when 2>&1 is used. With the script-wide Stop preference that can
    # abort verification even if apksigner exits successfully (e.g. Java's
    # restricted-native-access warning). Scope Continue to this one native
    # invocation; never bypass the actual exit, cert or zipalign checks.
    $previousErrorActionPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = "Continue"
        $verificationOutput = & $apksigner verify --verbose --print-certs $signedApk 2>&1
        $verifyExit = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $verificationOutput | ForEach-Object { Write-Host $_ }
    if ($verifyExit -ne 0) { throw "apksigner verify failed ($verifyExit)." }

    # Without precisely the same signing key as v0.2.1, Android will refuse
    # the update. Refuse to package an unusable or unexpectedly signed APK.
    $expectedCertificate = "3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32"
    # Do not use Out-String: its host-width formatting may split a
    # 64-character SHA-256 fingerprint. The helper recognizes old apksigner
    # "Signer #1" and Build Tools 37 "V2 Signer:" / "V3.0 Signer:"
    # output, and rejects conflicting fingerprints.
    $verifyText = (@($verificationOutput | ForEach-Object { $_.ToString() })) -join "`n"
    $actualCertificate = Get-ApkSignerCertificateDigest -OutputText $verifyText
    if ($actualCertificate -ne $expectedCertificate) {
        throw "Wrong release-signing certificate ($actualCertificate): APK cannot update STR Remote 0.2.1."
    }
    Write-Host "Release signer matches the v0.2.1 update identity."

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
