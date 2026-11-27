# Builds Flow Connect (the Scrcpy Flow phone app) without Android Studio.
# Downloads a private copy of Java 17, the Android SDK and Gradle into .buildtools
# (about 1.5 GB the first time), builds the app, and copies it next to Scrcpy Flow
# as <folder name>.apk (FlowConnect.apk). Run it again any time to rebuild - downloads are reused.

$ErrorActionPreference = 'Stop'
trap {
    Write-Host ''
    Write-Host "BUILD FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Read-Host 'Press Enter to close this window'
    exit 1
}
$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$Root = $PSScriptRoot
$Tools = Join-Path $Root '.buildtools'
$Sdk = Join-Path $Tools 'android-sdk'
$GradleVer = '8.9'
$JdkUrl = 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk'
$CltUrls = @(
    'https://dl.google.com/android/repository/commandlinetools-win-15859902_latest.zip',
    'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip'
)
$GradleUrl = "https://services.gradle.org/distributions/gradle-$GradleVer-bin.zip"

function Step([string]$t) { Write-Host ''; Write-Host "==> $t" -ForegroundColor Cyan }

function Get-Zip([string[]]$Urls, [string]$Dest) {
    $tmp = Join-Path $env:TEMP ('studioconnect_' + [guid]::NewGuid().ToString() + '.zip')
    $ok = $false
    foreach ($u in $Urls) {
        try {
            Write-Host "    downloading $u"
            Invoke-WebRequest -Uri $u -OutFile $tmp -UseBasicParsing
            $ok = $true
            break
        } catch {
            Write-Host "    failed: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
    if (-not $ok) { throw "Download failed. Check your internet connection." }
    Write-Host '    unpacking...'
    Expand-Archive -LiteralPath $tmp -DestinationPath $Dest -Force
    Remove-Item -LiteralPath $tmp -Force
}

New-Item -ItemType Directory -Force -Path $Tools | Out-Null

# 1. Java 17
Step 'Java 17'
$javaExe = Get-ChildItem -Path (Join-Path $Tools 'jdk') -Filter java.exe -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $javaExe) {
    Get-Zip @($JdkUrl) (Join-Path $Tools 'jdk')
    $javaExe = Get-ChildItem -Path (Join-Path $Tools 'jdk') -Filter java.exe -Recurse | Select-Object -First 1
} else { Write-Host '    already downloaded' }
$env:JAVA_HOME = Split-Path (Split-Path $javaExe.FullName -Parent) -Parent
$env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path

# 2. Android SDK command-line tools
Step 'Android SDK tools'
$sdkManager = Join-Path $Sdk 'cmdline-tools\latest\bin\sdkmanager.bat'
if (-not (Test-Path $sdkManager)) {
    $unpack = Join-Path $Tools 'clt-unpack'
    if (Test-Path $unpack) { Remove-Item -Recurse -Force $unpack }
    Get-Zip $CltUrls $unpack
    New-Item -ItemType Directory -Force -Path (Join-Path $Sdk 'cmdline-tools') | Out-Null
    $latest = Join-Path $Sdk 'cmdline-tools\latest'
    if (Test-Path $latest) { Remove-Item -Recurse -Force $latest }
    Move-Item -LiteralPath (Join-Path $unpack 'cmdline-tools') -Destination $latest
    Remove-Item -Recurse -Force $unpack
} else { Write-Host '    already downloaded' }
$env:ANDROID_HOME = $Sdk
$env:ANDROID_SDK_ROOT = $Sdk

Step 'Accepting Android SDK licenses'
$yes = ("y`n" * 40)
$yes | & $sdkManager "--sdk_root=$Sdk" --licenses | Out-Null

Step 'Android 15 SDK platform and build tools'
$yes | & $sdkManager "--sdk_root=$Sdk" 'platforms;android-35' 'build-tools;35.0.0' | Out-Host

# 3. Gradle
Step "Gradle $GradleVer"
$gradleBat = Join-Path $Tools "gradle-$GradleVer\bin\gradle.bat"
if (-not (Test-Path $gradleBat)) { Get-Zip @($GradleUrl) $Tools } else { Write-Host '    already downloaded' }

# 4. Build
Step 'Building Flow Connect'
$sdkProp = ($Sdk -replace '\\', '\\') -replace ':', '\:'
Set-Content -Path (Join-Path $Root 'local.properties') -Value "sdk.dir=$sdkProp" -Encoding ASCII
& $gradleBat -p $Root assembleDebug --no-daemon
if ($LASTEXITCODE -ne 0) { throw "The build failed (see the messages above)." }

$apk = Join-Path $Root 'app\build\outputs\apk\debug\app-debug.apk'
if (-not (Test-Path $apk)) { throw "Build finished but the APK was not found." }
$target = Join-Path (Split-Path $Root -Parent) ((Split-Path $Root -Leaf) + '.apk')
Copy-Item -LiteralPath $apk -Destination $target -Force

Write-Host ''
Write-Host "Done! The phone app is ready: $target" -ForegroundColor Green
Write-Host 'Now in Scrcpy Flow > Devices, click the download button on your phone.' -ForegroundColor Green
Start-Sleep -Seconds 3
exit 0
