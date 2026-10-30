# Pure parsing helper for apksigner --print-certs (no keystore or APK access).
# Android Build Tools 36 and earlier use "Signer #1 certificate ...";
# 37 also uses "V2 Signer:" / "V3.0 Signer:" prefixes.
function Get-ApkSignerCertificateDigest {
    param(
        [Parameter(Mandatory = $true)]
        [string]$OutputText
    )

    $pattern = '(?im)^[ \t]*(?:Signer\s+#\d+|Signer\s+\([^\r\n)]+\)|V\d+(?:\.\d+)?\s+Signer:)\s+certificate\s+SHA-256\s+digest:[ \t]*([0-9a-f]{64})[ \t]*$'
    $matches = [regex]::Matches($OutputText, $pattern)

    if ($matches.Count -eq 0) {
        throw "Could not locate a supported signing-certificate SHA-256 digest in apksigner output. Check the printed verification details above."
    }

    # A single APK may report v2 and v3.0 with the same certificate.
    # Different certificates across schemes/signers are a release blocker.
    $uniqueDigests = @($matches |
        ForEach-Object { $_.Groups[1].Value.ToLowerInvariant() } |
        Sort-Object -Unique)

    if ($uniqueDigests.Count -ne 1) {
        throw "Multiple different signing certificates were reported by apksigner. Refusing to publish an ambiguous APK."
    }

    return $uniqueDigests[0]
}
