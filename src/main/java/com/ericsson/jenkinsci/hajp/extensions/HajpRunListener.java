package com.ericsson.jenkinsci.hajp.extensions;

import com.ericsson.jenkinsci.hajp.actors.HajpClusterSender;
import com.ericsson.jenkinsci.hajp.api.BuildsManagementException;
import com.ericsson.jenkinsci.hajp.api.BuildsManager;
import com.ericsson.jenkinsci.hajp.api.JobsManager;
import com.ericsson.jenkinsci.hajp.cluster.Messages;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.CreateBuildMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.DeleteBuildMessage;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * @see hudson.model.listeners.RunListener
 */
@Log4j2 @Extension public class HajpRunListener extends RunListener<Run<?, ?>> {

    @Setter private JobsManager jobsManager = new JobsManager(Jenkins.getInstance());
    @Setter private BuildsManager buildsManager = new BuildsManager(jobsManager);
    @Setter private HajpClusterSender sender = new HajpClusterSender();

    /**
     * On completion of a new build, a create build message is sent for replication.
     *
     * @param run the build
     */
    @Override public void onFinalized(Run<?, ?> run) {
        Job job = run.getParent();
        String jobName = job.getName();
        int buildNumber = run.getNumber();

        try {
            byte[] fileAsByteArray = buildsManager.grabBuild(jobName, buildNumber);
            String dirName = buildsManager.getTimestampBuildDirName(jobName, buildNumber);
            HajpMessage message =
                new CreateBuildMessage(jobName, buildNumber, dirName, fileAsByteArray);
            if (sender.send(message)) {
              log.debug(Messages.builds_listener_on_finalized(jobName, buildNumber));
            }
        } catch (BuildsManagementException e) {
            log.error(Messages
                .builds_listener_on_finalized_error(jobName, buildNumber, e.getLocalizedMessage()));
        }
    }

    /**
     * On deletion of a build, a delete build message is sent for replication
     *
     * @param run
     */
    @Override public void onDeleted(Run<?, ?> run) {
        Job job = run.getParent();
        String jobName = job.getName();
        int buildNumber = run.getNumber();

        HajpMessage message = new DeleteBuildMessage(jobName, run.getNumber());
        if (sender.send(message)) {
          log.debug(Messages.builds_listener_on_deleted(jobName, buildNumber));
        }
      }
}
