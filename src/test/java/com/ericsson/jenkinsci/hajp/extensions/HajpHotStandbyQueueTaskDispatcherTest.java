package com.ericsson.jenkinsci.hajp.extensions;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import hudson.ExtensionList;
import hudson.model.Action;
import hudson.model.Queue;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.CauseOfBlockage;
import jenkins.model.Jenkins;
import jenkins.model.TransientActionFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.jenkinsci.hajp.cluster.HajpClusterMembershipStatusProvider;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, HajpClusterMembershipStatusProvider.class})
@PowerMockIgnore("javax.management.*")
public class HajpHotStandbyQueueTaskDispatcherTest {

    private Queue queueMock;
    private Jenkins jenkinsMock;
    private HajpHotStandbyQueueTaskDispatcher dispatcher;

    /**
     * Prepare mocks.
     */
    @Before
    public void setUp() {
        HajpClusterMembershipStatusProvider mockStatusProvider = Mockito.mock(HajpClusterMembershipStatusProvider.class);
        PowerMockito.mockStatic(HajpClusterMembershipStatusProvider.class);
        when(HajpClusterMembershipStatusProvider.getInstance()).thenReturn(mockStatusProvider);
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster()).thenReturn(true);
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmActiveMaster()).thenReturn(false);

        dispatcher = new HajpHotStandbyQueueTaskDispatcher();
        queueMock = mock(Queue.class);
        jenkinsMock = mock(Jenkins.class);
        when(jenkinsMock.getQueue()).thenReturn(queueMock);
        PowerMockito.mockStatic(Jenkins.class);
        when(Jenkins.getInstance()).thenReturn(jenkinsMock);

        ExtensionList<TransientActionFactory> list = mock(ExtensionList.class);
        Iterator<TransientActionFactory> iterator = Collections.emptyIterator();
        when(list.iterator()).thenReturn(iterator);
        when(jenkinsMock.getExtensionList(same(TransientActionFactory.class))).thenReturn(list);

    }

    /**
     * Test that it should block and remove an item when acting as Hot Standby.
     */
    @Test
    public void shouldBlockWhenActingAsHotStandby() {
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster()).thenReturn(true);
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmActiveMaster()).thenReturn(false);
        List<Action> actions = new ArrayList<Action>();
        Queue.Item item = new WaitingItem(Calendar.getInstance(), null, actions);
        CauseOfBlockage cause = dispatcher.canRun(new Queue.BuildableItem((WaitingItem)item));
        assertNotNull("Build is blocked", cause);
    }

    /**
     * Test that it should NOT block when acting as Active Master.
     */
    @Test
    public void shouldNotBlockWhenActingAsActiveMaster() {
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster()).thenReturn(true);
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmActiveMaster()).thenReturn(true);
        List<Action> actions = new ArrayList<Action>();
        Queue.Item item = new WaitingItem(Calendar.getInstance(), null, actions);
        CauseOfBlockage cause = dispatcher.canRun(new Queue.BuildableItem((WaitingItem)item));
        assertNull("Build is NOT blocked", cause);
    }

    /**
     * Test that it should NOT block when acting as Active Master.
     */
    @Test
    public void shouldNotBlockWhenNonInCluster() {
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmActiveMaster()).thenReturn(false);
        Mockito.when(HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster()).thenReturn(false);
        List<Action> actions = new ArrayList<Action>();
        Queue.Item item = new WaitingItem(Calendar.getInstance(), null, actions);
        CauseOfBlockage cause = dispatcher.canRun(new Queue.BuildableItem((WaitingItem)item));
        assertNull("Build is NOT blocked", cause);
    }

}

