<#
.SYNOPSIS
Runs the real game through a scripted Roxy run (tools\smoke) and reports the result.

.DESCRIPTION
Builds RoxyMod.jar and the dev-only RoxySmokeTest.jar, then starts Slay the Spire through
ModTheSpire from a sandbox copy of the game folder (build\smoke-sandbox) that holds a copy of
your preferences. Your real profile, saves, run history and mods folder are never changed.

The game window opens for a few minutes and closes itself. Don't click in it while it runs.
Results go to build\smoke\out: smoke-report.txt, screenshots, combat-log.tsv and game logs.
#>
param(
    [int]$TimeoutSeconds = 600,
    [switch]$NoBuild
)

$ErrorActionPreference = 'Stop'
$Project = Split-Path $PSScriptRoot
$GameDir = if ($env:STS_GAME_DIR) { $env:STS_GAME_DIR } else { "C:\Program Files (x86)\Steam\steamapps\common\SlayTheSpire" }
$ModTheSpireJar = if ($env:MTS_JAR) { $env:MTS_JAR } else { "C:\Program Files (x86)\Steam\steamapps\workshop\content\646570\1605060445\ModTheSpire.jar" }
$BaseModJar = if ($env:BASEMOD_JAR) { $env:BASEMOD_JAR } else { "C:\Program Files (x86)\Steam\steamapps\workshop\content\646570\1605833019\BaseMod.jar" }
$GameJar = Join-Path $GameDir 'desktop-1.0.jar'
$GameJava = Join-Path $GameDir 'jre\bin\java.exe'
foreach ($file in @($GameJar, $GameJava, $ModTheSpireJar, $BaseModJar)) {
    if (-not (Test-Path -LiteralPath $file)) { throw "Required file not found: $file" }
}
$running = Get-Process -Name java, javaw -ErrorAction SilentlyContinue | Where-Object { $_.Path -and $_.Path.StartsWith($GameDir) }
if ($running) { throw 'Slay the Spire is already running. Close it first.' }

if (-not $NoBuild) { & (Join-Path $Project 'build.ps1') }
$ModJar = Join-Path $Project 'build\libs\RoxyMod.jar'
if (-not (Test-Path -LiteralPath $ModJar)) { throw 'build\libs\RoxyMod.jar is missing; run build.ps1 first.' }

$JdkBin = if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    Join-Path $env:JAVA_HOME 'bin'
} else {
    Split-Path (Get-Command javac -ErrorAction Stop).Source
}

# Build the dev-only harness mod. It is never copied into the real mods folder.
$SmokeDir = Join-Path $Project 'build\smoke'
$Classes = Join-Path $SmokeDir 'classes'
$Out = Join-Path $SmokeDir 'out'
foreach ($dir in @($Classes, $Out)) {
    if (Test-Path -LiteralPath $dir) { Remove-Item -LiteralPath $dir -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
}
$sources = (Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'smoke\src') -Recurse -Filter *.java).FullName
& (Join-Path $JdkBin 'javac.exe') --release 8 -Xlint:-options -proc:none -encoding UTF-8 -cp "$GameJar;$ModTheSpireJar;$BaseModJar;$ModJar" -d $Classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Smoke test harness failed to compile.' }
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'smoke\ModTheSpire.json') -Destination $Classes
$SmokeJar = Join-Path $SmokeDir 'RoxySmokeTest.jar'
& (Join-Path $JdkBin 'jar.exe') "-J-Djava.io.tmpdir=$SmokeDir" cf $SmokeJar -C $Classes .
if ($LASTEXITCODE -ne 0) { throw 'Could not package the smoke test harness.' }

# Sandbox game folder: the game reads and writes preferences, saves and runs relative to it.
$Sandbox = Join-Path $Project 'build\smoke-sandbox'
New-Item -ItemType Directory -Force -Path $Sandbox | Out-Null
$SandboxJar = Join-Path $Sandbox 'desktop-1.0.jar'
if (-not (Test-Path -LiteralPath $SandboxJar) -or (Get-Item -LiteralPath $SandboxJar).Length -ne (Get-Item -LiteralPath $GameJar).Length) {
    Copy-Item -LiteralPath $GameJar -Destination $SandboxJar -Force
}
Copy-Item -LiteralPath (Join-Path $GameDir 'steam_appid.txt') -Destination $Sandbox -Force
foreach ($dir in @('preferences', 'saves', 'runs', 'mods', 'sendToDevs')) {
    $path = Join-Path $Sandbox $dir
    if (Test-Path -LiteralPath $path) { Remove-Item -LiteralPath $path -Recurse -Force }
}
Copy-Item -LiteralPath (Join-Path $GameDir 'preferences') -Destination (Join-Path $Sandbox 'preferences') -Recurse
# Keep the window mode: a missing display config makes the game pick its own defaults.
$display = @(Get-Content -LiteralPath (Join-Path $GameDir 'info.displayconfig'))
if ($display.Count -ge 5) { $display[3] = 'false'; $display[4] = 'false' }
Set-Content -LiteralPath (Join-Path $Sandbox 'info.displayconfig') -Value $display -Encoding ASCII
$SandboxMods = Join-Path $Sandbox 'mods'
New-Item -ItemType Directory -Force -Path $SandboxMods | Out-Null
Copy-Item -LiteralPath $ModJar, $SmokeJar -Destination $SandboxMods

$javaArgs = @(
    '-Droxy.dev.combatLog=true',
    "-Droxy.dev.combatLogFile=$Out\combat-log.tsv",
    "-Droxy.smoke.out=$Out",
    '-jar', $ModTheSpireJar,
    # Not --close-when-finished: ModTheSpire then exits as soon as the game window opens.
    '--skip-launcher', '--skip-intro',
    '--mods', 'basemod,roxymod,roxysmoke'
)
$quoted = $javaArgs | ForEach-Object { if ($_ -match '\s') { '"' + $_ + '"' } else { $_ } }
Write-Host 'Starting Slay the Spire from the sandbox. The window closes itself; please do not click in it.'
$process = Start-Process -FilePath $GameJava -ArgumentList $quoted -WorkingDirectory $Sandbox -NoNewWindow -PassThru `
    -RedirectStandardOutput (Join-Path $Out 'game-stdout.log') -RedirectStandardError (Join-Path $Out 'game-stderr.log')

# The harness writes its report and then quits the game. ModTheSpire's own window can keep
# the Java process alive afterwards, so close it once the game has had time to shut down.
$Report = Join-Path $Out 'smoke-report.txt'
$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
$reportSeenAt = $null
while (-not $process.HasExited) {
    if ((Get-Date) -gt $deadline) {
        Stop-Process -Id $process.Id -Force
        throw "The game did not finish within $TimeoutSeconds seconds; see $Out."
    }
    if (-not $reportSeenAt -and (Test-Path -LiteralPath $Report)) { $reportSeenAt = Get-Date }
    if ($reportSeenAt -and ((Get-Date) - $reportSeenAt).TotalSeconds -gt 20) {
        Stop-Process -Id $process.Id -Force
        break
    }
    Start-Sleep -Milliseconds 500
}
$GameLog = Join-Path $Sandbox 'sendToDevs\logs\SlayTheSpire.log'
if (Test-Path -LiteralPath $GameLog) { Copy-Item -LiteralPath $GameLog -Destination $Out }

if (-not (Test-Path -LiteralPath $Report)) {
    Get-Content -LiteralPath (Join-Path $Out 'game-stdout.log') -Tail 40
    throw "The game exited without writing a smoke report; see $Out\game-stdout.log."
}
Get-Content -LiteralPath $Report -Encoding UTF8
$CombatLog = Join-Path $Out 'combat-log.tsv'
if (Test-Path -LiteralPath $CombatLog) {
    Write-Host ''
    Write-Host 'Combat log events:'
    Import-Csv -LiteralPath $CombatLog -Delimiter "`t" -Encoding UTF8 | Group-Object event |
        Sort-Object Name | ForEach-Object { Write-Host ("  {0,-22} {1}" -f $_.Name, $_.Count) }
}
Write-Host ''
Write-Host "Screenshots and logs: $Out"
if ((Get-Content -LiteralPath $Report -TotalCount 1) -notlike 'RESULT: PASS*') { exit 1 }
