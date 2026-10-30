# Run with PowerShell 5.1 or pwsh (CI): no Android SDK or signing key needed.
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "apksigner-certificate.ps1")

$expected = "3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32"
$wrong = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
$newline = [Environment]::NewLine

function Assert-Digest {
    param([string]$Text, [string]$Expected)
    $found = Get-ApkSignerCertificateDigest -OutputText $Text
    if ($found -cne $Expected) { throw "Certificate parsing regression: found '$found'." }
}

function Assert-Rejected {
    param([string]$Text)
    $rejected = $false
    try {
        $result = Get-ApkSignerCertificateDigest -OutputText $Text
    } catch {
        $rejected = $true
    }
    if (-not $rejected) { throw "Certificate parsing must reject this output." }
}

Assert-Digest "Signer #1 certificate SHA-256 digest: $expected" $expected
Assert-Digest "V2 Signer: certificate SHA-256 digest: $expected" $expected
Assert-Digest "V3.0 Signer: certificate SHA-256 digest: $expected" $expected
Assert-Digest "V3.1 Signer: certificate SHA-256 digest: $expected" $expected
Assert-Digest "Signer (minSdkVersion=24, maxSdkVersion=32) certificate SHA-256 digest: $expected" $expected

# Java stderr is irrelevant to identifying the certificate.
$warning = "WARNING: A restricted method in java.lang.System has been called"
Assert-Digest ("$warning$newline" + "V2 Signer: certificate SHA-256 digest: $expected") $expected

# Multiple signing schemes must all identify the same certificate.
$consistent = "V2 Signer: certificate SHA-256 digest: $expected" + $newline + "V3.0 Signer: certificate SHA-256 digest: $expected"
Assert-Digest $consistent $expected

# Never mistake a public key digest for a certificate fingerprint.
Assert-Rejected "Signer #1 public key SHA-256 digest: $expected"
Assert-Rejected ("$warning$newline" + "No certificate found")
Assert-Rejected ("Signer #1 certificate SHA-256 digest: $expected" + $newline + "V2 Signer: certificate SHA-256 digest: $wrong")

Write-Host "apksigner certificate-output parser: 10 regression scenarios PASSED"
