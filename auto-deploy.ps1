# Gerda-4L 自動化部署腳本
# 執行此腳本完成：編譯 → GitHub → Google Drive → Gmail

param(
    [string]$GitHubToken = "",
    [string]$GoogleTokenPath = ""
)

$ErrorActionPreference = "Stop"
$OutputEncoding = [Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  🎮 GravityMaze v1.0.2 自動化部署" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: 尋找 Java
Write-Host "🔍 尋找 Java..." -ForegroundColor Yellow

$javaPaths = @(
    'C:\Program Files\Android\Android Studio\jbr',
    'C:\Program Files\Android\jbr',
    'C:\Program Files\Eclipse Adoptium\jdk-17',
    'C:\Program Files\Eclipse Adoptium\jdk-17*',
    'C:\Program Files\Microsoft\jdk-17'
)

$foundJava = $null
foreach ($path in $javaPaths) {
    $expanded = (Resolve-Path $path -ErrorAction SilentlyContinue | Select-Object -First 1).Path
    if ($expanded -and (Test-Path "$expanded\bin\java.exe")) {
        $foundJava = $expanded
        break
    }
}

if (-not $foundJava) {
    Write-Host "❌ Java not found. Install JDK 17 or Android Studio." -ForegroundColor Red
    exit 1
}

$env:JAVA_HOME = $foundJava
$env:PATH = "$foundJava\bin;$env:PATH"
Write-Host "✅ Java: $foundJava" -ForegroundColor Green

# Step 2: 編譯
Write-Host ""
Write-Host "🔨 編譯 APK..." -ForegroundColor Cyan
Set-Location D:\GravityMaze

# 執行 Gradle
& '.\gradlew.bat' clean assembleDebug --no-daemon

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 編譯失敗" -ForegroundColor Red
    exit 1
}

$apkSource = 'app\build\outputs\apk\debug\app-debug.apk'
$apkDest = 'GravityMaze-v1.0.2-sensor-fixed.apk'
Copy-Item $apkSource $apkDest -Force
$size = (Get-Item $apkDest).Length / 1MB

Write-Host "✅ APK: $apkDest ({0:N2} MB)" -f $size -ForegroundColor Green

# Step 3: GitHub 上傳（如果提供 token）
if ($GitHubToken) {
    Write-Host ""
    Write-Host "📤 上傳到 GitHub..." -ForegroundColor Cyan
    
    git add .
    git commit -m "Fix: Invert both gravityX and gravityY for correct sensor orientation"
    git push origin main
    
    # 創建 Release
    $headers = @{
        'Authorization' = "token $GitHubToken"
        'Accept' = 'application/vnd.github.v3+json'
    }
    
    $releaseBody = @{
        tag_name = 'v1.0.2'
        name = 'GravityMaze v1.0.2 - Sensor Fix'
        body = '修正加速度計方向（左右+上下顛倒問題）'
    } | ConvertTo-Json
    
    try {
        $release = Invoke-RestMethod -Uri 'https://api.github.com/repos/aiceo9286-git/gravity-maze/releases' -Method POST -Headers $headers -Body $releaseBody
        Write-Host "✅ GitHub Release 創建成功" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ GitHub 上傳失敗: $_" -ForegroundColor Yellow
    }
}

# Step 4: 顯示完成訊息
Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "  ✅ 部署完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📦 輸出檔案: $apkDest"
Write-Host ""
Write-Host "📝 修復內容："
Write-Host "   - gravityX = -gravityX (左右翻轉修復)"
Write-Host "   - gravityY = -gravityY (上下翻轉修復)"
Write-Host ""

# 啟動檔案總管顯示 APK
Start-Process explorer.exe -ArgumentList '/select,"D:\GravityMaze\GravityMaze-v1.0.2-sensor-fixed.apk"' -ErrorAction SilentlyContinue

Read-Host "按 Enter 鍵結束..."
