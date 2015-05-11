#!/bin/bash

which xmlstarlet > /dev/null 2> /dev/null
if [ $? -ne 0 ] ; then
  echo "Error: xmlstarlet tool not found in path. Please install."
  exit 1
fi

set -e

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



HPIURL=https://oss.sonatype.org/content/repositories/releases/com/ericsson/jenkinsci/hajp/hajp-core/$VERSION/hajp-core-$VERSION.hpi


JENKINSVERSION=1.596.2

rm -rf WEB-INF
mkdir -p WEB-INF/plugins
mkdir -p WEB-INF/lib

curl -f -L http://mirrors.jenkins-ci.org/war-stable/$JENKINSVERSION/jenkins.war -o jenkins.war
curl -f -L https://updates.jenkins-ci.org/latest/jquery.hpi -o WEB-INF/plugins/jquery.hpi

 wget --no-proxy -O hajp-core.hpi $HPIURL

cp hajp*.hpi WEB-INF/plugins/hajp.hpi
cp jars/* WEB-INF/lib/

zip --grow jenkins.war WEB-INF/plugins/*
zip --grow jenkins.war WEB-INF/lib/*

rm -rf WEB-INF

