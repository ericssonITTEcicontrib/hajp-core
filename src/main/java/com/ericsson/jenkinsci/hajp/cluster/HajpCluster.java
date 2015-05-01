package com.ericsson.jenkinsci.hajp.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.ericsson.jenkinsci.hajp.actors.HajpDependencyInjector;
import com.ericsson.jenkinsci.hajp.exceptions.HajpClusterConfigurationException;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HajpCluster initialization.
 */
@Log4j2 public class HajpCluster implements GenericCluster {

    public static final String CLUSTER_NAME = "HajpCluster";
    public static final String CLUSTER_PROVIDER = "clusterListener";
    public static final int MIN_CLUSTER_MEMBER = 1;
    public static final String UNREACHABLE_TIMEOUT_LENGTH = "10s";
    public static final String LOG_REMOTE_LIFECYCLE_EVENTS = "off";

    // 100 MB maximum message size for akka remote
    public static final String MSG_SIZE = "100000 kilobytes";

    //Cluster roles
    public static final String AKKA_JENKINS_ROLE = "jenkins";
    public static final String AKKA_ORCHESTRATOR_ROLE = "orchestrator";
    public static final String AKKA_FRONTEND_ROLE = "frontend";

    //Akka Cluster Config Map Keys
    private static final String AKKA_OWN_HOSTNAME = "akka.remote.netty.tcp.hostname";
    private static final String AKKA_OWN_PORT = "akka.remote.netty.tcp.port";
    private static final String AKKA_MESSAGE_SIZE = "akka.remote.netty.tcp.maximum-frame-size";
    private static final String AKKA_CLUSTER_SEED_NODES = "akka.cluster.seed-nodes";
    private static final String AKKA_CLUSTER_ROLES = "akka.cluster.roles";
    private static final String AKKA_ROLE_JENKINS_MIN_NBR =
        "akka.cluster.role.jenkins.min-nr-of-members";
    private static final String AKKA_ROLE_ORCHESTRATOR_MIN_NBR =
        "akka.cluster.role.orchestrator.min-nr-of-members";
    private static final String AKKA_AUTO_DOWN_DUR = "akka.cluster.auto-down-unreachable-after";
    private static final String AKKA_REMOTE_TRANSPORT = "akka.remote.enabled-transports";
    private static final String AKKA_LIFECYCLE_LOG = "akka.remote.log-remote-lifecycle-events";


    //Akka Cluster Config Map Values
    private static final String AKKA_TCP_TRANSPORT = "akka.remote.netty.tcp";

    //Orchestrator String Preparer
    private static final String AKKA_ORCHESTRATOR_PREFIX = "akka.tcp://HajpCluster@";

    //Application configuration name
    private static final String APPLICATION_FILE_NAME = "application.conf";


    @Getter private static ActorSystem actorSystem;
    @Getter private static ActorRef actorRef;

    private Injector injector;

    /**
     * Create a HajpCluster with given Guice injector
     *
     * @param injector the Guice injector
     */
    public HajpCluster(Injector injector) {
        this.injector = injector;
    }

    /**
     * Launch HajpCluster by setting configuration.
     * <p>
     * Create an actor system and add our simple cluster listener.
     *
     * @throws IOException If unable to load akka config file
     */
    @SuppressWarnings("all") @Override public void launch(String ownIp, Integer ownPort,
        String orchestratorIp, Integer orchestratorPort)
        throws IOException, HajpClusterConfigurationException {
        if (actorSystem == null && actorRef == null) {
            actorSystem = ActorSystem
                .create(CLUSTER_NAME, getConfig(ownIp, ownPort, orchestratorIp, orchestratorPort));

            // Create an actor that handles cluster domain events
            actorRef = actorSystem
                .actorOf(Props.create(HajpDependencyInjector.class, injector), CLUSTER_PROVIDER);

            log.info("HajpCluster initialized.");
        } else {
            String warnMsg = String.format("Trying to relaunch an already running cluster with "
                    + "OwnIp:%s with ownPort:%d with orchIp:%s with orchPort:%d", ownIp, ownPort,
                orchestratorIp, orchestratorPort);

            log.warn(warnMsg);
            throw new HajpClusterConfigurationException(warnMsg);
        }

    }

    /**
     * Stop HajpCluster by shutting down the actor system and actor ref.
     */
    public void stop() {
        if (actorSystem != null) {
            actorSystem.stop(actorRef);
            actorSystem.shutdown();
        }
    }

    private Config getConfig(String ownIp, Integer ownPort, String orchestratorIp,
        Integer orchestratorPort) throws IOException {
        String orchestratorFullForm =
            AKKA_ORCHESTRATOR_PREFIX + orchestratorIp + ":" + orchestratorPort;
        Map<String, Object> configMap = new HashMap<>();
        List<String> seedNodeList = new ArrayList<>();
        List<String> enabledTransports = new ArrayList<>();
        List<String> rolesList = new ArrayList<>();

        seedNodeList.add(orchestratorFullForm);
        enabledTransports.add(AKKA_TCP_TRANSPORT);
        rolesList.add(AKKA_JENKINS_ROLE);

        //Self IP and port
        configMap.put(AKKA_OWN_HOSTNAME, ownIp);
        configMap.put(AKKA_OWN_PORT, ownPort);

        // Message size
        configMap.put(AKKA_MESSAGE_SIZE, MSG_SIZE);

        //Orchestrator(s) will be the seed-nodes
        configMap.put(AKKA_CLUSTER_SEED_NODES, seedNodeList);

        //Ensuring correct role is registered for cluster member
        configMap.put(AKKA_CLUSTER_ROLES, rolesList);

        //Cluster consistency require at least one jenkins and orchestrator member
        configMap.put(AKKA_ROLE_JENKINS_MIN_NBR, MIN_CLUSTER_MEMBER);
        configMap.put(AKKA_ROLE_ORCHESTRATOR_MIN_NBR, MIN_CLUSTER_MEMBER);

        //Auto-down if unreachable after 10s
        configMap.put(AKKA_AUTO_DOWN_DUR, UNREACHABLE_TIMEOUT_LENGTH);

        // Transport mode and logging of life cycle events
        configMap.put(AKKA_REMOTE_TRANSPORT, enabledTransports);
        configMap.put(AKKA_LIFECYCLE_LOG, LOG_REMOTE_LIFECYCLE_EVENTS);

        String appConfig = getFileContent(APPLICATION_FILE_NAME);
        Config config =
            ConfigFactory.parseMap(configMap).withFallback(ConfigFactory.parseString(appConfig));

        log.info("config:" + config.toString());

        return config;
    }

    private String getFileContent(String filename) throws IOException {
        InputStream applicationConf = this.getClass().getResourceAsStream(filename);
        return IOUtils.toString(applicationConf, "UTF-8");
    }
}
