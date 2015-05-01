package com.ericsson.jenkinsci.hajp.extensions;

import com.ericsson.jenkinsci.hajp.cluster.HajpClusterMembershipStatusProvider;

import hudson.ExtensionList;
import jenkins.model.Jenkins;
import lombok.extern.log4j.Log4j2;

/**
 * This class provides a method to fire events for the cluster extension point.
 */
@Log4j2 public class HajpClusterExtensionProvider {

    private Jenkins jenkins;


    /**
     * Event type for firing the cluster events
     */
    public enum EVENT {
        JOINED,
        DISCONNECTED,
        ACTIVE_MASTER_SELECTED,
        ACTIVE_MASTER_UNSELECTED
    }

    /**
     * Default constructor
     *
     * @param jenkins the jenkins instance
     */
    public HajpClusterExtensionProvider(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    private ExtensionList<HajpClusterExtension> getHajpClusterExtensions() {
        return jenkins.getExtensionList(HajpClusterExtension.class);
    }

    /**
     * Update MembershipProvider statuses.
     * Fire an event so that the extension point implementation will act on it.
     *
     * @param event the event type
     */
    public void fireClusterEvent(EVENT event) {

        switch (event) {
          case JOINED:
              HajpClusterMembershipStatusProvider.getInstance().setiAmInCluster(true);
              break;
          case DISCONNECTED:
              HajpClusterMembershipStatusProvider.getInstance().setiAmInCluster(false);
              HajpClusterMembershipStatusProvider.getInstance().setiAmActiveMaster(false);
              break;
          case ACTIVE_MASTER_SELECTED:
              HajpClusterMembershipStatusProvider.getInstance().setiAmActiveMaster(true);
              break;
          case ACTIVE_MASTER_UNSELECTED:
              HajpClusterMembershipStatusProvider.getInstance().setiAmActiveMaster(false);
              break;
          default:
              break;
        }

        for (HajpClusterExtension listener : getHajpClusterExtensions()) {
            try {
                switch (event) {
                    case JOINED:
                        listener.notifyOnClusterJoined();
                        break;
                    case DISCONNECTED:
                        listener.notifyOnClusterDisconnected();
                        break;
                    case ACTIVE_MASTER_SELECTED:
                        listener.notifyOnActiveMasterSelection(true);
                        break;
                    case ACTIVE_MASTER_UNSELECTED:
                        listener.notifyOnActiveMasterSelection(false);
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                log.warn(ex.getMessage());
            }
        }
    }
}
