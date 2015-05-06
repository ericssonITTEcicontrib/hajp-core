package com.ericsson.jenkinsci.hajp;


import com.ericsson.jenkinsci.hajp.extensions.HajpClusterExtensionProvider;
import com.ericsson.jenkinsci.hajp.processors.ClusterProcessor;
import com.ericsson.jenkinsci.hajp.processors.JenkinsProcessor;
import com.ericsson.jenkinsci.hajp.processors.impl.BuildMessageProcessor;
import com.ericsson.jenkinsci.hajp.processors.impl.ClusterMessageProcessor;
import com.ericsson.jenkinsci.hajp.processors.impl.CredentialMessageProcessor;
import com.ericsson.jenkinsci.hajp.processors.impl.GlobalConfigMessageProcessor;
import com.ericsson.jenkinsci.hajp.processors.impl.JobMessageProcessor;
import com.ericsson.jenkinsci.hajp.processors.impl.PluginMessageProcessor;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import jenkins.model.Jenkins;

/**
 * Guice configuration class binding implementations to
 * declarations.
 */
public class HajpContext extends AbstractModule {
    /**
     * Guice lifecycle calls this method to instantiate
     * bindings.
     */
    @Override protected void configure() {
        bind(JenkinsProcessor.class).annotatedWith(Names.named("jobMsgProcessor"))
            .to(JobMessageProcessor.class);
        bind(JenkinsProcessor.class).annotatedWith(Names.named("buildMsgProcessor"))
            .to(BuildMessageProcessor.class);
        bind(JenkinsProcessor.class).annotatedWith(Names.named("pluginMsgProcessor"))
            .to(PluginMessageProcessor.class);
        bind(JenkinsProcessor.class).annotatedWith(Names.named("credentialMessageProcessor"))
            .to(CredentialMessageProcessor.class);
        bind(JenkinsProcessor.class).annotatedWith(Names.named("globalConfigMsgProcessor"))
            .to(GlobalConfigMessageProcessor.class);

        bind(ClusterProcessor.class).to(ClusterMessageProcessor.class);
        bind(HajpClusterExtensionProvider.class)
            .toInstance(new HajpClusterExtensionProvider(Jenkins.getInstance()));

        bind(Jenkins.class).toInstance(Jenkins.getInstance());
    }
}
