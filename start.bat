@echo off

echo ========================================
echo   Reader - Start
echo ========================================
echo.

cd /d d:\FudanCoding\repo\Reader\reader-backend
start "Backend" cmd /k mvnw.cmd spring-boot:run

cd /d d:\FudanCoding\repo\Reader\reader-frontend
start "Frontend" cmd /k npm run dev -- --host

cd /d d:\FudanCoding\repo\Reader

echo Started!
echo   Frontend: http://localhost:5173
echo   Backend:  http://localhost:8080
echo.
pause
