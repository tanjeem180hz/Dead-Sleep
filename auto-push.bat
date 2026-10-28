@echo off
title Dead Sleep - Auto Git Push
cd /d "%~dp0"
echo Starting Git Auto-Push Monitor for Dead Sleep...
echo Any modified, added, or deleted files will be pushed to GitHub automatically.
echo Keep this window open in the background, or press Ctrl+C to stop.
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\auto-push.ps1"
pause
