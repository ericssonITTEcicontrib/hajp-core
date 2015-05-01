package com.ericsson.jenkinsci.hajp.processors;

import com.ericsson.jenkinsci.hajp.api.PluginsManager;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.CreateBuildMessage;
import com.ericsson.jenkinsci.hajp.messages.plugins.SynchronizePluginMessage;
import com.ericsson.jenkinsci.hajp.operation.OperationType;
import com.ericsson.jenkinsci.hajp.processors.impl.PluginMessageProcessor;
import jenkins.model.Jenkins;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class) public class PluginMessageProcessorTest {

    @Mock private static Jenkins mockJenkins;
    private PluginMessageProcessor unitUnderTest;
    private HajpMessage hajpMessage;
    @Mock private HajpMessage mockHajpMessage;

    @Mock private PluginsManager mockPluginsManager;

    @Before public void setUp() throws Exception {
        unitUnderTest = new PluginMessageProcessor(mockJenkins);
        unitUnderTest.setPluginsManager(mockPluginsManager);
    }

    @After public void tearDown() throws Exception {

    }

    @Test public void testConstructor() throws Exception {
        Assert.assertNotNull(unitUnderTest);
    }

    @Test public void testProcessInvalidMessage() throws Exception {
        String jobName = null;
        int buildNumber = 0;
        String dirName = null;
        byte[] fileAsByteArray = null;
        hajpMessage = new CreateBuildMessage(jobName, buildNumber, dirName, fileAsByteArray);
        boolean result = unitUnderTest.process(hajpMessage);
        Assert.assertEquals(false, result);
    }

    @Test public void testProcessSynchronizePluginMessage() throws Exception {

        String fileName = "fileName";
        byte[] fileAsByteArray = null;
        hajpMessage = new SynchronizePluginMessage(fileName, fileAsByteArray);

        Mockito.doNothing().when(mockPluginsManager).updatePluginsConfig(Mockito.anyMap());

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockPluginsManager, Mockito.times(1)).updatePluginsConfig(Mockito.anyMap());
        Assert.assertEquals(true, result);
    }


    @Test public void testProcessValidAndUnknownMessage() throws Exception {

        Mockito.when(mockHajpMessage.isValid()).thenReturn(true);
        // return any type but ones Plugin related
        Mockito.when(mockHajpMessage.getOperationType()).thenReturn(OperationType.DELETE_JOB);

        boolean result = unitUnderTest.process(mockHajpMessage);

        Mockito.verify(mockHajpMessage, Mockito.times(1)).isValid();
        Mockito.verify(mockHajpMessage, Mockito.times(1)).getOperationType();

        Assert.assertEquals(false, result);
    }



    @Test public void testProcessMessageWithIOException() throws Exception {

        String fileName = "fileName";
        byte[] fileAsByteArray = null;
        hajpMessage = new SynchronizePluginMessage(fileName, fileAsByteArray);

        Mockito.doThrow(new IOException("testProcessMessageWithIOException"))
            .when(mockPluginsManager).updatePluginsConfig(Mockito.anyMap());

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockPluginsManager,Mockito.times(1)).updatePluginsConfig(Mockito.anyMap());
        Assert.assertEquals(false, result);
    }

}

