@echo off
setlocal

set "ROOT=%~dp0"
set "ENV_FILE=%ROOT%.env"

rem --- Doc tham so ---
set "KEEP_DOCKER="
set "STOP_ONLY="
set "WIPE="
set "KILL_WSL="

:parse
if "%~1"=="" goto :parsed
if /i "%~1"=="--help" goto :usage
if /i "%~1"=="-h" goto :usage
if /i "%~1"=="--keep-docker" (
    set "KEEP_DOCKER=1"
) else if /i "%~1"=="--stop" (
    set "STOP_ONLY=1"
    set "KEEP_DOCKER=1"
) else if /i "%~1"=="--wipe" (
    set "WIPE=1"
) else if /i "%~1"=="--wsl" (
    set "KILL_WSL=1"
) else (
    echo [LOI] Tham so khong hieu: %~1
    echo.
    goto :usage_fail
)
shift
goto :parse
:parsed

for /f "tokens=2 delims=:" %%c in ('chcp') do set "OLD_CP=%%c"
set "OLD_CP=%OLD_CP: =%"
chcp 65001 >nul

cd /d "%ROOT%" 2>nul
if not exist "docker-compose.yml" (
    echo [LOI] Khong thay docker-compose.yml trong "%CD%".
    echo       File .bat nay phai nam o THU MUC GOC cua repo.
    goto :fail
)

echo.
echo === TAT VNSEARCH ===

set "RAM_BEFORE="
for /f "delims=" %%m in ('powershell -NoProfile -Command "[math]::Round((Get-CimInstance Win32_OperatingSystem).FreePhysicalMemory/1MB,2)"') do set "RAM_BEFORE=%%m"
if defined RAM_BEFORE echo RAM trong luc bat dau: %RAM_BEFORE% GB

where docker >nul 2>nul
if errorlevel 1 (
    echo [LOI] Khong tim thay lenh `docker` - khong co gi de tat.
    goto :fail
)

docker info >nul 2>nul
if errorlevel 1 (
    echo Docker engine khong chay - khong co container nao dang song.
    goto :shutdown_desktop
)

if defined ADMIN_API_KEY goto :key_ok
if not exist "%ENV_FILE%" goto :key_placeholder
for /f "usebackq eol=# tokens=1,* delims==" %%a in ("%ENV_FILE%") do (
    if /i "%%a"=="ADMIN_API_KEY" set "ADMIN_API_KEY=%%b"
)
if defined ADMIN_API_KEY goto :key_ok
:key_placeholder
set "ADMIN_API_KEY=khoa-tam-chi-de-compose-doc-duoc-file"
:key_ok

set "PROFILES=--profile kafka --profile monitoring"

if defined STOP_ONLY (
    echo.
    echo Dang dung container ^(van giu lai de bat lai cho nhanh^)...
    docker compose %PROFILES% stop
    if errorlevel 1 goto :compose_failed
    echo.
    echo Container da dung. Bat lai: run-backend.bat --no-build
    goto :leftovers
)

if defined WIPE goto :wipe
goto :plain_down

:wipe
echo.
echo [CANH BAO] --wipe se XOA cac volume:
echo              postgres-data      toan bo CSDL tai lieu da crawl
echo              kafka-data         log cac topic
echo              prometheus-data    lich su so lieu do
echo              grafana-data       nguoi dung va thiet lap Grafana
echo            Khong the hoan tac. Corpus JSON trong search-engine\data
echo            KHONG bi anh huong ^(no nam tren may that, khong phai volume^).
echo.
set "CONFIRM="
set /p "CONFIRM=Go dung chu XOA roi Enter de xac nhan: "
if /i not "%CONFIRM%"=="XOA" (
    echo Da huy - khong xoa gi.
    goto :fail
)
echo.
echo Dang ha container va xoa volume...
docker compose %PROFILES% down --volumes --remove-orphans
if errorlevel 1 goto :compose_failed
echo Da ha xong va da XOA volume. Lan bat lai se khoi tao CSDL rong.
goto :leftovers

:plain_down
echo.
echo Dang ha container...
docker compose %PROFILES% down --remove-orphans
if errorlevel 1 goto :compose_failed

:down_ok
echo Da ha xong. Volume du lieu van con - bat lai la co ngay corpus cu.

:leftovers
set "PORT_PID="
for /f "tokens=5" %%p in ('netstat -ano -p TCP ^| findstr /r /c:":8080 .*LISTENING"') do set "PORT_PID=%%p"
if defined PORT_PID (
    echo.
    echo [CANH BAO] Cong 8080 VAN bi tien trinh PID %PORT_PID% chiem sau khi da ha container.
    echo            Gan nhu chac chan la mot ban backend chay tay con sot lai.
    echo            Xem no la gi : tasklist /FI "PID eq %PORT_PID%"
    echo            Tat di       : taskkill /PID %PORT_PID% /F
)

:shutdown_desktop
if defined KEEP_DOCKER goto :report

set "DOCKER_DESKTOP=%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
if not exist "%DOCKER_DESKTOP%" set "DOCKER_DESKTOP=%ProgramW6432%\Docker\Docker\Docker Desktop.exe"
if not exist "%DOCKER_DESKTOP%" set "DOCKER_DESKTOP=%LocalAppData%\Docker\Docker Desktop.exe"

tasklist /FI "IMAGENAME eq Docker Desktop.exe" /NH | findstr /i /c:"Docker Desktop.exe" >nul
if errorlevel 1 (
    echo.
    echo Docker Desktop khong chay - khong co gi de dong.
    goto :report
)

if not exist "%DOCKER_DESKTOP%" (
    echo.
    echo [CANH BAO] Khong tim thay Docker Desktop.exe de dong bang lenh.
    echo            Dong bang tay: chuot phai bieu tuong ca voi o khay he thong,
    echo            chon "Quit Docker Desktop".
    goto :report
)

echo.
echo Dang dong Docker Desktop de tra lai RAM cua may ao...
start "" "%DOCKER_DESKTOP%" -Shutdown

set /a DD_WAIT=0
:wait_dd
ping -n 3 127.0.0.1 >nul
tasklist /FI "IMAGENAME eq Docker Desktop.exe" /NH | findstr /i /c:"Docker Desktop.exe" >nul
if errorlevel 1 goto :dd_done
set /a DD_WAIT+=2
if %DD_WAIT% GEQ 90 (
    echo [CANH BAO] Doi 90 giay ma Docker Desktop chua dong han.
    echo            Cu de no tu dong not, hoac dong tay o khay he thong.
    goto :report
)
goto :wait_dd

:dd_done
echo Docker Desktop da dong sau %DD_WAIT%s.

if defined KILL_WSL (
    echo.
    echo Dang tat may ao WSL2...
    wsl --shutdown
    echo Da tat WSL2.
)

:report
ping -n 4 127.0.0.1 >nul
set "RAM_AFTER="
for /f "delims=" %%m in ('powershell -NoProfile -Command "[math]::Round((Get-CimInstance Win32_OperatingSystem).FreePhysicalMemory/1MB,2)"') do set "RAM_AFTER=%%m"

echo.
echo === XONG ===
if defined RAM_BEFORE if defined RAM_AFTER (
    echo RAM trong: %RAM_BEFORE% GB  -^>  %RAM_AFTER% GB
)
echo Bat lai he thong: run-backend.bat
echo.

call :restore_cp
endlocal
exit /b 0

rem ===========================================================================
:compose_failed
echo.
echo [LOI] Lenh docker compose that bai. Cuon len xem thong bao o tren.
echo       Cach manh tay hon, dong theo TEN container:
echo           docker rm -f vnsearch-backend vnsearch-postgres vnsearch-kafka
echo           docker rm -f vnsearch-kafka-ui vnsearch-crawler-worker
echo           docker rm -f vnsearch-prometheus vnsearch-grafana vnsearch-alertmanager
echo           docker rm -f vnsearch-kafka-exporter
goto :fail

:usage
echo.
echo   end-backend.bat                 ha container + tat Docker Desktop
echo   end-backend.bat --keep-docker   chi ha container, de Docker Desktop chay
echo   end-backend.bat --stop          chi dung container, KHONG xoa - bat lai nhanh
echo   end-backend.bat --wipe          ha container VA XOA volume - MAT DU LIEU
echo   end-backend.bat --wsl           tat luon may ao WSL2 sau khi tat Docker
echo.
echo   Bat lai: run-backend.bat
echo.
call :restore_cp
endlocal
exit /b 0

:usage_fail
echo   Chay "end-backend.bat --help" de xem cac tham so hop le.
echo.
echo Nhan phim bat ky de dong...
pause >nul
endlocal
exit /b 1

:fail
echo.
echo Nhan phim bat ky de dong...
pause >nul
call :restore_cp
endlocal
exit /b 1

:restore_cp
if defined OLD_CP chcp %OLD_CP% >nul
goto :eof
