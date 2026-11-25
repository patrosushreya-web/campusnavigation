@echo off
echo ==================================================
echo       Compiling and Launching Campus Map GUI...
echo ==================================================
javac CampusMapApp.java CampusMapGUI.java
if %ERRORLEVEL% equ 0 (
    echo Launching Graphical User Interface...
    start javaw CampusMapGUI
) else (
    echo Compilation failed. Ensure JDK is installed and configured.
    pause
)
