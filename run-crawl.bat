@echo off
setlocal

set "RUNNER=com.vnsearch.crawler.MultiDomainCrawlRunner"

for /f "tokens=2 delims=:" %%c in ('chcp') do set "OLD_CP=%%c"
set "OLD_CP=%OLD_CP: =%"
chcp 65001 >nul

set "MAVEN_OPTS=-Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dfile.encoding=UTF-8 %MAVEN_OPTS%"

if not defined CRAWL_PROGRESS set "CRAWL_PROGRESS=bar"

set "MAX_PAGES=%~1"
set "MAX_DEPTH=%~2"
set "OUTPUT=%~3"
set "FRESH=%~4"

if "%MAX_PAGES%"=="" set "MAX_PAGES=10000"
if "%MAX_DEPTH%"=="" set "MAX_DEPTH=4"
if "%OUTPUT%"==""    set "OUTPUT=data/crawled-documents.json"

cd /d "%~dp0search-engine" 2>nul
if errorlevel 1 (
    echo [LOI] Khong tim thay thu muc "%~dp0search-engine".
    echo       File .bat nay phai nam o THU MUC GOC cua repo, canh docker-compose.yml.
    goto :fail
)

if not exist "pom.xml" (
    echo [LOI] Khong thay pom.xml trong "%CD%".
    echo       Thu muc search-engine co ve khong day du.
    goto :fail
)

set "MVNW=%CD%\mvnw.cmd"
if not exist "%MVNW%" (
    echo [LOI] Khong thay Maven Wrapper ^(mvnw.cmd^) trong "%CD%".
    goto :fail
)

where java >nul 2>nul
if errorlevel 1 (
    echo [LOI] Khong tim thay Java.
    echo       Can JDK 17 tro len - cai tai https://adoptium.net roi mo lai cua so nay.
    goto :fail
)
for /f "delims=" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    echo Java %%v
    goto :java_done
)
:java_done

echo.
echo === CRAWL DA DOMAIN ===
echo So trang toi da : %MAX_PAGES%
echo Do sau toi da   : %MAX_DEPTH%
echo Ngon ngu        : CHI tieng Viet va tieng Anh
echo Tep dau ra      : %OUTPUT%

rem --- Corpus cu: noi tiep hay xoa lam lai ---
if /i "%FRESH%"=="--fresh" goto :ask_fresh

if exist "%OUTPUT%" (
    echo Che do          : NOI TIEP corpus san co ^(khong tai lai trang da co^)
) else (
    echo Che do          : crawl moi ^(chua co corpus nao tai duong dan nay^)
)
set "EXEC_ARGS=%MAX_PAGES% %MAX_DEPTH% %OUTPUT%"
goto :run

:ask_fresh
if not exist "%OUTPUT%" (
    echo Che do          : --fresh ^(chua co corpus cu nen khong mat gi^)
    set "EXEC_ARGS=%MAX_PAGES% %MAX_DEPTH% %OUTPUT% --fresh"
    goto :run
)
echo Che do          : --fresh - XOA corpus cu va crawl lai tu dau
echo.
echo [CANH BAO] "%OUTPUT%" dang ton tai va se bi GHI DE.
echo            Toan bo cong crawl cua cac phien truoc se mat.
echo.
set "CONFIRM="
set /p "CONFIRM=Go XOA roi Enter de xac nhan, hoac Enter de huy: "
if /i not "%CONFIRM%"=="XOA" (
    echo.
    echo Da huy. Khong co gi bi thay doi.
    goto :fail
)
set "EXEC_ARGS=%MAX_PAGES% %MAX_DEPTH% %OUTPUT% --fresh"

:run
echo.
echo Dang bien dich va chay crawler... ^(Ctrl+C de dung - checkpoint moi 250 trang^)
echo.
call "%MVNW%" -q compile exec:java -Dexec.mainClass=%RUNNER% -Dexec.args="%EXEC_ARGS%" -Dcrawl.progress=%CRAWL_PROGRESS%
if errorlevel 1 (
    echo.
    echo [LOI] Phien crawl ket thuc bat thuong.
    echo       Cuon len xem thong bao loi cua Maven/crawler o tren.
    echo       Phan da crawl toi diem kiem tra gan nhat van nam trong "%OUTPUT%".
    goto :fail
)

echo.
echo Xong. Corpus da luu tai "%CD%\%OUTPUT%".
echo Muon ket qua vao bo tim kiem thi khoi dong lai backend, hoac goi:
echo     curl -X POST http://localhost:8080/api/admin/reindex
echo.
echo Nhan phim bat ky de dong...
pause >nul
call :restore_cp
endlocal
exit /b 0

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
