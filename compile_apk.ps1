# compile_apk.ps1 - GravityMaze APK 建置腳本
# 執行方式: .\compile_apk.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   🎮 GravityMaze APK 建置" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 進入專案目錄
Set-Location D:\GravityMaze

Write-Host "📁 工作目錄: $(Get-Location)" -ForegroundColor Yellow
Write-Host ""

# 檢查 Android SDK
if (-not $env:ANDROID_HOME) {
    Write-Host "🔍 偵測 Android SDK..." -ForegroundColor Blue
    $env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
}

Write-Host "✅ ANDROID_HOME: $env:ANDROID_HOME" -ForegroundColor Green
Write-Host ""

# 建置前清理
Write-Host "🧹 清理舊建置..." -ForegroundColor Blue
if (Test-Path "app\build") {
    Remove-Item "app\build" -Recurse -Force
    Write-Host "   已清理 app/build" -ForegroundColor Gray
}

# 建置 APK
Write-Host ""
Write-Host "🔨 開始建置 APK..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$startTime = Get-Date

try {
    & .\gradlew.bat assembleDebug --no-daemon
    
    if ($LASTEXITCODE -eq 0) {
        $endTime = Get-Date
        $duration = $endTime - $startTime
        
        Write-Host ""
        Write-Host "==========================================" -ForegroundColor Green
        Write-Host "   ✅ APK 建置成功！" -ForegroundColor Green
        Write-Host "==========================================" -ForegroundColor Green
        Write-Host ""
        
        $apkSource = "app\build\outputs\apk\debug\app-debug.apk"
        $apkDest = "GravityMaze-latest.apk"
        
        if (Test-Path $apkSource) {
            Copy-Item $apkSource $apkDest -Force
            $size = (Get-Item $apkDest).Length / 1MB
            Write-Host "📦 APK 檔案: $apkDest" -ForegroundColor Yellow
            Write-Host "📊 檔案大小: {0:N2} MB" -f $size -ForegroundColor Gray
            Write-Host "⏱️  建置時間: {0:N0}分 {1:N0}秒" -f $duration.Minutes, $duration.Seconds -ForegroundColor Gray
            Write-Host ""
            Write-Host "✅ 建置完成！請通知 AI 進行上傳" -ForegroundColor Green
        }
    } else {
        Write-Host ""
        Write-Host "❌ 建置失敗，錯誤碼: $LASTEXITCODE" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "❌ 建置過程發生例外:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host ""
Read-Host "按 Enter 結束"
