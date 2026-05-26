@echo off
chcp 65001 > nul
echo Building GravityMaze v1.0.2...

:: Find Java
set "JAVA_HOME="
if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
) else if exist "C:\Program Files\Android\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Android\jbr"
)

if "%JAVA_HOME%"=="" (
    echo ERROR: Java not found. Please install JDK 17 or Android Studio.
    exit /b 1
)

echo Using Java: %JAVA_HOME%
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d D:\GravityMaze

echo Cleaning...
if exist "app\build" rmdir /s /q "app\build"

echo Building APK...
call .\gradlew.bat assembleDebug --no-daemon

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    exit /b 1
)

copy /Y "app\build\outputs\apk\debug\app-debug.apk" "GravityMaze-v1.0.2-sensor-fixed.apk"
echo Build complete: GravityMaze-v1.0.2-sensor-fixed.apk
