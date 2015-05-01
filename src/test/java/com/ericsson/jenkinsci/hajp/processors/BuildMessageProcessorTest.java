package com.ericsson.jenkinsci.hajp.processors;

import com.ericsson.jenkinsci.hajp.api.BuildsManagementException;
import com.ericsson.jenkinsci.hajp.api.BuildsManager;
import com.ericsson.jenkinsci.hajp.api.JobManagementException;
import com.ericsson.jenkinsci.hajp.api.JobsManager;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.CreateBuildMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.DeleteBuildMessage;
import com.ericsson.jenkinsci.hajp.operation.OperationType;
import com.ericsson.jenkinsci.hajp.processors.impl.BuildMessageProcessor;
import jenkins.model.Jenkins;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class) public class BuildMessageProcessorTest {

    @Mock private static Jenkins mockJenkins;
    private BuildMessageProcessor unitUnderTest;
    private HajpMessage hajpMessage;
    @Mock private HajpMessage mockHajpMessage;

    @Mock private JobsManager mockJobsManager;
    @Mock private BuildsManager mockBuildsManager;

    @Before public void setUp() throws Exception {
        unitUnderTest = new BuildMessageProcessor(mockJenkins);
        unitUnderTest.setBuildsManager(mockBuildsManager);
        unitUnderTest.setJobsManager(mockJobsManager);
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

        Assert.assertFalse(hajpMessage.isValid());

        boolean result = unitUnderTest.process(hajpMessage);

        Assert.assertEquals(false, result);
    }

    @Test public void testProcessCreateBuildMessage() throws Exception {

        String jobName = "jobName";
        int buildNumber = 0;
        String dirName = null;
        byte[] fileAsByteArray = null;
        hajpMessage = new CreateBuildMessage(jobName, buildNumber, dirName, fileAsByteArray);

        Mockito.when(mockBuildsManager.buildExists(jobName, buildNumber)).thenReturn(true);
        Mockito.when(mockBuildsManager.extractBuild(fileAsByteArray, jobName, buildNumber, dirName))
            .thenReturn(null);
        Mockito.when(mockBuildsManager.createFreeStyleBuild(jobName, null)).thenReturn(null);
        Mockito.doNothing().when(mockJobsManager).reloadJob(jobName);

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockBuildsManager, Mockito.times(1)).buildExists(jobName, buildNumber);
        Mockito.verify(mockBuildsManager, Mockito.times(1))
            .extractBuild(fileAsByteArray, jobName, buildNumber, dirName);
        Mockito.verify(mockBuildsManager, Mockito.times(1)).createFreeStyleBuild(jobName, null);
        Mockito.verify(mockJobsManager, Mockito.times(1)).reloadJob(jobName);
        Assert.assertEquals(true, result);
    }

    @Test public void testProcessDeleteBuildMessage() throws Exception {

        String jobName = "jobName";
        int buildNumber = 0;
        hajpMessage = new DeleteBuildMessage(jobName, buildNumber);
        Mockito.when(mockBuildsManager.buildExists(jobName, buildNumber)).thenReturn(false);

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockBuildsManager, Mockito.times(1)).buildExists(jobName, buildNumber);
        Assert.assertEquals(true, result);
    }

    @Test public void testProcessValidAndUnknownMessage() throws Exception {

        Mockito.when(mockHajpMessage.isValid()).thenReturn(true);
        // return any type but ones Build related
        Mockito.when(mockHajpMessage.getOperationType()).thenReturn(OperationType.DELETE_JOB);

        boolean result = unitUnderTest.process(mockHajpMessage);

        Mockito.verify(mockHajpMessage, Mockito.times(1)).isValid();
        Mockito.verify(mockHajpMessage, Mockito.times(1)).getOperationType();
        Assert.assertEquals(false, result);
    }

    @Test public void testProcessMessageWithBuildsManagementException() throws Exception {

        String jobName = "jobName";
        int buildNumber = 0;
        String dirName = null;
        byte[] fileAsByteArray = null;
        hajpMessage = new CreateBuildMessage(jobName, buildNumber, dirName, fileAsByteArray);

        Mockito.when(mockBuildsManager.buildExists(jobName, buildNumber)).thenReturn(true);
        Mockito.when(mockBuildsManager.extractBuild(fileAsByteArray, jobName, buildNumber, dirName))
            .thenThrow(
                new BuildsManagementException("testProcessMessageWithBuildsManagementException"));

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockBuildsManager, Mockito.times(1)).buildExists(jobName, buildNumber);
        Mockito.verify(mockBuildsManager, Mockito.times(1))
            .extractBuild(fileAsByteArray, jobName, buildNumber, dirName);
        Assert.assertEquals(false, result);
    }

    @Test public void testProcessMessageWithJobManagementException() throws Exception {

        String jobName = "jobName";
        int buildNumber = 0;
        String dirName = null;
        byte[] fileAsByteArray = null;
        hajpMessage = new CreateBuildMessage(jobName, buildNumber, dirName, fileAsByteArray);

        Mockito.when(mockBuildsManager.buildExists(jobName, buildNumber)).thenReturn(true);
        Mockito.when(mockBuildsManager.extractBuild(fileAsByteArray, jobName, buildNumber, dirName))
            .thenReturn(null);
        Mockito.when(mockBuildsManager.createFreeStyleBuild(jobName, null)).thenReturn(null);
        Mockito.doThrow(new JobManagementException("testProcessMessageWithJobManagementException"))
            .when(mockJobsManager).reloadJob(jobName);

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockBuildsManager, Mockito.times(1)).buildExists(jobName, buildNumber);
        Mockito.verify(mockBuildsManager, Mockito.times(1))
            .extractBuild(fileAsByteArray, jobName, buildNumber, dirName);
        Mockito.verify(mockBuildsManager, Mockito.times(1)).createFreeStyleBuild(jobName, null);
        Mockito.verify(mockJobsManager, Mockito.times(1)).reloadJob(jobName);
        Assert.assertEquals(false, result);
    }

}

