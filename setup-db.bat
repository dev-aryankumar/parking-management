@echo off
setlocal
set "ROOT=%~dp0"
set "MYSQL=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

if not exist "%MYSQL%" (
    echo [ERROR] mysql.exe was not found at:
    echo         %MYSQL%
    echo Edit this file and set MYSQL to your installation path.
    exit /b 1
)

echo This will DROP and recreate the smart_parking database, then load seed data.
pause
"%MYSQL%" -u root -p < "%ROOT%schema.sql"
if errorlevel 1 (
    echo [ERROR] Database setup failed.
    exit /b 1
)
echo Database ready.
