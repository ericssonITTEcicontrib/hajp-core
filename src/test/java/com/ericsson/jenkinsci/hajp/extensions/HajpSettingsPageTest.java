package com.ericsson.jenkinsci.hajp.extensions;

import com.ericsson.jenkinsci.hajp.HajpPlugin;
import com.ericsson.jenkinsci.hajp.cluster.HajpCluster;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class HajpSettingsPageTest {
    @Mock private StaplerRequest mockReq;
    @Mock private StaplerResponse mockRsp;
    @Mock private HajpCluster mockHajpCluster;
    private JSONObject mockJson;

    private HajpSettingsPage unitUnderTest;

    private HajpPlugin hajpPlugin;

    @Before public void setUp() throws Exception {
        hajpPlugin = new HajpPlugin(null);
        HajpPlugin.setHajpCluster(mockHajpCluster);
        assertNotNull(HajpPlugin.getHajpCluster());
        unitUnderTest = new HajpSettingsPage();
        mockJson = new JSONObject();
        JSONObject mockSelf = new JSONObject();
        mockSelf.put("ip", "127.0.0.1");
        mockSelf.put("port", "1");

        mockJson.put("self", mockSelf);
        mockJson.put("orchestrator", mockSelf);
        when(mockReq.getSubmittedForm()).thenReturn(mockJson);
        //when(mockHajpCluster.launch())

    }

    @After public void tearDown() throws Exception {

    }

    @Test public void testDoSubmitClusterMembersWithBlankJson() throws Exception {
        unitUnderTest.doSubmitClusterMembers(mockReq, mockRsp);
        verify(mockHajpCluster).launch("127.0.0.1", 1, "127.0.0.1", 1);
    }
}
