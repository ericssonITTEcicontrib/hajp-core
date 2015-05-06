package com.ericsson.jenkinsci.hajp.processors.impl;

import com.ericsson.jenkinsci.hajp.api.GlobalConfigsManager;
import com.ericsson.jenkinsci.hajp.messages.GlobalConfig.SynchronizeGlobalConfigMessage;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.processors.JenkinsProcessor;
import com.google.inject.Inject;
import jenkins.model.Jenkins;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jvnet.hudson.reactor.ReactorException;

import java.io.IOException;
import java.util.Map;

/**
 * Golbal config message processing via HAJP-Api
 */
@Log4j2 public class GlobalConfigMessageProcessor implements JenkinsProcessor {
    @Setter private GlobalConfigsManager globalConfigsManager;

    /**
     * Constructor
     *
     * @param jenkins the jenkins instance
     */
    @Inject
    public GlobalConfigMessageProcessor(Jenkins jenkins) {
        this.globalConfigsManager = new GlobalConfigsManager(jenkins);
    }

    /**
     * Replicates the operation carried by the received job message..
     *
     * @param hajpMessage the plugin message to be processed
     * @return true if successful, false otherwise
     */
    @Override public boolean process(HajpMessage hajpMessage) {
        if (hajpMessage.isValid()) {
            log.info("Global Config Sync message:" + hajpMessage.toString());
            try {
                switch (hajpMessage.getOperationType()) {
                    case SYNCHRONIZE_GLOBAL_CONFIG:
                        SynchronizeGlobalConfigMessage synchronizeGlobalConfigMessage =
                            (SynchronizeGlobalConfigMessage) hajpMessage;
                        synchronizeGlobalConfig(synchronizeGlobalConfigMessage.getFileMap());
                        break;
                    default:
                        return false;
                }
                return true;
            } catch (IOException | ReactorException | InterruptedException e) {
                log.error(e.getMessage());
            }
        }
        return false;
    }

    private void synchronizeGlobalConfig(Map<String, byte[]> fileMap)
        throws IOException, ReactorException, InterruptedException {
        globalConfigsManager.updateGlobalConfig(fileMap);
    }
}
