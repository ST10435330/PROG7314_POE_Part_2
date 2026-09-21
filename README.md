# StudySync

A native Kotlin Android student task planner for PROG7314.


## Run

1. Extract the project and open PowerShell in the folder containing `settings.gradle.kts`.
2. Run the command below once to download the official Gradle wrapper and check its published checksum.
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\bootstrap-gradle.ps1
3. Open this folder in Android Studio, allow Gradle to sync, and install Android SDK 35 if prompted.
4. Use JDK 17 or 21 with a compatible Android Studio version. Select an Android device and press Run.
5. In Android Studio's terminal, run:

```powershell
.\gradlew.bat signingReport
```

Google sign-in uses Firebase Authentication and Android Credential Manager.
Register `com.studysync.app` in the shared Firebase project, add the local
debug signing fingerprints, enable Google sign-in, and place the downloaded
configuration at `app/google-services.json`.

GitHub Actions restores this file from the `GOOGLE_SERVICES_JSON`
repository secret. Subjects and tasks currently use temporary local
storage separated by Firebase user ID; hosted API integration follows.




