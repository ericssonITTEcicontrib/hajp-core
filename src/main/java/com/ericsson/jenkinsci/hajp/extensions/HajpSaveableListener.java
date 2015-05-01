package com.ericsson.jenkinsci.hajp.extensions;

import com.ericsson.jenkinsci.hajp.actors.HajpClusterSender;
import com.ericsson.jenkinsci.hajp.api.PluginsManager;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.plugins.SynchronizePluginMessage;
import com.google.common.io.Files;

import hudson.Plugin;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import jenkins.model.Jenkins;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

/**
 * @see hudson.model.listeners.SaveableListener
 */
@Log4j2 public class HajpSaveableListener extends SaveableListener {
    @Setter private PluginsManager pluginsManager = new PluginsManager(Jenkins.getInstance());
    @Setter private HajpClusterSender sender = new HajpClusterSender();


    /**
     * This method is called when a change to a {@link hudson.model.Saveable} Object is made
     * and it is about to be persisted.
     * <p/>
     * <p/>
     * This listener gets called for all Saveable Objects which means that there
     * is no way of telling them apart except checking if the Saveable Object is
     * an instance of the type we are interesting in.
     * <p/>
     * This listener is used for taking action on events regarding jobs, users,
     * and changes made to the Jenkins global configuration.
     *
     * @param saveableObject The Saveable instance that is about to be persisted.
     * @param file           The {@link hudson.XmlFile} which represents the persisted form of the
     *                       Saveable instance.
     * @see hudson.model.listeners.SaveableListener#onChange(hudson.model.Saveable
     *, hudson.XmlFile)
     */
    @Override public void onChange(Saveable saveableObject, XmlFile file) {
        if (saveableObject instanceof Plugin) {
            Plugin plugin = (Plugin) saveableObject;
            String pluginName = plugin.getWrapper().getDisplayName();
            try {
                HajpMessage message =
                    new SynchronizePluginMessage(pluginName, Files.toByteArray(file.getFile()));
                sender.send(message);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
