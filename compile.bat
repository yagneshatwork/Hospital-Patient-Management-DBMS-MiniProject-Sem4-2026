@echo off
title Hospital Patient Management System - Setup & Compile
color 0B
setlocal

echo.
echo  ====================================================
echo   Hospital Patient Management System
echo   Setup ^& Compile Script
echo  ====================================================
echo.

:: Locate javac
set JAVAC=javac

where javac >nul 2>&1
if %ERRORLEVEL% == 0 goto :compile

if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.10.7-hotspot\bin\javac.exe" (
    set "JAVAC=C:\Program Files\Eclipse Adoptium\jdk-17.0.10.7-hotspot\bin\javac.exe"
    goto :compile
)

echo  [ERROR] Java JDK not found!
pause
exit /b 1

:compile
if not exist bin mkdir bin

echo  Compiling Java source files...
echo.

"%JAVAC%" -encoding UTF-8 -cp "lib\*" -d bin src\*.java

if %ERRORLEVEL% == 0 (
    echo  [SUCCESS] Compilation complete!
    echo  Run 'run.bat' to launch the application.
    echo.
) else (
    echo  [ERROR] Compilation failed. Review errors above.
    echo.
)

pause
endlocal
