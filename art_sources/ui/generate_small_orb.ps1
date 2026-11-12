param(
    [string]$Badge = "..\..\src\main\resources\roxymod\images\512\card_roxy_orb.png",
    [string]$Output = "..\..\src\main\resources\roxymod\images\512\card_small_orb.png",
    [int]$Size = 24
)

# Builds the [E] text icon by shrinking the card cost badge, so the energy icon in card,
# relic and tooltip text matches the badge on Roxy's cards.

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$badgePath = [System.IO.Path]::GetFullPath((Join-Path $scriptDirectory $Badge))
$outputPath = [System.IO.Path]::GetFullPath((Join-Path $scriptDirectory $Output))

$source = New-Object System.Drawing.Bitmap($badgePath)
try {
    # Find the badge inside the 512 card layer: the bounding box of non-transparent pixels.
    $rect = New-Object System.Drawing.Rectangle(0, 0, $source.Width, $source.Height)
    $data = $source.LockBits($rect, [System.Drawing.Imaging.ImageLockMode]::ReadOnly, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $bytes = New-Object byte[] ($data.Stride * $source.Height)
    [System.Runtime.InteropServices.Marshal]::Copy($data.Scan0, $bytes, 0, $bytes.Length)
    $source.UnlockBits($data)
    $minX = $source.Width; $minY = $source.Height; $maxX = -1; $maxY = -1
    for ($y = 0; $y -lt $source.Height; $y++) {
        $row = $y * $data.Stride
        for ($x = 0; $x -lt $source.Width; $x++) {
            if ($bytes[$row + $x * 4 + 3] -ne 0) {
                if ($x -lt $minX) { $minX = $x }
                if ($x -gt $maxX) { $maxX = $x }
                if ($y -lt $minY) { $minY = $y }
                if ($y -gt $maxY) { $maxY = $y }
            }
        }
    }
    if ($maxX -lt 0) { throw "No visible pixels in $badgePath" }
    $crop = New-Object System.Drawing.Rectangle($minX, $minY, ($maxX - $minX + 1), ($maxY - $minY + 1))

    # Leave a 1 px margin so the frame is not clipped at the canvas edge.
    $inner = $Size - 1
    $icon = New-Object System.Drawing.Bitmap($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($icon)
    try {
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
        $attributes = New-Object System.Drawing.Imaging.ImageAttributes
        $attributes.SetWrapMode([System.Drawing.Drawing2D.WrapMode]::TileFlipXY)
        $target = New-Object System.Drawing.Rectangle(0, 0, $inner, $inner)
        $graphics.DrawImage($source, $target, $crop.X, $crop.Y, $crop.Width, $crop.Height, [System.Drawing.GraphicsUnit]::Pixel, $attributes)
    } finally {
        $graphics.Dispose()
    }
} finally {
    $source.Dispose()
}

$icon.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
$icon.Dispose()
Write-Host "Wrote $outputPath from badge area $($crop.X),$($crop.Y) $($crop.Width)x$($crop.Height)"
