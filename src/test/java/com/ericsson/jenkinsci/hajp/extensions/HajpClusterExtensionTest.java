package com.ericsson.jenkinsci.hajp.extensions;

import com.ericsson.jenkinsci.hajp.HajpPlugin;

import jenkins.model.Jenkins;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import static org.junit.Assert.assertNotNull;

/**
 * This test is designed to test whether a Jenkins plugin is notified
 * of cluster membership status changes.
 */
public class HajpClusterExtensionTest {
    private String FAKE_HOSTNAME = "127.0.0.1";
    private Integer FAKE_PORT = 25000;

    /**
     * An instance of Jenkins Rule.
     */
    @Rule public JenkinsRule jenkinsRule = new JenkinsRule();

    /**
     * @throws Exception
     * @see src/test/features/extensionPoint.feature
     */
    @Test public void testExtensionPoint() throws Exception {
        Thread.sleep(10000);
        Jenkins jenkins = jenkinsRule.getInstance();
        HajpClusterExtension extension =
            jenkins.getExtensionList(HajpClusterExtension.class).get(0);

        assertNotNull(extension);

        HajpPlugin.getHajpCluster().launch(FAKE_HOSTNAME, FAKE_PORT, FAKE_HOSTNAME, FAKE_PORT);
        // Connection without an orchestrator will result in no role assignment
        Thread.sleep(5000);

        HajpPlugin.getHajpCluster().stop();
        // Connection stopping without any prior role assignment should result in
        // no change.
        Thread.sleep(5000);

    }

    /**
     * A {@link com.ericsson.jenkinsci.hajp.extensions.HajpClusterExtension} implementation class.
     * This would be automatically registered to system by @TestExtension annotation.
     */
    @TestExtension public static class HajpClusterExtensionImpl extends HajpClusterExtension {

        @Override public void notifyOnActiveMasterSelection(boolean b) {}

        @Override public void notifyOnClusterJoined() {}

        @Override public void notifyOnClusterDisconnected() {}
    }
}
