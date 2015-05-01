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

\rm -fr "$JENKINS_HOME1"
\rm -fr "$JENKINS_HOME2"

"$SCRIPTDIR/create-new-instance.sh" -l "$JENKINS_HOME1" -v R5A51
if [ $? -ne 0 ] ; then
  echo "Error: create-new-instance.sh failed"
  exit 1
fi
"$SCRIPTDIR/create-new-instance.sh" -l "$JENKINS_HOME2" -v R5A51
if [ $? -ne 0 ] ; then
  echo "Error: create-new-instance.sh failed"
  exit 1
fi

hajp_version="hajp-core"
hpi="$hajp_version".hpi
cp "$ROOTDIR"/target/$hpi "$JENKINS_HOME1/jenkins_home/plugins/"
cp "$ROOTDIR"/target/$hpi "$JENKINS_HOME2/jenkins_home/plugins/"

# lines to be removed => backlogged
# build-pipeline causes JellyTagException related to global views => failing many tests
\rm "$JENKINS_HOME1/jenkins_home/plugins/build-pipeline-plugin.jpi"
\rm "$JENKINS_HOME2/jenkins_home/plugins/build-pipeline-plugin.jpi"

# lines to be removed => backlogged
# envInject causes first build to failed => failing build tests
\rm "$JENKINS_HOME1/jenkins_home/plugins/envinject.jpi"
\rm "$JENKINS_HOME2/jenkins_home/plugins/envinject.jpi"

