@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not defined JAVA_HOME set "JAVA_HOME=E:\JAVA"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] JDK not found at: %JAVA_HOME%
    echo Please edit this file and set JAVA_HOME to your JDK 17 directory.
    pause
    exit /b 1
)

set "MVN=%~dp0tools\apache-maven-3.9.9\bin\mvn.cmd"
if not exist "%MVN%" set "MVN=mvn"

echo ============================================================
echo  Campus Market - Starting backend
echo  JAVA_HOME : %JAVA_HOME%
echo  Maven     : %MVN%
echo  URL       : http://localhost:8080
echo ============================================================
echo.

call "%MVN%" spring-boot:run
pause
