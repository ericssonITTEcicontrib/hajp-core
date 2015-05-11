#!/bin/bash

which xmlstarlet > /dev/null 2> /dev/null
if [ $? -ne 0 ] ; then
  echo "Error: xmlstarlet tool not found in path. Please install."
  exit 1
fi

set -e

PROJECT=hajp-core
REGISTRY=docker.io/ericssonitte
VERSIONSTR=`xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" -t -m "/x:project/x:version/text()"  -c . -n ../pom.xml  | head -1`

echo "$VERSION is a release version"

SNAPSHOTBEGIN=`echo $VERSIONSTR | grep -b -o '-' | awk 'BEGIN {FS=":"}{print $1}' | bc`
STRSIZE=${#VERSIONSTR}

FINALCHARPOS=$((STRSIZE))

if [[ (( "$SNAPSHOTBEGIN" -gt 0 )) ]]; then
  ## If version has -SNAPSHOT in it, then the
  ## release version should have its micro 1 less
  ## than the snapshot
  ## Example: version in version.sbt = 1.0.2-SNAPSHOT
  ##          release version is 1.0.1
  VERSION=${VERSIONSTR:0:$SNAPSHOTBEGIN}
  major=$(echo $VERSION | cut -d. -f1)
  minor=$(echo $VERSION | cut -d. -f2)
  micro=$(echo $VERSION | cut -d. -f3)
  releasemicro=$(echo "$micro - 1" | bc)
  RELEASEVERSION="$major.$minor.$releasemicro"
  SNAPSHOTVERSION="$major.$minor.$micro"
else
  ## If not -SNAPSHOT in version
  ## Then the release version is the version.
  VERSION=${VERSIONSTR:0:$FINALCHARPOS}
  RELEASEVERSION=$VERSION
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
    docker build --no-cache=true -t $REGISTRY/$PROJECT:$VERSION .
    rm -f *.hpi
    rm -f *.war
fi

if [ $1 == "runRelease" ]
  then
   	docker run -p 8080:8080 $REGISTRY/$PROJECT:$VERSION
fi

if [ $1 == "pushRelease" ]
  then
    docker tag -f $REGISTRY/$PROJECT:$VERSION $REGISTRY/$PROJECT:latest
    docker push $REGISTRY/$PROJECT:$VERSION
    docker push $REGISTRY/$PROJECT:latest
fi

