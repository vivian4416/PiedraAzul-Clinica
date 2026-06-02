$ErrorActionPreference = "Stop"

$env:SONAR_TOKEN = "sqp_d6f57ab21853419910aab32905e7235dfd290128"
Set-Location "C:\Users\USUARIO\Documents\GitHub\PiedraAzul-Clinica\Back"
sonar-scanner
