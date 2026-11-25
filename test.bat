@echo off
echo ==================================================
echo       Compiling and Running Test Cases...
echo ==================================================
javac CampusMapApp.java
if %ERRORLEVEL% equ 0 (
    echo Compilation successful! Running automated test suite...
    echo.
    java CampusMapApp --test
) else (
    echo Compilation failed.
)
pause
