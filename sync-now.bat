@echo off
title Dead Sleep - Instant Git Push
cd /d "%~dp0"
echo Syncing all files to GitHub repository immediately...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\auto-push.ps1" -Once
echo.
echo Sync finished.
timeout /t 3 >nul
