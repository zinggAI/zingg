param([Parameter(Mandatory=$true)][string]$Bundle)
$ErrorActionPreference = 'Stop'
$launcher = Join-Path $Bundle 'bin\zingg-duckdb.cmd'
$java = Join-Path $Bundle 'runtime\java\bin\java.exe'
if (!(Test-Path -LiteralPath $launcher)) { throw "bundled launcher is missing: $launcher" }
if (!(Test-Path -LiteralPath $java)) { throw "bundled Java runtime is missing: $java" }
$psi = [System.Diagnostics.ProcessStartInfo]::new()
$psi.FileName = $launcher
$psi.WorkingDirectory = (Resolve-Path $Bundle).Path
$psi.UseShellExecute = $false
$psi.CreateNoWindow = $true
$psi.RedirectStandardInput = $true
$psi.RedirectStandardOutput = $true
$process = [System.Diagnostics.Process]::new()
$process.StartInfo = $psi
try {
  if (!$process.Start()) { throw 'worker failed to start' }
  $process.StandardInput.WriteLine("kill`tping`t")
  $process.StandardInput.Flush()
  $response = $process.StandardOutput.ReadLine()
  if ($response -ne "kill`tok`tpong") { throw "unexpected ping response: $response" }
  $process.Kill($true)
  if (!$process.WaitForExit(10000)) { throw 'worker process tree did not terminate' }
  Start-Sleep -Milliseconds 200
  Write-Output 'PROCESS_KILL_WINDOWS_SMOKE_SUCCESS'
} finally {
  if (!$process.HasExited) { $process.Kill($true); $process.WaitForExit(10000) }
  $process.Dispose()
}
