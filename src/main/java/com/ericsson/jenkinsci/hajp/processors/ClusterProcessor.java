package com.ericsson.jenkinsci.hajp.processors;

import com.ericsson.jenkinsci.hajp.messages.HajpMessage;

/**
 * Standardized HajpMessage cluster processing
 * functionality.
 */
public interface ClusterProcessor {

    /**
     * Process the incoming message
     *
     * @param hajpMessage the hajp message to be processed
     */
    public void process(HajpMessage hajpMessage);
}
