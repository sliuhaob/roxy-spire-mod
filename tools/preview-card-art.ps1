param(
    [string]$Manifest = (Join-Path $PSScriptRoot '..\art_sources\cards\chibi_20261023\manifest.json'),
    [string]$Output = (Join-Path $PSScriptRoot '..\outputs\chibi_20261023')
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$Project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$Cards = @(Get-Content -LiteralPath $Manifest -Raw | ConvertFrom-Json | Sort-Object id)
New-Item -ItemType Directory -Path $Output -Force | Out-Null
$Font = [Drawing.Font]::new('Microsoft YaHei', 11)
$Brush = [Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(230, 238, 248))
try {
    for ($Page = 0; $Page -lt [Math]::Ceiling($Cards.Count / 25.0); $Page++) {
        $Rows = [Math]::Ceiling([Math]::Min(25, $Cards.Count - $Page * 25) / 5.0)
        $Canvas = [Drawing.Bitmap]::new(1340, [int]($Rows * 228 + 16))
        $G = [Drawing.Graphics]::FromImage($Canvas)
        try {
            $G.Clear([Drawing.Color]::FromArgb(22, 32, 49))
            for ($Index = 0; $Index -lt 25 -and ($Page * 25 + $Index) -lt $Cards.Count; $Index++) {
                $Entry = $Cards[$Page * 25 + $Index]
                $X = 16 + ($Index % 5) * 264
                $Y = 16 + [Math]::Floor($Index / 5.0) * 228
                $Art = [Drawing.Image]::FromFile((Join-Path $Project ('src\main\resources\startermod\images\cards\' + $Entry.id + '.png')))
                try { $G.DrawImageUnscaled($Art, [int]$X, [int]$Y) } finally { $Art.Dispose() }
                $G.DrawString($Entry.name, $Font, $Brush, [single]$X, [single]($Y + 194))
            }
            $Canvas.Save((Join-Path $Output ('card-preview-' + ($Page + 1) + '.png')), [Drawing.Imaging.ImageFormat]::Png)
        } finally { $G.Dispose(); $Canvas.Dispose() }
    }
} finally { $Font.Dispose(); $Brush.Dispose() }
Write-Host "Rendered $($Cards.Count) card thumbnails at game resolution."
