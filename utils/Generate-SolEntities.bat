@echo off
REM ============================================================
REM  Generate-SolEntities.bat  (DIAGNOSTIC VERSION)
REM  Logs every step. Window will NOT close until you press a key.
REM ============================================================

echo.
echo ============================================================
echo  Generate-SolEntities  -  starting up
echo ============================================================
echo.

echo [STEP 1] Switching to script folder...
echo   Script's folder is: %~dp0
cd /d "%~dp0"
if errorlevel 1 (
    echo   FAILED to cd into that folder.
    goto :HoldOpen
)
echo   Now in folder: %CD%
echo.

echo [STEP 2] Resolving input/output paths...
set "INPUT=%~1"
if "%INPUT%"=="" set "INPUT=bodies.csv"
set "OUTPUT=%~2"
if "%OUTPUT%"=="" set "OUTPUT=entries.txt"
echo   INPUT  = %INPUT%
echo   OUTPUT = %OUTPUT%
echo.

echo [STEP 3] Looking for the PowerShell script...
set "SCRIPT=%~dp0Generate-SolEntities.ps1"
echo   Expecting: %SCRIPT%
if not exist "%SCRIPT%" (
    echo   *** NOT FOUND ***
    echo   Make sure Generate-SolEntities.ps1 is in the SAME folder as this .bat
    echo   Files currently in this folder:
    dir /b
    goto :HoldOpen
)
echo   Found it.
echo.

echo [STEP 4] Looking for the input CSV...
echo   Expecting: %CD%\%INPUT%
if not exist "%INPUT%" (
    echo   *** NOT FOUND ***
    echo   Files currently in this folder:
    dir /b
    echo.
    echo   The CSV must have a header row like:
    echo     Name,TextureID,Diameter,IsContactBinary,DefaultName,Discoverable
    goto :HoldOpen
)
echo   Found it. Contents preview:
echo   ----
type "%INPUT%"
echo   ----
echo.

echo [STEP 5] Checking PowerShell is available...
where powershell.exe
if errorlevel 1 (
    echo   *** powershell.exe NOT FOUND on PATH ***
    goto :HoldOpen
)
echo.

echo [STEP 6] Running PowerShell script...
echo   Command: powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" -CsvPath "%INPUT%" -OutputPath "%OUTPUT%"
echo   ----
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" -CsvPath "%INPUT%" -OutputPath "%OUTPUT%"
set "RC=%ERRORLEVEL%"
echo   ----
echo   PowerShell exit code: %RC%
echo.

echo [STEP 7] Checking output...
if exist "%OUTPUT%" (
    echo   Output file exists: %CD%\%OUTPUT%
    for %%I in ("%OUTPUT%") do echo   Size: %%~zI bytes
    echo   First lines:
    echo   ----
    powershell.exe -NoProfile -Command "Get-Content -LiteralPath '%OUTPUT%' -TotalCount 30"
    echo   ----
) else (
    echo   *** Output file was NOT created ***
)

echo.
if "%RC%"=="0" (
    echo ============================================================
    echo  DONE - success
    echo ============================================================
) else (
    echo ============================================================
    echo  FINISHED with errors  (exit code %RC%)
    echo ============================================================
)

:HoldOpen
echo.
echo (Press any key to close this window.)
pause >nul
exit /b 0