<#
.SYNOPSIS
Starts Slay the Spire with Roxy and the dev combat log on, for playtesting.

.DESCRIPTION
Launches ModTheSpire from the real game folder with the mods you name, skipping its launcher,
and turns on the combat log (one row per chant, Continuation, Freeze and card event, plus a
summary row per fight). Steam must be running. Install the mod first with
.\build.ps1 -Verify -Install.

Launching from Steam with "Play with mods" works too, but cannot turn the log on.

.EXAMPLE
.\tools\play.ps1
.\tools\play.ps1 -Mods basemod,stslib,roxymod
#>
param(
    [string[]]$Mods = @('basemod', 'roxymod'),
    [string]$LogFile = (Join-Path (Split-Path $PSScriptRoot) 'outputs\playtest\combat-log.tsv')
)

$ErrorActionPreference = 'Stop'
$GameDir = if ($env:STS_GAME_DIR) { $env:STS_GAME_DIR } else { "C:\Program Files (x86)\Steam\steamapps\common\SlayTheSpire" }
$ModTheSpireJar = if ($env:MTS_JAR) { $env:MTS_JAR } else { "C:\Program Files (x86)\Steam\steamapps\workshop\content\646570\1605060445\ModTheSpire.jar" }
$GameJava = Join-Path $GameDir 'jre\bin\java.exe'
foreach ($file in @($GameJava, $ModTheSpireJar)) {
    if (-not (Test-Path -LiteralPath $file)) { throw "Required file not found: $file" }
}
if (-not (Test-Path -LiteralPath (Join-Path $GameDir 'mods\RoxyMod.jar'))) {
    throw 'RoxyMod.jar is not installed. Run .\build.ps1 -Verify -Install first.'
}
if (-not (Get-Process -Name steam -ErrorAction SilentlyContinue)) {
    throw 'Steam is not running. Start Steam first so ModTheSpire can find BaseMod.'
}

$LogFile = [IO.Path]::GetFullPath($LogFile)
New-Item -ItemType Directory -Force -Path (Split-Path $LogFile) | Out-Null
$javaArgs = @(
    '-Droxy.dev.combatLog=true',
    "-Droxy.dev.combatLogFile=$LogFile",
    '-jar', $ModTheSpireJar,
    '--skip-launcher',
    '--mods', ($Mods -join ',')
)
$quoted = $javaArgs | ForEach-Object { if ($_ -match '\s') { '"' + $_ + '"' } else { $_ } }
Start-Process -FilePath $GameJava -ArgumentList $quoted -WorkingDirectory $GameDir | Out-Null
Write-Host "Starting Slay the Spire with: $($Mods -join ', ')"
Write-Host "Combat log: $LogFile"
