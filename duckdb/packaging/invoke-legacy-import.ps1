param(
  [Parameter(Mandatory=$true)][string]$SparkSubmit,
  [Parameter(Mandatory=$true)][string]$MainClass,
  [Parameter(Mandatory=$true)][string]$InputDirectory,
  [Parameter(Mandatory=$true)][string]$OutputDirectory,
  [string]$Classpath,
  [long]$MaxBytes = 1073741824,
  [int]$MaxDepth = 64,
  [long]$MaxReferences = 1000000,
  [long]$TimeoutMillis = 900000,
  [string[]]$AllowedClasses = @()
)
$ErrorActionPreference = 'Stop'
$sparkItem = Get-Item -LiteralPath $SparkSubmit
$inputItem = Get-Item -LiteralPath $InputDirectory
if ($sparkItem.LinkType) { throw "spark-submit must not be a symbolic link" }
if ($inputItem.LinkType) { throw "import input must not be a symbolic link" }
$spark = $sparkItem.FullName
$input = $inputItem.FullName
$output = [IO.Path]::GetFullPath($OutputDirectory)
if (-not (Test-Path -LiteralPath $spark -PathType Leaf)) { throw "spark-submit is not a regular file: $spark" }
if (-not (Test-Path -LiteralPath $input -PathType Container)) { throw "import input directory is missing: $input" }
if ($MaxBytes -lt 1 -or $MaxDepth -lt 1 -or $MaxReferences -lt 1 -or $TimeoutMillis -lt 1) { throw 'import limits must be positive' }
New-Item -ItemType Directory -Force -Path $output | Out-Null
$relativeOutput = [IO.Path]::GetRelativePath([IO.Path]::GetFullPath($input), [IO.Path]::GetFullPath($output))
if ($relativeOutput -eq '.' -or $relativeOutput -notmatch '^(\.\.[\\/]|\.\.)') { throw 'import output must be outside input' }
$env:ZINGG_IMPORT_INPUT = $input
$env:ZINGG_IMPORT_OUTPUT = $output
$env:ZINGG_IMPORT_MAX_BYTES = [string]$MaxBytes
$env:ZINGG_IMPORT_MAX_DEPTH = [string]$MaxDepth
$env:ZINGG_IMPORT_MAX_REFERENCES = [string]$MaxReferences
$env:ZINGG_IMPORT_TIMEOUT_MILLIS = [string]$TimeoutMillis
$env:ZINGG_IMPORT_ALLOWED_CLASSES = ($AllowedClasses -join "`n")
$env:ZINGG_IMPORT_NETWORK = 'disabled'
$args = @('--class', $MainClass)
if ($Classpath) { $args += @('--jars', $Classpath) }
$args += @($input, $output)
$process = Start-Process -FilePath $spark -ArgumentList $args -Wait -PassThru -NoNewWindow
if ($process.ExitCode -ne 0) { throw "legacy importer failed with exit code $($process.ExitCode)" }
Write-Output $output
