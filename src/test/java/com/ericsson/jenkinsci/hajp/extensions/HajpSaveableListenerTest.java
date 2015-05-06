package com.ericsson.jenkinsci.hajp.extensions;

import com.ericsson.jenkinsci.hajp.actors.HajpClusterSender;
import com.ericsson.jenkinsci.hajp.messages.GlobalConfig.SynchronizeGlobalConfigMessage;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.plugins.SynchronizePluginMessage;
import hudson.XmlFile;
import hudson.model.Saveable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class) public class HajpSaveableListenerTest {

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @Mock private HajpClusterSender mockSender;
    @Mock private hudson.model.FreeStyleProject mockProject;
    @Mock private hudson.Plugin mockPlugin;
    @Mock private Saveable mockSaveable;

    private HajpSaveableListener unitUnderTest;
    private Saveable saveableObject;
    private XmlFile xmlFile;

    @Before public void setUp() throws Exception {
        unitUnderTest = new HajpSaveableListener();
        File file = testFolder.newFile();
        xmlFile = new XmlFile(file);
        unitUnderTest.setSender(mockSender);
    }

    @Test public void testConstructor() throws Exception {
        Assert.assertNotNull(unitUnderTest);
    }

    @Test public void testOnChangeFreeStypeProject() throws Exception {
        Mockito.when(mockSender.send(Mockito.any(HajpMessage.class))).thenReturn(false);

        saveableObject = mockProject;
        unitUnderTest.onChange(saveableObject, xmlFile);
        Mockito.verify(mockSender, Mockito.times(0)).send(Mockito.any(HajpMessage.class));
    }

    @Test public void testOnChangePlugin() throws Exception {
        Mockito.when(mockSender.send(Mockito.any(SynchronizePluginMessage.class))).thenReturn(false);

        saveableObject = mockPlugin;
        unitUnderTest.onChange(saveableObject, xmlFile);
        Mockito.verify(mockSender, Mockito.times(1)).send(Mockito.any(SynchronizePluginMessage.class));
    }

    @Test public void testOnChangeSynchronizeGlobal() throws Exception {
        Mockito.when(mockSender.send(Mockito.any(SynchronizeGlobalConfigMessage.class))).thenReturn(false);

        saveableObject = mockSaveable;
        unitUnderTest.onChange(saveableObject, xmlFile);
        Mockito.verify(mockSender, Mockito.times(1)).send(Mockito.any(SynchronizeGlobalConfigMessage.class));
    }
}
