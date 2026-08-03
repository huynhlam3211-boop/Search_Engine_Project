@echo off
REM ============================================================
REM  VnSearch - chay frontend Electron (browser-app) che do dev
REM  Double-click file nay hoac go: run-frontend.bat
REM ============================================================
title VnSearch - Frontend (Electron dev)
cd /d "%~dp0browser-app"

where node >nul 2>&1
if errorlevel 1 (
  echo [LOI] Khong tim thay Node.js trong PATH. Cai Node 20+ tai https://nodejs.org
  goto :fail
)

if not exist node_modules (
  echo [1/2] Chua co node_modules - dang chay npm install...
  call npm install
  if errorlevel 1 (
    echo [LOI] npm install that bai.
    goto :fail
  )
)

echo [2/2] Dang khoi dong Electron... (Ctrl+C de dung)
call npm run dev
if errorlevel 1 goto :fail

exit /b 0

:fail
echo.
echo Nhan phim bat ky de dong cua so...
pause >nul
exit /b 1
