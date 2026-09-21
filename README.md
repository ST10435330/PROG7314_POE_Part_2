# StudySync

A native Kotlin Android student task planner for PROG7314.

## Current implementation

The starter opens a simple Tasks screen. Task management, Google sign-in, saved settings, the custom REST API and automated tests will be added incrementally. This is the initial project, not a completed Part 2 submission.

## Run

1. Extract the project and open PowerShell in the folder containing `settings.gradle.kts`.
2. Run the command below once to download the official Gradle wrapper and check its published checksum.
3. Open this folder in Android Studio, allow Gradle to sync, and install Android SDK 35 if prompted.
4. Use JDK 17 or 21 with a compatible Android Studio version. Select an Android device and press Run.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\bootstrap-gradle.ps1
```

Requires Android 8.0 or later. The source/configuration has been reviewed, but the Android build has not been executed in the preparation environment.

## AI assistance

ChatGPT/Codex helped prepare this starter code. Further assistance and actual testing will be recorded as development progresses.
