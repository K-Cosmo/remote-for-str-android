# Offline-only preparation: does NOT push, merge, delete remote refs or touch Releases.
[CmdletBinding()]
param(
    [string]$RepositoryUrl = "https://github.com/K-Cosmo/remote-for-str-android.git",
    [string]$Workspace = (Join-Path (Split-Path -Parent (Get-Location).Path) "remote-for-str-history-cleanup")
)
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Invoke-Git {
    param([string[]]$GitArgs)
    & git @GitArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Git failed with exit status $LASTEXITCODE. No remote ref was changed."
    }
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    throw "Git is not available."
}
& git filter-repo --version *> $null
if ($LASTEXITCODE -ne 0) {
    throw "Install git-filter-repo first; verify: git filter-repo --version"
}
if (Test-Path -LiteralPath $Workspace) {
    throw "Workspace already exists. Supply a NEW empty -Workspace directory."
}

# This stays on your own computer. Never commit or paste the historical address.
$privateAddress = Read-Host "Enter the exact old private LAN IPv4 (local-only input)"
$parsed = $null
if ($privateAddress -notmatch '^\d{1,3}(\.\d{1,3}){3}$' -or
    -not [System.Net.IPAddress]::TryParse($privateAddress, [ref]$parsed) -or
    $parsed.AddressFamily -ne [System.Net.Sockets.AddressFamily]::InterNetwork) {
    throw "Expected dotted IPv4 format."
}
$o = $parsed.GetAddressBytes()
$private = $o[0] -eq 10 -or
    ($o[0] -eq 172 -and $o[1] -ge 16 -and $o[1] -le 31) -or
    ($o[0] -eq 192 -and $o[1] -eq 168)
if (-not $private) {
    throw "Address is not in an RFC1918 network. Stop and check the original evidence."
}

New-Item -ItemType Directory -Path $Workspace | Out-Null
$backup = Join-Path $Workspace "ORIGINAL-full-mirror.git"
$cleaned = Join-Path $Workspace "CLEANED-preview.git"
$replacementFile = Join-Path $Workspace "PRIVATE-mapping-TEMPORARY.txt"
$refSnapshot = Join-Path $Workspace "original-refs.txt"

Write-Host "Creating an original MIRROR backup (sensitive: keep encrypted and offline)."
Invoke-Git -GitArgs @("clone", "--mirror", $RepositoryUrl, $backup)
& git -C $backup show-ref | Set-Content -LiteralPath $refSnapshot -Encoding ascii
if ($LASTEXITCODE -ne 0) { throw "Could not snapshot refs." }

Write-Host "Creating a second MIRROR for offline replacement."
Invoke-Git -GitArgs @("clone", "--mirror", $RepositoryUrl, $cleaned)
try {
    [System.IO.File]::WriteAllText(
        $replacementFile,
        "literal:$privateAddress==>REDACTED_LOCAL_IP" + [Environment]::NewLine,
        (New-Object System.Text.UTF8Encoding($false))
    )
    Invoke-Git -GitArgs @("-C", $cleaned, "filter-repo", "--sensitive-data-removal",
        "--replace-text", $replacementFile)
} finally {
    if (Test-Path -LiteralPath $replacementFile) {
        Remove-Item -LiteralPath $replacementFile -Force
    }
}

# The original backup preserves these divergent branches for archival review.
$obsolete = @(
    "feature/0.2.1-ux",
    "feature/build-0012-bose-wifi-provisioning",
    "fix/release-030-apksigner-37-cert-format",
    "fix/release-030-apksigner-stderr",
    "fix/release-030-clean-user-text",
    "release/v0.3.0-prep"
)
foreach ($name in $obsolete) {
    $ref = "refs/heads/$name"
    & git -C $cleaned show-ref --quiet --verify $ref
    if ($LASTEXITCODE -eq 0) {
        Invoke-Git -GitArgs @("-C", $cleaned, "update-ref", "-d", $ref)
        Write-Host "Staged deletion of obsolete branch: $name"
    } elseif ($LASTEXITCODE -ne 1) {
        throw "Error checking a branch reference."
    }
}

# Verify the exact string is absent from *every historic commit tree*
# reachable from any remaining ref, including branches, tags and fetched PR
# refs. A clean tip and git log -S alone are not an exhaustive historical scan.
# This is still a targeted string scan, not a full credential audit.
$refs = @(& git -C $cleaned for-each-ref '--format=%(refname)' refs/heads refs/tags)
if ($LASTEXITCODE -ne 0 -or $refs.Count -lt 7) {
    throw "Unexpected branch/tag inventory; inspect before publishing."
}
foreach ($ref in $refs) {
    $null = & git -C $cleaned grep -I -l -F $privateAddress $ref --
    if ($LASTEXITCODE -eq 0) {
        throw "Private IPv4 still found in a rewritten ref. No remote push."
    }
    if ($LASTEXITCODE -ne 1) {
        throw "Git grep verification error. No remote push."
    }
}
$allCommits = @(& git -C $cleaned rev-list --all)
if ($LASTEXITCODE -ne 0 -or $allCommits.Count -lt 1) {
    throw "No rewritten commits found. No remote push."
}
foreach ($commitId in $allCommits) {
    # This emits neither matching text nor the address to the terminal.
    & git -C $cleaned grep -I -F -q -- $privateAddress $commitId --
    if ($LASTEXITCODE -eq 0) {
        throw "Historical private address remains in a reachable commit. No remote push."
    }
    if ($LASTEXITCODE -ne 1) {
        throw "Historical Git-object scan failed. No remote push."
    }
}
Write-Host "Verified $($allCommits.Count) reachable commit trees for the targeted private address."

Write-Host "Offline rewrite prepared and checked; NOTHING PUSHED."
Write-Host "Sensitive backup: $backup"
Write-Host "Rewritten preview: $cleaned"
$pullHeadRefs = @(& git -C $backup for-each-ref "--format=%(refname)" "refs/pull")
if ($LASTEXITCODE -ne 0) {
    throw "Could not inspect GitHub PR refs from original mirror."
}
Write-Host "Original mirror PR refs requiring separate GitHub verification: $($pullHeadRefs.Count)"
Write-Host "Original ref snapshot: $refSnapshot"
Write-Host "STOP: review doc/GIT_HISTORY_CLEANUP.md before any force-push."
