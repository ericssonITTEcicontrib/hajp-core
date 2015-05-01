#!/bin/bash

AKKA_PORT=2554
PORT=8082
DEBUG_PORT=8002

JENKINS_HOME=~/.jenkins2/jenkins_home
SELF_PATH=$(cd -P -- "$(dirname -- "$0")" && pwd -P) && SELF_PATH=$SELF_PATH/$(basename -- "$0")
SCRIPTDIR=`dirname "${SELF_PATH}"`
HAPLUGIN_SRC_DIR=$(cd -P -- "$(dirname -- "${SCRIPTDIR}/../../../..")" && pwd -P)

HAJP_AKKA_PORT="-Dhajp.akka.port=$AKKA_PORT"
JAVA_OPTS="-Xms256m -Xmx1024m -XX:PermSize=128m -XX:MaxPermSize=256m"
DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=n"
JACOCO_AGENT="-javaagent:${HAPLUGIN_SRC_DIR}/lib/jacocoagent.jar=destfile=${HAPLUGIN_SRC_DIR}/target/jacoco-it.exec"
HTTP_PORT="--httpPort=$PORT"

if [ -f "$HAPLUGIN_SRC_DIR/target/jacoco-it.exec" ] ; then
   rm "$HAPLUGIN_SRC_DIR/target/jacoco-it.exec"
fi

echo "JENKINS_HOME=$JENKINS_HOME java $HAJP_AKKA_PORT $DEBUG $JACOCO_AGENT -jar $JENKINS_HOME/../conf/jenkins.war $HTTP_PORT"
JENKINS_HOME=$JENKINS_HOME java $JAVA_OPTS $HAJP_AKKA_PORT $DEBUG "${JACOCO_AGENT}" -jar $JENKINS_HOME/../conf/jenkins.war $HTTP_PORT
