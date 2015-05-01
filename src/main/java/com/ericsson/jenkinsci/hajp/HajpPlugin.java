package com.ericsson.jenkinsci.hajp;

import com.ericsson.jenkinsci.hajp.api.CredentialsManager;
import com.ericsson.jenkinsci.hajp.cluster.GenericCluster;
import com.ericsson.jenkinsci.hajp.cluster.HajpCluster;
import com.ericsson.jenkinsci.hajp.cluster.Messages;
import com.ericsson.jenkinsci.hajp.filechangedetectors.FileDirectoryWatcher;
import com.google.inject.Guice;
import com.google.inject.Injector;
import hudson.Plugin;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Core class for the plugin.
 * <p>
 * Responsible for initializing, starting up and shutting down plugin
 * and associated resources.
 */
@Log4j2 public class HajpPlugin extends Plugin {
    public static final String PRESERVED_FIELDS_FILENAME = "preservedFields.xml";

    @Setter @Getter private static GenericCluster hajpCluster;
    @Setter @Getter private static Injector injector;
    @Setter @Getter private static Callable fileDirectoryWatcher;
    @Setter @Getter private static ExecutorService executorService;
    @Setter @Getter private static CredentialsManager credentialsManager;

    /**
     * Create a HajpPlugin with default Guice injector
     */
    public HajpPlugin() {
        injector = Guice.createInjector(new HajpContext());
    }

    /**
     * Create a HajpPlugin with given Guice injector
     *
     * @param inj Guice injector
     */
    public HajpPlugin(Injector inj) {
        injector = inj;
    }

    /**
     * Entry point for the plugin.
     * <p>
     * Overridden for log messaging.
     */
    @Override public void start() {
        hajpCluster = new HajpCluster(injector);
        log.debug(Messages.hajp_init_start());
    }

    /**
     * Stop the cluster when the plugin stops
     */
    @Override public void stop() throws Exception {
        super.stop();
        if (hajpCluster != null) {
            hajpCluster.stop();
        }
    }

    @Initializer(after = InitMilestone.JOB_LOADED) public static void initFileWatcher() {
        log.debug("Launching File Listener");
        executorService = Executors.newFixedThreadPool(1);
        executorService.submit(injector.getInstance(FileDirectoryWatcher.class));
    }
}

