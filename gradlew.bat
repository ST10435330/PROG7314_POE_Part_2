@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\bootstrap-gradle.ps1"
if errorlevel 1 exit /b 1
set "STUDYSYNC_JAVA=java.exe"
if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" set "STUDYSYNC_JAVA=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
if defined JAVA_HOME set "STUDYSYNC_JAVA=%JAVA_HOME%\bin\java.exe"
"%STUDYSYNC_JAVA%" -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
