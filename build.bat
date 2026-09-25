@echo off
echo ========================================================
echo   Building HealthDesk Application (Standalone JAR)...
echo ========================================================
set "DIR=%~dp0"
cd /d "%DIR%"

call .\mvnw.cmd clean package -DskipTests
echo.
echo Build completed. Executable JAR located at target\healthdesk-1.0-SNAPSHOT.jar
pause
