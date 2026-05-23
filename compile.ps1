$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "Java版本:"
java -version

Set-Location "D:\GravityMaze"
Write-Host "Current: $(Get-Location)"

Remove-Item -Recurse -Force app\build\outputs\apk -ErrorAction SilentlyContinue
Write-Host "清除舊輸出..."

.Agradlew.bat clean assembleDebug --no-daemon

Write-Host "編譯完成狀態: $?"
