param(
  [Parameter(Mandatory=$true)][string]$Bundle,
  [Parameter(Mandatory=$true)][string]$CertificateThumbprint,
  [string]$TimestampServer = 'http://timestamp.digicert.com'
)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $Bundle).Path
$hashFile = Join-Path $root 'SHA256SUMS'
if (-not (Test-Path -LiteralPath $hashFile -PathType Leaf)) { throw 'SHA256SUMS is missing' }
$cert = Get-ChildItem Cert:\CurrentUser\My\$CertificateThumbprint -ErrorAction SilentlyContinue
if (-not $cert) { $cert = Get-ChildItem Cert:\LocalMachine\My\$CertificateThumbprint -ErrorAction SilentlyContinue }
if (-not $cert) { throw "signing certificate not found: $CertificateThumbprint" }
$signature = Set-AuthenticodeSignature -FilePath $hashFile -Certificate $cert -TimestampServer $TimestampServer
if ($signature.Status -ne 'Valid') { throw "package signature failed: $($signature.Status) $($signature.StatusMessage)" }
Write-Output (Join-Path $root 'SHA256SUMS')
