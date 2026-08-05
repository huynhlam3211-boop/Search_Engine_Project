@echo off
REM ===========================================================================
REM  VnSearch - chay RIENG frontend (Electron + React)
REM
REM  File nay KHONG dung toi backend: khong kiem tra, khong khoi dong Docker.
REM  Muon chay backend thi mo cua so khac va tu go:
REM      docker compose up -d --build
REM ===========================================================================
setlocal

echo.
echo === VnSearch - trinh duyet (chi frontend) ===
echo.

REM --- 1. Ve dung thu muc frontend ---
REM %~dp0 la thu muc chua file .bat nay (da co dau \ o cuoi), nen chay duoc
REM du go lenh tu bat ky dau.
cd /d "%~dp0browser-app" 2>nul
if errorlevel 1 (
    echo [LOI] Khong tim thay thu muc "%~dp0browser-app".
    echo       File .bat nay phai nam o THU MUC GOC cua repo, canh docker-compose.yml.
    goto :fail
)

REM Kiem tra lai bang mot moc chac chan. Neu chi dua vao errorlevel cua `cd`
REM thi mot thu muc rong cung duoc coi la hop le, va cac buoc sau se chay
REM nham cho - dung loi da gap khi thu nghiem file nay.
if not exist "package.json" (
    echo [LOI] Khong thay package.json trong "%CD%".
    echo       Thu muc browser-app co ve khong day du.
    goto :fail
)

REM --- 2. Kiem tra Node.js ---
where node >nul 2>nul
if errorlevel 1 (
    echo [LOI] Khong tim thay Node.js.
    echo       Cai dat tai https://nodejs.org roi mo lai cua so nay.
    goto :fail
)
for /f "delims=" %%v in ('node --version') do echo Node.js %%v

REM --- 3. Cai thu vien neu chua co ---
REM Kiem tra node_modules thay vi chay `npm install` moi lan: npm install mat
REM vai chuc giay ngay ca khi khong co gi thay doi.
if not exist "node_modules" (
    echo.
    echo Chua co node_modules, dang cai dat... ^(lan dau mat vai phut^)
    echo.
    call npm install

    REM KHONG tin errorlevel cua `call npm install`: npm tren Windows la mot
    REM shim .cmd va co truong hop no tra ve 0 du da bao loi. Kiem tra KET QUA
    REM that su thay vi ma tra ve.
    if not exist "node_modules" (
        echo.
        echo [LOI] npm install that bai - van chua co node_modules.
        echo       Cuon len xem thong bao loi cua npm o tren.
        goto :fail
    )
)

REM --- 4. Chay Electron ---
REM Backend chua chay thi trinh duyet van mo binh thuong, chi la o tim kiem
REM se bao loi khi goi API. Do la chuyen cua backend, khong phai cua file nay.
echo.
echo Dang khoi dong Electron... ^(dong cua so nay de dung^)
echo.
call npm run dev
if errorlevel 1 goto :fail

endlocal
exit /b 0

:fail
echo.
echo Nhan phim bat ky de dong...
pause >nul
endlocal
exit /b 1
