@echo off
setlocal

set "STARSECTOR=C:\Program Files (x86)\Fractal Softworks\Starsector"
set "MOD=%STARSECTOR%\mods\Solsector"
set "SRCROOT=%MOD%\jars"
set "SRCROOT_FWD=%SRCROOT:\=/%"
set "JAR_OUT=%SRCROOT%\solsector.jar"

set "CLASSPATH=%STARSECTOR%\starsector-core\starfarer.api.jar;%STARSECTOR%\starsector-core\json.jar;%STARSECTOR%\starsector-core\log4j-1.2.9.jar;%STARSECTOR%\starsector-core\lwjgl_util.jar;%STARSECTOR%\mods\Wide Horizons v1.4.0\jars\WideHorizons.jar"

REM --- Locate 7-Zip ---
set "SEVENZIP="
if exist "%ProgramFiles%\7-Zip\7z.exe" set "SEVENZIP=%ProgramFiles%\7-Zip\7z.exe"
if not defined SEVENZIP if exist "%ProgramFiles(x86)%\7-Zip\7z.exe" set "SEVENZIP=%ProgramFiles(x86)%\7-Zip\7z.exe"
if not defined SEVENZIP (
    where 7z.exe >nul 2>nul
    if not errorlevel 1 set "SEVENZIP=7z.exe"
)

set "ARGFILE=%TEMP%\soljars_files.txt"
> "%ARGFILE%" (
    echo "%SRCROOT_FWD%/soljars/compat/widehorizons/LocationXY.java"
    echo "%SRCROOT_FWD%/soljars/econ/utils/IntelHelper.java"
    echo "%SRCROOT_FWD%/soljars/econ/utils/IndustryCompat.java"
    echo "%SRCROOT_FWD%/soljars/econ/utils/Apocalypse.java"
    echo "%SRCROOT_FWD%/soljars/econ/utils/DistanceConditionManager.java"
    echo "%SRCROOT_FWD%/soljars/econ/utils/OrbitRulerHelper.java"
    echo "%SRCROOT_FWD%/soljars/gen/utils/SolHyperspaceGen.java"
    echo "%SRCROOT_FWD%/soljars/gen/utils/RemnantPatrolFactory.java"
    echo "%SRCROOT_FWD%/soljars/gen/utils/RemnantNexusFactory.java"
    echo "%SRCROOT_FWD%/soljars/gen/utils/AstroCalc.java"
    echo "%SRCROOT_FWD%/soljars/gen/utils/CompoundOrbitTool.java"
    echo "%SRCROOT_FWD%/soljars/gen/utils/ThreeBodySolution.java"
    echo "%SRCROOT_FWD%/soljars/econ/industries/utils/RemoveReplaceIndustry.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AncientDrugLab.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AncientOrbitalManufactories.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AntimatterInfrastructure.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AutomatedHabitats.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/ContactBinary.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/Degenerate.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/DegenerateSubpop.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/DistAbyssal.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/DistErebal.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/DistHadal.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/DistTartarean.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/DistDistant.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/DistOortal.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/FrozenAtmosphere.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/TenousAtmosphere.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/FrozenAtmospherePolar.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/FastRotator.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/GoblinWorld.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/GoblinSubpop.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/InsurgentNetwork.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/InsurgentNetworkDesperate.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/InsurgentNetworkComplete.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/LooseBioweapon.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/LECQHQ.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/Megaforges.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/MegaforgesComplete.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/MegaforgesHyperenergetic.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/MegaforgesHyperenergeticComplete.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/Meteoroids.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/OrbitalFleetworks.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/OrganComplex.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/PenalWorld.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/PreDomainSapience.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AntimatterNonproliferationTreatyBreacher.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AntimatterNonproliferationTreatySignatory.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AITerminators.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AISecuritySystems.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AISecuritySystemsComplete.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AIFreedomFighters.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/AccessCondition.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/CradleOfAsh.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/CivilisedWorld.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/CivilisedSubpop.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/DustStorms.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/MonumentFallen.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/OortStrikes.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/PondScum.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/SubsurfaceOcean.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/WorldWar.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/TinyPolity.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/TinyStripped.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/UnexplodedOrdnance.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/Unpronounceable.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/CometExtreme.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/CometActive.java"
    echo "%SRCROOT_FWD%/soljars/econ/conditions/CometInactive.java"
    echo "%SRCROOT_FWD%/soljars/econ/industries/SubsurfaceAquaponicsIndustry.java"
    echo "%SRCROOT_FWD%/soljars/gen/systems/sol/SolEconomies.java"
    echo "%SRCROOT_FWD%/soljars/gen/systems/sol/SolDeferredSetupScript.java"
    echo "%SRCROOT_FWD%/soljars/gen/systems/sol/GiantMoonsTotal.java"
    echo "%SRCROOT_FWD%/soljars/gen/systems/sol/SolTotal.java"
    echo "%SRCROOT_FWD%/soljars/gen/systems/sol/SolInnit.java"
    echo "%SRCROOT_FWD%/soljars/gen/systems/sol/SolInner.java"
)

echo.
echo === Compiling ===
echo.

javac -d "%SRCROOT%" -classpath "%CLASSPATH%" -sourcepath "%SRCROOT%" "@%ARGFILE%"
set "JAVAC_ERR=%ERRORLEVEL%"

del "%ARGFILE%"

if not "%JAVAC_ERR%"=="0" (
    echo.
    echo === BUILD FAILED ===
    echo.
    pause
    endlocal & exit /b %JAVAC_ERR%
)

echo === COMPILE OK ===
echo.
echo === Packaging solsector.jar ===
echo.

if not defined SEVENZIP (
    echo ERROR: 7z.exe not found. Install 7-Zip or add it to PATH.
    pause
    endlocal & exit /b 1
)

REM Delete existing jar so we get a fresh archive instead of an update-merge
if exist "%JAR_OUT%" del /q "%JAR_OUT%"

REM Zip the CONTENTS of the soljars folder (note the \soljars\* pattern) so
REM package paths sit at the jar root. -tzip = ZIP format (jar is a zip).
pushd "%SRCROOT%"
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