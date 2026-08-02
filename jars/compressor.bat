@echo off
setlocal

set "STARSECTOR=C:\Program Files (x86)\Fractal Softworks\Starsector"
set "MOD=%STARSECTOR%\mods\SolSystems"
set "SRCROOT=%MOD%\jars"
set "CLSROOT=%SRCROOT%\classes"
set "JAR_OUT=%SRCROOT%\solsystems.jar"

set "CLASSPATH=%STARSECTOR%\starsector-core\starfarer.api.jar;%STARSECTOR%\starsector-core\json.jar;%STARSECTOR%\starsector-core\log4j-1.2.9.jar;%STARSECTOR%\starsector-core\lwjgl.jar;%STARSECTOR%\starsector-core\lwjgl_util.jar;%STARSECTOR%\mods\Wide Horizons v1.4.0\jars\WideHorizons.jar;%STARSECTOR%\mods\Industrial.Evolution4.1.b\jars\IndEvo.jar"

REM --- Locate 7-Zip ---
set "SEVENZIP="
if exist "%ProgramFiles%\7-Zip\7z.exe" set "SEVENZIP=%ProgramFiles%\7-Zip\7z.exe"
if not defined SEVENZIP if exist "%ProgramFiles(x86)%\7-Zip\7z.exe" set "SEVENZIP=%ProgramFiles(x86)%\7-Zip\7z.exe"
if not defined SEVENZIP (
    where 7z.exe >nul 2>nul
    if not errorlevel 1 set "SEVENZIP=7z.exe"
)

REM --- Build the argfile: recurse soljars\ for every .java, writing each path
REM     with forward slashes so javac's @argfile parser doesn't eat the
REM     backslashes as escape sequences. ---
set "ARGFILE=%TEMP%\soljars_files.txt"
if exist "%ARGFILE%" del /q "%ARGFILE%"
for /f "delims=" %%F in ('dir /s /b "%SRCROOT%\soljars\*.java"') do call :ADD "%%F"

if not exist "%ARGFILE%" (
    echo.
    echo ERROR: no .java files found under "%SRCROOT%\soljars".
    echo.
    pause
    endlocal & exit /b 1
)

echo.
echo === Cleaning classes dir ===
echo.

REM Wipe and recreate so no stale .class files ever survive a build
if exist "%CLSROOT%" rmdir /s /q "%CLSROOT%"
if exist "%CLSROOT%" (
    echo.
    echo ERROR: could not clear "%CLSROOT%" - a file is locked or in use.
    echo Close anything holding it open ^(Starsector, an editor^) and retry.
    echo.
    pause
    endlocal & exit /b 1
)
mkdir "%CLSROOT%"

echo === Compiling ===
echo.

javac -d "%CLSROOT%" -classpath "%CLASSPATH%" -sourcepath "%SRCROOT%" "@%ARGFILE%"
set "JAVAC_ERR=%ERRORLEVEL%"

del /q "%ARGFILE%"

if not "%JAVAC_ERR%"=="0" (
    echo.
    echo === BUILD FAILED ===
    echo.
    pause
    endlocal & exit /b %JAVAC_ERR%
)

echo === COMPILE OK ===
echo.
echo === Packaging solsystems.jar ===
echo.

if not defined SEVENZIP (
    echo ERROR: 7z.exe not found. Install 7-Zip or add it to PATH.
    pause
    endlocal & exit /b 1
)

REM Delete existing jar so we get a fresh archive instead of an update-merge
if exist "%JAR_OUT%" del /q "%JAR_OUT%"

REM Zip the CONTENTS of classes\ so package paths sit at the jar root. Sourcing
REM from classes\ means only .class files are packaged; .java sources never leak
REM into the jar. -tzip = ZIP format (jar is a zip).
pushd "%CLSROOT%"
"%SEVENZIP%" a -tzip -mx=9 -r "%JAR_OUT%" "soljars\*"
set "ZIP_ERR=%ERRORLEVEL%"
popd

if not "%ZIP_ERR%"=="0" (
    echo.
    echo === PACKAGING FAILED ===
    echo.
    pause
    endlocal & exit /b %ZIP_ERR%
)

echo.
echo === BUILD OK ===
echo Output: %JAR_OUT%

endlocal
exit /b 0

REM === Subroutine: append one path to the argfile, backslashes -> slashes ===
:ADD
set "F=%~1"
>> "%ARGFILE%" echo "%F:\=/%"
goto :eof