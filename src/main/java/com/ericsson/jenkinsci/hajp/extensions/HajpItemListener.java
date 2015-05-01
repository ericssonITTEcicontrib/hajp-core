package com.ericsson.jenkinsci.hajp.extensions;

import com.ericsson.jenkinsci.hajp.actors.HajpClusterSender;

import com.ericsson.jenkinsci.hajp.api.JobManagementException;
import com.ericsson.jenkinsci.hajp.api.JobsManager;
import com.ericsson.jenkinsci.hajp.cluster.Messages;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.CreateJobMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.DeleteJobMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.RenameJobMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.UpdateJobMessage;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import jenkins.model.Jenkins;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * @see hudson.model.listeners.ItemListener
 */
@Log4j2 @Extension public class HajpItemListener extends ItemListener {

    @Setter private JobsManager jobsManager = new JobsManager(Jenkins.getInstance());
    @Setter private HajpClusterSender sender = new HajpClusterSender();

    /**
     * On creation of a job, a message is sent for replication.
     *
     * @param item the item
     * @see hudson.model.listeners.ItemListener#onCreated(hudson.model.Item)
     */
    @Override public void onCreated(Item item) {
        String jobName = item.getName();

        try {
            String xmlContent = jobsManager.getJobConfigAsXml(item);
            HajpMessage message = new CreateJobMessage(jobName, xmlContent);
            if (sender.send(message)) {
              log.debug(Messages.jobs_listener_on_created(jobName));
            }
        } catch (JobManagementException e) {
            log.error(Messages.jobs_listener_on_created_error(jobName, e.getLocalizedMessage()));
        }
    }

    /**
     * On update of a job, a message is sent for replication.
     *
     * @param item the item
     * @see hudson.model.listeners.ItemListener#onUpdated(hudson.model.Item)
     */
    @Override public void onUpdated(Item item) {
        String jobName = item.getName();

        try {
            String xmlContent = jobsManager.getJobConfigAsXml(item);
            HajpMessage message = new UpdateJobMessage(jobName, xmlContent);
            if (sender.send(message)) {
              log.debug(Messages.jobs_listener_on_updated(jobName));
            }
        } catch (JobManagementException e) {
            log.error(Messages.jobs_listener_on_updated_error(jobName, e.getLocalizedMessage()));
        }
    }

    /**
     * On renaming of a job, a message is sent for replication.
     *
     * @param item    the item
     * @param oldName the old name
     * @param newName the new name
     * @see hudson.model.listeners.ItemListener#onRenamed(hudson.model.Item, String, String)
     */
    @Override public void onRenamed(Item item, String oldName, String newName) {
        HajpMessage message = new RenameJobMessage(oldName, newName);
        if (sender.send(message)) {
          log.debug(Messages.jobs_listener_on_renamed(oldName, newName));
        }
    }

    /**
     * On deletion of a job, a message is sent for replication.
     *
     * @param item the item
     * @see hudson.model.listeners.ItemListener#onDeleted(hudson.model.Item)
     */
    @Override public void onDeleted(Item item) {
        String jobName = item.getName();
        HajpMessage message = new DeleteJobMessage(jobName);
        if (sender.send(message)) {
          log.debug(Messages.jobs_listener_on_deleted(jobName));
        }
    }
}
