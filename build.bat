@echo off
setlocal
set "ROOT=%~dp0"

if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

where javac >nul 2>nul
if errorlevel 1 (
    echo [ERROR] javac was not found. Install a JDK and set JAVA_HOME.
    exit /b 1
)

if not exist "%ROOT%out" mkdir "%ROOT%out"
dir /s /b "%ROOT%src\*.java" > "%ROOT%out\sources.txt"

javac -encoding UTF-8 -cp "%ROOT%lib\mysql-connector-j-26.10.0.jar" -d "%ROOT%out" @"%ROOT%out\sources.txt"
if errorlevel 1 (
    echo [ERROR] Compilation failed.
    exit /b 1
)

echo Build complete. Classes are in out\
