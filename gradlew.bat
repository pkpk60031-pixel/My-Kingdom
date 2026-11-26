@echo off
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle -p "%~dp0" %*
  exit /b %ERRORLEVEL%
)
echo Gradle 8.6 is not installed. Install Gradle 8.6 or run gradlew from Git Bash/WSL.
exit /b 1
