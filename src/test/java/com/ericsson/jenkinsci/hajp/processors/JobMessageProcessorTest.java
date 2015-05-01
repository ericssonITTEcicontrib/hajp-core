package com.ericsson.jenkinsci.hajp.processors;

import static org.mockito.Mockito.times;

import com.ericsson.jenkinsci.hajp.actors.HajpClusterSender;
import com.ericsson.jenkinsci.hajp.api.BuildsManagementException;
import com.ericsson.jenkinsci.hajp.api.BuildsManager;
import com.ericsson.jenkinsci.hajp.api.JobManagementException;
import com.ericsson.jenkinsci.hajp.api.JobsManager;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.CreateJobMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.DeleteAllJobsMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.DeleteJobMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.RenameJobMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.SendAllJobsMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.UpdateJobMessage;
import com.ericsson.jenkinsci.hajp.operation.OperationType;
import com.ericsson.jenkinsci.hajp.processors.impl.JobMessageProcessor;
import hudson.model.Run;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@RunWith(MockitoJUnitRunner.class) public class JobMessageProcessorTest {

    @Mock private static Jenkins mockJenkins;
    private JobMessageProcessor unitUnderTest;
    private HajpMessage hajpMessage;
    @Mock private HajpMessage mockHajpMessage;

    @Mock private JobsManager mockJobsManager;
    @Mock private BuildsManager mockBuildsManager;
    @Mock private HajpClusterSender mockSender;

    @Mock private Run mockRun;
    @Mock private RunList mockRunList;

    @Before public void setUp() throws Exception {
        unitUnderTest = new JobMessageProcessor(mockJenkins);
        unitUnderTest.setBuildsManager(mockBuildsManager);
        unitUnderTest.setJobsManager(mockJobsManager);
        unitUnderTest.setSender(mockSender);
    }

    @After public void tearDown() throws Exception {

    }

    @Test public void testConstructor() throws Exception {
        Assert.assertNotNull(unitUnderTest);
    }

    @Test public void testProcessInvalidMessage() throws Exception {
        String jobName = null;
        String xmlFileContents = null;
        hajpMessage = new CreateJobMessage(jobName, xmlFileContents);
        boolean result = unitUnderTest.process(hajpMessage);
        Assert.assertEquals(false, result);
    }

    @Test public void testProcessCreateJobMessage() throws Exception {

        String jobName = "jobName";
        String xmlFileContents = "xmlFileContents";
        hajpMessage = new CreateJobMessage(jobName, xmlFileContents);
        Mockito.when(mockJobsManager.getJobNames())
            .thenReturn(new HashSet<String>(Arrays.asList(jobName)));

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockJobsManager, times(1)).getJobNames();
        Assert.assertEquals(true, result);
    }

    @Test public void testProcessUpdateJobMessage() throws Exception {

        String jobName = "jobName";
        String xmlFileContents = "xmlFileContents";
        hajpMessage = new UpdateJobMessage(jobName, xmlFileContents);
        Mockito.when(mockJobsManager.getJobNames()).thenReturn(new HashSet<String>());

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockJobsManager, times(1)).getJobNames();
        Assert.assertEquals(true, result);
    }

    @Test public void testProcessRenameJobMessage() throws Exception {

        String jobName = "jobName";
        String xmlFileContents = "xmlFileContents";
        hajpMessage = new RenameJobMessage(jobName, xmlFileContents);
        Mockito.when(mockJobsManager.getJobNames()).thenReturn(new HashSet<String>());

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockJobsManager, times(1)).getJobNames();
        Assert.assertEquals(true, result);
    }

    @Test public void testProcessDeleteJobMessage() throws Exception {

        String jobName = "jobName";
        hajpMessage = new DeleteJobMessage(jobName);
        Mockito.when(mockJobsManager.getJobNames()).thenReturn(new HashSet<String>());

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockJobsManager, times(1)).getJobNames();
        Assert.assertEquals(true, result);
    }

    @Test public void testProcessDeleteAllJobsMessage() throws Exception {

        hajpMessage = new DeleteAllJobsMessage();
        Mockito.doNothing().when(mockJobsManager).deleteAllJobs();

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockJobsManager, times(1)).deleteAllJobs();
        Assert.assertEquals(true, result);
    }

    @Test public void testProcessSendAllJobsMessage() throws Exception {
        String origAddress = "origAddress";
        String destAddress = "destAddress";
        hajpMessage = new SendAllJobsMessage(origAddress, destAddress);
        Mockito.when(mockJobsManager.getJobNames()).thenReturn(new HashSet<String>());

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockJobsManager, times(1)).getJobNames();
        Assert.assertEquals(true, result);
    }

    @Test public void testProcessValidAndUnknownMessage() throws Exception {

        Mockito.when(mockHajpMessage.isValid()).thenReturn(true);
        // return any type but ones Job related
        Mockito.when(mockHajpMessage.getOperationType()).thenReturn(OperationType.CREATE_BUILD);

        boolean result = unitUnderTest.process(mockHajpMessage);

        Mockito.verify(mockHajpMessage, Mockito.times(1)).isValid();
        Mockito.verify(mockHajpMessage, Mockito.times(1)).getOperationType();
        Assert.assertEquals(false, result);
    }

    @Test public void testProcessMessageWithJobManagementException() throws Exception {

        String jobName = "jobName";
        String xmlFileContents = "xmlFileContents";
        hajpMessage = new CreateJobMessage(jobName, xmlFileContents);
        Mockito.when(mockJobsManager.getJobNames()).thenReturn(new HashSet<String>());
        Mockito.doThrow(new JobManagementException("testProcessMessageWithJobManagementException"))
            .when(mockJobsManager).createJob(jobName, xmlFileContents);

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockJobsManager, times(1)).getJobNames();
        Mockito.verify(mockJobsManager, times(1)).createJob(jobName, xmlFileContents);
        Assert.assertEquals(false, result);
    }

    @Test public void testProcessMessageWithBuildsManagementException() throws Exception {

        String jobName = "jobName";
        String origAddress = "origAddress";
        String destAddress = "destAddress";
        hajpMessage = new SendAllJobsMessage(origAddress, destAddress);
        Mockito.when(mockJobsManager.getJobNames())
            .thenReturn(new HashSet<String>(Arrays.asList(jobName)));
        Mockito.when(mockJobsManager.getJob(jobName)).thenReturn(null);
        Mockito.when(mockJobsManager.getJobConfigAsXml(null)).thenReturn(null);
        Mockito.when(mockSender.send(Mockito.anyString(), Mockito.any(CreateJobMessage.class)))
            .thenReturn(true);
        Mockito.when(mockJobsManager.getAbstractProject(jobName)).thenReturn(null);
        Collection<Run> runs = Arrays.asList(mockRun);
        //RunList runList = new RunList(runs);
        // really hard work here. Daniel: I need help here.
        Mockito.when(mockBuildsManager.listBuilds(null)).thenReturn(mockRunList);

        Mockito.when(mockRunList.iterator()).thenReturn(runs.iterator());

        Mockito.when(mockRun.getNumber()).thenReturn(-1);
        Mockito.when(mockBuildsManager.grabBuild(jobName, -1)).thenThrow(
            new BuildsManagementException("testProcessMessageWithBuildsManagementException"));

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockJobsManager, times(1)).getJobNames();
        Mockito.verify(mockJobsManager, times(1)).getJob(jobName);
        Mockito.verify(mockJobsManager, times(1)).getJobConfigAsXml(null);
        Mockito.verify(mockSender, times(1))
            .send(Mockito.anyString(), Mockito.any(CreateJobMessage.class));
        Mockito.verify(mockJobsManager, times(1)).getAbstractProject(jobName);
        Mockito.verify(mockBuildsManager, times(1)).listBuilds(null);
        Mockito.verify(mockRunList, times(1)).iterator();

        Mockito.verify(mockRun, times(1)).getNumber();
        Mockito.verify(mockBuildsManager, times(1)).grabBuild(jobName, -1);

        Assert.assertEquals(false, result);
    }

}

