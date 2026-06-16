param(
    [Parameter(Mandatory=$true)]
    [string]$Path,
    [switch]$Exact,
    [int]$Threads = 0
)

$projectRoot = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $projectRoot "target\opsfiles-1.0.jar"

if (-not (Test-Path $jar)) {
    Write-Host "JAR not found. Building project..." -ForegroundColor Yellow
    Push-Location $projectRoot
    mvn clean package -q
    Pop-Location
}

$jarArgs = @("similarity", $Path)
if ($Exact) { $jarArgs += "--exact" }
if ($Threads -gt 0) { $jarArgs += @("--threads", "$Threads") }

java -jar $jar @jarArgs
