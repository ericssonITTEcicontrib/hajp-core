#!/bin/bash


DATE='date +%H:%M:%S'
scriptDir=`dirname "$0"`

function log {
    echo `$DATE`" $1"
}

args=`getopt l:v: $*`
if test $? != 0
     then
         log 'Usage: -l location of jenkins instance home'
         exit 1
fi
set -- $args
for i
do
  case "$i" in
        -l) shift;
            instance_home="$1"
            shift;;
        -v) shift;
            release_version=$1
            shift;;
  esac
done

if [ -z "$instance_home" ]; then
  log "Error: no instance_home passed via -l argument"
  exit 1
fi
if [ -z "$release_version" ]; then
  log "Error: no release_version passed via -v argument. Use '-v latest' to get latest version."
  exit 1
fi

if [ ! -d "$instance_home" ]; then
  mkdir -p "$instance_home"
else
  log "Error: Directory $instance_home already exists!"
  exit 1
fi


releasePrefix=EIS-Jenkins-Release
path=https://arm.mo.ca.am.ericsson.se/artifactory/proj-jnkserv-release-local/com/ericsson/itte/eis-jenkins/$releasePrefix

if [ "$release_version" == "latest" ]; then
  releaseVersion=`curl -s $path/maven-metadata.xml | grep latest | sed "s/.*<latest>\([^<]*\)<\/latest>.*/\1/"`
else
  releaseVersion=$release_version
fi

releaseFileName=$releasePrefix-$releaseVersion.zip
releaseUrl="$path/$releaseVersion/$releaseFileName"

rm -f "$SCRIPTDIR/*.zip.*"

log ""
log "Deploying new Jenkins instance using EIS Service Release Version: $releaseVersion"
log "To --> $instance_home"
log ""

downloadRequired=1
if [ -f "$releaseFileName" ] ; then
  log "$releaseFileName exists...will verify"
  VERIFY=`unzip -q -t $releaseFileName`
  if [ $? -ne 0 ] ; then
    log "Warning: $releaseFileName is corrupted. Will force download"
    log $VERIFY
  else
    downloadRequired=0
    log "$releaseFileName OK"
  fi
fi

if [ $downloadRequired -eq 1 ]; then
  log ""
  log "Downloading release file from $releaseUrl"
  wget --no-proxy -N -q --no-check-certificate $releaseUrl

  if [ $? -ne 0 ] ; then
   log "Error downloading - Aborting!"
   exit 1
  fi
fi

unzipPath=unzippedRelease
unzip -q -o "$releaseFileName" -d "$unzipPath"

if [ $? -ne 0 ] ; then
 log "Error unzipping - Aborting!"
 exit 2
fi

jenkinsHome="$instance_home/jenkins_home"
jenkinsConf="$instance_home/conf"
log ""
log "Creating instance directories"
log ""
mkdir "$jenkinsHome" "$jenkinsConf"

coreVersion=`grep -A 1 'Jenkins Core Version' "$unzipPath/README.txt" | tail -1 | awk '{print $2}'`
coreFileName=jenkins-$coreVersion-lts.war
coreDestFile="$jenkinsConf/$coreFileName"

log "Deploying release core version $coreVersion to $jenkinsConf/$coreFileName"
mv "$unzipPath/core/jenkins.war" "$coreDestFile"

log "Creating softlink in $jenkinsConf: jenkins.war --> $coreDestFile"
log ""
ln -s "$coreFileName" "$jenkinsConf/jenkins.war"

log "Deploying release plugins to $jenkinsHome/plugins"
mv "$unzipPath/plugins" "$jenkinsHome/plugins"
log ""

##rm -f "$releaseFileName"*
rm -rf "$unzipPath"

log "Done!"
log ""

