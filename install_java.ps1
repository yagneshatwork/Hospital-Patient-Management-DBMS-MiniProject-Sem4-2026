$url = 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10+7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.10_7.msi'
$out = "$env:TEMP\jdk17.msi"
Write-Host 'Downloading Eclipse Temurin JDK 17 LTS...'
Invoke-WebRequest -Uri $url -OutFile $out -UseBasicParsing
Write-Host "Download complete. Size: $((Get-Item $out).Length) bytes"
Write-Host 'Installing JDK 17 silently (this may take 1-2 minutes)...'
Start-Process msiexec.exe -ArgumentList "/i `"$out`" /qn ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome" -Wait -Verb RunAs
Write-Host 'Installation complete!'
Write-Host 'Refreshing environment variables...'
$env:Path = [System.Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [System.Environment]::GetEnvironmentVariable('Path','User')
javac -version
Write-Host 'JDK 17 is ready!'
