package com.ericsson.jenkinsci.hajp.processors.impl;

import com.ericsson.jenkinsci.hajp.api.BuildsManagementException;
import com.ericsson.jenkinsci.hajp.api.BuildsManager;
import com.ericsson.jenkinsci.hajp.api.JobManagementException;
import com.ericsson.jenkinsci.hajp.api.JobsManager;
import com.ericsson.jenkinsci.hajp.cluster.Messages;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.CreateBuildMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.DeleteBuildMessage;
import com.ericsson.jenkinsci.hajp.processors.JenkinsProcessor;
import jenkins.model.Jenkins;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Actual Build Processing via HAJP-API
 */
@Log4j2 public class BuildMessageProcessor implements JenkinsProcessor {

    @Setter private JobsManager jobsManager;
    @Setter private BuildsManager buildsManager;

    /**
     * Default constructor
     */
    public BuildMessageProcessor() {
        this.jobsManager = new JobsManager(Jenkins.getInstance());
        this.buildsManager = new BuildsManager(jobsManager);
    }

    /**
     * Replicates the operation carried by the received build message..
     *
     * @param message the build message to be processed
     * @return true if successful, false otherwise
     */
    @Override public boolean process(HajpMessage message) {

        if (message.isValid()) {
            log.info(Messages.jobs_process(message.toString()));
            try {
                switch (message.getOperationType()) {
                    case CREATE_BUILD:
                        CreateBuildMessage createBuildMsg = (CreateBuildMessage) message;
                        createBuild(createBuildMsg.getJobName(), createBuildMsg.getBuildNumber(),
                            createBuildMsg.getDirName(), createBuildMsg.getFileAsByteArray());
                        break;
                    case DELETE_BUILD:
                        DeleteBuildMessage deleteBuildMsg = (DeleteBuildMessage) message;
                        deleteBuild(deleteBuildMsg.getJobName(), deleteBuildMsg.getBuildNumber());
                        break;
                    default:
                        return false;
                }
            } catch (BuildsManagementException e) {
                log.error(Messages
                    .builds_process_build_error(e.getLocalizedMessage(), e.getJobName(),
                        e.getBuildNumber(), message));
                return false;
            } catch (JobManagementException e) {
                log.error(Messages.jobs_process_error(e.getMessage(), e.getItemName(), message));
                return false;
            }
            return true;
        }
        log.info(Messages.jobs_process_error_invalid_job(message.toString()));
        return false;
    }

    private void createBuild(String jobName, int buildNumber, String buildDirName,
        byte[] fileAsByteArray) throws BuildsManagementException, JobManagementException {

        boolean existed = buildsManager.buildExists(jobName, buildNumber);

        Path buildPath =
            buildsManager.extractBuild(fileAsByteArray, jobName, buildNumber, buildDirName);
        try {
            buildsManager.createFreeStyleBuild(jobName, buildPath);
        } catch (IOException| InterruptedException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        jobsManager.reloadJob(jobName);

        if (existed) {
            log.info(Messages.builds_process_build_create_existing(jobName, buildNumber));
        }
        log.debug(Messages.builds_process_build_created(jobName, buildNumber));
    }

    private void deleteBuild(String jobName, int buildNumber)
        throws BuildsManagementException, JobManagementException {
        if (buildsManager.buildExists(jobName, buildNumber)) {
            buildsManager.deleteFreeStyleBuild(jobName, buildNumber);
            jobsManager.reloadJob(jobName);
            log.debug(Messages.builds_process_build_deleted(jobName, buildNumber));
        }
    }
}

