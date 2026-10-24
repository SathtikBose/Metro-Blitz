# tools/run_device.ps1
# Installs debug APK, launches MainActivity, and monitors logcat for crashes.
param(
    [string]$Package = "com.buildstack.metroblitz",
    [string]$Activity = "com.buildstack.metroblitz/.MainActivity",
    [string]$ApkPath = "app\build\outputs\apk\debug\app-debug.apk"
)

$ErrorActionPreference = "Stop"

Write-Host "==> Checking connected devices..." -ForegroundColor Cyan
adb devices

Write-Host "==> Installing $ApkPath..." -ForegroundColor Cyan
adb install -r $ApkPath
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to install APK." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "==> Launching $Activity..." -ForegroundColor Cyan
adb shell am start -n $Activity
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start activity." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "==> Tailing logcat for MetroBlitz and crashes (press Ctrl+C to stop)..." -ForegroundColor Green
adb logcat -s AndroidRuntime:E MetroBlitz:V
