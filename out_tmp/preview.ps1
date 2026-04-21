Add-Type -AssemblyName System.Drawing
$root='c:\Users\ÄþÓîº¼\IdeaProjects\Game1'
$bg = New-Object System.Drawing.Bitmap((Join-Path $root 'image\images\±³¾°\backimage.jpg'))
$player = New-Object System.Drawing.Bitmap((Join-Path $root 'tmp_player_overlay.png'))
$out = New-Object System.Drawing.Bitmap(1000,640)
$g = [System.Drawing.Graphics]::FromImage($out)
$g.DrawImage($bg,0,0,1000,640)
$x = [int](1000/2 - 34/2)
$y = 640 - 54 - 26
$drawX = $x + (34 - $player.Width) / 2
$drawY = $y + 54 - $player.Height
$g.DrawImage($player, [int]$drawX, [int]$drawY, $player.Width, $player.Height)
$g.Dispose()
$outPath = Join-Path $root 'out_tmp\expected_spawn.png'
$out.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
$bg.Dispose(); $player.Dispose(); $out.Dispose()
Write-Output $outPath
