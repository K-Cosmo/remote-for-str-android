param(
    [string]$ProjectRoot
)

$ErrorActionPreference = "Stop"

if (-not $ProjectRoot) {
    $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
} else {
    $ProjectRoot = (Resolve-Path $ProjectRoot).Path
}

$failures = [System.Collections.Generic.List[string]]::new()

function Add-Failure([string]$Message) {
    $script:failures.Add($Message)
}

function Read-Required([string]$RelativePath) {
    $path = Join-Path $ProjectRoot $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        Add-Failure "Missing required file: $RelativePath"
        return ""
    }
    return Get-Content -LiteralPath $path -Raw -Encoding UTF8
}

function Capture-Required([string]$Text, [string]$Pattern, [string]$Label) {
    $match = [regex]::Match(
        $Text,
        $Pattern,
        [System.Text.RegularExpressions.RegexOptions]::Multiline
    )
    if (-not $match.Success) {
        Add-Failure "Could not read $Label"
        return ""
    }
    return $match.Groups[1].Value.Trim()
}

Write-Host "== STR Remote documentation consistency =="
Write-Host "Project: $ProjectRoot"

$buildFile = Read-Required "app/build.gradle.kts"
$statusFile = Read-Required "doc/STATUS.md"
$readmeFile = Read-Required "README.md"
$changelogFile = Read-Required "doc/CHANGELOG.md"
$gatesFile = Read-Required "doc/RELEASE_GATES.md"
$plannedFile = Read-Required "doc/PLANNED.md"

$appVersion = Capture-Required $buildFile 'versionName\s*=\s*"([^"]+)"' "application versionName"
$versionCode = Capture-Required $buildFile 'versionCode\s*=\s*(\d+)' "application versionCode"
$developmentVersion = Capture-Required $statusFile '^\*\*Development version:\*\*\s*`?([^`\r\n ]+)`?' "STATUS Development version"
$latestRelease = Capture-Required $statusFile '^\*\*Latest public release:\*\*\s*`?([^`\r\n ]+)`?' "STATUS Latest public release"
$statusState = Capture-Required $statusFile '^\*\*Status:\*\*\s*(.+?)\s*$' "STATUS state"
$readmeRelease = Capture-Required $readmeFile '^\*\*Current release:\*\*\s*`?([^`\r\n ]+)`?' "README Current release"

if ($appVersion -and $developmentVersion -and $appVersion -ne $developmentVersion) {
    Add-Failure "Version drift: app versionName '$appVersion' != STATUS Development version '$developmentVersion'"
}

if ($latestRelease -and $readmeRelease -and $latestRelease -ne $readmeRelease) {
    Add-Failure "Release drift: README Current release '$readmeRelease' != STATUS Latest public release '$latestRelease'"
}

if ($latestRelease) {
    # Accept "-", en dash, or em dash without embedding non-ASCII source characters.
    $releaseHeading = '(?m)^##\s+' + [regex]::Escape($latestRelease) + '(?:\s+[-\u2013\u2014]\s+.*)?\s*$'
    if (-not [regex]::IsMatch($changelogFile, $releaseHeading)) {
        Add-Failure "CHANGELOG has no release heading for latest public release '$latestRelease'"
    }
}

$isAccepted = $statusState -match '(?i)RELEASED\s*/\s*ACCEPTED|\bACCEPTED\b'
if ($isAccepted) {
    if ($developmentVersion -and $latestRelease -and $developmentVersion -ne $latestRelease) {
        Add-Failure "Accepted state is inconsistent: Development version '$developmentVersion' != Latest public release '$latestRelease'"
    }

    $unchecked = [regex]::Matches($gatesFile, '(?m)^\s*-\s*\[\s\]\s+')
    if ($unchecked.Count -gt 0) {
        Add-Failure "Accepted state still has $($unchecked.Count) unchecked release gate(s)"
    }

    if ($latestRelease -and $plannedFile -match [regex]::Escape("$latestRelease final publication")) {
        Add-Failure "PLANNED still describes accepted release '$latestRelease' as the current final-publication step"
    }
}

if ($versionCode) {
    Write-Host "App version:      $appVersion (code $versionCode)"
}
Write-Host "Development:      $developmentVersion"
Write-Host "Public release:   $latestRelease"
Write-Host "Status:           $statusState"

# Check relative links in the public README only. Internal documentation remains a human-maintained corpus.
$linkPattern = [regex]'!?\[[^\]]*\]\(([^)]+)\)'
foreach ($match in $linkPattern.Matches($readmeFile)) {
    $target = $match.Groups[1].Value.Trim()
    if (-not $target) { continue }

    if ($target -match '^(\S+)\s+["''].*["'']$') {
        $target = $matches[1]
    }
    $target = $target.Trim('<', '>')

    if ($target -match '^(?i:https?|mailto|ftp):' -or $target.StartsWith('#')) {
        continue
    }

    $target = ($target -split '#', 2)[0]
    $target = ($target -split '\?', 2)[0]
    if (-not $target) { continue }

    try {
        $target = [Uri]::UnescapeDataString($target)
    } catch {
        Add-Failure "Invalid README link encoding: $target"
        continue
    }

    if ($target.StartsWith('/')) {
        $candidate = Join-Path $ProjectRoot $target.TrimStart('/')
    } else {
        $candidate = Join-Path $ProjectRoot $target
    }

    if (-not (Test-Path -LiteralPath $candidate)) {
        Add-Failure "Broken relative README link: $target"
    }
}

if ($failures.Count -gt 0) {
    Write-Host ""
    Write-Host "Documentation consistency FAILED:" -ForegroundColor Red
    foreach ($failure in $failures) {
        Write-Host " - $failure" -ForegroundColor Red
    }
    throw "Documentation consistency failed."
}

Write-Host "Documentation consistency PASSED." -ForegroundColor Green
