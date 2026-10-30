@echo off
setlocal
set "ROOT=%~dp0.."
set "JAVA=%ROOT%\runtime\java\bin\java.exe"
set "JAR=%ROOT%\worker\runtime-worker-0.1.0-SNAPSHOT.jar"
if not exist "%JAVA%" (
  echo Bundled Java 21 runtime is missing: "%JAVA%" 1>&2
  exit /b 2
)
if not exist "%JAR%" (
  echo Runtime worker is missing: "%JAR%" 1>&2
  exit /b 2
)
"%JAVA%" --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED -jar "%JAR%" %*
exit /b %ERRORLEVEL%
