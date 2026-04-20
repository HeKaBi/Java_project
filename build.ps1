$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

New-Item -ItemType Directory -Force -Path ".\bin" | Out-Null
$sources = Get-ChildItem ".\src" -Recurse -Filter *.java | ForEach-Object { $_.FullName }

if (-not $sources) {
    throw "No Java source files found under .\src"
}

javac -encoding UTF-8 -d ".\bin" $sources
Write-Host "Build completed: .\bin"
