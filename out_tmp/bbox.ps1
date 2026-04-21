Add-Type -AssemblyName System.Drawing
$files = 'tmp_player_overlay.png','tmp_run_overlay.png','tmp_fire_overlay.png','tmp_crouch_stand_weapon1.png','tmp_crouch_overlay.png','tmp_crouch_fire_weapon1.png'
foreach($name in $files){
  $path = Join-Path 'c:\Users\ÄþÓîº¼\IdeaProjects\Game1' $name
  $bmp = New-Object System.Drawing.Bitmap($path)
  $minX = $bmp.Width
  $minY = $bmp.Height
  $maxX = -1
  $maxY = -1
  for($y=0; $y -lt $bmp.Height; $y++){
    for($x=0; $x -lt $bmp.Width; $x++){
      $c = $bmp.GetPixel($x,$y)
      if($c.A -gt 10){
        if($x -lt $minX){$minX=$x}
        if($y -lt $minY){$minY=$y}
        if($x -gt $maxX){$maxX=$x}
        if($y -gt $maxY){$maxY=$y}
      }
    }
  }
  if($maxX -ge 0){
    Write-Output "$name bbox=($minX,$minY)-($maxX,$maxY) size=$($maxX-$minX+1)x$($maxY-$minY+1)"
  } else {
    Write-Output "$name no opaque pixels"
  }
  $bmp.Dispose()
}
