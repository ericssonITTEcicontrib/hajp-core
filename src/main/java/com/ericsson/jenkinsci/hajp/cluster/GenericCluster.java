package com.ericsson.jenkinsci.hajp.cluster;

import com.ericsson.jenkinsci.hajp.exceptions.HajpClusterConfigurationException;

import java.io.IOException;

/**
 * Standardizing cluster operations.
 */
public interface GenericCluster {
    /**
     * Launch the Cluster
     *
     * @throws IOException if encountered
     */
    public void launch(String ownIp, Integer port, String orchestratorIp, Integer orchestratorPort)
        throws IOException, HajpClusterConfigurationException;

    /**
     * Stop the Cluster
     *
     * @throws IOException if encountered
     */
    public void stop() throws IOException;
}
