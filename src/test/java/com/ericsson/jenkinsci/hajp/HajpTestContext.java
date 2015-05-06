package com.ericsson.jenkinsci.hajp;

import com.ericsson.jenkinsci.hajp.api.BuildsManager;
import com.ericsson.jenkinsci.hajp.api.CredentialsManager;
import com.ericsson.jenkinsci.hajp.api.JobsManager;
import com.ericsson.jenkinsci.hajp.api.PluginsManager;
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
import lombok.Getter;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.IOException;

public class HajpTestContext extends AbstractModule {

    @Getter private Jenkins mockJenkins;
    @Getter private JobsManager mockJobsManager;
    @Getter private BuildsManager mockBuildsManager;
    @Getter private PluginsManager mockPluginsManager;
    @Getter private CredentialsManager mockCredentialsManager;

    @Rule TemporaryFolder tmpFolder = new TemporaryFolder();

    public HajpTestContext() throws IOException {
        mockJenkins = Mockito.mock(Jenkins.class);
        mockJobsManager = Mockito.mock(JobsManager.class);
        mockBuildsManager = Mockito.mock(BuildsManager.class);
        mockPluginsManager = Mockito.mock(PluginsManager.class);
        mockCredentialsManager = Mockito.mock(CredentialsManager.class);
        tmpFolder.create();
        Mockito.when(mockJenkins.getRootDir()).thenReturn(tmpFolder.getRoot());
    }

    public HajpTestContext(Jenkins jenkins) {
        this.mockJenkins = jenkins;
        mockJobsManager = Mockito.mock(JobsManager.class);
        mockBuildsManager = Mockito.mock(BuildsManager.class);
        mockPluginsManager = Mockito.mock(PluginsManager.class);
        mockCredentialsManager = Mockito.mock(CredentialsManager.class);
    }

    @Override
    protected void configure() {
        JobMessageProcessor jobMsgProcessor = new JobMessageProcessor();
        jobMsgProcessor.setJobsManager(mockJobsManager);
        jobMsgProcessor.setBuildsManager(mockBuildsManager);
        BuildMessageProcessor buildMsgProcessor = new BuildMessageProcessor();
        buildMsgProcessor.setBuildsManager(mockBuildsManager);
        buildMsgProcessor.setJobsManager(mockJobsManager);
        PluginMessageProcessor pluginMsgProcessor = new PluginMessageProcessor();
        pluginMsgProcessor.setPluginsManager(mockPluginsManager);
        CredentialMessageProcessor credentialMessageProcessor = new CredentialMessageProcessor(mockJenkins);
        credentialMessageProcessor.setCredentialsManager(mockCredentialsManager);


        bind(JenkinsProcessor.class).annotatedWith(Names.named("jobMsgProcessor"))
            .toInstance(jobMsgProcessor);
        bind(JenkinsProcessor.class).annotatedWith(Names.named("buildMsgProcessor"))
            .toInstance(buildMsgProcessor);
        bind(JenkinsProcessor.class).annotatedWith(Names.named("pluginMsgProcessor"))
            .toInstance(pluginMsgProcessor);
        bind(JenkinsProcessor.class).annotatedWith(Names.named("credentialMessageProcessor"))
            .toInstance(credentialMessageProcessor);
        bind(JenkinsProcessor.class).annotatedWith(Names.named("globalConfigMsgProcessor"))
            .to(GlobalConfigMessageProcessor.class);

        bind(ClusterProcessor.class).to(ClusterMessageProcessor.class);
        bind(HajpClusterExtensionProvider.class).toInstance(
            new HajpClusterExtensionProvider(mockJenkins));

        bind(Jenkins.class).toInstance(mockJenkins);
    }

}
