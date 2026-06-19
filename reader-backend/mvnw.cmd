@REM Maven Wrapper script for Windows
@REM Downloads Maven if not present, then runs it
@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"
set "MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Downloading Maven 3.9.9...
    powershell -Command "Expand-Archive -Path (Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile $env:TEMP\maven.zip; $env:TEMP\maven.zip) -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force"
    if errorlevel 1 (
        echo ERROR: Failed to download Maven. Please install Maven manually.
        exit /b 1
    )
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
