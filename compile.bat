@echo off
chcp 65001 > nul
setlocal

echo ========================================
echo  重力滾球：時空迷宮 - 編譯工具
echo ========================================
echo.

REM 尋找可用的 PowerShell
set "PS=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
if not exist "%PS%" (
    echo ❌ 找不到 PowerShell
    pause
    exit /b 1
)

REM 以系統管理員權限啟動
powershell -Command "Start-Process '%PS%' -ArgumentList '-ExecutionPolicy Bypass -File "Build-APK.ps1"' -Verb RunAs"

exit /b %ERRORLEVEL%
