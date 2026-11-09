@echo off
setlocal
cd /d "%~dp0"
if "%ANDROID_HOME%"=="" if exist "%LOCALAPPDATA%\Android\Sdk" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
if "%ANDROID_SDK_ROOT%"=="" set "ANDROID_SDK_ROOT=%ANDROID_HOME%"
if "%ANDROID_HOME%"=="" (
  echo Android SDK was not found.
  echo Install Android Studio and Android SDK Platform 35 first.
  pause
  exit /b 1
)
set "GRADLE_EXE="
where gradle >nul 2>nul && set "GRADLE_EXE=gradle"
if "%GRADLE_EXE%"=="" (
  set "GDIR=%USERPROFILE%\.gradle\manual\gradle-8.7"
  if not exist "%USERPROFILE%\.gradle\manual\gradle-8.7\bin\gradle.bat" (
    echo Downloading Gradle 8.7. This requires an internet connection...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; New-Item -ItemType Directory -Force -Path '%USERPROFILE%\.gradle\manual' ^| Out-Null; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.7-bin.zip' -OutFile '%TEMP%\gradle-8.7-bin.zip'; Expand-Archive -Force '%TEMP%\gradle-8.7-bin.zip' '%USERPROFILE%\.gradle\manual'"
    if errorlevel 1 (echo Gradle download failed. Install Gradle 8.7 manually and retry.& pause & exit /b 1)
  )
  set "GRADLE_EXE=%USERPROFILE%\.gradle\manual\gradle-8.7\bin\gradle.bat"
)
echo Building debug APK...
call "%GRADLE_EXE%" --no-daemon assembleDebug
if errorlevel 1 (echo Build failed. Check Android Studio SDK installation and Gradle output.& pause & exit /b 1)
echo.
echo APK created at:
copy /Y "%CD%\app\build\outputs\apk\debug\app-debug.apk" "%CD%\rahla90.apk" >nul
echo %CD%\rahla90.apk
echo You can transfer rahla90.apk to your Android phone and install it.
pause
