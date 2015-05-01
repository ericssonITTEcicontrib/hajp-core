package com.ericsson.jenkinsci.hajp.extensions;

import static org.mockito.Mockito.when;

import com.ericsson.jenkinsci.hajp.api.JobsManager;
import com.ericsson.jenkinsci.hajp.cluster.HajpClusterMembershipStatusProvider;
import com.ericsson.jenkinsci.hajp.HajpAbstractTest;

import hudson.model.Item;
import hudson.model.Job;
import jenkins.model.Jenkins;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HajpClusterMembershipStatusProvider.class)
@PowerMockIgnore("javax.management.*")
public class HajpItemListenerTest extends HajpAbstractTest {

    @Rule public TemporaryFolder tmpFolder = new TemporaryFolder();

    private HajpItemListener listener;
    private JobsManager mockJobsManager;
    private Jenkins mockJenkins;

    private String jobName = "test1";
    private String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project/>\n";
    private Item item;
    private Job job;
    private File rootDir;
    public HajpItemListenerTest() {
        mockJobsManager = context.getMockJobsManager();
        listener = new HajpItemListener();
        listener.setJobsManager(mockJobsManager);
        listener.setSender(mockSender);

        item = Mockito.mock(Item.class);
        job = Mockito.mock(Job.class);
        mockJenkins = Mockito.mock(Jenkins.class);

        HajpClusterMembershipStatusProvider mockStatusProvider = Mockito.mock(HajpClusterMembershipStatusProvider.class);
        PowerMockito.mockStatic(HajpClusterMembershipStatusProvider.class);
        when(HajpClusterMembershipStatusProvider.getInstance()).thenReturn(mockStatusProvider);
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster()).thenReturn(true);
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmActiveMaster()).thenReturn(true);

    }

    @Before public void beforeTest() throws Exception {
        tmpFolder.create();
        rootDir = tmpFolder.newFolder();
        Mockito.when(mockJobsManager.getJobConfigAsXml(item)).thenReturn(xmlContent);
        Mockito.when(mockJobsManager.getJobConfigAsXml(job)).thenReturn(xmlContent);
        Mockito.when(mockJobsManager.getJob(jobName)).thenReturn(job);
        Mockito.when(job.getName()).thenReturn(jobName);
        Mockito.when(job.getRootDir()).thenReturn(rootDir);
        Mockito.when(item.getName()).thenReturn(jobName);
        Mockito.when(item.getRootDir()).thenReturn(rootDir);
    }

    @After
    public void afterTest() {
        Mockito.reset(mockJobsManager);
        Mockito.reset(item);
    }

    @Test public void testCreateJob() throws Exception {
        Mockito.doNothing().when(mockJobsManager).createJob(jobName, xmlContent);

        listener.onCreated(item);

        Mockito.verify(mockJobsManager).getJobNames();
        Mockito.verify(mockJobsManager).getJobConfigAsXml(item);
        Mockito.verify(mockJobsManager).createJob(jobName, xmlContent);
    }

    @Test public void testUpdateJob() throws Exception {
        Collection<String> names = new ArrayList<>();
        names.add(jobName);
        Mockito.when(mockJobsManager.getJobNames()).thenReturn(names);
        Mockito.when(mockJobsManager.getJenkins()).thenReturn(mockJenkins);
        Mockito.when(mockJenkins.getRootDir()).thenReturn(rootDir);

        listener.onUpdated(item);

        Mockito.verify(mockJobsManager).getJobNames();
        Mockito.verify(mockJobsManager).getJobConfigAsXml(job);
        Mockito.verify(mockJobsManager).updateJobConfig(jobName, xmlContent);
    }

    @Test public void testRenameJob() throws Exception {
        String newName = jobName + ".1";
        Collection<String> names = new ArrayList<>();
        names.add(jobName);
        Mockito.when(mockJobsManager.getJobNames()).thenReturn(names);

        listener.onRenamed(item, jobName, newName);

        Mockito.verify(mockJobsManager).getJobNames();
        Mockito.verify(mockJobsManager).renameJob(jobName, newName);
    }

    @Test public void testDeleteJob() throws Exception {
        Collection<String> names = new ArrayList<>();
        names.add(jobName);
        Mockito.when(mockJobsManager.getJobNames()).thenReturn(names);

        listener.onDeleted(item);

        Mockito.verify(mockJobsManager).getJobNames();
        Mockito.verify(mockJobsManager).deleteJob(jobName);
    }
}
