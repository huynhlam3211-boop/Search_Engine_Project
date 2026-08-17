@echo off
setlocal

set "ROOT=%~dp0"
set "ENV_FILE=%ROOT%.env"

set "MODE=full"
set "PROFILES=--profile kafka --profile monitoring"
set "BUS=kafka"
set "BUILD=--build"
set "FOLLOW_LOGS="

:parse
if "%~1"=="" goto :parsed
if /i "%~1"=="--help" goto :usage
if /i "%~1"=="-h" goto :usage
if /i "%~1"=="--full" (
    set "MODE=full"
    set "PROFILES=--profile kafka --profile monitoring"
    set "BUS=kafka"
) else if /i "%~1"=="--kafka" (
    set "MODE=kafka"
    set "PROFILES=--profile kafka"
    set "BUS=kafka"
) else if /i "%~1"=="--core" (
    set "MODE=core"
    set "PROFILES="
    set "BUS=memory"
) else if /i "%~1"=="--no-build" (
    set "BUILD="
) else if /i "%~1"=="--logs" (
    set "FOLLOW_LOGS=1"
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

rem --- Thu muc goc ---
cd /d "%ROOT%" 2>nul
if not exist "docker-compose.yml" (
    echo [LOI] Khong thay docker-compose.yml trong "%CD%".
    echo       File .bat nay phai nam o THU MUC GOC cua repo.
    goto :fail
)

rem ===========================================================================
rem DOCKER
rem ===========================================================================
where docker >nul 2>nul
if errorlevel 1 (
    echo [LOI] Khong tim thay lenh `docker`.
    echo       Cai Docker Desktop tai https://docker.com/products/docker-desktop
    echo       roi MO LAI cua so nay ^(PATH chi duoc nap luc mo terminal^).
    goto :fail
)

docker compose version >nul 2>nul
if errorlevel 1 (
    echo [LOI] Docker co, nhung khong co plugin `docker compose` ^(v2^).
    echo       Ban Docker Desktop qua cu. Cap nhat len ban moi nhat.
    goto :fail
)

docker info >nul 2>nul
if not errorlevel 1 goto :docker_ready

echo Docker Desktop chua chay - dang bat...

set "DOCKER_DESKTOP=%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
if not exist "%DOCKER_DESKTOP%" set "DOCKER_DESKTOP=%ProgramW6432%\Docker\Docker\Docker Desktop.exe"
if not exist "%DOCKER_DESKTOP%" set "DOCKER_DESKTOP=%LocalAppData%\Docker\Docker Desktop.exe"
if not exist "%DOCKER_DESKTOP%" (
    echo [LOI] Khong tim thay Docker Desktop.exe o cac vi tri quen thuoc.
    echo       Mo Docker Desktop bang tay, doi bieu tuong ca voi xanh, roi chay lai.
    goto :fail
)

start "" "%DOCKER_DESKTOP%"

rem loi socket kho hieu.
set /a DOCKER_WAIT=0
:wait_docker
ping -n 4 127.0.0.1 >nul
docker info >nul 2>nul
if not errorlevel 1 goto :docker_started
set /a DOCKER_WAIT+=3
if %DOCKER_WAIT% GEQ 240 (
    echo.
    echo [LOI] Doi 4 phut ma Docker engine van chua san sang.
    echo       Mo Docker Desktop xem no bao gi ^(hay gap: WSL2 chua cai, hoac
    echo       Virtualization tat trong BIOS^).
    goto :fail
)
echo    ... %DOCKER_WAIT%s
goto :wait_docker

:docker_started
echo Docker Desktop da san sang sau %DOCKER_WAIT%s.

:docker_ready

rem ===========================================================================
rem KHOA QUAN TRI
rem ===========================================================================
if defined ADMIN_API_KEY goto :key_ok
if not exist "%ENV_FILE%" goto :key_new

for /f "usebackq eol=# tokens=1,* delims==" %%a in ("%ENV_FILE%") do (
    if /i "%%a"=="ADMIN_API_KEY" set "ADMIN_API_KEY=%%b"
)
if defined ADMIN_API_KEY (
    echo Khoa quan tri : doc tu "%ENV_FILE%"
    goto :key_ok
)

:key_new
echo Khoa quan tri : chua co, dang sinh khoa moi...
for /f "delims=" %%k in ('powershell -NoProfile -Command "$b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); ([BitConverter]::ToString($b) -replace [char]45, [string]::Empty).ToLower()"') do set "ADMIN_API_KEY=%%k"
if not defined ADMIN_API_KEY (
    echo [LOI] Khong sinh duoc khoa ^(khong goi duoc PowerShell^).
    echo       Dat tay roi chay lai:
    echo           set ADMIN_API_KEY=mot-chuoi-bat-ky-tu-16-ky-tu-tro-len
    goto :fail
)
if not exist "%ENV_FILE%" (
    >"%ENV_FILE%" echo # Sinh tu dong boi run-backend.bat. KHONG commit - .gitignore da chan.
)
>>"%ENV_FILE%" echo ADMIN_API_KEY=%ADMIN_API_KEY%
echo                 da ghi vao "%ENV_FILE%" ^(tep nay khong len Git^)

:key_ok
set "KEY_PROBE=%ADMIN_API_KEY:~15,1%"
if not defined KEY_PROBE (
    echo [LOI] ADMIN_API_KEY ngan hon 16 ky tu nen SecurityConfig se tu choi khoi dong.
    echo       Sua gia tri ADMIN_API_KEY trong "%ENV_FILE%", hoac xoa han dong do
    echo       de file nay sinh lai khoa moi.
    goto :fail
)

rem ===========================================================================
rem BUS SU KIEN
rem ===========================================================================

set "APP_CRAWLER_BUS=%BUS%"

rem ===========================================================================
rem CHAY
rem ===========================================================================
if "%MODE%"=="full"  set "MODE_SHOW=FULL - backend + postgres + kafka + monitoring, ~4 GB RAM"
if "%MODE%"=="kafka" set "MODE_SHOW=KAFKA - backend + postgres + cum Kafka, ~3 GB RAM"
if "%MODE%"=="core"  set "MODE_SHOW=CORE - backend + postgres, ~1,5 GB RAM"

set "BUILD_SHOW=co - build lai anh tu ma nguon"
if not defined BUILD set "BUILD_SHOW=khong - dung anh da co, do co --no-build"

echo.
echo === VNSEARCH ===
echo Thu muc      : %CD%
echo Che do       : %MODE_SHOW%
echo Build        : %BUILD_SHOW%
echo Bus crawler  : %APP_CRAWLER_BUS%
echo Khoa quan tri: %ADMIN_API_KEY:~0,8%... ^(day du trong .env^)
set "INDEX_STALE="
if not exist "search-engine\data\index.json" goto :stale_done
if not exist "search-engine\data\crawled-documents.json" goto :stale_done
for /f "delims=" %%s in ('powershell -NoProfile -Command "if ((Get-Item search-engine\data\index.json).LastWriteTime -lt (Get-Item search-engine\data\crawled-documents.json).LastWriteTime) { Write-Output STALE }"') do set "INDEX_STALE=%%s"
if not defined INDEX_STALE goto :stale_done
echo [CANH BAO] data\index.json CU HON data\crawled-documents.json.
echo            Backend se nap chi muc cu, nen cac trang crawl gan day chua tim
echo            duoc. Sau khi backend len, lap lai chi muc mot lan:
echo                curl -X POST -H "X-API-Key: %ADMIN_API_KEY:~0,8%..." http://localhost:8080/api/admin/reindex
:stale_done

set "STALE_LIST="
for /f "delims=" %%c in ('docker compose %PROFILES% ps -a --format "{{.Name}}" 2^>nul') do call :check_net %%c
if defined STALE_LIST (
    echo.
    echo Don container mac ket o mang cu:%STALE_LIST%
    docker rm -f %STALE_LIST% >nul 2>nul
)

echo.
echo Dang build va khoi dong... ^(lan dau tai anh nen mat vai phut^)
echo.

docker compose %PROFILES% up -d %BUILD%
if errorlevel 1 (
    echo.
    echo [LOI] `docker compose up` that bai. Cuon len xem dong loi DAU TIEN -
    echo       phan con lai thuong chi la he qua. Vai nguyen nhan hay gap:
    echo         - port is already allocated : con mot ban cu dang chay.
    echo                                       Chay end-backend.bat roi thu lai.
    echo         - no space left on device   : docker system prune -a
    echo         - build that bai o buoc mvn : loi bien dich that trong ma nguon
    goto :fail
)

echo.
echo Container da tao. Dang doi backend nap chi muc...
set /a HEALTH_WAIT=0
:wait_backend
set "HEALTH="
for /f "delims=" %%s in ('docker inspect -f "{{.State.Health.Status}}" vnsearch-backend 2^>nul') do set "HEALTH=%%s"
if "%HEALTH%"=="healthy" goto :backend_up
if "%HEALTH%"=="" (
    echo [CANH BAO] Khong doc duoc trang thai container vnsearch-backend.
    goto :backend_unknown
)
set /a HEALTH_WAIT+=5
if %HEALTH_WAIT% GEQ 420 (
    echo.
    echo [CANH BAO] Doi 7 phut ma backend van "%HEALTH%".
    echo            Xem no ket o dau : docker compose logs -f backend
    goto :backend_unknown
)
ping -n 6 127.0.0.1 >nul
echo    ... %HEALTH_WAIT%s ^(%HEALTH%^)
goto :wait_backend

:backend_up
echo Backend san sang sau %HEALTH_WAIT%s.
:backend_unknown

echo.
docker compose %PROFILES% ps
echo.
echo === DIA CHI ===
echo   Backend API   http://localhost:8080/api/health
echo   Thu tim kiem  http://localhost:8080/api/search?q=ha+noi
if not "%MODE%"=="core" (
    echo   Kafka UI      http://localhost:8081
)
if "%MODE%"=="full" (
    echo   Grafana       http://localhost:3000    admin / xem GRAFANA_PASSWORD trong .env
    echo   Prometheus    http://localhost:9090
    echo   Alertmanager  http://localhost:9093
)
echo.
echo   Giao dien     chay run-frontend.bat o mot cua so khac
echo   Xem log       docker compose logs -f backend
echo   TAT HET       end-backend.bat        ^<-- nho chay de tra lai RAM
echo.

if defined FOLLOW_LOGS (
    echo Dang bam theo log backend. Ctrl+C de thoat - container VAN CHAY tiep.
    echo.
    docker compose logs -f backend
)

call :restore_cp
endlocal
exit /b 0

:check_net
set "NETS="
for /f "delims=" %%n in ('docker inspect -f "{{range .NetworkSettings.Networks}}{{.NetworkID}} {{end}}" %1 2^>nul') do set "NETS=%%n"
if not defined NETS goto :eof
for %%i in (%NETS%) do (
    rem `network inspect` HOI daemon nen day la phep thu that. Dinh danh cua mot
    rem mang da xoa khong con tra loi duoc, va do la toan bo phep thu.
    docker network inspect %%i >nul 2>nul
    if errorlevel 1 goto :net_stale
)
goto :eof

:net_stale
set "STALE_LIST=%STALE_LIST% %1"
goto :eof

rem ===========================================================================
:usage
echo.
echo   run-backend.bat              FULL - backend + postgres + kafka + monitoring
echo   run-backend.bat --kafka      backend + postgres + cum Kafka
echo   run-backend.bat --core       backend + postgres
echo   run-backend.bat --no-build   dung anh da co, khong build lai
echo   run-backend.bat --logs       bam theo log backend sau khi len
echo.
echo   Tat va giai phong RAM: end-backend.bat
echo.
echo   Bien moi truong:
echo     ADMIN_API_KEY   khoa cho /api/admin/**. Khong dat thi lay tu .env,
echo                     khong co nua thi tu sinh va ghi vao .env.
echo.
call :restore_cp
endlocal
exit /b 0

:usage_fail
echo   Chay "run-backend.bat --help" de xem cac tham so hop le.
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
