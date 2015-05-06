package com.ericsson.jenkinsci.hajp.processors;

import com.ericsson.jenkinsci.hajp.messages.GlobalConfig.SynchronizeGlobalConfigMessage;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.CreateBuildMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.CredentialsCreateMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.CreateJobMessage;
import com.ericsson.jenkinsci.hajp.messages.plugins.SynchronizePluginMessage;
import com.ericsson.jenkinsci.hajp.processors.impl.ClusterMessageProcessor;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) public class ClusterMessageProcessorTest {
    @Mock private static JenkinsProcessor jobMsgProcessor;
    @Mock private static JenkinsProcessor buildMsgProcessor;
    @Mock private static JenkinsProcessor pluginMsgProcessor;
    @Mock private static JenkinsProcessor credentialsProcessor;
    @Mock private static JenkinsProcessor globalConfigMsgProcessor;
    private ClusterMessageProcessor unitUnderTest;
    private HajpMessage hajpMessage;

    @Before public void setUp() throws Exception {
        Mockito.when(jobMsgProcessor.process(hajpMessage)).thenReturn(false);
        Mockito.when(buildMsgProcessor.process(hajpMessage)).thenReturn(false);
        Mockito.when(credentialsProcessor.process(hajpMessage)).thenReturn(false);
        Mockito.when(pluginMsgProcessor.process(hajpMessage)).thenReturn(false);

        Injector myInjector = Guice.createInjector(new TestModule());
        Assert.assertNotNull(myInjector);
        unitUnderTest = myInjector.getInstance(ClusterMessageProcessor.class);
        Assert.assertNotNull(unitUnderTest);
    }

    @After public void tearDown() throws Exception {

    }

    @Test public void testProcess() throws Exception {
        unitUnderTest.process(null);
    }

    @Test public void testProcessJobMessage() throws Exception {
        String jobName = null;
        String xmlFileContents = null;
        hajpMessage = new CreateJobMessage(jobName, xmlFileContents);
        unitUnderTest.process(hajpMessage);
        Mockito.verify(jobMsgProcessor, Mockito.times(1)).process(hajpMessage);
        Mockito.verify(buildMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(pluginMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(credentialsProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(globalConfigMsgProcessor, Mockito.times(0)).process(hajpMessage);
    }

    @Test public void testProcessBuildMessage() throws Exception {
        String jobName = null;
        int buildNumber = 0;
        String dirName = null;
        byte[] fileAsByteArray = null;
        hajpMessage = new CreateBuildMessage(jobName, buildNumber, dirName, fileAsByteArray);
        unitUnderTest.process(hajpMessage);
        Mockito.verify(jobMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(buildMsgProcessor, Mockito.times(1)).process(hajpMessage);
        Mockito.verify(pluginMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(credentialsProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(globalConfigMsgProcessor, Mockito.times(0)).process(hajpMessage);
    }

    @Test public void testProcessPluginMessage() throws Exception {
        String fileName = null;
        byte[] fileAsByteArray = null;
        hajpMessage = new SynchronizePluginMessage(fileName, fileAsByteArray);
        unitUnderTest.process(hajpMessage);
        Mockito.verify(jobMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(buildMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(pluginMsgProcessor, Mockito.times(1)).process(hajpMessage);
        Mockito.verify(credentialsProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(globalConfigMsgProcessor, Mockito.times(0)).process(hajpMessage);
    }

    @Test public void testProcessCredentialsMessage() throws Exception {
        byte[] credentialsFile = null;
        byte[] embeddedSecrets = null;
        hajpMessage = new CredentialsCreateMessage(credentialsFile, embeddedSecrets);
        unitUnderTest.process(hajpMessage);
        Mockito.verify(jobMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(buildMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(pluginMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(credentialsProcessor, Mockito.times(1)).process(hajpMessage);
        Mockito.verify(globalConfigMsgProcessor, Mockito.times(0)).process(hajpMessage);
    }

    @Test public void testProcessGlobalConfigMessage() throws Exception {
        String fileName = null;
        byte[] fileAsByteArray = null;
        hajpMessage = new SynchronizeGlobalConfigMessage(fileName, fileAsByteArray);
        unitUnderTest.process(hajpMessage);
        Mockito.verify(jobMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(buildMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(pluginMsgProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(credentialsProcessor, Mockito.times(0)).process(hajpMessage);
        Mockito.verify(globalConfigMsgProcessor, Mockito.times(1)).process(hajpMessage);
    }

    private static class TestModule extends AbstractModule {
        @Override public void configure() {
            bind(JenkinsProcessor.class).annotatedWith(Names.named("jobMsgProcessor"))
                .toInstance(jobMsgProcessor);
            bind(JenkinsProcessor.class).annotatedWith(Names.named("buildMsgProcessor"))
                .toInstance(buildMsgProcessor);
            bind(JenkinsProcessor.class).annotatedWith(Names.named("pluginMsgProcessor"))
                .toInstance(pluginMsgProcessor);
            bind(JenkinsProcessor.class).annotatedWith(Names.named("credentialMessageProcessor"))
                .toInstance(credentialsProcessor);
            bind(JenkinsProcessor.class).annotatedWith(Names.named("globalConfigMsgProcessor"))
                .toInstance(globalConfigMsgProcessor);
        }
    }
}

