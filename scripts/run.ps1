param(
    [string]$Operation,
    [string]$Path,
    [switch]$Exact,
    [int]$Threads = 0
)

$projectRoot = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $projectRoot "target\opsfiles-1.0.jar"

if (-not (Test-Path $jar)) {
    Write-Host "JAR no encontrado, compilando..." -ForegroundColor Yellow
    & "$PSScriptRoot\build.ps1"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

# --- Operacion ---
if (-not $Operation) {
    Write-Host ""
    Write-Host "Operaciones:" -ForegroundColor Cyan
    Write-Host "  1) copy               Mueve archivos de subdirectorios al folder origen"
    Write-Host "  2) delete             Elimina videos duplicados por resolucion"
    Write-Host "  3) delete_incomplete  Elimina archivos .part incompletos"
    Write-Host "  4) similarity         Detecta duplicados visuales (fingerprints)"
    Write-Host ""
    $sel = Read-Host "Seleccion (1-4)"
    switch ($sel) {
        "1" { $Operation = "copy" }
        "2" { $Operation = "delete" }
        "3" { $Operation = "delete_incomplete" }
        "4" { $Operation = "similarity" }
        default {
            Write-Host "Seleccion invalida: $sel" -ForegroundColor Red
            exit 1
        }
    }
}

# --- Ruta ---
if (-not $Path) {
    Write-Host ""
    $Path = Read-Host "Ruta a procesar"
}
$Path = $Path.Trim().Trim('"').Trim("'")
if (-not $Path) {
    Write-Host "Ruta requerida" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path -LiteralPath $Path)) {
    Write-Host "La ruta no existe: $Path" -ForegroundColor Red
    exit 1
}

# --- Flags por operacion ---
$jarArgs = @($Operation, $Path)

if ($Operation -eq "similarity") {
    if (-not $PSBoundParameters.ContainsKey('Exact')) {
        $resp = Read-Host "Detectar tambien duplicados byte-exactos via SHA256? (s/n) [n]"
        if ($resp -match '^(s|si|y|yes)$') { $Exact = $true }
    }
    if ($Threads -le 0) {
        $resp = Read-Host "Hilos para extraccion (Enter = default min(cores,16))"
        if ($resp -match '^\d+$') { $Threads = [int]$resp }
    }
    if ($Exact) { $jarArgs += "--exact" }
    if ($Threads -gt 0) { $jarArgs += @("--threads", "$Threads") }
}

# --- Ejecutar ---
$displayArgs = ($jarArgs | ForEach-Object {
    if ($_ -match '\s') { '"' + $_ + '"' } else { $_ }
}) -join ' '

Write-Host ""
Write-Host "Comando:" -ForegroundColor Cyan
Write-Host "  java -jar `"$jar`" $displayArgs"
Write-Host ""

java -jar $jar @jarArgs
exit $LASTEXITCODE
