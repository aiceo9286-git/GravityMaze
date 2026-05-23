
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

try {
    java -version 2>&1 | Write-Host
} catch {
    Write-Host "Java 測試失敗"
}

Set-Location 'D:\GravityMaze'

# 清理
Remove-Item -Recurse -Force app\build\outputs\apk -ErrorAction SilentlyContinue

# 編譯
& .\gradlew.bat clean assembleDebug --no-daemon --console=plain 2>&1

Write-Host "Exit code: $LASTEXITCODE"
