package com.ericsson.jenkinsci.hajp.processors.impl;

import com.ericsson.jenkinsci.hajp.actors.HajpClusterSender;
import com.ericsson.jenkinsci.hajp.api.CredentialsManager;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.CredentialsCreateMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.SecretsAndKeysMessage;
import com.ericsson.jenkinsci.hajp.processors.JenkinsProcessor;
import com.google.inject.Inject;
import hudson.lifecycle.RestartNotSupportedException;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jvnet.hudson.reactor.ReactorException;

import java.io.IOException;
import java.util.Random;

/**
 * This processor serves dual purpose of processing other's secrets and credentials
 * as well as producing and matching with own Jenkins instance.
 */
@Log4j2 public class CredentialMessageProcessor implements JenkinsProcessor {
    private static Random rand = new Random();
    @Setter @Getter private CredentialsManager credentialsManager;
    @Setter @Getter private HajpClusterSender sender;

    /**
     * Default constructor
     */
    @Inject
    public CredentialMessageProcessor(Jenkins jenkins) {
        this.credentialsManager = new CredentialsManager(jenkins);
        this.sender = new HajpClusterSender();
    }

    /**
     * @return Secret keys and secret dir in message
     * @throws IOException
     */
    public SecretsAndKeysMessage CreateOwnSecretsMsg() throws IOException {
        return new SecretsAndKeysMessage(credentialsManager.packSecretKey(),
            credentialsManager.packSecretsDir());
    }

    /**
     * Compares two zip files
     * @param arr1 byte array 1
     * @param arr2 byte array 2
     * @return true if comparison match
     */
    public boolean arrayComp(byte[] arr1, byte[] arr2) {
        try {
            return credentialsManager.compareZip(arr1, arr2);
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return false;
    }

    /**
     * Compare credentials message with local copy
     * @param credMsg received credentials copy
     * @return true if comparison match
     */
    public boolean credentialsComp(CredentialsCreateMessage credMsg) {
        return credentialsManager.compareCredentials(credMsg.getCredentialsFile());
    }

    /**
     * Creates a credentials message with embedded secrets dir
     * @return a new CredentialsMessage
     */
    public CredentialsCreateMessage CredentialsCreateMessage(){
        try {
            return new CredentialsCreateMessage(credentialsManager.packCredentials(), credentialsManager.packSecretsDir());
        } catch (IOException|ReactorException|InterruptedException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    /**
     * Process credentials message based on inheritance
     * @param hajpMessage the hajp message to be processed
     * @return correctly processed or not
     */
    @Override public boolean process(HajpMessage hajpMessage) {
        if (hajpMessage instanceof SecretsAndKeysMessage) {
            unpackSecretDirKeys((SecretsAndKeysMessage) hajpMessage);
            return true;
        }

        if (hajpMessage instanceof CredentialsCreateMessage) {
            unpackCredentials((CredentialsCreateMessage) hajpMessage);
            return true;
        }

        return false;
    }

    private void unpackSecretDirKeys(SecretsAndKeysMessage hajpMessage) {
        try {
            credentialsManager.unpackSecretDir(hajpMessage.getSecretsFile());
            credentialsManager.unpackSecretKeys(hajpMessage.getKeysFile());
            credentialsManager.restartJenkins();
        } catch (IOException | RestartNotSupportedException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }


    private void unpackCredentials(CredentialsCreateMessage hajpMessage) {
        try {
            credentialsManager.unpackCredentials(hajpMessage.getCredentialsFile());
            credentialsManager.unpackSecretDir(hajpMessage.getEmbeddedSecrets());
            credentialsManager.restartJenkins();
        } catch (IOException | InterruptedException | ReactorException | RestartNotSupportedException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
