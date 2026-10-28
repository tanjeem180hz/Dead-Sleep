@echo off
title Stop Auto Git Push
echo Stopping any running Auto-Push processes...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like '*auto-push.ps1*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force; Write-Host 'Stopped auto-push process ID:' $_.ProcessId }"
taskkill /FI "WINDOWTITLE eq Dead Sleep - Auto Git Push*" /F >nul 2>&1
echo Done.
timeout /t 3 >nul
