$projectRoot = Split-Path -Parent $PSScriptRoot

Push-Location $projectRoot
try {
    Write-Host "Compilando opsfiles..." -ForegroundColor Cyan
    mvn clean package
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "Build OK" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "Build FAILED (exit code: $LASTEXITCODE)" -ForegroundColor Red
        exit $LASTEXITCODE
    }
} finally {
    Pop-Location
}
