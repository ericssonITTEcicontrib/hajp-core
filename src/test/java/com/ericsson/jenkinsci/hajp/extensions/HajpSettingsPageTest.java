package com.ericsson.jenkinsci.hajp.extensions;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.jenkinsci.hajp.HajpPlugin;
import com.ericsson.jenkinsci.hajp.cluster.HajpCluster;
import com.ericsson.jenkinsci.hajp.cluster.HajpClusterMembershipStatusProvider;
import com.ericsson.jenkinsci.hajp.cluster.Messages;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;

@RunWith(MockitoJUnitRunner.class) public class HajpSettingsPageTest {
    @Mock private StaplerRequest mockReq;
    @Mock private StaplerResponse mockRsp;
    @Mock private HajpCluster mockHajpCluster;
    private JSONObject jsonObject;

    private HajpSettingsPage unitUnderTest;

    private HajpPlugin hajpPlugin;

    @Mock private HajpClusterMembershipStatusProvider mockHajpClusterMembershipStatusProvider;

    @Before public void setUp() throws Exception {
        hajpPlugin = new HajpPlugin(null);
        HajpPlugin.setHajpCluster(mockHajpCluster);
        assertNotNull(HajpPlugin.getHajpCluster());
        unitUnderTest = new HajpSettingsPage();
        jsonObject = new JSONObject();
        JSONObject mockSelf = new JSONObject();
        mockSelf.put("ip", "127.0.0.1");
        mockSelf.put("port", "1");

        jsonObject.put("self", mockSelf);
        jsonObject.put("orchestrator", mockSelf);
        when(mockReq.getSubmittedForm()).thenReturn(jsonObject);
        //when(mockHajpCluster.launch())

        Field clusterField = HajpClusterMembershipStatusProvider.class.getDeclaredField("instance");
        clusterField.setAccessible(true);
        clusterField.set(null, mockHajpClusterMembershipStatusProvider);

    }

    @After public void tearDown() throws Exception {

    }

    @Test public void testDoSubmitClusterMembersWithBlankJson() throws Exception {
        unitUnderTest.doSubmitClusterMembers(mockReq, mockRsp);
        verify(mockHajpCluster).launch("127.0.0.1", 1, "127.0.0.1", 1);
    }

    @Test public void testGetHajpMasterStatusIAmActiveMaster() throws Exception {
        Mockito.when(mockHajpClusterMembershipStatusProvider.isIAmActiveMaster()).thenReturn(true);
        String result = unitUnderTest.getHajpMasterStatus();

        String expected = Messages.cluster_activemaster();

        Mockito.verify(mockHajpClusterMembershipStatusProvider, Mockito.times(1)).isIAmActiveMaster();
        Assert.assertEquals(expected, result);
    }

    @Test public void testGetHajpMasterStatusIAmInCluster() throws Exception {
        Mockito.when(mockHajpClusterMembershipStatusProvider.isIAmActiveMaster()).thenReturn(false);
        Mockito.when(mockHajpClusterMembershipStatusProvider.isIAmInCluster()).thenReturn(true);

        String result = unitUnderTest.getHajpMasterStatus();

        String expected = Messages.cluster_hotstandby();
        Mockito.verify(mockHajpClusterMembershipStatusProvider, Mockito.times(1)).isIAmActiveMaster();
        Mockito.verify(mockHajpClusterMembershipStatusProvider, Mockito.times(1)).isIAmInCluster();
        Assert.assertEquals(expected, result);
    }

    @Test public void testGetHajpMasterStatusOther() throws Exception {
        Mockito.when(mockHajpClusterMembershipStatusProvider.isIAmActiveMaster()).thenReturn(false);
        Mockito.when(mockHajpClusterMembershipStatusProvider.isIAmInCluster()).thenReturn(false);

        String result = unitUnderTest.getHajpMasterStatus();

        String expected = "";
        Mockito.verify(mockHajpClusterMembershipStatusProvider, Mockito.times(1)).isIAmActiveMaster();
        Mockito.verify(mockHajpClusterMembershipStatusProvider, Mockito.times(1)).isIAmInCluster();
        Assert.assertEquals(expected, result);
    }

    @Test public void testGetHajpClusterStatusIAmInCluster() throws Exception {
        Mockito.when(mockHajpClusterMembershipStatusProvider.isIAmInCluster()).thenReturn(true);

        String result = unitUnderTest.getHajpClusterStatus();

        String expected = Messages.cluster_joined();
        Mockito.verify(mockHajpClusterMembershipStatusProvider, Mockito.times(1)).isIAmInCluster();
        Assert.assertEquals(expected, result);
    }

    @Test public void testGetHajpClusterStatusOther() throws Exception {
        Mockito.when(mockHajpClusterMembershipStatusProvider.isIAmInCluster()).thenReturn(false);

        String result = unitUnderTest.getHajpClusterStatus();

        String expected = Messages.cluster_disconnected();
        Mockito.verify(mockHajpClusterMembershipStatusProvider, Mockito.times(1)).isIAmInCluster();
        Assert.assertEquals(expected, result);
    }
}
