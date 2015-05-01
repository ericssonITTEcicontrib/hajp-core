package com.ericsson.jenkinsci.hajp;

import static org.mockito.Mockito.verify;

import com.ericsson.jenkinsci.hajp.cluster.GenericCluster;
import com.ericsson.jenkinsci.hajp.cluster.HajpCluster;
import com.google.inject.Guice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;

import jenkins.model.Jenkins;

@RunWith(MockitoJUnitRunner.class) public class HajpPluginTest extends HajpAbstractTest{

    private HajpPlugin unitUnderTest;

    @Mock private HajpCluster hajpCluster;

    @Mock private GenericCluster mockCluster;

    @Before public void setUp() throws Exception {
        unitUnderTest = new HajpPlugin(Guice.createInjector(new HajpTestContext()));
        setMockHajpCluster();
    }

    /**
     * Private field mocking as normal DI methods failed due to hudson plugin lifecycle restrictions.
     * @throws Exception if encountered
     */
    private void setMockHajpCluster() throws Exception {
        Field clusterField = HajpPlugin.class.getDeclaredField("hajpCluster");
        clusterField.setAccessible(true);
        clusterField.set(unitUnderTest, mockCluster);
    }

    @Test public void testStart() {
        unitUnderTest.start();
    }
}
