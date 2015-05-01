#!/bin/bash

which xmlstarlet > /dev/null 2> /dev/null
if [ $? -ne 0 ] ; then
  echo "Error: xmlstarlet tool not found in path. Please install."
  exit 1
fi

set -e

PROJECT=proj_hajp
REGISTRY=armdocker.rnd.ericsson.se
SUBPROJECT=core
VERSION=`xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" -t -m "/x:project/x:version/text()"  -c . -n ../pom.xml  | head -1`
if [ -z `echo $VERSION | grep SNAPSHOT` ] ; then
  echo "$VERSION is a release version"
else
  echo "$VERSION is a snapshot version"
  VERSION=`echo $VERSION | cut -f1 -d-`
fi

if [ -z "$1" ]
  then
    echo "No arguments supplied"
    exit 1
fi

export http_proxy=
export https_proxy=

set -e

if [ $1 == "buildRelease" ]
  then
    ./packageJenkins.sh
    docker build --no-cache=true -t $REGISTRY/$PROJECT/$SUBPROJECT:$VERSION .
    rm -f *.hpi
    rm -f *.war
fi

if [ $1 == "buildSnapshot" ]
  then
    ./packageJenkins.sh
    docker build --no-cache=true -t $REGISTRY/$PROJECT/$SUBPROJECT:SNAPSHOT .
    rm -f *.hpi
    rm -f *.war
fi

if [ $1 == "runRelease" ]
  then
   	docker run -p 8080:8080 $REGISTRY/$PROJECT/$SUBPROJECT:$VERSION
fi

if [ $1 == "runSnapshot" ]
  then
   	docker run -p 8080:8080 $REGISTRY/$PROJECT/$SUBPROJECT:SNAPSHOT
fi

if [ $1 == "pushRelease" ]
  then
    docker push $REGISTRY/$PROJECT/$SUBPROJECT:$VERSION
fi

if [ $1 == "pushSnapshot" ]
  then
    docker push $REGISTRY/$PROJECT/$SUBPROJECT:SNAPSHOT
fi
