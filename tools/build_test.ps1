# tools/build_test.ps1
# Runs full build, unit tests, and lint checks. Exits with non-zero exit code on failure.
$ErrorActionPreference = "Stop"

Write-Host "==> Running assembleDebug, testDebugUnitTest, lintDebug..." -ForegroundColor Cyan
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "==> Build/Test/Lint FAILED with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "==> All build, test, and lint checks PASSED!" -ForegroundColor Green
exit 0
