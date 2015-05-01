#!/bin/bash

which xmlstarlet > /dev/null 2> /dev/null
if [ $? -ne 0 ] ; then
  echo "Error: xmlstarlet tool not found in path. Please install."
  exit 1
fi

set -e

USERNAME=artread
PASSWD="\{DESede\}YNtyA/TMlbuQjz/BlYj9Pw=="
VERSION=`xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" -t -m "/x:project/x:version/text()"  -c . -n ../pom.xml  | head -1`
if [ -z `echo $VERSION | grep SNAPSHOT` ] ; then
  echo "$VERSION is a release version"
  HPIURL=https://arm.mo.ca.am.ericsson.se/artifactory/simple/proj-jnkserv-staging-local/com/ericsson/jenkinsci/hajp/hajp-core/$VERSION/hajp-core-$VERSION.hpi
else
  echo "$VERSION is a snapshot version"
  VERSION=`echo $VERSION | cut -f1 -d-`
  HPIURL=https://arm.mo.ca.am.ericsson.se/artifactory/simple/proj-jnkserv-dev-local/com/ericsson/jenkinsci/hajp/hajp-core/$VERSION-SNAPSHOT/hajp-core-$VERSION-SNAPSHOT.hpi
fi
JENKINSVERSION=1.580.2

rm -rf WEB-INF
mkdir -p WEB-INF/plugins
mkdir -p WEB-INF/lib

curl -f -L http://mirrors.jenkins-ci.org/war-stable/$JENKINSVERSION/jenkins.war -o jenkins.war
curl -f -L https://updates.jenkins-ci.org/latest/jquery.hpi -o WEB-INF/plugins/jquery.hpi

wget --no-proxy -O hajp-core.hpi --user=$USERNAME --password=$PASSWD $HPIURL

cp hajp*.hpi WEB-INF/plugins/hajp.hpi
cp jars/* WEB-INF/lib/

zip --grow jenkins.war WEB-INF/plugins/*
zip --grow jenkins.war WEB-INF/lib/*

rm -rf WEB-INF

