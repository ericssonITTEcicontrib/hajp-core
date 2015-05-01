#!/bin/bash

SELF_PATH=$(cd -P -- "$(dirname -- "$0")" && pwd -P) && SELF_PATH=$SELF_PATH/$(basename -- "$0")
SCRIPTDIR=`dirname "$SELF_PATH"`
ROOTDIR=$(cd -P -- "$(dirname -- "$SCRIPTDIR/../../../..")" && pwd -P)

if [ -z "${JENKINS_HOME1}" ] ; then
  # support for using this in a jenkins job
  export JENKINS_HOME1=~/.jenkins1
  export JENKINS_HOME2=~/.jenkins2
fi

# vs. debug:
#export JENKINS_HOME1=~/git/jenkins/war/work
#export JENKINS_HOME2=~/git2/jenkins/war/work

hajp_version="hajp-core"
hpi="$hajp_version".hpi

cp "$ROOTDIR"/target/$hpi "$JENKINS_HOME1/jenkins_home/plugins/"
cp "$ROOTDIR"/target/$hpi "$JENKINS_HOME2/jenkins_home/plugins/"

hajp_lib_dir="$ROOTDIR"/target/"$hajp_version"/WEB-INF/lib
jenkins1_lib_dir="$JENKINS_HOME1"/lib
jenkins2_lib_dir="$JENKINS_HOME2"/lib
jenkins1_war_lib_dir="$JENKINS_HOME1"/jenkins_home/war/WEB-INF/lib
jenkins2_war_lib_dir="$JENKINS_HOME2"/jenkins_home/war/WEB-INF/lib

\mkdir -p "$jenkins1_lib_dir"
\mkdir -p "$jenkins2_lib_dir"

\mkdir -p "$jenkins1_war_lib_dir"
\mkdir -p "$jenkins2_war_lib_dir"

# copy requires akka lib
cp "$hajp_lib_dir"/akka-actor_2.11-2.3.9.jar "$jenkins1_lib_dir"
cp "$hajp_lib_dir"/akka-cluster_2.11-2.3.9.jar "$jenkins1_lib_dir"
cp "$hajp_lib_dir"/akka-remote_2.11-2.3.9.jar "$jenkins1_lib_dir"
#cp "$hajp_lib_dir"/api-1.0.0-SNAPSHOT.jar "$jenkins1_lib_dir"
cp "$hajp_lib_dir"/config-1.2.1.jar "$jenkins1_lib_dir"
#cp "$hajp_lib_dir"/hajp-core.jar "$jenkins1_lib_dir"
#cp "$hajp_lib_dir"/javax.servlet-api-3.1.0.jar "$jenkins1_lib_dir"
#cp "$hajp_lib_dir"/log4j-api-2.1.jar "$jenkins1_lib_dir"
#cp "$hajp_lib_dir"/log4j-core-2.1.jar "$jenkins1_lib_dir"
#cp "$hajp_lib_dir"/log4j-slf4j-impl-2.1.jar "$jenkins1_lib_dir"
cp "$hajp_lib_dir"/netty-3.8.0.Final.jar "$jenkins1_lib_dir"
cp "$hajp_lib_dir"/protobuf-java-2.5.0.jar "$jenkins1_lib_dir"
cp "$hajp_lib_dir"/scala-library-2.11.5.jar "$jenkins1_lib_dir"
#cp "$hajp_lib_dir"/slf4j-api-1.7.7.jar "$jenkins1_lib_dir"
#cp "$hajp_lib_dir"/uncommons-maths-1.2.2a.jar "$jenkins1_lib_dir"

cp "$hajp_lib_dir"/akka-actor_2.11-2.3.9.jar "$jenkins2_lib_dir"
cp "$hajp_lib_dir"/akka-cluster_2.11-2.3.9.jar "$jenkins2_lib_dir"
cp "$hajp_lib_dir"/akka-remote_2.11-2.3.9.jar "$jenkins2_lib_dir"
#cp "$hajp_lib_dir"/api-1.0.0-SNAPSHOT.jar "$jenkins2_lib_dir"
cp "$hajp_lib_dir"/config-1.2.1.jar "$jenkins2_lib_dir"
#cp "$hajp_lib_dir"/hajp-core.jar "$jenkins2_lib_dir"
#cp "$hajp_lib_dir"/javax.servlet-api-3.1.0.jar "$jenkins2_lib_dir"
#cp "$hajp_lib_dir"/log4j-api-2.1.jar "$jenkins2_lib_dir"
#cp "$hajp_lib_dir"/log4j-core-2.1.jar "$jenkins2_lib_dir"
#cp "$hajp_lib_dir"/log4j-slf4j-impl-2.1.jar "$jenkins2_lib_dir"
cp "$hajp_lib_dir"/netty-3.8.0.Final.jar "$jenkins2_lib_dir"
cp "$hajp_lib_dir"/protobuf-java-2.5.0.jar "$jenkins2_lib_dir"
cp "$hajp_lib_dir"/scala-library-2.11.5.jar "$jenkins2_lib_dir"
#cp "$hajp_lib_dir"/slf4j-api-1.7.7.jar "$jenkins2_lib_dir"
#cp "$hajp_lib_dir"/uncommons-maths-1.2.2a.jar "$jenkins2_lib_dir"

# create links to jars
for file in "$jenkins1_lib_dir"/*
do
  if [ -f $file ]; then
     name=`basename "$file"`
     ln -s "$file" "$jenkins1_war_lib_dir"/"$name"
  fi
done

for file in "$jenkins2_lib_dir"/*
do
    if [ -f $file ]; then
     name=`basename "$file"`
     ln -s "$file" "$jenkins2_war_lib_dir"/"$name"
    fi
done
