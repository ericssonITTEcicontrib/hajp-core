package com.ericsson.jenkinsci.hajp.extensions;

import lombok.extern.log4j.Log4j2;

import com.ericsson.jenkinsci.hajp.cluster.HajpClusterMembershipStatusProvider;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.model.queue.QueueTaskDispatcher;
import hudson.model.queue.CauseOfBlockage;

/**
 * When an instance has the HotStandby role, builds cannot be executed.
 * This extension flushes the queue.
 */
@Extension
@Log4j2 public class HajpHotStandbyQueueTaskDispatcher extends QueueTaskDispatcher {

    /**
     * Remove the item from queue if we are not an active master.
     */
    @Override
    public CauseOfBlockage canRun(Item item) {
        if (!HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster() || 
            HajpClusterMembershipStatusProvider.getInstance().isIAmActiveMaster()) {
          // not in cluster or active master...do not block
          return null;
        }

        log.debug("Item being removed from queue since we have hot-standby HAJP role!");
        //Cancel the item in queue
        Queue.getInstance().cancel(item);
        //We still must return CauseOfBlockage
        return new HajpHotStandbyCauseOfBlockage();
    }
}
