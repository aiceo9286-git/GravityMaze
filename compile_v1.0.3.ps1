# GravityMaze v1.0.3 Build Script
$ErrorActionPreference = "Stop"

Write-Host "=== GravityMaze v1.0.3 Build ===" -ForegroundColor Cyan
Write-Host "修復內容: 鋼珠邊界檢查邏輯，避免重置物理狀態" -ForegroundColor Yellow

# Setup Java
$javaHome = "C:\Program Files\Eclipse Adoptium\jdk-17"
$env:JAVA_HOME = $javaHome
$env:PATH = "$javaHome\bin;$env:PATH"

Write-Host "`nJava version:" 
java -version

# Navigate to project
cd "D:\GravityMaze"

# Clean and build
Write-Host "`n[1/3] Cleaning..." -ForegroundColor Yellow
.\gradlew.bat clean --no-daemon

Write-Host "`n[2/3] Building APK..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug --no-daemon --parallel

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n[3/3] Build SUCCESS!" -ForegroundColor Green
    
    # Rename APK with version
    $timestamp = Get-Date -Format "yyyyMMdd"
    $sourceApk = "app\build\outputs\apk\debug\app-debug.apk"
    $targetApk = "GravityMaze_v1.0.3_fixed_$timestamp.apk"
    
    Copy-Item $sourceApk $targetApk -Force
    Write-Host "APK saved: $targetApk" -ForegroundColor Green
    
    # Git commit
    Write-Host "`nGit commit..." -ForegroundColor Yellow
    git add app/src/main/java/com/sharn/gravitymaze/GameView.java
    git commit -m "Fix ball movement - boundary check logic using teleport"
    git push origin main
    
    Write-Host "`n=== Done! ===" -ForegroundColor Green
} else {
    Write-Host "`n=== Build FAILED ===" -ForegroundColor Red
    exit 1
}
