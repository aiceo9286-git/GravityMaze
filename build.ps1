$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Set-Location "D:\GravityMaze"
Write-Host "清理舊輸出..."
Remove-Item -Recurse -Force app\build\outputs\apk -ErrorAction SilentlyContinue

Write-Host "開始編譯..."
.\gradlew.bat clean assembleDebug --no-daemon --console=plain

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 編譯成功!" -ForegroundColor Green
} else {
    Write-Host "❌ 編譯失敗" -ForegroundColor Red
}
