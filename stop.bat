@echo off

echo Stopping Reader...

REM Kill Spring Boot (Java)
taskkill /FI "WINDOWTITLE eq Reader Backend*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq reader-backend*" /F >nul 2>&1

REM Kill Vite dev server (Node)
taskkill /FI "WINDOWTITLE eq Reader Frontend*" /F >nul 2>&1

echo Stopped
pause
