#!/usr/bin/env python3
# Copyright 2024 Electron Android Project
# Build script for Electron Android

import argparse
import os
import subprocess
import sys
from pathlib import Path

# Project root
ROOT = Path(__file__).parent.parent.parent.resolve()


def check_prerequisites():
    """Check if required tools are installed."""
    errors = []
    
    # Check for Java
    try:
        result = subprocess.run(['java', '-version'], capture_output=True, text=True)
        if result.returncode != 0:
            errors.append("Java not found. Please install JDK.")
    except FileNotFoundError:
        errors.append("Java not found. Please install JDK.")
    
    # Check for Android SDK
    if 'ANDROID_SDK_ROOT' not in os.environ and 'ANDROID_HOME' not in os.environ:
        errors.append("Android SDK not found. Set ANDROID_SDK_ROOT or ANDROID_HOME.")
    
    # Check for NDK
    if 'ANDROID_NDK_ROOT' not in os.environ:
        errors.append("Android NDK not found. Set ANDROID_NDK_ROOT.")
    
    # Check for Gradle or Android build tools
    try:
        subprocess.run(['gradle', '--version'], capture_output=True, check=True)
    except (FileNotFoundError, subprocess.CalledProcessError):
        try:
            subprocess.run(['./gradlew', '--version'], capture_output=True, check=True,
                         cwd=ROOT / 'shell' / 'android')
        except (FileNotFoundError, subprocess.CalledProcessError):
            pass  # Not critical if not found
    
    return errors


def generate_build_files():
    """Generate necessary build files."""
    print("Generating build files...")
    
    # Create gradle wrapper if needed
    android_dir = ROOT / 'shell' / 'android'
    gradle_dir = android_dir / 'gradle' / 'wrapper'
    gradle_dir.mkdir(parents=True, exist_ok=True)
    
    # Create settings.gradle
    settings_gradle = android_dir / 'settings.gradle'
    if not settings_gradle.exists():
        with open(settings_gradle, 'w') as f:
            f.write("""pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ElectronAndroid"
include ':app'
""")
    
    # Create build.gradle (project level)
    build_gradle = android_dir / 'build.gradle'
    if not build_gradle.exists():
        with open(build_gradle, 'w') as f:
            f.write("""// Top-level build file
plugins {
    id 'com.android.application' version '8.2.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.20' apply false
}
""")
    
    # Create gradle.properties
    gradle_properties = android_dir / 'gradle.properties'
    if not gradle_properties.exists():
        with open(gradle_properties, 'w') as f:
            f.write("""org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
android.nonTransitiveRClass=true
""")
    
    print("Build files generated successfully.")


def build_apk(target='debug', output_dir=None):
    """Build the Android APK."""
    android_dir = ROOT / 'shell' / 'android'
    
    if output_dir is None:
        output_dir = ROOT / 'out' / 'android' / target
    
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    print(f"Building Electron Android APK ({target})...")
    
    # Build using gradle
    build_type = 'assembleRelease' if target == 'release' else 'assembleDebug'
    
    try:
        # Try using system gradle
        result = subprocess.run(
            ['gradle', build_type, '-p', str(android_dir), 
             f'-PoutputDir={output_dir}'],
            cwd=android_dir,
            capture_output=False
        )
        return result.returncode == 0
    except FileNotFoundError:
        print("Gradle not found. Please install Gradle or use Android Studio.")
        return False


def build_native(target='debug'):
    """Build the native C++ code using GN/Ninja."""
    print(f"Building native code ({target})...")
    
    out_dir = ROOT / 'out' / 'android' / target
    
    # Check if GN is available
    try:
        subprocess.run(['gn', '--version'], capture_output=True, check=True)
    except (FileNotFoundError, subprocess.CalledProcessError):
        print("GN not found. Please install Chromium depot tools.")
        return False
    
    # Generate ninja files
    args_file = ROOT / 'build' / 'args' / 'android.gn'
    if not args_file.exists():
        print(f"Error: {args_file} not found")
        return False
    
    # Create build directory
    out_dir.mkdir(parents=True, exist_ok=True)
    
    # Run gn gen
    result = subprocess.run(
        ['gn', 'gen', str(out_dir), f'--args-file={args_file}'],
        cwd=ROOT,
        capture_output=True
    )
    
    if result.returncode != 0:
        print(f"GN gen failed: {result.stderr.decode()}")
        return False
    
    # Build
    result = subprocess.run(
        ['ninja', '-C', str(out_dir), 'electron'],
        cwd=ROOT,
        capture_output=True
    )
    
    if result.returncode != 0:
        print(f"Build failed: {result.stderr.decode()}")
        return False
    
    print("Native build successful!")
    return True


def clean():
    """Clean build artifacts."""
    print("Cleaning build artifacts...")
    
    # Clean out directory
    out_dir = ROOT / 'out'
    if out_dir.exists():
        import shutil
        shutil.rmtree(out_dir)
    
    print("Clean complete.")


def main():
    parser = argparse.ArgumentParser(description='Build Electron Android')
    parser.add_argument('action', choices=['build', 'build-native', 'build-apk', 'setup', 'clean'],
                       help='Action to perform')
    parser.add_argument('--target', default='debug', choices=['debug', 'release'],
                       help='Build target')
    parser.add_argument('--output-dir', help='Output directory for APK')
    parser.add_argument('--check-prereq', action='store_true',
                       help='Check prerequisites only')
    
    args = parser.parse_args()
    
    # Check prerequisites first
    errors = check_prerequisites()
    if errors:
        print("Prerequisites check failed:")
        for error in errors:
            print(f"  - {error}")
        if args.check_prereq:
            return 1
        print("\nContinuing anyway (some checks may be optional)...\n")
    
    if args.action == 'check-prereq':
        return 0 if not errors else 1
    
    if args.action == 'setup':
        errors = check_prerequisites()
        if errors:
            print("Cannot setup - missing prerequisites:")
            for error in errors:
                print(f"  - {error}")
            return 1
        generate_build_files()
        return 0
    
    if args.action == 'clean':
        clean()
        return 0
    
    if args.action == 'build':
        # Build everything
        if not build_native(args.target):
            return 1
        return 0 if build_apk(args.target, args.output_dir) else 1
    
    if args.action == 'build-native':
        return 0 if build_native(args.target) else 1
    
    if args.action == 'build-apk':
        return 0 if build_apk(args.target, args.output_dir) else 1
    
    return 0


if __name__ == '__main__':
    sys.exit(main())
