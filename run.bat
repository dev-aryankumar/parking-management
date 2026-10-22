@echo off
setlocal
set "ROOT=%~dp0"

if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

if not exist "%ROOT%out\com\parking\Main.class" (
    echo Classes not found. Running build.bat first...
    call "%ROOT%build.bat"
    if errorlevel 1 exit /b 1
)

if "%MYSQL_PASSWORD%"=="" (
    echo [WARNING] MYSQL_PASSWORD is not set. The connection will use an empty password.
    echo           Run:  setx MYSQL_PASSWORD "your-mysql-root-password"
    echo           then open a NEW terminal before launching again.
    echo.
)

java -cp "%ROOT%out;%ROOT%lib\mysql-connector-j-26.10.0.jar" com.parking.Main
