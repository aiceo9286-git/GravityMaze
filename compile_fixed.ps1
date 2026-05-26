
$ErrorActionPreference = "Stop"
Write-Host "=== GravityMaze Build Script ===" -ForegroundColor Cyan

# Setup Java
$javaHome = "C:\Program Files\Eclipse Adoptium\jdk-17"
if (-not (Test-Path $javaHome)) {
    Write-Error "Java not found at $javaHome"
    exit 1
}

$env:JAVA_HOME = $javaHome
$env:PATH = "$javaHome\bin;$env:PATH"

Write-Host "Java version:" 
java -version

# Navigate to project
cd "D:\GravityMaze"

Write-Host "Building APK..." -ForegroundColor Yellow

# Run Gradle build
.\gradlew.bat clean assembleDebug --no-daemon --parallel

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n=== BUILD SUCCESS ===" -ForegroundColor Green
    Write-Host "APK location: app\build\outputs\apk\debug\app-debug.apk"
    exit 0
} else {
    Write-Host "`n=== BUILD FAILED ===" -ForegroundColor Red
    exit 1
}
