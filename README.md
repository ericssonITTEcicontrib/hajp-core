Table of Contents

* [Installation Instructions](#InstallationInstructions)

* [User Guide](#UserGuide)
    * [Configuration](#Configuration)
    

# Installation Instructions <a name="InstallationInstructions"></a>

Assuming a TOMCAT deployment, these jar files must be placed under TOMCAT/lib as managed dependencies for hpi to work correctly when built from a cloned repo instance.
Otherwise these jars must be moved pre-packaging to hpi within TARGET folder.

akka-actor_2.11-2.3.9.jar
akka-cluster_2.11-2.3.9.jar
akka-remote_2.11-2.3.9.jar
hajp-common-1.0.5.jar
config-1.2.1.jar
netty-3.8.0.Final.jar
protobuf-java-2.5.0.jar
scala-library-2.11.5.jar
slf4j-api-1.7.7.jar


# User Guide <a name="UserGuide"></a>
High Availability Jenkins Project is about creating a fail-over capable cluster of Jenkins instances with selective synchronization
, monitoring and control capabilities.

Configuration is described below with annotated images. First Jenkins instance to join the cluster becomes Active Master
while all others become Hot Standby members. 

When a fail-over occurs, a new Active Master is elected from remaining Hot Standby members.

a Vagrant file is provided for testing purposes... it needs Vagrant version > 1.7.0 and VirtualBox of version 4.3 and above.
[VagrantFile](https://raw.githubusercontent.com/danielyinanc/hajp-test/develop/functional/deploy/vagrant/Vagrantfile)

##Configuration <a name="Configuration"></a>
After launching the Jenkins instance, please click manage jenkins on left corner of screen.

<img src="http://i62.tinypic.com/25flv1l.jpg">

HAJP-Settings button is among other buttons on the right side of the screen. 

<img src="http://i61.tinypic.com/8wzc3s.jpg">

This setup information is taken to match Vagrant file provided for testing purposes [VagrantFile](https://raw.githubusercontent.com/danielyinanc/hajp-test/develop/functional/deploy/vagrant/Vagrantfile). 
Other than IP ranges, same configuration steps are applicable for any HAJP installation.

### Empty settings page <a name="EmptySettings"></a>
<img src="http://i59.tinypic.com/x393jq.jpg">

### ipFinder output
Use the following settings

* 172.17.0.1 as Orchestrator IP
* 172.17.0.2 as Core1 (port 9080)
* 172.17.0.3 as Core2 (port 9081)
* For convenience please use port 2551 for all components.

<img src="http://i62.tinypic.com/2rnze4h.jpg">

### Jenkins Instance 1 (port 9080) Settings Filled
<img src="http://i57.tinypic.com/s6p2xv.jpg">

### Jenkins Instance 2 (port 9081) Settings Filled
<img src="http://i58.tinypic.com/2u4src1.jpg">

### Jenkins Instance 1 becomes Active Master
After the settings are entered, wait for about 10 seconds and reload 
settings page to see Jenkins Instance 1 becoming Active Master.

<img src="http://i57.tinypic.com/20gla4o.jpg">

### Jenkins Instance 2 becomes Hot Standby
After the settings are entered, wait for about 10 seconds and reload 
settings page to see Jenkins Instance 1 becoming Active Master.

<img src="http://i57.tinypic.com/2vhyarr.jpg">


