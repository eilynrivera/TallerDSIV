@echo off
setlocal enabledelayedexpansion

echo ==========================================================
echo   Verificacion del entorno - Taller OWASP Top 10 (Java)
echo ==========================================================
echo.

set ALL_OK=1

REM ---------------------------------------------------------
REM 1) Java 21
REM ---------------------------------------------------------
set JAVA_OK=0
set JAVA_VER=
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%v
set JAVA_VER=!JAVA_VER:"=!
for /f "delims=.-_+ tokens=1" %%a in ("!JAVA_VER!") do set JAVA_MAJOR=%%a
if "!JAVA_MAJOR!"=="21" set JAVA_OK=1

if "!JAVA_OK!"=="1" (
    echo [OK]    Java 21 detectado ^(version !JAVA_VER!^).
) else (
    if "!JAVA_VER!"=="" (
        echo [FALLA] No se encontro Java en el PATH. Instala JDK 21 y ejecuta: java -version
    ) else (
        echo [FALLA] Java detectado: !JAVA_VER! ^(se necesita 21^).
    )
    set ALL_OK=0
)

REM ---------------------------------------------------------
REM 2) Puertos 3000, 3001 y 4000 libres en 127.0.0.1
REM ---------------------------------------------------------
call :checkPort 3000
call :checkPort 3001
call :checkPort 4000

echo.
if "!ALL_OK!"=="1" (
    echo ==========================================================
    echo   Listo para el taller
    echo ==========================================================
    endlocal
    exit /b 0
) else (
    echo ==========================================================
    echo   Hay problemas: revisa las lineas [FALLA] de arriba.
    echo ==========================================================
    endlocal
    exit /b 1
)

REM ---------------------------------------------------------
REM Subrutina: comprueba si un puerto esta ocupado
REM ---------------------------------------------------------
:checkPort
set PORT=%1
netstat -ano | findstr /C:":%PORT% " >nul
if errorlevel 1 (
    echo [OK]    Puerto %PORT% libre.
) else (
    echo [FALLA] Puerto %PORT% ocupado. Libera el puerto o cierra el proceso.
    set ALL_OK=0
)
goto :eof
