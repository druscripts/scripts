#!/bin/bash

# Build script for OSMB free scripts repository
# Compiles all scripts from src into separate JAR files

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$SCRIPT_DIR/build"
JAVA_DIR="$SCRIPT_DIR/src"
scripts=(dyemaker piemaker)

echo "=== Druscripts Free OSMB Build Script ==="
echo

# Clean build directory
if [ -d "$BUILD_DIR" ]; then
    echo "Cleaning existing build directory..."
    rm -rf "$BUILD_DIR"
fi

# Create build directory
echo "Creating build directory..."
mkdir -p "$BUILD_DIR"
mkdir -p "$BUILD_DIR/temp/classes"

echo

# Check if source directory exists
if [ ! -d "$JAVA_DIR" ]; then
    echo "No source directory found at $JAVA_DIR"
    exit 1
fi

# Find all Java files
java_files=$(find "$JAVA_DIR" -name "*.java")

if [ -z "$java_files" ]; then
    echo "No Java files found"
    exit 1
fi

# Compile all Java files
echo "=== Compiling all sources ==="
CLASSPATH="$SCRIPT_DIR/API.jar"

if javac --release 17 -cp "$CLASSPATH" -d "$BUILD_DIR/temp/classes" $java_files 2>&1 | tee /tmp/javac_error.log; then
    echo "Compilation successful"
else
    echo "Compilation failed!"
    echo "Errors:"
    cat /tmp/javac_error.log
    exit 1
fi

echo

# Create individual JAR files for each script
for script in "${scripts[@]}"; do
    echo "=== Packaging: $script ==="

    script_pkg_dir="$BUILD_DIR/temp/classes/com/druscripts/$script"

    if [ ! -d "$script_pkg_dir" ]; then
        echo "  Package directory not found for $script, skipping..."
        continue
    fi

    # Create temp directory for this JAR
    jar_temp="$BUILD_DIR/temp/jar_$script"
    mkdir -p "$jar_temp/com/druscripts"

    # Copy utils (shared by all scripts)
    if [ -d "$BUILD_DIR/temp/classes/com/druscripts/utils" ]; then
        cp -r "$BUILD_DIR/temp/classes/com/druscripts/utils" "$jar_temp/com/druscripts/"
    fi

    # Copy script-specific classes
    cp -r "$script_pkg_dir" "$jar_temp/com/druscripts/"

    # Create JAR
    jar_name="$script.druscripts.com.jar"
    echo "  Creating JAR: $jar_name"

    cd "$jar_temp"
    jar cf "$BUILD_DIR/$jar_name" * > /dev/null 2>&1
    cd "$SCRIPT_DIR"

    if [ -f "$BUILD_DIR/$jar_name" ]; then
        jar_size=$(du -h "$BUILD_DIR/$jar_name" | cut -f1)
        echo "  Success! ($jar_size)"
    else
        echo "  Failed to create JAR"
    fi

    echo
done

# Clean up temp directory
rm -rf "$BUILD_DIR/temp"

echo "=== Build Complete ==="
echo
echo "Output:"
echo "  Script JARs in: $BUILD_DIR"
ls -lh "$BUILD_DIR"/*.jar 2>/dev/null | awk '{print "    " $9 " (" $5 ")"}'

echo
