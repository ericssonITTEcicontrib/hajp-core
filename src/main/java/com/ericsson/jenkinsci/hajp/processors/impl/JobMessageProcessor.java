package com.ericsson.jenkinsci.hajp.processors.impl;

import com.ericsson.jenkinsci.hajp.HajpPlugin;
import com.ericsson.jenkinsci.hajp.actors.HajpClusterSender;
import com.ericsson.jenkinsci.hajp.api.BuildsManagementException;
import com.ericsson.jenkinsci.hajp.api.BuildsManager;
import com.ericsson.jenkinsci.hajp.api.JobManagementException;
import com.ericsson.jenkinsci.hajp.api.JobsManager;
import com.ericsson.jenkinsci.hajp.api.files.PreservedFields;
import com.ericsson.jenkinsci.hajp.api.files.XmlUtil;
import com.ericsson.jenkinsci.hajp.cluster.Messages;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.CreateBuildMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.CreateJobMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.DeleteJobMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.RenameJobMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.SendAllJobsMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.UpdateJobMessage;
import com.ericsson.jenkinsci.hajp.processors.JenkinsProcessor;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Actual Job Processing via HAJP-Api
 */
@Log4j2 public class JobMessageProcessor implements JenkinsProcessor {

    @Setter private JobsManager jobsManager;
    @Setter private BuildsManager buildsManager;
    @Setter private HajpClusterSender sender;

    /**
     * Default constructor
     */
    public JobMessageProcessor() {
        this.jobsManager = new JobsManager(Jenkins.getInstance());
        this.buildsManager = new BuildsManager(jobsManager);
        this.sender = new HajpClusterSender();
    }

    /**
     * Constructor
     *
     * @param jenkins the jenkins instance
     */
    public JobMessageProcessor(Jenkins jenkins) {
        this.jobsManager = new JobsManager(jenkins);
        this.buildsManager = new BuildsManager(jobsManager);
        this.sender = new HajpClusterSender();
    }

    /**
     * Replicates the operation carried by the received job message.
     *
     * @param message the job message to be processed
     * @return true if successful, false otherwise
     */
    @Override public boolean process(HajpMessage message) {

        if (message.isValid()) {
            log.info(Messages.jobs_process(message.toString()));
            try {
                switch (message.getOperationType()) {
                    case CREATE_JOB:
                        CreateJobMessage createJobMsg = (CreateJobMessage) message;
                        createJob(createJobMsg.getJobName(), createJobMsg.getXmlFileContents());
                        break;
                    case UPDATE_JOB:
                        UpdateJobMessage updateJobMsg = (UpdateJobMessage) message;
                        updateJob(updateJobMsg.getJobName(), updateJobMsg.getXmlFileContents());
                        break;
                    case RENAME_JOB:
                        RenameJobMessage renameJobMsg = (RenameJobMessage) message;
                        renameJob(renameJobMsg.getJobName(), renameJobMsg.getNewName());
                        break;
                    case DELETE_JOB:
                        DeleteJobMessage deleteJobMsg = (DeleteJobMessage) message;
                        deleteJob(deleteJobMsg.getJobName());
                        break;
                    case DELETE_ALL_JOBS:
                        deleteAllJobs();
                        break;
                    case SEND_ALL_JOBS:
                        SendAllJobsMessage sendMeAllJobsMsg = (SendAllJobsMessage) message;
                        sendAllJobs(sendMeAllJobsMsg.getDestAddress());
                        break;
                    default:
                        return false;
                }
            } catch (JobManagementException e) {
                log.error(
                    Messages.jobs_process_error(e.getMessage(), e.getItemName(), e.getContent()));
                return false;
            } catch (BuildsManagementException e) {
                log.error(Messages.builds_process_build_error(e.getMessage(), e.getJobName(),
                        e.getBuildNumber(), message));
                return false;
            }
            return true;
        }
        log.info(Messages.jobs_process_error_invalid_job(message.toString()));
        return false;
    }

    private void createJob(String jobName, String jobXmlContent) throws JobManagementException {
        if (!jobExists(jobName)) {
            jobsManager.createJob(jobName, jobXmlContent);
            log.debug(Messages.jobs_process_job_created(jobName));
        }
    }

    private void updateJob(String jobName, String jobXmlContent) throws JobManagementException {
        if (jobExists(jobName)) {
            String origJobXmlContent = jobsManager.getJobConfigAsXml(jobsManager.getJob(jobName));

            // Preserved fields functionality on hold right now
            //TODO Dead code either use or remove
            //String newJobXml = mergeXml(origJobXmlContent, jobXmlContent);

            jobsManager.updateJobConfig(jobName, jobXmlContent);
            log.debug(Messages.jobs_process_job_updated(jobName));
        }
    }

    private void renameJob(String jobName, String newName) throws JobManagementException {
        if (jobExists(jobName)) {
            jobsManager.renameJob(jobName, newName);
            log.debug(Messages.jobs_process_job_renamed(jobName, newName));
        }
    }

    private void deleteJob(String jobName) throws JobManagementException {
        if (jobExists(jobName)) {
            jobsManager.deleteJob(jobName);
            log.debug(Messages.jobs_process_job_deleted(jobName));
        }
    }

    private void deleteAllJobs() throws JobManagementException {
        jobsManager.deleteAllJobs();
        log.debug(Messages.jobs_process_all_deleted());
    }

    private void sendAllJobs(String destAddress)
        throws JobManagementException, BuildsManagementException {
        Collection<String> jobNames = jobsManager.getJobNames();
        for (String jobName : jobNames) {
            Item job = jobsManager.getJob(jobName);
            String configXml = jobsManager.getJobConfigAsXml(job);
            CreateJobMessage msg = new CreateJobMessage(jobName, configXml);
            sender.send(destAddress, msg);
            sendAllBuilds(destAddress, jobName);
        }
        log.debug(Messages.jobs_process_all_sent(destAddress));
    }

    private void sendAllBuilds(String destAddress, String jobName)
        throws JobManagementException, BuildsManagementException {
        AbstractProject project = jobsManager.getAbstractProject(jobName);
        RunList<Run> builds = buildsManager.listBuilds(project);
        for (Run build : builds) {
            int buildNumber = build.getNumber();
            byte[] buildBytes = buildsManager.grabBuild(jobName, buildNumber);
            String buildDirName = buildsManager.getTimestampBuildDirName(jobName, buildNumber);
            CreateBuildMessage buildMessage =
                new CreateBuildMessage(jobName, buildNumber, buildDirName, buildBytes);
            sender.send(destAddress, buildMessage);
        }
        log.debug(Messages.jobs_process_all_builds_sent(jobName, destAddress));
    }

    private boolean jobExists(String jobName) {
        return jobsManager.getJobNames().contains(jobName);
    }

    /**
     * Merge 2 job config xml files while preserving fields defined in preservedFields.xml.
     *
     * @param origJobXml
     * @param newJobXml
     * @return the merged job config xml
     * @throws JobManagementException if any
     */
    private String mergeXml(String origJobXml, String newJobXml) throws JobManagementException {
        try {
            Path preservedFieldsFile =
                getPreservedFieldsFile(jobsManager.getJenkins().getRootDir().toPath());
            PreservedFields preservedFields = loadPreservedFieldsFromFile(preservedFieldsFile);
            return XmlUtil
                .mergeXmlByPreservingField(origJobXml, newJobXml, preservedFields.getJobs());
        } catch (Exception e) {
            throw new JobManagementException(Messages.jobs_process_job_update_merge_error());
        }
    }

    /**
     * Load the PreservedFields from preservedFields.xml file. If it fails to load from file, then
     * return a new empty PreservedFields object.
     *
     * @param preservedFieldsFile the path to preservedFields.xml file
     * @return a not-null PreservedFields object
     */
    private PreservedFields loadPreservedFieldsFromFile(Path preservedFieldsFile) {
        try {
            String preservedFieldsXml = new String(Files.readAllBytes(preservedFieldsFile));
            log.debug("File output:"+preservedFieldsXml);
            return XmlUtil.xmlToPreservedFields(preservedFieldsXml);
        } catch (Exception e) {
            return new PreservedFields();
        }
    }

    /**
     * Preserved Fields file is found and loaded as resources.
     *
     * @return the path to preservedFields.xml file
     * @throws IOException
     */
    private Path getPreservedFieldsFile(Path jenkinsRoot) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try{
            return Paths.get(classLoader.getResource(HajpPlugin.PRESERVED_FIELDS_FILENAME).getPath());
        } catch(NullPointerException exception) {
            log.error(exception.getStackTrace());
            return null;
        }
    }
}

