@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0backend"
echo ========================================================
echo Starting Digital Library System on JDK 17 (Port 8080)
echo ========================================================
start http://localhost:8080
"%JAVA_HOME%\bin\java.exe" -jar "target\digital-library-backend-1.0.0.jar"
pause
