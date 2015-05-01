package com.ericsson.jenkinsci.hajp.actors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;

import com.ericsson.jenkinsci.hajp.HajpAbstractTest;
import com.ericsson.jenkinsci.hajp.messages.orchestration.ActiveMasterAssignmentMessage;
import com.ericsson.jenkinsci.hajp.messages.orchestration.HotStandbyAssignmentMessage;
import com.ericsson.jenkinsci.hajp.processors.impl.ClusterMessageProcessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import java.lang.reflect.Field;

@RunWith(MockitoJUnitRunner.class) public class HajpClusterListenerTest extends HajpAbstractTest {

    @Mock ClusterEvent.MemberRemoved mockClusterMemberRemovedMsg;
    @Mock ClusterEvent.MemberUp mockClusterMemberUpMsg;
    @Mock ClusterEvent.MemberEvent mockMemberEvent;
    @Mock ClusterEvent.UnreachableMember mockUnreachableEvent;
    @Mock ClusterEvent.LeaderChanged mockLeaderChangedEvent;
    @Mock Member mockClusterMember;
    @Mock Cluster mockHajpCluster;
    @Mock ClusterMessageProcessor mockMsgProcessor;

    ActiveMasterAssignmentMessage mockMasterAssignmentMsg;
    HotStandbyAssignmentMessage mockHotStandbyAssignmentMsg;

    @Before public void mockBehaviour() throws NoSuchFieldException, IllegalAccessException {
        Address myAddress = new Address("mock", "mock");
        when(mockHajpCluster.selfAddress()).thenReturn(myAddress);

        // Private field mocking as normal DI methods failed due to hudson plugin lifecycle
        // restrictions.
        Field clusterField = HajpClusterListener.class.getDeclaredField("cluster");
        clusterField.setAccessible(true);
        clusterField.set(actor, mockHajpCluster);

        Field processorField = HajpClusterListener.class.getDeclaredField("messageProcessor");
        processorField.setAccessible(true);
        processorField.set(actor, mockMsgProcessor);

        when(mockClusterMember.address()).thenReturn(myAddress);
        when(mockClusterMemberUpMsg.member()).thenReturn(mockClusterMember);
        when(mockClusterMemberRemovedMsg.member()).thenReturn(mockClusterMember);

        mockMasterAssignmentMsg = new ActiveMasterAssignmentMessage();
        mockHotStandbyAssignmentMsg = new HotStandbyAssignmentMessage();
    }

    @Test public void testPreStart() throws Exception {
        actor.preStart();
    }

    @Test public void testMemberAddedRemovedMessageReceive() throws Exception {
        akka.pattern.Patterns.ask(ref, mockClusterMemberUpMsg, 3000);
        akka.pattern.Patterns.ask(ref, mockClusterMemberRemovedMsg, 3000);
    }

    @Test public void testMemberUnreachable() throws Exception {
        akka.pattern.Patterns.ask(ref, mockUnreachableEvent, 3000);
    }

    @Test public void testMemberEvent() throws Exception {
        akka.pattern.Patterns.ask(ref, mockMemberEvent, 3000);
    }

    @Test public void testMasterAssignmentMsg() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(mockMasterAssignmentMsg);
        byte[] msgBytes = bos.toByteArray();
        akka.pattern.Patterns.ask(ref, msgBytes, 3000);
    }

    @Test public void testHotStandbyAssignmentMsg() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(mockHotStandbyAssignmentMsg);
        byte[] msgBytes = bos.toByteArray();
        akka.pattern.Patterns.ask(ref, msgBytes, 3000);
    }
}
