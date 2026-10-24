# tools/screenshot.ps1
# Captures a screenshot from the connected device and saves it to the specified path.
param(
    [string]$OutputFile = "docs\evidence\screenshot.png"
)

$ErrorActionPreference = "Stop"

$parentDir = Split-Path -Parent $OutputFile
if ($parentDir -and -not (Test-Path $parentDir)) {
    New-Item -ItemType Directory -Path $parentDir -Force | Out-Null
}

Write-Host "==> Capturing screenshot to $OutputFile..." -ForegroundColor Cyan
adb exec-out screencap -p > $OutputFile

if (Test-Path $OutputFile) {
    Write-Host "==> Screenshot captured successfully: $OutputFile" -ForegroundColor Green
} else {
    Write-Host "==> Failed to capture screenshot." -ForegroundColor Red
    exit 1
}
