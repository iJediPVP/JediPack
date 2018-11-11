param([String]$NewJar)

$NewJar = "C:\Projects\JediPack\target\JediPack-1.0-SNAPSHOT.jar"

$DestDir = "C:\Servers\Spigot 1.13.2\plugins"
$ExistingFiles = Get-ChildItem -Path $DestDir -File  | ? { $_.Name.ToUpper().StartsWith("JEDIPACK") -and $_.Extension -eq ".jar" }
$ExistingFiles | ForEach-Object { Remove-Item $_.FullName -Force -ErrorAction Stop }

Copy-Item $NewJar $DestDir -Force -ErrorAction Stop