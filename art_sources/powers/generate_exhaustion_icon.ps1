param(
    [string]$OutputDirectory = "..\..\src\main\resources\roxymod\images\powers"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$outputPath = [System.IO.Path]::GetFullPath((Join-Path $scriptDirectory $OutputDirectory))
[System.IO.Directory]::CreateDirectory($outputPath) | Out-Null

function New-ExhaustionIcon {
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

    $glow = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(70, 147, 87, 220), 8.0)
    $outline = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(245, 113, 73, 188), 4.0)
    $highlight = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 218, 195, 255), 1.5)
    foreach ($pen in @($glow, $outline, $highlight)) {
        $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
        $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    }

    $crystal = [System.Drawing.PointF[]]@(
        (New-Object System.Drawing.PointF(42, 8)),
        (New-Object System.Drawing.PointF(67, 28)),
        (New-Object System.Drawing.PointF(58, 59)),
        (New-Object System.Drawing.PointF(42, 74)),
        (New-Object System.Drawing.PointF(26, 59)),
        (New-Object System.Drawing.PointF(17, 28))
    )

    $fill = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(210, 61, 42, 112))
    $graphics.FillPolygon($fill, $crystal)
    foreach ($pen in @($glow, $outline, $highlight)) {
        $graphics.DrawPolygon($pen, $crystal)
    }

    $crackGlow = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(90, 20, 9, 45), 6.0)
    $crack = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 238, 218, 255), 2.0)
    foreach ($pen in @($crackGlow, $crack)) {
        $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
        $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
        $graphics.DrawLines($pen, [System.Drawing.PointF[]]@(
            (New-Object System.Drawing.PointF(48, 15)),
            (New-Object System.Drawing.PointF(38, 34)),
            (New-Object System.Drawing.PointF(48, 43)),
            (New-Object System.Drawing.PointF(34, 65))
        ))
        $graphics.DrawLine($pen, 38, 34, 26, 38)
        $graphics.DrawLine($pen, 48, 43, 61, 49)
    }

    $graphics.Dispose()
    foreach ($resource in @($glow, $outline, $highlight, $fill, $crackGlow, $crack)) {
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

New-ExhaustionIcon -Size 84 -Path (Join-Path $outputPath "exhaustion84.png")
New-ExhaustionIcon -Size 32 -Path (Join-Path $outputPath "exhaustion32.png")
Write-Output "Generated exhaustion power icons in $outputPath"
