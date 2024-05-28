@echo off
rem Copyright 2011-2019 Xian Jointsky Software Holding CO., LTD

title ${project.artifactId}(${project.version})

if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & EXIT /B 1
set "JAVA=%JAVA_HOME%\bin\java.exe"

setlocal enabledelayedexpansion

set BASE_DIR=%~dp0
rem added double quotation marks to avoid the issue caused by the folder names containing spaces.
rem removed the last 5 chars(which means \bin\) to get the base DIR.
set BASE_DIR="%BASE_DIR:~0,-5%"
set CUSTOM_SEARCH_LOCATIONS=file:%BASE_DIR%/config/
set APP_NAME=${project.artifactId}
set DEBUG=n
set SWAGENT=n

set i=0
for %%a in (%*) do (
    if "%%a" == "-a" ( set SWAGENT=y )
    if "%%a" == "-d" ( set DEBUG=y )
    set /a i+=1
)

set "JAVA_OPT=%JAVA_OPT% -Xms256m -Xmx1024m -Xmn256m"

if %SWAGENT% == y (
    set "JAVA_OPT=%JAVA_OPT% -javaagent:D:\jointsky\swagent\skywalking-agent.jar=agent.service_name=%APP_NAME%"
)

if %DEBUG% == y (
  set "JAVA_OPT=%JAVA_OPT% -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%BASE_DIR%\logs\java_heapdump.hprof -XX:-UseLargePages"  
  set "JAVA_OPT=%JAVA_OPT% -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=10001"  
)

set "JAVA_OPT=%JAVA_OPT% -jar %BASE_DIR%\%APP_NAME%.jar"
set "JAVA_OPT=%JAVA_OPT% -Djointframe.log.dir=%BASE_DIR%/logs"

rem set spring config location
set "JASP_CONFIG_OPTS=--spring.config.additional-location=%CUSTOM_SEARCH_LOCATIONS%"

rem set log4j file location
set "JASP_LOG4J_OPTS=--logging.config=%BASE_DIR%/config/logback.xml"

call "%JAVA%" %JAVA_OPT% %JASP_CONFIG_OPTS% %JASP_LOG4J_OPTS% %APP_NAME% %*
