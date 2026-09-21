$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path $PSScriptRoot -Parent
$wrapperPath = Join-Path $projectDirectory 'gradle\wrapper\gradle-wrapper.jar'
$expectedHash = '2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046'
if (-not (Test-Path $wrapperPath)) {
    Write-Host 'Downloading the official Gradle wrapper (first run only)...'
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    $temporaryPath = "$wrapperPath.download"
    Invoke-WebRequest -UseBasicParsing -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar' -OutFile $temporaryPath
    if ((Get-FileHash $temporaryPath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expectedHash) {
        Remove-Item $temporaryPath
        throw 'Gradle wrapper checksum mismatch. Download stopped.'
    }
    Move-Item $temporaryPath $wrapperPath
}
if ((Get-FileHash $wrapperPath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expectedHash) {
    throw 'The Gradle wrapper checksum does not match the official Gradle 8.11.1 wrapper.'
}
