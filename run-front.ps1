$ErrorActionPreference = "Stop"

$env:SONAR_TOKEN = "sqp_38b65bd177bd5926eb6308f6be77607bcb11a2a6"
Set-Location "C:\Users\USUARIO\Documents\GitHub\PiedraAzul-Clinica\Front"
sonar-scanner
