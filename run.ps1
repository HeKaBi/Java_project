$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

& ".\build.ps1"
java -cp ".\bin;.\src" com.tedu.game.GameStart
