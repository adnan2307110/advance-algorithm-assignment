@echo off
echo ========================================================
echo   Launching HealthDesk Healthcare Management System...
echo ========================================================
set "DIR=%~dp0"
cd /d "%DIR%"

if exist "target\healthdesk-1.0-SNAPSHOT.jar" (
    java -jar target\healthdesk-1.0-SNAPSHOT.jar
) else (
    call .\mvnw.cmd javafx:run
)
pause
