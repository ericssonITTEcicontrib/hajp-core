package com.ericsson.jenkinsci.hajp.cluster;

import lombok.Getter;

/**
 * This class is responsible for providing information regarding
 * the status of the membership in the cluster.
 *
 */
public class HajpClusterMembershipStatusProvider {

    private static HajpClusterMembershipStatusProvider instance = null;
    @Getter private boolean iAmActiveMaster = false;
    @Getter private boolean iAmInCluster = false;

    public synchronized void setiAmActiveMaster(boolean iAmActiveMaster) {
        this.iAmActiveMaster = iAmActiveMaster;
    }

    public synchronized void setiAmInCluster(boolean iAmInCluster) {
        this.iAmInCluster = iAmInCluster;
    }

    protected HajpClusterMembershipStatusProvider() {
    }

    /**
     * Provide the instance of the HajpClusterMembershipStatusProvider.
     * @return instance of HajpClusterMembershipStatusProvider.
     */
    public static HajpClusterMembershipStatusProvider getInstance() {
       if(instance == null) {
          instance = new HajpClusterMembershipStatusProvider();
       }
       return instance;
    }
}
