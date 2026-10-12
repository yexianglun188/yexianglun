@echo off
chcp 65001 >nul
cd /d "%~dp0frontend"

where npm >nul 2>nul
if errorlevel 1 (
    echo [ERROR] npm not found. Please install Node.js first.
    pause
    exit /b 1
)

if not exist "node_modules" (
    echo Installing dependencies for the first time, please wait...
    call npm install
)

echo ============================================================
echo  Campus Market - Starting frontend
echo  URL : http://localhost:5173
echo ============================================================
echo.

call npm run dev
pause
