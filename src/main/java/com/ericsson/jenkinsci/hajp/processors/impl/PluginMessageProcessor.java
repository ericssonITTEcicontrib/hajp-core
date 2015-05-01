package com.ericsson.jenkinsci.hajp.processors.impl;

import com.ericsson.jenkinsci.hajp.api.PluginsManager;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.plugins.SynchronizePluginMessage;
import com.ericsson.jenkinsci.hajp.processors.JenkinsProcessor;
import jenkins.model.Jenkins;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Map;

/**
 * Plugins message processing via HAJP-Api
 */
@Log4j2 public class PluginMessageProcessor implements JenkinsProcessor {
    @Setter private PluginsManager pluginsManager;

    /**
     * Default constructor
     */
    public PluginMessageProcessor() {
        this.pluginsManager = new PluginsManager(Jenkins.getInstance());
    }

    /**
     * Constructor
     *
     * @param jenkins the jenkins instance
     */
    public PluginMessageProcessor(Jenkins jenkins) {
        this.pluginsManager = new PluginsManager(jenkins);
    }

    /**
     * Replicates the operation carried by the received job message..
     *
     * @param hajpMessage the plugin message to be processed
     * @return true if successful, false otherwise
     */
    @Override public boolean process(HajpMessage hajpMessage) {
        if (hajpMessage.isValid()) {
            log.info("Plugin Sync message:" + hajpMessage.toString());
            try {
                switch (hajpMessage.getOperationType()) {
                    case SYNCHRONIZE_PLUGIN:
                        SynchronizePluginMessage synchronizePluginMessage =
                            (SynchronizePluginMessage) hajpMessage;
                        synchronizePlugin(synchronizePluginMessage.getFileMap());
                        break;
                    default:
                        return false;
                }
                return true;
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return false;
    }

    private void synchronizePlugin(Map<String, byte[]> fileMap) throws IOException {
        pluginsManager.updatePluginsConfig(fileMap);
    }
}
