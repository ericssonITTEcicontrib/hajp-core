package com.ericsson.jenkinsci.hajp.extensions;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.ExtensionList;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Iterator;

/**
 * Created by ehongka on 4/30/15.
 */
@RunWith(MockitoJUnitRunner.class) public class HajpClusterExtensionProviderTest {

    @Mock private Jenkins mockJenkins;
    private HajpClusterExtensionProvider unitUnderTest;

    @Before public void setup() throws Exception {
        unitUnderTest = new HajpClusterExtensionProvider(mockJenkins);
        //Mockito.when(mockJenkins.getRootDir()).thenReturn(testFolder.getRoot());
        // could not mock: HajpClusterMembershipStatusProvider
        //
        ExtensionList<HajpClusterExtension> list = mock(ExtensionList.class);
        Iterator<HajpClusterExtension> iterator = Collections.emptyIterator();
        when(list.iterator()).thenReturn(iterator);
        when(mockJenkins.getExtensionList(same(HajpClusterExtension.class))).thenReturn(list);
        // jenkins.getExtensionList(HajpClusterExtension.class);
    }

    @Test public void testFireClusterEvent() throws Exception {

    }
}
