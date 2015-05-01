package com.ericsson.jenkinsci.hajp.processors;

import com.ericsson.jenkinsci.hajp.messages.HajpMessage;

/**
 * Standardize JenkinsProcessing functionality.
 */
public interface JenkinsProcessor {

    /**
     * Process the incoming message
     *
     * @param hajpMessage the hajp message to be processed
     * @return true if successful, false otherwise
     */
    boolean process(HajpMessage hajpMessage);
}
