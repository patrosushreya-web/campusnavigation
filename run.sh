#!/usr/bin/env bash
echo "Compiling CampusMapApp.java..."
javac CampusMapApp.java
if [ $? -eq 0 ]; then
    echo "Running CampusMapApp..."
    java CampusMapApp
else
    echo "Compilation failed."
fi
