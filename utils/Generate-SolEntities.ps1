[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$CsvPath,

    [Parameter(Mandatory = $false)]
    [string]$OutputPath,

    [Parameter(Mandatory = $false)]
    [string]$SettingsPath = 'C:\Program Files (x86)\Fractal Softworks\Starsector\mods\Solsector\data\config\sol_settings.json'
)

$ErrorActionPreference = 'Stop'
trap {
    Write-Host ""
    Write-Host "!!! UNHANDLED ERROR !!!" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host $_.ScriptStackTrace -ForegroundColor DarkRed
    exit 99
}

Write-Host "[PS1] Started." -ForegroundColor Cyan
Write-Host "[PS1] CsvPath      = $CsvPath"
Write-Host "[PS1] OutputPath   = $OutputPath"
Write-Host "[PS1] SettingsPath = $SettingsPath"
Write-Host "[PS1] PSVersion    = $($PSVersionTable.PSVersion)"
Write-Host ""

function ConvertTo-Bool {
    param([string]$Value)
    if ([string]::IsNullOrWhiteSpace($Value)) { return $false }
    return $Value.Trim().ToLower() -in @('true', '1', 'yes', 'y', 't')
}

function Read-SolSettings {
    param([string]$Path)

    $defaults = @{
        sizeExt    = 220.0
        sizeInt    = 0.00005
        sizeDenom  = 0.001
        sizeConst  = 3.0
        sizeLinMod = 1.0
        Linear_Size = $false
        To_Scale    = $false
        Linear_Distance = $false
        distLinMod  = 1.0
    }

    if (-not (Test-Path -LiteralPath $Path)) {
        Write-Host "[PS1] Settings file NOT found at $Path - using defaults." -ForegroundColor Yellow
        return $defaults
    }

    Write-Host "[PS1] Reading settings from $Path"
    $raw = Get-Content -LiteralPath $Path -Raw

    # Strip ## line and trailing comments
    $cleaned = ($raw -split "`n" | ForEach-Object {
        ($_ -replace '##.*$', '').TrimEnd()
    }) -join "`n"

    # Remove trailing commas before } or ]
    $cleaned = $cleaned -replace ',(\s*[}\]])', '$1'

    try {
        $json = $cleaned | ConvertFrom-Json
    } catch {
        Write-Host "[PS1] WARN: settings file failed to parse: $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host "[PS1] Falling back to defaults." -ForegroundColor Yellow
        return $defaults
    }

    $merged = @{}
    foreach ($k in $defaults.Keys) { $merged[$k] = $defaults[$k] }
    foreach ($p in $json.PSObject.Properties) {
        if ($merged.ContainsKey($p.Name)) { $merged[$p.Name] = $p.Value }
    }

    Write-Host ("[PS1] Settings: sizeExt={0} sizeInt={1} sizeDenom={2} sizeConst={3} sizeLinMod={4} Linear_Size={5} To_Scale={6}" -f `
        $merged.sizeExt, $merged.sizeInt, $merged.sizeDenom, $merged.sizeConst, $merged.sizeLinMod, $merged.Linear_Size, $merged.To_Scale)

    return $merged
}

function Get-GameSize {
    # Mirror of AstroCalc.getSize(km)
    param([double]$km, [hashtable]$S)

    $sizeStop = 1.0
    $sizeLin  = 0.0

    $sizeExt   = [double]$S.sizeExt
    $sizeInt   = [double]$S.sizeInt
    $sizeDenom = [double]$S.sizeDenom
    $sizeConst = [double]$S.sizeConst

    $term1 = $sizeExt * [math]::Log($sizeInt * $km + 1.0)
    $term2 = ([math]::Sqrt($km) / (1.0 + $sizeDenom * $km)) * $sizeStop
    $term3 = $sizeConst
    $term4 = $sizeLin * $km

    return $term1 + $term2 + $term3 + $term4
}

function Get-EntryString {
    param($Row, [int]$RowNum, [hashtable]$Settings)

    $name      = ($Row.Name      | Out-String).Trim()
    $textureId = ($Row.TextureID | Out-String).Trim()
    $diamRaw   = ($Row.Diameter  | Out-String).Trim()
    $isCBRaw   = ($Row.IsContactBinary | Out-String).Trim()

    if ([string]::IsNullOrWhiteSpace($name))      { throw "Row $RowNum missing Name." }
    if ([string]::IsNullOrWhiteSpace($textureId)) { throw "Row $RowNum ('$name') missing TextureID." }
    if ([string]::IsNullOrWhiteSpace($diamRaw))   { throw "Row $RowNum ('$name') missing Diameter." }

    $diameterKm = 0.0
    if (-not [double]::TryParse($diamRaw, [ref]$diameterKm) -or $diameterKm -le 0) {
        throw "Row $RowNum ('$name') has invalid Diameter '$diamRaw' (must be km, > 0)."
    }
    $isCB = ConvertTo-Bool $isCBRaw

    $defaultName = if ($Row.PSObject.Properties['DefaultName'] -and $Row.DefaultName) { ($Row.DefaultName | Out-String).Trim() } else { 'Asteroid' }
    $nameInText  = if ($Row.PSObject.Properties['NameInText']  -and $Row.NameInText)  { ($Row.NameInText  | Out-String).Trim() } else { $defaultName }
    $shortName   = if ($Row.PSObject.Properties['ShortName']   -and $Row.ShortName)   { ($Row.ShortName   | Out-String).Trim() } else { $defaultName }
    $aOrAn       = if ($Row.PSObject.Properties['AOrAn']       -and $Row.AOrAn)       { ($Row.AOrAn       | Out-String).Trim() } else { 'a' }
    $layer       = if ($Row.PSObject.Properties['Layer']       -and $Row.Layer)       { ($Row.Layer       | Out-String).Trim() } else { 'TERRAIN_1' }

    $subsection = if ($Row.PSObject.Properties['FileSubsection'] -and $Row.FileSubsection) {
                      ($Row.FileSubsection | Out-String).Trim().Trim('/','\')
                  } else { '' }
    $spritePath = if ($subsection) { "graphics/asteroids/$subsection/$textureId.png" } else { "graphics/asteroids/$textureId.png" }

    $discoverable = $false
    if ($Row.PSObject.Properties['Discoverable']) { $discoverable = ConvertTo-Bool $Row.Discoverable }

    # size: if this body belongs to a system (SystemDiameter set), size it as a
    # linear fraction of the whole system's game-size. The log formula is applied
    # ONCE to the system extent, then divided by real proportion, so companions like
    # Dactyl stay small relative to Ida instead of being flattened to near-equal.
    # Otherwise fall back to the plain log formula on the body's own diameter.
    $systemDiamKm = 0.0
    $hasSystem = $Row.PSObject.Properties['SystemDiameter'] -and `
                 [double]::TryParse( ($Row.SystemDiameter | Out-String).Trim(), [ref]$systemDiamKm ) -and `
                 $systemDiamKm -gt 0

    if ($hasSystem) {
        $systemGameSize  = Get-GameSize -km $systemDiamKm -S $Settings
        $gameRadiusFloat = [math]::Max(2.0, $systemGameSize * ($diameterKm / $systemDiamKm))
    } else {
        $gameRadiusFloat = Get-GameSize -km $diameterKm -S $Settings
    }
    $radius      = [int][math]::Round($gameRadiusFloat)
    $spriteSize  = [int][math]::Round($gameRadiusFloat * 2.0)

    # Icon size: every 3 radius -> +1 icon size, then x1.3 if contact binary
    if ($Row.PSObject.Properties['IconSize'] -and $Row.IconSize) {
        $iconSize = [int]$Row.IconSize
    } else {
        $baseIcon = [math]::Floor($radius / 3.0)
        if ($isCB) { $baseIcon = $baseIcon * 1.3 }
        $iconSize = [math]::Max(2, [int][math]::Round($baseIcon))
    }

    # Icon path. Default depends on contact-binary flag; CustomIcon column overrides.
    # CustomIcon is rooted under graphics/, e.g. "warroom/icon_planet.png".
    if ($isCB) {
        $icon = 'graphics/icons/icon_contactbinary.png'
    } else {
        $icon = 'graphics/warroom/icon_asteroid.png'
    }
    if ($Row.PSObject.Properties.Match('CustomIcon').Count -gt 0) {
        $customIcon = "$($Row.CustomIcon)".Trim()
        if ($customIcon) {
            # Strip leading slashes and an optional leading "graphics/"
            while ($customIcon.StartsWith('/') -or $customIcon.StartsWith('\')) {
                $customIcon = $customIcon.Substring(1)
            }
            if ($customIcon.ToLower().StartsWith('graphics/') -or $customIcon.ToLower().StartsWith('graphics\')) {
                $customIcon = $customIcon.Substring(9)
            }
            $icon = "graphics/$customIcon"
        }
    }

    if ($discoverable) { $discStr = 'true' } else { $discStr = 'false' }

    Write-Host ("[PS1]   row {0}: {1,-25} tex={2,-20} km={3,7:N2} -> radius={4,3} sprite={5,3} icon={6} cb={7}" -f `
        $RowNum, $name, $textureId, $diameterKm, $radius, $spriteSize, $iconSize, $isCB) -ForegroundColor DarkGray

    # Output
    $common = "`"defaultName`":`"$defaultName`", `"nameInText`":`"$nameInText`", `"shortName`":`"$shortName`", `"aOrAn`":`"$aOrAn`", `"isOrAre`":`"is`", `"discoverable`":$discStr, `"defaultRadius`":$radius, `"customDescriptionId`":`"sol_asteroid`", `"interactionImage`":`"graphics/illustrations/free_orbit.jpg`", `"icon`":`"$icon`", `"iconWidth`":$iconSize, `"iconHeight`":$iconSize, `"sprite`":`"$spritePath`", `"spriteWidth`":$spriteSize, `"spriteHeight`":$spriteSize, `"renderShadow`":true, `"useLightColor`":true, `"showInCampaign`":true, `"showIconOnMap`":true"

    $tail = "`"scaleNameWithZoom`":false, `"scaleIconWithZoom`":true, `"layers`":[$layer], `"renderCircleIndicator`":false, `"renderCircleIndicatorSelectionFlash`":false, },"

    $noName  = "`t`"${name}no_name`":{ $common, `"showNameOnMap`":false, $tail"

    return $named + "`r`n" + $noName
}

# main

$settings = Read-SolSettings -Path $SettingsPath
Write-Host ""

if (-not (Test-Path -LiteralPath $CsvPath)) {
    Write-Host "[PS1] CSV not found: $CsvPath" -ForegroundColor Red
    exit 2
}

Write-Host "[PS1] Importing CSV..."
$rows = @(Import-Csv -LiteralPath $CsvPath)
Write-Host "[PS1] Got $($rows.Count) data row(s)."

if ($rows.Count -eq 0) {
    Write-Host "[PS1] CSV has no data rows (header only?)." -ForegroundColor Yellow
    exit 3
}

$cols = ($rows[0].PSObject.Properties | ForEach-Object Name) -join ', '
Write-Host "[PS1] Columns detected: $cols"
Write-Host ""

$entries = New-Object System.Collections.Generic.List[string]
$failed  = 0
$i = 0
foreach ($row in $rows) {
    $i++
    try {
        $entries.Add( (Get-EntryString -Row $row -RowNum $i -Settings $settings) )
    } catch {
        Write-Host ("[PS1]   row {0} SKIPPED: {1}" -f $i, $_.Exception.Message) -ForegroundColor Yellow
        $failed++
    }
}

Write-Host ""
$entryCount = $entries.Count * 2  # each Add() returns a string holding 2 entries
Write-Host "[PS1] Built $entryCount entries from $($entries.Count) row(s), $failed skipped."

$output = ($entries -join "`r`n")

if ($OutputPath) {
    Write-Host "[PS1] Writing to: $OutputPath"
    Set-Content -LiteralPath $OutputPath -Value $output -Encoding UTF8
    Write-Host "[PS1] Wrote OK." -ForegroundColor Green
} else {
    Write-Host "[PS1] (no -OutputPath; printing below)"
    Write-Output $output
}

if ($failed -gt 0) { exit 1 }
exit 0