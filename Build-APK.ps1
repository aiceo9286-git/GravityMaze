# 重力滾球編譯腳本
# 在 Windows PowerShell 中執行

$ErrorActionPreference = "Stop"

# 尋找 Java
$javaPaths = @(
    "C:\Program Files\Eclipse Adoptium\jdk-17",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Android\Android Studio\jbr"
)

$foundJava = $null
foreach ($path in $javaPaths) {
    if (Test-Path "$path\bin\java.exe") {
        $foundJava = $path
        break
    }
}

if (-not $foundJava) {
    Write-Host "❌ 找不到 Java，請安裝 Eclipse Temurin JDK 17" -ForegroundColor Red
    Read-Host "按 Enter 離開"
    exit 1
}

Write-Host "✅ 使用 Java: $foundJava" -ForegroundColor Green

$env:JAVA_HOME = $foundJava
$env:PATH = "$foundJava\bin;$env:PATH"

# 測試 Java
java -version

Set-Location $PSScriptRoot

Write-Host "🧹 清理舊輸出..."
Remove-Item -Recurse -Force app\build\outputs\apk -ErrorAction SilentlyContinue

Write-Host "🔨 開始編譯 APK..." -ForegroundColor Cyan
.\gradlew.bat clean assembleDebug --no-daemon --console=plain

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ 編譯成功!" -ForegroundColor Green
    $apk = "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apk) {
        Write-Host "📦 APK 位置: $(Resolve-Path $apk)" -ForegroundColor Yellow
        Write-Host "`n安裝到手機:" -ForegroundColor Cyan
        Write-Host "  adb install $apk"
    }
} else {
    Write-Host "`n❌ 編譯失敗" -ForegroundColor Red
}

Read-Host "`n按 Enter 離開"
