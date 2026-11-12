param([string]$Manifest = (Join-Path $PSScriptRoot '..\art_sources\cards\chibi_20261023\manifest.json'))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$Project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$SourceDir = Split-Path ([IO.Path]::GetFullPath($Manifest))
$CardDir = Join-Path $Project 'src\main\resources\roxymod\images\cards'
$Entries = @(Get-Content -LiteralPath $Manifest -Raw | ConvertFrom-Json)
$Seen = [Collections.Generic.HashSet[string]]::new()
foreach ($entry in $Entries) {
    if ($entry.id -notmatch '^roxy_[a-z_]+$') { throw 'Invalid card asset ID.' }
    if (-not $Seen.Add($entry.id)) { throw "Duplicate card asset: $($entry.id)" }
    if (-not (Test-Path -LiteralPath (Join-Path $CardDir ($entry.id + '.png')))) { throw "Unknown card asset: $($entry.id)" }
    $MasterPath = Join-Path $SourceDir ($entry.id + '.png')
    if (-not (Test-Path -LiteralPath $MasterPath)) {
        Copy-Item -LiteralPath $entry.source -Destination $MasterPath
    }
    $MasterImage = [Drawing.Image]::FromFile($MasterPath)
    try {
        # Engine delivery sizes only. Creative changes are made with image_gen.
        $Ratio = 250.0 / 190.0
        $CropWidth = [Math]::Min($MasterImage.Width, $MasterImage.Height * $Ratio)
        $CropHeight = $CropWidth / $Ratio
        $Crop = [Drawing.RectangleF]::new(($MasterImage.Width - $CropWidth) / 2, ($MasterImage.Height - $CropHeight) / 2, $CropWidth, $CropHeight)
        foreach ($Scale in @(1, 2)) {
            $Bitmap = [Drawing.Bitmap]::new(250 * $Scale, 190 * $Scale)
            $Graphics = [Drawing.Graphics]::FromImage($Bitmap)
            try {
                $Graphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
                $Graphics.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::HighQuality
                $DestRect = [Drawing.RectangleF]::new(0, 0, $Bitmap.Width, $Bitmap.Height)
                $Graphics.DrawImage($MasterImage, $DestRect, $Crop, [Drawing.GraphicsUnit]::Pixel)
                $Suffix = if ($Scale -eq 2) { '_p.png' } else { '.png' }
                $Bitmap.Save((Join-Path $CardDir ($entry.id + $Suffix)), [Drawing.Imaging.ImageFormat]::Png)
            } finally { $Graphics.Dispose(); $Bitmap.Dispose() }
        }
    } finally { $MasterImage.Dispose() }
}
Write-Host "Imported $($Entries.Count) card illustrations and their large portraits."
