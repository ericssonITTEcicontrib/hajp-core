package com.ericsson.jenkinsci.hajp.actors;

import akka.actor.ActorRef;
import com.ericsson.jenkinsci.hajp.cluster.HajpCluster;
import com.ericsson.jenkinsci.hajp.cluster.HajpClusterMembershipStatusProvider;
import com.ericsson.jenkinsci.hajp.cluster.Messages;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.forward.ForwardToOrchestratorMessage;
import com.ericsson.jenkinsci.hajp.messages.forward.ForwardedMessage;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * A handy sender class for send message to the cluster using the actor ref
 */
@Log4j2 public class HajpClusterSender {

    @Setter private ActorRef actorRef;

    public HajpClusterSender() {
        actorRef = HajpCluster.getActorRef();
    }

    /**
     * Send a message to the cluster.
     *
     * @param message the message to be sent
     */
    public boolean send(HajpMessage message) {
        log.debug(Messages.cluster_sender_send(message));

        if (HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster()) {
            initializeActorRef();
            actorRef.tell(new ForwardedMessage(message), HajpCluster.getActorRef());
            return true;
        }
        return false;
    }

    /**
     * Send a message to the cluster.
     *
     * @param destAddress the address of the destination member
     * @param message     the message to be sent
     */
    public boolean send(String destAddress, HajpMessage message) {
        log.debug(Messages.cluster_sender_send(message));

        if (HajpClusterMembershipStatusProvider.getInstance().isIAmInCluster()) {
            initializeActorRef();
            actorRef.tell(new ForwardedMessage(destAddress, message), HajpCluster.getActorRef());
            return true;
        }
        return false;
    }

    public boolean sendToOrchestrator(HajpMessage message) {
        log.debug(Messages.cluster_sender_send(message));

        if (HajpClusterMembershipStatusProvider.getInstance().isIAmActiveMaster()) {
            initializeActorRef();
            actorRef.tell(new ForwardToOrchestratorMessage(message), HajpCluster.getActorRef());
            return true;
        } else {
            log.debug(
                "Message did not send to Orchestrator because I am not Active Master:" + message);
        }
        return false;
    }

    private void initializeActorRef() {
        //Delayed actorRef initiation creates problems with instantiation
        if (actorRef == null) {
            actorRef = HajpCluster.getActorRef();
        }
    }
}
