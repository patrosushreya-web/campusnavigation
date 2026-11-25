#!/usr/bin/env bash
echo "Compiling and running automated tests..."
javac CampusMapApp.java
if [ $? -eq 0 ]; then
    java CampusMapApp --test
else
    echo "Compilation failed."
fi
