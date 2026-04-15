@echo off
title Hospital Patient Management System
color 0A
setlocal

:: Search for java.exe
set JAVA=java
where java >nul 2>&1
if %ERRORLEVEL% == 0 goto :run

:: Manually check the installed JDK path
if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.10.7-hotspot\bin\java.exe" (
    set "JAVA=C:\Program Files\Eclipse Adoptium\jdk-17.0.10.7-hotspot\bin\java.exe"
    goto :run
)

echo  [ERROR] Java not found. Run compile.bat first.
pause
exit /b 1

:run
if not exist bin (
    echo  [ERROR] bin\ folder missing. Run compile.bat first!
    pause
    exit /b 1
)

echo.
echo  Starting Hospital Patient Management System...
echo.

"%JAVA%" -cp "bin;lib\*" MainApp

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo  [ERROR] Application exited with an error. Check output above.
    pause
)
endlocal
