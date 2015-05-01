package com.ericsson.jenkinsci.hajp.exceptions;


public class HajpClusterConfigurationException extends Exception {

    /**
     * @param message the message to be carried by the exception
     * @see Exception
     */
    public HajpClusterConfigurationException(String message) {
        super(message);
    }
}
