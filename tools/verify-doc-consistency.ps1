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

Write-Host "== Remote for STR documentation consistency =="
Write-Host "Project: $ProjectRoot"

$buildFile = Read-Required "app/build.gradle.kts"
$statusFile = Read-Required "doc/STATUS.md"
$readmeFile = Read-Required "README.md"
$changelogFile = Read-Required "doc/CHANGELOG.md"
$gatesFile = Read-Required "doc/RELEASE_GATES.md"
$plannedFile = Read-Required "doc/PLANNED.md"

# Privacy-hygiene guard: repository evidence/doc must use documentation test
# ranges instead of real household or organizational LAN endpoints.
# The sole exception is Bose's explicitly documented alternative setup host.
# Never print matched private addresses into CI logs.
$docRoot = Join-Path $ProjectRoot "doc"
$privateIpv4 = [regex]'\b(?:10\.(?:\d{1,3}\.){2}\d{1,3}|192\.168\.\d{1,3}\.\d{1,3}|172\.(?:1[6-9]|2\d|3[01])\.\d{1,3}\.\d{1,3})\b'
$protocolExamples = @("192.168.1.1")
if (Test-Path -LiteralPath $docRoot -PathType Container) {
    Get-ChildItem -LiteralPath $docRoot -Recurse -File -Filter "*.md" |
        ForEach-Object {
            $file = $_
            $rows = Get-Content -LiteralPath $file.FullName -Encoding UTF8
            for ($lineIndex = 0; $lineIndex -lt $rows.Count; $lineIndex++) {
                foreach ($hit in $privateIpv4.Matches($rows[$lineIndex])) {
                    if ($hit.Value -notin $protocolExamples) {
                        $relative = $file.FullName.Substring($ProjectRoot.Length).TrimStart('\', '/')
                        Add-Failure "Potential real LAN IPv4 at $($relative):$($lineIndex + 1). Replace with RFC5737 documentation IP; do not log the original."
                    }
                }
            }
        }
}


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

# Validate links in public entry points and current normative doc/*.md files.
# Dated work/evidence and specs are preserved snapshots, not current policy.
$linkPattern = [regex]'!?\[[^\]]*\]\(([^)]+)\)'
$entryFiles = @("README.md", "AGENTS.md", "CONTRIBUTING.md", "PRIVACY.md") |
    ForEach-Object { Join-Path $ProjectRoot $_ }
$normativeFiles = @(Get-ChildItem -LiteralPath $docRoot -File -Filter "*.md" |
    ForEach-Object { $_.FullName })
$markdownFiles = @($entryFiles) + $normativeFiles

foreach ($file in $markdownFiles) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        Add-Failure "Missing Markdown entry point: $file"
        continue
    }

    $text = Get-Content -LiteralPath $file -Raw -Encoding UTF8
    $relativeSource = $file.Substring($ProjectRoot.Length).TrimStart('\', '/')

    foreach ($match in $linkPattern.Matches($text)) {
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
            Add-Failure "Invalid Markdown link encoding in $($relativeSource)"
            continue
        }

        if ($target.StartsWith('/')) {
            $candidate = Join-Path $ProjectRoot $target.TrimStart('/')
        } else {
            $candidate = Join-Path (Split-Path -Parent $file) $target
        }

        if (-not (Test-Path -LiteralPath $candidate)) {
            Add-Failure "Broken relative Markdown link in $($relativeSource): $target"
        }
    }
}

# Regression guard for canonical decision and finding identifiers.
foreach ($register in @(
    @{ Path = "doc/DECISIONS.md"; Prefix = "D" },
    @{ Path = "doc/FINDINGS.md"; Prefix = "F" }
)) {
    $document = Read-Required $register.Path
    $pattern = '(?m)^##\s+(' + $register.Prefix + '-\d{3})\b'
    $ids = @([regex]::Matches($document, $pattern) |
        ForEach-Object { $_.Groups[1].Value })
    $duplicates = @($ids | Group-Object | Where-Object { $_.Count -gt 1 })
    foreach ($duplicate in $duplicates) {
        Add-Failure "Duplicate register ID in $($register.Path): $($duplicate.Name)"
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
