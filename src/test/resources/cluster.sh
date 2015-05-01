#!/bin/bash

if [ -z "${JENKINS_HOME}" ] ; then
  # support for using this in a jenkins job
  export JENKINS_HOME1=~/.jenkins1/jenkins_home
  export JENKINS_HOME2=~/.jenkins2/jenkins_home
fi

if [ -z "${JENKINS_PORT}" ] ; then
  export JENKINS_PORT1=8081
  export JENKINS_PORT2=8082
  export AKKA_PORT1=2553
  export AKKA_PORT2=2554
fi

if [ ! -d "${JENKINS_HOME1}" ] ;then
  echo "Error: JENKINS_HOME1 ${JENKINS_HOME1} does not exist!"
  exit 1
fi

if [ ! -d "${JENKINS_HOME2}" ] ;then
  echo "Error: JENKINS_HOME2 ${JENKINS_HOME2} does not exist!"
  exit 1
fi

SELF_PATH=$(cd -P -- "$(dirname -- "$0")" && pwd -P) && SELF_PATH=$SELF_PATH/$(basename -- "$0")
SCRIPTDIR=`dirname "${SELF_PATH}"`
SRCDIR=$(cd -P -- "$(dirname -- "${SCRIPTDIR}/../../../..")" && pwd -P)

usage() {
   echo "`basename $0` [start|restart|startandwait|stop|status]"
   echo start/stop the HA cluster with 2 jenkins instances running in background
   echo pids of jenkins instances are stored in [JENKINS_HOME]/ha-[PORT].pid
   echo for instance:
   echo - Jenkins1: $JENKINS_HOME1 at $JENKINS_PORT1
   echo - Jenkins2: $JENKINS_HOME2 at $JENKINS_PORT2
   echo ""
   echo "You may override defaults for JENKINS_HOME1, JENKINS_HOME2"
   echo "and JENKINS_PORT and JENKINS_PORT2 by setting the environment variables"
   echo ""
}

checkInstance() {
  INSTANCE=$1
  echo "Waiting until $INSTANCE is available..."
  OK=
  for i in `seq 1 30`
  do
    SITESTATUS="`curl --noproxy '*' -S -s --head $INSTANCE | head -1 | tr -d '\n' | tr -d '\r'`"
    if echo $SITESTATUS| grep -q 'HTTP/1.1 200 OK' ; then
      echo "$INSTANCE is up!"
      OK=1
      break
    else
      sleep 10
    fi
  done

  if [ -z "${OK}" ]; then
    exit 1
  fi
}

startInstance() {
   JENKINS_DIR=$1
   JENKINS_WAR=$2
   JENKINS_HTTPPORT=$3
   JENKINS_AKKAPORT=$4
   DEBUG_PORT=$5

   DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=n"
   JACOCO_AGENT="-javaagent:${SRCDIR}/lib/jacocoagent.jar=destfile=${SRCDIR}/target/jacoco-it.exec"
   HTTP_PORT="--httpPort=$JENKINS_HTTPPORT"
   HAJP_AKKA_PORT="-Dhajp.akka.port=$JENKINS_AKKAPORT"

   if [ -f "$SRCDIR/target/jacoco-it.exec" ] ; then
      rm "$SRCDIR/target/jacoco-it.exec"
   fi

   running=$(statusInstance $JENKINS_DIR $JENKINS_HTTPPORT)
   echo starting jenkins $JENKINS_HTTPPORT
   if [ "$running" = "jenkins $JENKINS_HTTPPORT is NOT running" ] ; then
      echo "executing silently: JENKINS_HOME=$JENKINS_DIR java $HAJP_AKKA_PORT $DEBUG $JACOCO_AGENT -jar $JENKINS_WAR $ARGS"
      export JENKINS_HOME=$JENKINS_DIR
      nohup java $HAJP_AKKA_PORT $DEBUG "${JACOCO_AGENT}" -jar $JENKINS_WAR $HTTP_PORT > $JENKINS_DIR/ha-$JENKINS_HTTPPORT.log &
      echo $! > $JENKINS_DIR/ha-$JENKINS_HTTPPORT.pid

      echo jenkins $JENKINS_HTTPPORT is starting...
   else
      echo jenkins $JENKINS_HTTPPORT is already running
   fi

   unset JENKINS_DIR
   unset JENKINS_WAR
   unset JENKINS_HTTPPORT
   unset JENKINS_AKKAPORT
   unset DEBUG_PORT
   unset running
}

stopInstance() {
   JENKINS_DIR=$1
   JENKINS_HTTPPORT=$2
   if [ "$running" != "jenkins $JENKINS_HTTPPORT is NOT running" ] ; then
      echo shutting down jenkins $2 ...
      if [ -f $JENKINS_DIR/ha-$JENKINS_HTTPPORT.pid ] ; then
         kill `cat $JENKINS_DIR/ha-$JENKINS_HTTPPORT.pid`
         rm $JENKINS_DIR/ha-$JENKINS_HTTPPORT.pid
      fi
      echo jenkins $2 is shutdown
   else
      echo jenkins $JENKINS_HTTPPORT is already NOT running
   fi

   unset JENKINS_DIR
   unset JENKINS_HTTPPORT
   unset running
}

start() {
   startInstance $JENKINS_HOME1 $JENKINS_HOME1/../conf/jenkins.war $JENKINS_PORT1 $AKKA_PORT1 8001
   echo "wait 30 seconds for the first instance to get initialized"
   sleep 30
   startInstance $JENKINS_HOME2 $JENKINS_HOME2/../conf/jenkins.war $JENKINS_PORT2 $AKKA_PORT2 8002
}

stop() {
   stopInstance $JENKINS_HOME1 $JENKINS_PORT1
   stopInstance $JENKINS_HOME2 $JENKINS_PORT2
}

pingInstance() {
  URL=$1

  SITESTATUS="`curl --noproxy '*' -S -s --head $URL | head -1 | tr -d '\n' | tr -d '\r'`"
  if echo $SITESTATUS| grep -q 'HTTP/1.1 200 OK' ; then
    echo "$URL is up!"
  else
    echo "$URL is DOWN!"
  fi

}

statusInstance() {
   JENKINS_DIR=$1
   JENKINS_HTTPPORT=$2
   if [ -f $JENKINS_DIR/ha-$JENKINS_HTTPPORT.pid ] ; then
      pid=`cat $JENKINS_DIR/ha-$JENKINS_HTTPPORT.pid`
      running=`ps -ef | awk '{print $2}' | grep $pid`
   fi
   if [ "" != "$running" ] ; then
      echo jenkins $2 is running with pid $running
   else
      echo jenkins $2 is NOT running
   fi

   unset JENKINS_DIR
   unset JENKINS_HTTPPORT
   unset running
}

startandwait() {
   start
   checkInstance http://localhost:$JENKINS_PORT1
   checkInstance http://localhost:$JENKINS_PORT2
}

status() {
   statusInstance $JENKINS_HOME1 $JENKINS_PORT1
   pingInstance http://localhost:$JENKINS_PORT1
   statusInstance $JENKINS_HOME2 $JENKINS_PORT2
   pingInstance http://localhost:$JENKINS_PORT2
}

if [ "start" = "$1" ] ; then
   start;
elif [ "restart" = "$1" ] ; then
   stop;
   start;
elif [ "stop" = "$1" ] ; then
   stop;
elif [ "status" = "$1" ] ; then
   status;
elif [ "startandwait" = "$1" ] ; then
   startandwait;
else
   usage
fi
