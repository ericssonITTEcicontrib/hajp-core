package com.ericsson.jenkinsci.hajp.processors.impl;

import com.ericsson.jenkinsci.hajp.messages.GlobalConfig.AbstractGlobalConfigMessage;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.AbstractBuildMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.AbstractCredentialsMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.AbstractJobMessage;
import com.ericsson.jenkinsci.hajp.messages.plugins.AbstractPluginsMessage;
import com.ericsson.jenkinsci.hajp.processors.ClusterProcessor;
import com.ericsson.jenkinsci.hajp.processors.JenkinsProcessor;
import com.google.inject.Inject;

import javax.inject.Named;

/**
 * Processing entry point, called via Actors
 * with HajpMessage.
 */
public class ClusterMessageProcessor implements ClusterProcessor {

    @Inject @Named("jobMsgProcessor") private JenkinsProcessor jobMsgProcessor;
    @Inject @Named("buildMsgProcessor") private JenkinsProcessor buildMsgProcessor;
    @Inject @Named("pluginMsgProcessor") private JenkinsProcessor pluginMsgProcessor;
    @Inject @Named("credentialMessageProcessor") private JenkinsProcessor
        credentialMessageProcessor;
    @Inject @Named("globalConfigMsgProcessor") private JenkinsProcessor globalConfigMsgProcessor;


    /**
     * Method assigns processor based on message type.
     *
     * @param hajpMessage Received message from cluster.
     */
    public void process(HajpMessage hajpMessage) {
        if (hajpMessage instanceof AbstractJobMessage) {
            jobMsgProcessor.process(hajpMessage);
        }
        if (hajpMessage instanceof AbstractBuildMessage) {
            buildMsgProcessor.process(hajpMessage);
        }
        if (hajpMessage instanceof AbstractPluginsMessage) {
            pluginMsgProcessor.process(hajpMessage);
        }
        if (hajpMessage instanceof AbstractCredentialsMessage) {
            credentialMessageProcessor.process(hajpMessage);
        }
        if (hajpMessage instanceof AbstractGlobalConfigMessage) {
            globalConfigMsgProcessor.process(hajpMessage);
        }
    }
}
