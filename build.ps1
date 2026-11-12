param(
    [switch]$Install,
    [switch]$Verify
)

$ErrorActionPreference = "Stop"

$GameDir = if ($env:STS_GAME_DIR) { $env:STS_GAME_DIR } else { "C:\Program Files (x86)\Steam\steamapps\common\SlayTheSpire" }
$ModTheSpireJar = if ($env:MTS_JAR) { $env:MTS_JAR } else { "C:\Program Files (x86)\Steam\steamapps\workshop\content\646570\1605060445\ModTheSpire.jar" }
$BaseModJar = if ($env:BASEMOD_JAR) { $env:BASEMOD_JAR } else { "C:\Program Files (x86)\Steam\steamapps\workshop\content\646570\1605833019\BaseMod.jar" }
$GameJar = Join-Path $GameDir "desktop-1.0.jar"

$requiredFiles = @($GameJar, $ModTheSpireJar, $BaseModJar)
foreach ($file in $requiredFiles) {
    if (-not (Test-Path -LiteralPath $file)) {
        throw "Required file not found: $file"
    }
}

$ProjectDir = $PSScriptRoot
$BuildDir = Join-Path $ProjectDir "build"
$ClassesDir = Join-Path $BuildDir "classes"
$DistDir = Join-Path $BuildDir "libs"
$JarPath = Join-Path $DistDir "RoxyMod.jar"
$JdkBin = if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    Join-Path $env:JAVA_HOME 'bin'
} else {
    Split-Path (Get-Command jar -ErrorAction Stop).Source
}
$JavaCompiler = Join-Path $JdkBin 'javac.exe'
$JarTool = Join-Path $JdkBin 'jar.exe'
if (-not (Test-Path -LiteralPath $JavaCompiler)) {
    throw 'Set JAVA_HOME to a full JDK (javac, java and jar must come from the same JDK).'
}

# Never allow a future path edit or a junction to redirect recursive cleanup.
$ResolvedProject = [IO.Path]::GetFullPath($ProjectDir).TrimEnd('\')
$ResolvedBuild = [IO.Path]::GetFullPath($BuildDir).TrimEnd('\')
if ($ResolvedBuild -ne (Join-Path $ResolvedProject 'build')) {
    throw "Refusing to clean a directory outside this project's build folder."
}
if (Test-Path -LiteralPath $BuildDir) {
    if ((Get-Item -LiteralPath $BuildDir).Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw "Refusing to recursively clean a build folder that is a junction or symlink."
    }
    Remove-Item -LiteralPath $BuildDir -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $ClassesDir, $DistDir | Out-Null

$sources = Get-ChildItem -LiteralPath (Join-Path $ProjectDir "src\main\java") -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }

if (-not $sources) {
    throw "No Java source files found."
}

$classpath = @($GameJar, $ModTheSpireJar, $BaseModJar) -join ";"
$SourcesFile = Join-Path $BuildDir "sources.txt"
$sources | ForEach-Object { '"' + ($_ -replace "\\", "/") + '"' } | Set-Content -LiteralPath $SourcesFile -Encoding ASCII

& $JavaCompiler --release 8 -Xlint:-options -proc:none -encoding UTF-8 -cp $classpath -d $ClassesDir "@$SourcesFile"
if ($LASTEXITCODE -ne 0) {
    throw "javac failed with exit code $LASTEXITCODE"
}

$ResourcesDir = Join-Path $ProjectDir "src\main\resources"
if (Test-Path -LiteralPath $ResourcesDir) {
    Copy-Item -Path (Join-Path $ResourcesDir "*") -Destination $ClassesDir -Recurse -Force
}

& $JarTool "-J-Djava.io.tmpdir=$BuildDir" cf $JarPath -C $ClassesDir .
if ($LASTEXITCODE -ne 0) {
    throw "jar failed with exit code $LASTEXITCODE"
}

Write-Host "Built: $JarPath"

if ($Verify) {
    & (Join-Path $ProjectDir 'tools\verify.ps1') -Classpath $classpath
}

if ($Install) {
    $ModsDir = Join-Path $GameDir "mods"
    New-Item -ItemType Directory -Force -Path $ModsDir | Out-Null
    $InstalledJar = Join-Path $ModsDir "RoxyMod.jar"
    Copy-Item -LiteralPath $JarPath -Destination $InstalledJar -Force
    if ((Get-FileHash -LiteralPath $JarPath).Hash -ne (Get-FileHash -LiteralPath $InstalledJar).Hash) {
        throw "Installed JAR does not match the build."
    }
    Write-Host "Installed and verified: $InstalledJar"
}
