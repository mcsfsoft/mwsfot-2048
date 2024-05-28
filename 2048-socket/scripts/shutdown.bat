@echo off
rem Copyright 2011-2019 Xian Jointsky Software Holding CO., LTD
if not exist "%JAVA_HOME%\bin\jps.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & EXIT /B 1

setlocal

set "PATH=%JAVA_HOME%\bin;%PATH%"

echo killing ${project.artifactId} server

for /f "tokens=1" %%i in ('jps -m ^| find "${project.artifactId}"') do ( taskkill /F /PID %%i )

echo Shutdown successfully!
