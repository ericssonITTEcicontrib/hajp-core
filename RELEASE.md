Version 2.0.2

***

HAJP-451 Message Size Problem

HAJP core maximum message size for receive and send set to 100 MB.

For further reference: [HAJP-451](http://esekilx353.rnd.ki.sw.ericsson.se:8080/browse/HAJP-451)

***

***

HAJP-328 HotStandby now discards queued items

When the instance is acting as a hot standby it is
not permitted to build.

This change adds a QueueTaskDispatcher that forbids
builds to start and keeps the queue empty.

***

HAJP-142 Credentials, Master Key and Secrets Sync

HAJP Core is capable of dealing with credentials, secrets and keys packing, unpacking and
comparison synchronization.

For further reference: [HAJP-142](http://esekilx353.rnd.ki.sw.ericsson.se:8080/browse/HAJP-142)

***


Version 2.0.1

***

HAJP-90 HAJP-113 Status on HAJP Settings page

This change re-introduces the MembershipStatusProvider
and uses it to display the cluster and master status
on the Setting Page.

A bug was also fixed which involved an incorrect comparion
which result in events not being fired.

A Status feature will be introduced to hajp-test for this.

For further reference: [HAJP-90](http://esekilx353.rnd.ki.sw.ericsson.se:8080/browse/HAJP-90)
[HAJP-113](http://esekilx353.rnd.ki.sw.ericsson.se:8080/browse/HAJP-113)

***

HAJP-266 Akka Cluster Serialization

HAJP core is now capable of sending and receiving Java Serialized messages.

For further reference: [HAJP-266](http://esekilx353.rnd.ki.sw.ericsson.se:8080/browse/HAJP-266)

***
