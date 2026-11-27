@echo off
rem Opens Scrcpy Flow
start "" powershell.exe -NoProfile -STA -ExecutionPolicy Bypass -WindowStyle Hidden -File "%~dp0scrcpy-flow.ps1"
