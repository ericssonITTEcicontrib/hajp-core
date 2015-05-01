package com.ericsson.jenkinsci.hajp.processors;

import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.processors.impl.ClusterMessageProcessor;
import com.ericsson.jenkinsci.hajp.processors.impl.CredentialMessageProcessor;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) public class ClusterMessageProcessorTest {
    private ClusterMessageProcessor unitUnderTest;

    @Mock private HajpMessage mockMessage;
    @Mock private static JenkinsProcessor jobMsgProcessor;
    @Mock private static JenkinsProcessor buildMsgProcessor;
    @Mock private static JenkinsProcessor pluginMsgProcessor;
    @Mock private static JenkinsProcessor credentialsProcessor;

    @Before public void setUp() throws Exception {
        Mockito.when(jobMsgProcessor.process(mockMessage)).thenReturn(false);
        Mockito.when(buildMsgProcessor.process(mockMessage)).thenReturn(false);

        Injector myInjector = Guice.createInjector(new TestModule());
        unitUnderTest = myInjector.getInstance(ClusterMessageProcessor.class);
    }

    @After public void tearDown() throws Exception {

    }

    @Test public void testProcess() throws Exception {
        unitUnderTest.process(null);
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

        }
    }
}

