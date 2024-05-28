#!/bin/bash

#appName
export APP_NAME=${project.artifactId}
export JAVA_HOME
export JAVA="$JAVA_HOME/bin/java"
export APP_BASE_DIR=`cd $(dirname $0)/..; pwd`
export LOG_FOLDER="${APP_BASE_DIR}"/logs
export LOG_FILENAME="${APP_NAME}.out"
export PID_FOLDER="${LOG_FOLDER}"
export CUSTOM_SEARCH_LOCATIONS="file:${APP_BASE_DIR}/config/"
#JVM参数
export JAVA_OPTS="$JAVA_OPTS -Xms256m -Xmx1024m -Xmn256m"
export JAVA_OPTS="$JAVA_OPTS -server -Djava.net.preferIPv4Stack=true -Duser.timezone=Asia/Shanghai -Dclient.encoding.override=UTF-8 -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"
export JAVA_OPTS="$JAVA_OPTS -Djointframe.log.dir=${LOG_FOLDER}"
export JAVA_OPTS="$JAVA_OPTS -Dserver.max-http-header-size=524288"
export JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError"

JASP_CONFIG_OPTS="--spring.config.additional-location=${CUSTOM_SEARCH_LOCATIONS}"
JASP_LOG4J_OPTS="--logging.config=${APP_BASE_DIR}/config/logback.xml"

BOOT_JAR=`echo "${APP_BASE_DIR}"/${APP_NAME}*.jar`


if [ "$1" = "" ];
then
    echo -e "\033[0;31m 未输入操作名 \033[0m  \033[0;34m {start|stop|restart|status} \033[0m"
    exit 1
fi

if [ "$APP_NAME" = "" ];
then
    echo -e "\033[0;31m 未输入应用名 \033[0m"
    exit 1
fi

function start()
{
    PID=`ps -ef |grep java|grep $APP_NAME|grep -v grep|awk '{print $2}'`

	if [ x"$PID" != x"" ]; then
	    echo "$APP_NAME is running..."
	else
        if [ ! -d "${LOG_FOLDER}" ]; then
            mkdir ${LOG_FOLDER}
        fi
        rm -fr $LOG_FOLDER/$LOG_FILENAME
        nohup java -Dfile.encoding=utf-8 -jar  $JAVA_OPTS  $APP_BASE_DIR/$APP_NAME.jar $JASP_CONFIG_OPTS $JASP_LOG4J_OPTS > $LOG_FOLDER/$LOG_FILENAME 2>&1 &
        echo "Start $APP_NAME success..."
	fi
}

function stop()
{
    echo "Stop $APP_NAME"

	PID=""
	query(){
		PID=`ps -ef |grep java|grep $APP_NAME|grep -v grep|awk '{print $2}'`
	}

	query
	if [ x"$PID" != x"" ]; then
		kill -TERM $PID
		echo "$APP_NAME (pid:$PID) exiting..."
		while [ x"$PID" != x"" ]
		do
			sleep 1
			query
		done
		echo "$APP_NAME exited."
	else
		echo "$APP_NAME already stopped."
	fi
}

function restart()
{
    stop
    sleep 2
    start
}

function status()
{
    PID=`ps -ef |grep java|grep $APP_NAME|grep -v grep|wc -l`
    if [ $PID != 0 ];then
        echo "$APP_NAME is running..."
    else
        echo "$APP_NAME is not running..."
    fi
}

case $1 in
    start)
    start;;
    stop)
    stop;;
    restart)
    restart;;
    status)
    status;;
    *)

esac
