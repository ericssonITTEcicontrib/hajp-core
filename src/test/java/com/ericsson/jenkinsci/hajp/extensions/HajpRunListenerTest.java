package com.ericsson.jenkinsci.hajp.extensions;

import static org.mockito.Mockito.when;

import com.ericsson.jenkinsci.hajp.api.BuildsManagementException;
import com.ericsson.jenkinsci.hajp.api.BuildsManager;
import com.ericsson.jenkinsci.hajp.api.JobManagementException;
import com.ericsson.jenkinsci.hajp.api.JobsManager;
import com.ericsson.jenkinsci.hajp.cluster.HajpClusterMembershipStatusProvider;
import com.ericsson.jenkinsci.hajp.HajpAbstractTest;

import hudson.model.FreeStyleBuild;
import hudson.model.Job;
import hudson.model.Run;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Path;

import jenkins.model.Jenkins;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HajpClusterMembershipStatusProvider.class)
@PowerMockIgnore("javax.management.*")
public class HajpRunListenerTest extends HajpAbstractTest {

    private HajpRunListener listener;
    private JobsManager mockJobsManager;
    private BuildsManager mockBuildsManager;

    public HajpRunListenerTest() throws IOException, ClassNotFoundException {
        mockJobsManager = context.getMockJobsManager();
        mockBuildsManager = context.getMockBuildsManager();

        listener = new HajpRunListener();
        listener.setJobsManager(mockJobsManager);
        listener.setBuildsManager(mockBuildsManager);
        listener.setSender(mockSender);

        HajpClusterMembershipStatusProvider mockStatusProvider = Mockito.mock(HajpClusterMembershipStatusProvider.class);
        PowerMockito.mockStatic(HajpClusterMembershipStatusProvider.class);
        when(HajpClusterMembershipStatusProvider.getInstance()).thenReturn(mockStatusProvider);
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster()).thenReturn(true);
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmActiveMaster()).thenReturn(true);

    }

    @After
    public void afterTest() {
        Mockito.reset(mockJobsManager);
        Mockito.reset(mockBuildsManager);
    }


    @Test public void testOnFinalized()
        throws BuildsManagementException, InterruptedException, JobManagementException,
        IOException {
        Run<?, ?> run = Mockito.mock(Run.class);
        Job job = Mockito.mock(Job.class);
        FreeStyleBuild build = Mockito.mock(FreeStyleBuild.class);
        String jobName = "test1";
        int buildNumber = 1;
        String timestampBuildDir = "test1";
        byte[] byteArray = new byte[0];
        Path buildDir = Mockito.mock(Path.class);

        Mockito.when(run.getParent()).thenReturn(job);
        Mockito.when(run.getNumber()).thenReturn(buildNumber);
        Mockito.when(job.getName()).thenReturn(jobName);

        Mockito.when(mockBuildsManager.grabBuild(jobName, buildNumber)).thenReturn(byteArray);
        Mockito.when(mockBuildsManager.getTimestampBuildDirName(jobName, buildNumber))
            .thenReturn(timestampBuildDir);
        Mockito.when(mockBuildsManager.buildExists(jobName, buildNumber)).thenReturn(false);
        Mockito.when(
            mockBuildsManager.extractBuild(byteArray, jobName, buildNumber, timestampBuildDir))
            .thenReturn(buildDir);
        Mockito.when(mockBuildsManager.createFreeStyleBuild(jobName, buildDir)).thenReturn(build);

        listener.onFinalized(run);

        Mockito.verify(mockBuildsManager).grabBuild(jobName, buildNumber);
        Mockito.verify(mockBuildsManager).getTimestampBuildDirName(jobName, buildNumber);
        Mockito.verify(mockBuildsManager).buildExists(jobName, buildNumber);
        Mockito.verify(mockBuildsManager).extractBuild(byteArray, jobName, buildNumber,
            timestampBuildDir);
        Mockito.verify(mockBuildsManager).createFreeStyleBuild(jobName, buildDir);
    }

    @Test public void testOnDeleted() throws BuildsManagementException {

        Run<?, ?> run = Mockito.mock(Run.class);
        Job job = Mockito.mock(Job.class);
        String jobName = "test1";
        int buildNumber = 1;

        Mockito.when(run.getParent()).thenReturn(job);
        Mockito.when(run.getNumber()).thenReturn(buildNumber);
        Mockito.when(job.getName()).thenReturn(jobName);

        Mockito.when(mockBuildsManager.buildExists(jobName, buildNumber)).thenReturn(true);
        Mockito.doNothing().when(mockBuildsManager).deleteFreeStyleBuild(jobName, buildNumber);

        listener.onDeleted(run);

        Mockito.verify(mockBuildsManager).buildExists(jobName, buildNumber);
        Mockito.verify(mockBuildsManager).deleteFreeStyleBuild(jobName, buildNumber);
    }
}
