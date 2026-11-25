@echo off
echo ==================================================
echo       Compiling CampusMapApp.java...
echo ==================================================
javac CampusMapApp.java
if %ERRORLEVEL% equ 0 (
    echo Compilation successful! Launching console app...
    echo.
    java CampusMapApp
) else (
    echo Compilation failed. Please ensure JDK is installed and configured in PATH.
)
pause
