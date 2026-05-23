$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17'
$env:PATH = "C:\Program Files\Eclipse Adoptium\jdk-17\bin;$env:PATH"

Write-Host "Java version:"
java -version
Set-Location 'D:\GravityMaze'
Write-Host "Building..."
.\gradlew.bat assembleDebug --no-daemon
