@echo off
echo Running PowerShell script...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0generate-mkdocs.ps1"
pause