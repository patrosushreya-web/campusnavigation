#!/usr/bin/env bash
echo "Compiling CampusMapApp and CampusMapGUI..."
javac CampusMapApp.java CampusMapGUI.java
if [ $? -eq 0 ]; then
    echo "Launching GUI..."
    java CampusMapGUI
else
    echo "Compilation failed."
fi
