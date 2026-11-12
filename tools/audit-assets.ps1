$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem
$Project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$JavaDir = Join-Path $Project 'src\main\java\roxymod'
$ResourceDir = Join-Path $Project 'src\main\resources'
$ModResources = Join-Path $ResourceDir 'roxymod'
$Registration = Get-Content (Join-Path $JavaDir 'RoxyMod.java') -Raw
$Cards = @(Get-ChildItem (Join-Path $JavaDir 'cards') -Filter '*.java')
$Localized = Get-Content (Join-Path $ModResources 'localization\zhs\CardStrings.json') -Raw | ConvertFrom-Json
$IDs = @($Localized.PSObject.Properties.Name)
$ExpectedImages = [Collections.Generic.HashSet[string]]::new()
foreach ($Card in $Cards) {
    $Source = Get-Content $Card.FullName -Raw
    $ID = 'roxymod:' + $Card.BaseName
    if ($IDs -notcontains $ID) { throw "Missing localization: $ID" }
    if ($Source -notmatch ('makeID\("' + $Card.BaseName + '"\)')) { throw "Mismatched card ID: $Card" }
    if ([regex]::Matches($Registration, ('new ' + $Card.BaseName + '\(\)')).Count -ne 1) { throw "Missing or duplicate registration: $ID" }
    $Match = [regex]::Match($Source, 'images/cards/([^"\r\n]+\.png)')
    if (-not $Match.Success) { throw "Missing card art reference: $ID" }
    $Name = $Match.Groups[1].Value
    foreach ($Large in @($false, $true)) {
        $ImageName = if ($Large) { $Name -replace '\.png$', '_p.png' } else { $Name }
        [void]$ExpectedImages.Add($ImageName)
        $Path = Join-Path $ModResources ('images\cards\' + $ImageName)
        if (-not (Test-Path -LiteralPath $Path)) { throw "Missing card portrait: $Path" }
        $Image = [Drawing.Image]::FromFile($Path)
        try {
            $Width = if ($Large) { 500 } else { 250 }
            $Height = if ($Large) { 380 } else { 190 }
            if ($Image.Width -ne $Width -or $Image.Height -ne $Height) { throw "Wrong card image dimensions: $Path" }
        } finally { $Image.Dispose() }
    }
}
if ($Cards.Count -ne 75 -or $IDs.Count -ne 75) { throw 'Expected 75 registered, localized cards.' }
$ArtDir = Join-Path $Project 'art_sources\cards\chibi_20261023'
$ArtManifest = @((Get-Content (Join-Path $ArtDir 'manifest.json') -Raw | ConvertFrom-Json))
if ($ArtManifest.Count -ne 75 -or @($ArtManifest.id | Sort-Object -Unique).Count -ne 75) { throw 'Expected 75 unique chibi card originals.' }
foreach ($Art in $ArtManifest) {
    if (-not $ExpectedImages.Contains($Art.id + '.png')) { throw "Unregistered art manifest entry: $($Art.id)" }
    if (-not (Test-Path -LiteralPath (Join-Path $ArtDir ($Art.id + '.png')))) { throw "Missing original artwork: $($Art.id)" }
}
$CharacterSpecs = @(
    @('character\roxy.png', 512, 512, $true),
    @('character\corpse.png', 512, 512, $true),
    @('character\roxy_campfire.png', 1561, 1008, $true),
    @('character\shoulder.png', 256, 256, $true),
    @('character\shoulder2.png', 256, 256, $true),
    @('charSelect\button.png', 200, 200, $true),
    @('charSelect\portrait.png', 1920, 1200, $false)
)
foreach ($Spec in $CharacterSpecs) {
    $Bitmap = [Drawing.Bitmap]::new((Join-Path $ModResources ('images\' + $Spec[0])))
    try {
        if ($Bitmap.Width -ne $Spec[1] -or $Bitmap.Height -ne $Spec[2]) { throw "Wrong character image dimensions: $($Spec[0])" }
        if ($Spec[3] -and ($Bitmap.GetPixel(0, 0).A -ne 0 -or $Bitmap.GetPixel($Bitmap.Width - 1, 0).A -ne 0)) { throw "Missing transparent character padding: $($Spec[0])" }
    } finally { $Bitmap.Dispose() }
}
foreach ($Kind in @('Relic', 'Potion')) {
    $ExpectedCount = if ($Kind -eq 'Relic') { 10 } else { 3 }
    $Localization = Get-Content (Join-Path $ModResources ('localization\zhs\' + $Kind + 'Strings.json')) -Raw | ConvertFrom-Json
    $Keys = @($Localization.PSObject.Properties.Name)
    if ($Keys.Count -ne $ExpectedCount) { throw "Wrong $Kind localization count." }
    foreach ($Key in $Keys) {
        $ClassName = $Key -replace '^roxymod:', ''
        if (-not (Test-Path (Join-Path $JavaDir ($Kind.ToLower() + 's\' + $ClassName + '.java')))) { throw "Missing $Kind class: $Key" }
        if ($Registration -notmatch [regex]::Escape($ClassName)) { throw "Missing $Kind registration: $Key" }
    }
}
foreach ($SourceFile in (Get-ChildItem $JavaDir -Recurse -Filter '*.java')) {
    $Source = Get-Content $SourceFile.FullName -Raw
    foreach ($Reference in [regex]::Matches($Source, 'assetPath\("([^"]+)"\)')) {
        if (-not (Test-Path -LiteralPath (Join-Path $ModResources $Reference.Groups[1].Value))) { throw "Missing resource in $($SourceFile.Name): $Reference" }
    }
    foreach ($Icon in [regex]::Matches($Source, 'PowerIconHelper\.load(Large|Small)\("([^"]+)"\)')) {
        $Size = if ($Icon.Groups[1].Value -eq 'Large') { 84 } else { 32 }
        $Path = Join-Path $ModResources ('images\powers\' + $Icon.Groups[2].Value + $Size + '.png')
        $Image = [Drawing.Image]::FromFile($Path)
        try { if ($Image.Width -ne $Size -or $Image.Height -ne $Size) { throw "Wrong power icon size: $Path" } }
        finally { $Image.Dispose() }
    }
    if ($Source -match 'new ReducePowerAction\([^;]+ChantPower.POWER_ID') { throw "Legacy chant payment: $($SourceFile.Name)" }
    if ($SourceFile.Name -ne 'ApplyFreezeAction.java' -and $Source -match 'new FreezePower\(') { throw "Freeze source bypasses reaction: $($SourceFile.Name)" }
}
foreach ($File in (Get-ChildItem (Join-Path $ModResources 'images\cards') -Filter '*.png')) {
    if (-not $ExpectedImages.Contains($File.Name)) { Write-Warning "Unreferenced card image: $($File.Name)" }
}
$JarPath = Join-Path $Project 'build\libs\RoxyMod.jar'
$Zip = [IO.Compression.ZipFile]::OpenRead($JarPath)
try {
    $Entries = @($Zip.Entries.FullName)
    foreach ($Card in $Cards) {
        if ($Entries -notcontains ('roxymod/cards/' + $Card.BaseName + '.class')) { throw "Card missing from JAR: $Card" }
    }
    if (@($Entries | Where-Object { $_ -match 'MechanicsTest|test-classes|\.java$|sources.txt' }).Count) { throw 'Test/source files leaked into production JAR.' }
    foreach ($Resource in (Get-ChildItem $ResourceDir -Recurse -File)) {
        $Relative = $Resource.FullName.Substring($ResourceDir.Length + 1).Replace('\', '/')
        if ($Entries -notcontains $Relative) { throw "Resource missing from JAR: $Relative" }
        $Stream = $Zip.GetEntry($Relative).Open()
        $Hasher = [Security.Cryptography.SHA256]::Create()
        try {
            $PackagedHash = [BitConverter]::ToString($Hasher.ComputeHash($Stream)).Replace('-', '')
            if ($PackagedHash -ne (Get-FileHash -LiteralPath $Resource.FullName -Algorithm SHA256).Hash) { throw "Stale resource in JAR: $Relative" }
        } finally { $Stream.Dispose(); $Hasher.Dispose() }
    }
} finally { $Zip.Dispose() }
Write-Host 'PASS: 75 cards, 10 relics, 3 potions; 75 art originals; 150 portraits; 7 character resources; power icons; resource references; production JAR contents and SHA-256 hashes.'
