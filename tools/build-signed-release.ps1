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

    throw "Android SDK nicht gefunden. local.properties, ANDROID_SDK_ROOT/ANDROID_HOME oder %LOCALAPPDATA%\Android\Sdk prüfen."
}

function Resolve-BuildTool {
    param(
        [string]$SdkRoot,
        [string]$ToolName
    )

    $buildToolsRoot = Join-Path $SdkRoot "build-tools"
    if (-not (Test-Path $buildToolsRoot)) {
        throw "Android build-tools fehlen unter $buildToolsRoot"
    }

    $dirs = Get-ChildItem $buildToolsRoot -Directory | Sort-Object {
        try { [version]$_.Name } catch { [version]'0.0' }
    } -Descending

    foreach ($dir in $dirs) {
        $candidate = Join-Path $dir.FullName $ToolName
        if (Test-Path $candidate) { return $candidate }
    }

    throw "$ToolName wurde in den Android build-tools nicht gefunden."
}

# Wrapper startup still needs Java on PATH/JAVA_HOME. Prefer the verified Android Studio JBR if necessary.
if (-not (Get-Command java.exe -ErrorAction SilentlyContinue)) {
    $jbr = "C:\Program Files\Android\Android Studio\jbr"
    if (Test-Path (Join-Path $jbr "bin\java.exe")) {
        $env:JAVA_HOME = $jbr
        $env:Path = "$jbr\bin;$env:Path"
    } else {
        throw "Java nicht gefunden. Android Studio JBR/Java 25 über JAVA_HOME oder PATH bereitstellen."
    }
}

if ([string]::IsNullOrWhiteSpace($KeyAlias)) {
    $KeyAlias = Read-Host "Keystore-Alias"
}
if ([string]::IsNullOrWhiteSpace($KeyAlias)) {
    throw "Keystore-Alias darf nicht leer sein."
}

$gradlew = Join-Path $ProjectRoot "gradlew.bat"
if (-not (Test-Path $gradlew)) { throw "gradlew.bat fehlt unter $ProjectRoot" }

$sdkRoot = Resolve-AndroidSdk -Root $ProjectRoot
$zipalign = Resolve-BuildTool -SdkRoot $sdkRoot -ToolName "zipalign.exe"
$apksigner = Resolve-BuildTool -SdkRoot $sdkRoot -ToolName "apksigner.bat"

Write-Host "== STR Remote 0.1.0 final build =="
Write-Host "Project:   $ProjectRoot"
Write-Host "SDK:       $sdkRoot"
Write-Host "Keystore:  $KeystorePath"
Write-Host "Alias:     $KeyAlias"
Write-Host ""

Push-Location $ProjectRoot
try {
    & $gradlew --version
    if ($LASTEXITCODE -ne 0) { throw "Gradle --version fehlgeschlagen ($LASTEXITCODE)." }

    & $gradlew clean :app:assembleDebug :app:lintDebug :app:assembleRelease --stacktrace
    if ($LASTEXITCODE -ne 0) { throw "Finaler Gradle Build fehlgeschlagen ($LASTEXITCODE)." }

    $releaseDir = Join-Path $ProjectRoot "app\build\outputs\apk\release"
    $unsigned = Get-ChildItem $releaseDir -Filter "*.apk" -File |
        Sort-Object `
            @{ Expression = { if ($_.Name -match 'unsigned') { 0 } else { 1 } }; Ascending = $true }, `
            @{ Expression = { $_.LastWriteTime }; Descending = $true } |
        Select-Object -First 1

    if (-not $unsigned) { throw "Kein Release-APK unter $releaseDir gefunden." }

    $distDir = Join-Path $ProjectRoot "dist"
    New-Item -ItemType Directory -Path $distDir -Force | Out-Null

    $alignedApk = Join-Path $distDir "STR-Remote-0.1.0-aligned-unsigned.apk"
    $signedApk = Join-Path $distDir "STR-Remote-0.1.0.apk"
    $hashFile = Join-Path $distDir "STR-Remote-0.1.0.apk.sha256.txt"

    Remove-Item $alignedApk, $signedApk, $hashFile -Force -ErrorAction SilentlyContinue

    Write-Host ""
    Write-Host "== zipalign =="
    & $zipalign -P 16 -f -v 4 $unsigned.FullName $alignedApk
    if ($LASTEXITCODE -ne 0) { throw "zipalign fehlgeschlagen ($LASTEXITCODE)." }

    Write-Host ""
    Write-Host "== APK signing =="
    Write-Host "apksigner fragt das Keystore-/Key-Passwort interaktiv ab; nichts wird gespeichert."
    & $apksigner sign --ks $KeystorePath --ks-key-alias $KeyAlias --out $signedApk $alignedApk
    if ($LASTEXITCODE -ne 0) { throw "apksigner sign fehlgeschlagen ($LASTEXITCODE)." }

    Write-Host ""
    Write-Host "== Signature verification =="
    & $apksigner verify --verbose --print-certs $signedApk
    if ($LASTEXITCODE -ne 0) { throw "apksigner verify fehlgeschlagen ($LASTEXITCODE)." }

    & $zipalign -c -P 16 -v 4 $signedApk
    if ($LASTEXITCODE -ne 0) { throw "zipalign verification fehlgeschlagen ($LASTEXITCODE)." }

    $hash = (Get-FileHash $signedApk -Algorithm SHA256).Hash.ToLowerInvariant()
    "$hash  STR-Remote-0.1.0.apk" | Set-Content -Path $hashFile -Encoding ascii

    Write-Host ""
    Write-Host "== Final artifact =="
    Write-Host $signedApk
    Write-Host "SHA-256: $hash"
    Write-Host "Hash file: $hashFile"
    Write-Host ""
    Write-Host "Nächster Gate: exakt dieses APK auf dem Realgerät installieren und Discovery/Connect/Control smoke-testen."
}
finally {
    Pop-Location
}
