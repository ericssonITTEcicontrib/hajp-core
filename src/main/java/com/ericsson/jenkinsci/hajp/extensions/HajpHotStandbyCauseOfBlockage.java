package com.ericsson.jenkinsci.hajp.extensions;

import com.ericsson.jenkinsci.hajp.cluster.Messages;

import hudson.model.queue.CauseOfBlockage;

/**
 * Simple Cause of Blockage because we are acting as Hot Standby.
 */
public class HajpHotStandbyCauseOfBlockage extends CauseOfBlockage {

    @Override
    public String getShortDescription() {
        return Messages.cluster_extension_hotstandby_causeofblockage();
    }

}
