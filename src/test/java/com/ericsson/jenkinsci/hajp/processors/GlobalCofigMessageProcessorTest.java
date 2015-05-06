package com.ericsson.jenkinsci.hajp.processors;

import com.ericsson.jenkinsci.hajp.api.GlobalConfigsManager;
import com.ericsson.jenkinsci.hajp.messages.GlobalConfig.SynchronizeGlobalConfigMessage;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.CreateBuildMessage;
import com.ericsson.jenkinsci.hajp.operation.OperationType;
import com.ericsson.jenkinsci.hajp.processors.impl.GlobalConfigMessageProcessor;
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

@RunWith(MockitoJUnitRunner.class) public class GlobalCofigMessageProcessorTest {

    @Mock private static Jenkins mockJenkins;
    private GlobalConfigMessageProcessor unitUnderTest;
    private HajpMessage hajpMessage;
    @Mock private HajpMessage mockHajpMessage;

    @Mock private GlobalConfigsManager mockGlobalConfigsManager;

    @Before public void setUp() throws Exception {
        unitUnderTest = new GlobalConfigMessageProcessor(mockJenkins);
        unitUnderTest.setGlobalConfigsManager(mockGlobalConfigsManager);
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
        Assert.assertEquals(false, hajpMessage.isValid());
        boolean result = unitUnderTest.process(hajpMessage);
        Assert.assertEquals(false, result);
    }

    @Test public void testProcessSynchronizeGlobalConfigMessage() throws Exception {

        String fileName = "fileName";
        byte[] fileAsByteArray = null;
        hajpMessage = new SynchronizeGlobalConfigMessage(fileName, fileAsByteArray);

        Mockito.doNothing().when(mockGlobalConfigsManager).updateGlobalConfig(Mockito.anyMap());

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockGlobalConfigsManager, Mockito.times(1)).updateGlobalConfig(
            Mockito.anyMap());
        Assert.assertEquals(true, result);
    }


    @Test public void testProcessValidAndUnknownMessage() throws Exception {

        Mockito.when(mockHajpMessage.isValid()).thenReturn(true);
        // return any type but ones Global Config related
        Mockito.when(mockHajpMessage.getOperationType()).thenReturn(OperationType.DELETE_JOB);

        boolean result = unitUnderTest.process(mockHajpMessage);

        Mockito.verify(mockHajpMessage, Mockito.times(1)).isValid();
        Mockito.verify(mockHajpMessage, Mockito.times(1)).getOperationType();

        Assert.assertEquals(false, result);
    }



    @Test public void testProcessMessageWithIOException() throws Exception {

        String fileName = "fileName";
        byte[] fileAsByteArray = null;
        hajpMessage = new SynchronizeGlobalConfigMessage(fileName, fileAsByteArray);

        Mockito.doThrow(new IOException("testProcessMessageWithIOException"))
            .when(mockGlobalConfigsManager).updateGlobalConfig(Mockito.anyMap());

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockGlobalConfigsManager,Mockito.times(1)).updateGlobalConfig(
            Mockito.anyMap());
        Assert.assertEquals(false, result);
    }

}

