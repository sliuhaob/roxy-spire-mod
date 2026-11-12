param([Parameter(Mandatory=$true)][string]$Classpath)
$ErrorActionPreference = 'Stop'
$Project = Split-Path $PSScriptRoot
$Jdk = Split-Path (Get-Command jar -ErrorAction Stop).Source
if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    $Jdk = Join-Path $env:JAVA_HOME 'bin'
}
$TestClasses = Join-Path $Project 'build\test-classes'
New-Item -ItemType Directory -Force -Path $TestClasses | Out-Null
$MainClasses = Join-Path $Project 'build\classes'
$TestClasspath = "$MainClasses;$Classpath"
& (Join-Path $Jdk 'javac.exe') -proc:none -encoding UTF-8 -cp $TestClasspath -d $TestClasses (Join-Path $PSScriptRoot 'tests\PowerIconHelper.java') (Join-Path $PSScriptRoot 'tests\PatchEmulator.java') (Join-Path $PSScriptRoot 'tests\MechanicsTest.java')
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
# Apply the mod's patches to copies of the game classes, as ModTheSpire does at launch.
$PatchedClasses = Join-Path $Project 'build\patched-classes'
New-Item -ItemType Directory -Force -Path $PatchedClasses | Out-Null
& (Join-Path $Jdk 'java.exe') -cp "$TestClasses;$TestClasspath" PatchEmulator $MainClasses $PatchedClasses
if ($LASTEXITCODE -ne 0) { throw 'Patch emulation failed: a patch would not apply at game launch.' }
$TestState = Join-Path $Project 'build\headless-state'
New-Item -ItemType Directory -Force -Path $TestState | Out-Null
Push-Location $TestState
try {
    $LogFile = Join-Path $Project 'build\mechanics-tests.log'
    & (Join-Path $Jdk 'java.exe') "-Droxy.projectDir=$Project" -cp "$PatchedClasses;$TestClasses;$TestClasspath" MechanicsTest *> $LogFile
    Get-Content -LiteralPath $LogFile | Select-String '^PASS:|Exception|AssertionError'
    if ($LASTEXITCODE -ne 0) { throw 'Mechanics regression tests failed.' }
    & (Join-Path $PSScriptRoot 'audit-assets.ps1')
} finally { Pop-Location }
