param(
    [string]$OutputDirectory = "..\..\src\main\resources\startermod\images\powers"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$outputPath = [System.IO.Path]::GetFullPath((Join-Path $scriptDirectory $OutputDirectory))
[System.IO.Directory]::CreateDirectory($outputPath) | Out-Null

function New-ContinuationIcon {
    param(
        [int]$Size,
        [string]$Path
    )

    $scale = 4
    $canvasSize = $Size * $scale
    $bitmap = New-Object System.Drawing.Bitmap($canvasSize, $canvasSize, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $graphics.ScaleTransform($scale, $scale)

    $glow = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(75, 42, 203, 255), 8.0)
    $outer = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(230, 32, 139, 242), 4.0)
    $inner = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 184, 245, 255), 1.6)
    foreach ($pen in @($glow, $outer, $inner)) {
        $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
        $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    }

    foreach ($pen in @($glow, $outer, $inner)) {
        $graphics.DrawArc($pen, 10, 22, 38, 38, 45, 270)
        $graphics.DrawArc($pen, 36, 22, 38, 38, 225, 270)
    }

    $goldGlow = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(100, 255, 210, 52), 7.0)
    $gold = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 255, 220, 82), 2.5)
    $graphics.DrawLine($goldGlow, 36, 42, 48, 42)
    $graphics.DrawLine($gold, 36, 42, 48, 42)

    $arrowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 220, 250, 255))
    $leftArrow = [System.Drawing.PointF[]]@(
        (New-Object System.Drawing.PointF(16, 17)),
        (New-Object System.Drawing.PointF(28, 20)),
        (New-Object System.Drawing.PointF(20, 29))
    )
    $rightArrow = [System.Drawing.PointF[]]@(
        (New-Object System.Drawing.PointF(68, 67)),
        (New-Object System.Drawing.PointF(56, 64)),
        (New-Object System.Drawing.PointF(64, 55))
    )
    $graphics.FillPolygon($arrowBrush, $leftArrow)
    $graphics.FillPolygon($arrowBrush, $rightArrow)

    $graphics.Dispose()
    foreach ($resource in @($glow, $outer, $inner, $goldGlow, $gold, $arrowBrush)) {
        $resource.Dispose()
    }

    $output = New-Object System.Drawing.Bitmap($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $resizeGraphics = [System.Drawing.Graphics]::FromImage($output)
    $resizeGraphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $resizeGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $resizeGraphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $resizeGraphics.DrawImage($bitmap, 0, 0, $Size, $Size)
    $resizeGraphics.Dispose()
    $output.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    $output.Dispose()
    $bitmap.Dispose()
}

New-ContinuationIcon -Size 84 -Path (Join-Path $outputPath "continuation84.png")
New-ContinuationIcon -Size 32 -Path (Join-Path $outputPath "continuation32.png")
Write-Output "Generated continuation power icons in $outputPath"
