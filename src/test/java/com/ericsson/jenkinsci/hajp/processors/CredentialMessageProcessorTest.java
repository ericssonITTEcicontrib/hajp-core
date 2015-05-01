package com.ericsson.jenkinsci.hajp.processors;


import com.ericsson.jenkinsci.hajp.actors.HajpClusterSender;
import com.ericsson.jenkinsci.hajp.api.CredentialsManager;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.DeleteBuildMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.CredentialsCreateMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.SecretsAndKeysMessage;
import com.ericsson.jenkinsci.hajp.processors.impl.CredentialMessageProcessor;
import jenkins.model.Jenkins;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class) public class CredentialMessageProcessorTest {

    @Mock private static Jenkins mockJenkins;
    private CredentialMessageProcessor unitUnderTest;
    private HajpMessage hajpMessage;
    @Mock private HajpMessage mockHajpMessage;

    @Mock private CredentialsManager mockCredentialsManager;
    @Mock private HajpClusterSender mockSender;

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @Before public void setUp() throws Exception {

        Mockito.when(mockJenkins.getRootDir()).thenReturn(testFolder.getRoot());
        unitUnderTest = new CredentialMessageProcessor(mockJenkins);
        unitUnderTest.setCredentialsManager(mockCredentialsManager);
        unitUnderTest.setSender(mockSender);
    }

    @After public void tearDown() throws Exception {

    }

    @Test public void testConstructor() throws Exception {
        Assert.assertNotNull(unitUnderTest);
    }


    @Test public void testCreateOwnSecretsMsg() throws Exception {
        byte[] expectedPackSecretKey = "packSecretKey".getBytes();
        byte[] expectedPackSecretsDir = "packSecretsDir".getBytes();
        Mockito.when(mockCredentialsManager.packSecretKey()).thenReturn(expectedPackSecretKey);
        Mockito.when(mockCredentialsManager.packSecretsDir()).thenReturn(expectedPackSecretsDir);

        SecretsAndKeysMessage secretsAndKeysMessage = unitUnderTest.CreateOwnSecretsMsg();

        Mockito.verify(mockCredentialsManager, Mockito.times(1)).packSecretKey();
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).packSecretsDir();
        Assert.assertNotNull(secretsAndKeysMessage);
        Assert.assertArrayEquals(expectedPackSecretKey, secretsAndKeysMessage.getKeysFile());
        Assert.assertArrayEquals(expectedPackSecretsDir, secretsAndKeysMessage.getSecretsFile());
    }

    @Test(expected = IOException.class) public void testCreateOwnSecretsMsgWithIOException()
        throws Exception {
        Mockito.when(mockCredentialsManager.packSecretKey())
            .thenThrow(new IOException("testCreateOwnSecretsMsgWithIOException"));
        unitUnderTest.CreateOwnSecretsMsg();
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).packSecretKey();
        Assert.fail("should throw an IOException here");
    }


    @Test public void testArrayCompReturnTrue() throws Exception {
        Mockito.when(mockCredentialsManager.compareZip(null, null)).thenReturn(true);
        boolean result = unitUnderTest.arrayComp(null, null);
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).compareZip(null, null);
        Assert.assertEquals(true, result);
    }

    @Test public void testArrayCompReturnFalse() throws Exception {
        Mockito.when(mockCredentialsManager.compareZip(null, null)).thenReturn(false);
        boolean result = unitUnderTest.arrayComp(null, null);
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).compareZip(null, null);
        Assert.assertEquals(false, result);
    }

    @Test public void testArrayCompWithIOException() throws Exception {
        Mockito.when(mockCredentialsManager.compareZip(null, null))
            .thenThrow(new IOException("testArrayCompWithIOException"));
        boolean result = unitUnderTest.arrayComp(null, null);
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).compareZip(null, null);
        Assert.assertEquals(false, result);
    }

    @Test public void testCredentialsCompReturnTrue() throws Exception {
        Mockito.when(mockCredentialsManager.compareCredentials(null))
            .thenReturn(true);
        CredentialsCreateMessage credMsg = new CredentialsCreateMessage(null, null);
        boolean result = unitUnderTest.credentialsComp(credMsg);
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).compareCredentials(null);
        Assert.assertEquals(true, result);
    }

    @Test public void testCredentialsCompReturnFalse() throws Exception {
        Mockito.when(mockCredentialsManager.compareCredentials(null))
            .thenReturn(false);
        CredentialsCreateMessage credMsg = new CredentialsCreateMessage(null, null);
        boolean result = unitUnderTest.credentialsComp(credMsg);
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).compareCredentials(null);
        Assert.assertEquals(false, result);
    }

    @Test public void testCredentialsCreateMessage() throws Exception {
        byte[] expectedCredentialsFile = "credentialsFile".getBytes();
        byte[] expectedEmbeddedSecrets = "embeddedSecrets".getBytes();

        Mockito.when(mockCredentialsManager.packCredentials()).thenReturn(expectedCredentialsFile);
        Mockito.when(mockCredentialsManager.packSecretsDir()).thenReturn(expectedEmbeddedSecrets);

        CredentialsCreateMessage msg = unitUnderTest.CredentialsCreateMessage();

        Mockito.verify(mockCredentialsManager, Mockito.times(1)).packCredentials();
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).packSecretsDir();
        Assert.assertArrayEquals(expectedCredentialsFile, msg.getCredentialsFile());
        Assert.assertArrayEquals(expectedEmbeddedSecrets, msg.getEmbeddedSecrets());
    }

    @Test public void testCredentialsCreateMessageWithException() throws Exception {
        Mockito.when(mockCredentialsManager.packCredentials())
            .thenThrow(new IOException("testCredentialsCreateMessageWithException"));

        CredentialsCreateMessage msg = unitUnderTest.CredentialsCreateMessage();

        Mockito.verify(mockCredentialsManager, Mockito.times(1)).packCredentials();
        Mockito.verify(mockCredentialsManager, Mockito.times(0)).packSecretsDir();
        Assert.assertNull(msg);
    }

    @Test public void testProcessUnknownMessage() throws Exception {
        String jobName = "jobName";
        int buildNumber = 0;
        hajpMessage = new DeleteBuildMessage(jobName, buildNumber);
        boolean result = unitUnderTest.process(hajpMessage);
        Assert.assertEquals(false, result);
    }

    @Test public void testProcessSecretsAndKeysMessage() throws Exception {

        Mockito.doNothing().when(mockCredentialsManager).unpackSecretDir(null);
        Mockito.doNothing().when(mockCredentialsManager).unpackSecretKeys(null);
        Mockito.doNothing().when(mockCredentialsManager).restartJenkins();

        hajpMessage = new SecretsAndKeysMessage(null, null);

        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockCredentialsManager, Mockito.times(1)).unpackSecretDir(null);
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).unpackSecretKeys(null);
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).restartJenkins();
        Assert.assertEquals(true, result);
    }

    @Test public void testProcessCredentialsCreateMessage() throws Exception {

        Mockito.doNothing().when(mockCredentialsManager).unpackCredentials(null);
        Mockito.doNothing().when(mockCredentialsManager).unpackSecretDir(null);
        Mockito.doNothing().when(mockCredentialsManager).restartJenkins();

        hajpMessage = new CredentialsCreateMessage(null, null);
        boolean result = unitUnderTest.process(hajpMessage);

        Mockito.verify(mockCredentialsManager, Mockito.times(1)).unpackCredentials(null);
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).unpackSecretDir(null);
        Mockito.verify(mockCredentialsManager, Mockito.times(1)).restartJenkins();
        Assert.assertEquals(true, result);
    }

}

