# Build-GravityMaze.ps1
$ErrorActionPreference = "Stop"

# 設定環境
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"

Write-Host "Java: $(java -version 2>&1 | Select-Object -First 1)"
Write-Host "======================================"

# 進入專案
cd D:\GravityMaze

# 清理並建置
Write-Host "開始建置..."
.\gradlew.bat clean assembleDebug --no-daemon

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 建置成功!"
    $apk = "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apk) {
        Copy-Item $apk "GravityMaze-latest.apk" -Force
        $size = (Get-Item "GravityMaze-latest.apk").Length / 1MB
        Write-Host "📦 APK 大小: {0:N2} MB" -f $size
    }
} else {
    Write-Host "❌ 建置失敗!"
    exit 1
}
