@echo off
REM Windows launcher: forwards to resume-cli.ps1.
REM
REM Usage:
REM   .\resume-cli.cmd parse samples\resume.pdf
REM
REM This wrapper invokes the PowerShell launcher, which does JDK
REM auto-discovery (JAVA_HOME / common install dirs / PATH). It tries
REM several well-known PowerShell locations so it works even if the
REM user's PATH is minimal.

setlocal
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

set "PS="
for %%P in (
    "%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
    "%SystemRoot%\System32\PowerShell\v1.0\powershell.exe"
    "powershell.exe"
) do (
    if not defined PS if exist %%P set "PS=%%P"
)
if not defined PS if exist "%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" set "PS=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"

if not defined PS (
  for /f "delims=" %%P in ('where powershell.exe 2^>nul') do (
    if not defined PS set "PS=%%P"
  )
)

if not defined PS (
  echo [resume-cli] Cannot locate powershell.exe. Please run from PowerShell directly:
  echo     powershell -ExecutionPolicy Bypass -File "%SCRIPT_DIR%\resume-cli.ps1" %* 1>&2
  exit /b 1
)

"%PS%" -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%\resume-cli.ps1" %*
exit /b %ERRORLEVEL%
