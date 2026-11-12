param([string]$SourceDir = (Join-Path $PSScriptRoot '..\art_sources\character\chibi_20261023'))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$Project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$Images = Join-Path $Project 'src\main\resources\roxymod\images'
# This script only crops transparent padding and scales image_gen artwork to engine sizes.
Add-Type -ReferencedAssemblies System.Drawing.Common, System.Drawing.Primitives -TypeDefinition @'
using System;
using System.Drawing;
public static class RoxyAlphaBounds {
    public static Rectangle Get(Bitmap b) {
        int left=b.Width, top=b.Height, right=-1, bottom=-1;
        for(int y=0;y<b.Height;y++) for(int x=0;x<b.Width;x++) {
            if(b.GetPixel(x,y).A <= 16) continue;
            left=Math.Min(left,x); top=Math.Min(top,y);
            right=Math.Max(right,x); bottom=Math.Max(bottom,y);
        }
        if(right<left) throw new Exception("Empty artwork.");
        return Rectangle.FromLTRB(left,top,right+1,bottom+1);
    }
}
'@
$Entries = @(Get-Content (Join-Path $SourceDir 'manifest.json') -Raw | ConvertFrom-Json)
$Entries += Get-Content (Join-Path $SourceDir 'button.json') -Raw | ConvertFrom-Json
foreach ($Entry in $Entries) {
    if ($Entry.id -notmatch '^roxy_[a-z]+$') { throw 'Invalid character art ID.' }
    $Master = Join-Path $SourceDir ($Entry.id + '.png')
    if (-not (Test-Path -LiteralPath $Master)) { Copy-Item -LiteralPath $Entry.source -Destination $Master }
}
function Export-Art([string]$ID, [string]$Target, [int]$Width, [int]$Height, [Drawing.RectangleF]$Box, [bool]$Transparent) {
    $Source = [Drawing.Bitmap]::new((Join-Path $SourceDir ($ID + '.png')))
    $Canvas = [Drawing.Bitmap]::new($Width, $Height, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $G = [Drawing.Graphics]::FromImage($Canvas)
    try {
        $G.Clear([Drawing.Color]::Transparent)
        $G.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $G.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        if ($Transparent) {
            if ($Source.GetPixel(0, 0).A -gt 16) { throw "Artwork must have real alpha: $ID" }
            $Crop = [Drawing.RectangleF][RoxyAlphaBounds]::Get($Source)
            $Scale = [Math]::Min($Box.Width / $Crop.Width, $Box.Height / $Crop.Height)
            $W = $Crop.Width * $Scale; $H = $Crop.Height * $Scale
            $Dest = [Drawing.RectangleF]::new($Box.X + ($Box.Width - $W) / 2, $Box.Bottom - $H, $W, $H)
        } else {
            $Crop = [Drawing.RectangleF]::new(0, 0, $Source.Width, $Source.Height)
            $Dest = $Box
        }
        $G.DrawImage($Source, $Dest, $Crop, [Drawing.GraphicsUnit]::Pixel)
        $Canvas.Save((Join-Path $Images $Target), [Drawing.Imaging.ImageFormat]::Png)
    } finally { $G.Dispose(); $Canvas.Dispose(); $Source.Dispose() }
}
Export-Art 'roxy_battle' 'character\roxy.png' 512 512 ([Drawing.RectangleF]::new(106,201,300,298)) $true
Export-Art 'roxy_corpse' 'character\corpse.png' 512 512 ([Drawing.RectangleF]::new(130,240,282,161)) $true
Export-Art 'roxy_campfire' 'character\roxy_campfire.png' 1561 1008 ([Drawing.RectangleF]::new(16,72,871,936)) $true
Export-Art 'roxy_campfire' 'character\shoulder.png' 256 256 ([Drawing.RectangleF]::new(98,150,60,90)) $true
Export-Art 'roxy_campfire' 'character\shoulder2.png' 256 256 ([Drawing.RectangleF]::new(98,150,60,90)) $true
Export-Art 'roxy_portrait' 'charSelect\portrait.png' 1920 1200 ([Drawing.RectangleF]::new(0,0,1920,1200)) $false
Export-Art 'roxy_button' 'charSelect\button.png' 200 200 ([Drawing.RectangleF]::new(25,25,151,151)) $true
Write-Host 'Imported 7 character and selection resources from 5 original illustrations.'
