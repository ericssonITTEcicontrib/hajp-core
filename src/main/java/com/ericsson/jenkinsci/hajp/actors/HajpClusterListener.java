package com.ericsson.jenkinsci.hajp.actors;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import com.ericsson.jenkinsci.hajp.cluster.HajpCluster;
import com.ericsson.jenkinsci.hajp.cluster.Messages;
import com.ericsson.jenkinsci.hajp.extensions.HajpClusterExtensionProvider;
import com.ericsson.jenkinsci.hajp.extensions.HajpClusterExtensionProvider.EVENT;
import com.ericsson.jenkinsci.hajp.messages.GlobalConfig.AbstractGlobalConfigMessage;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.builds.AbstractBuildMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.AbstractCredentialsMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.CredentialsCreateMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.CredentialsMatchMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.SecretsAndKeysMatchMessage;
import com.ericsson.jenkinsci.hajp.messages.credentials.SecretsAndKeysMessage;
import com.ericsson.jenkinsci.hajp.messages.forward.ForwardToOrchestratorMessage;
import com.ericsson.jenkinsci.hajp.messages.forward.ForwardedMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.AbstractJobMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.DeleteAllJobsMessage;
import com.ericsson.jenkinsci.hajp.messages.jobs.SendAllJobsMessage;
import com.ericsson.jenkinsci.hajp.messages.orchestration.ActiveMasterAssignmentMessage;
import com.ericsson.jenkinsci.hajp.messages.orchestration.HotStandbyAssignmentMessage;
import com.ericsson.jenkinsci.hajp.messages.plugins.AbstractPluginsMessage;
import com.ericsson.jenkinsci.hajp.processors.ClusterProcessor;
import com.ericsson.jenkinsci.hajp.processors.impl.CredentialMessageProcessor;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * HajpCluster Listener designed to integrate all message handling.
 * Part of Akka actors, designed to deal with various member states
 * and cluster messages via message box.
 */
@Log4j2 public class HajpClusterListener extends UntypedActor implements Serializable {

    private static final long serialVersionUID = -2216514826304846417L;

    private static final String JENKINS_ACTOR_PATH = "/user/clusterListener";
    private static final String ORCHESTRATOR_ACTOR_PATH = "/user/orchestratorBackend";

    private transient Cluster cluster = Cluster.get(getContext().system());
    private List<Member> members = new ArrayList<>();

    @Getter private Address orchestratorAddress = null;
    private String activeMasterApi = null;
    private String hotStandbyApi = null;

    @Getter private ActorRef orchestrator = null;

    @Inject private transient ClusterProcessor messageProcessor;
    @Inject private transient HajpClusterExtensionProvider extensionProvider;
    @Inject private transient CredentialMessageProcessor credentialsProcessor;

    /**
     * Business logic method, any and all actor messages are handled here.
     * Message object carries cluster specific events such as unreachablemember
     * or can contain payload sent via different actors.
     *
     * @param message Payload from cluster.
     */
    @Override public void onReceive(final Object message)
        throws IOException, ClassNotFoundException {
        log.info(Messages.cluster_listener_on_received(message));
        if (message instanceof ClusterEvent.MemberUp) {
            Set<String> memberRoles = ((ClusterEvent.MemberUp) message).member().getRoles();
            List<String> memberRolesArr =
                Arrays.asList(HajpCluster.AKKA_JENKINS_ROLE, HajpCluster.AKKA_ORCHESTRATOR_ROLE);
            ClusterEvent.MemberUp mUp = (ClusterEvent.MemberUp) message;
            log.info(Messages.cluster_listener_on_received_member_up(mUp.member()));

            // Only add members that are not self and only send event Joined after
            // actually joining as a member. Only add members that
            // are jenkins instances and orchestrators to members list
            if (!mUp.member().address().equals(cluster.selfAddress()) && memberRoles
                .contains(HajpCluster.AKKA_JENKINS_ROLE) || memberRoles
                .contains(HajpCluster.AKKA_ORCHESTRATOR_ROLE)) {
                if (memberRoles.contains(HajpCluster.AKKA_ORCHESTRATOR_ROLE)) {
                    orchestratorAddress = mUp.member().address();
                    log.info(Messages
                        .cluster_listener_on_received_orchestrator_connected(orchestratorAddress));
                }
                members.add(mUp.member());
            } else {
                extensionProvider.fireClusterEvent(EVENT.JOINED);
            }
        } else if (message instanceof ClusterEvent.UnreachableMember) {
            ClusterEvent.UnreachableMember mUnreachable = (ClusterEvent.UnreachableMember) message;
            log.info(
                Messages.cluster_listener_on_received_member_unreachable(mUnreachable.member()));
        } else if (message instanceof ClusterEvent.MemberRemoved) {
            ClusterEvent.MemberRemoved mRemoved = (ClusterEvent.MemberRemoved) message;
            log.info(Messages.cluster_listener_on_received_member_removed(mRemoved.member()));
            members.remove(mRemoved.member());

            // If self is removed trigger disconnected event and remove all API
            if (mRemoved.member().address().equals(cluster.selfAddress())) {
                extensionProvider.fireClusterEvent(EVENT.DISCONNECTED);
                activeMasterApi = null;
                hotStandbyApi = null;
                orchestrator = null;
                orchestratorAddress = null;
            } else {
                Set<String> memberRoles = mRemoved.member().getRoles();
                // If we lose orchestrator, then we have no cluster!
                if (memberRoles != null && memberRoles
                    .contains(HajpCluster.AKKA_ORCHESTRATOR_ROLE)) {
                    if (mRemoved.member().address().toString()
                        .equals(orchestratorAddress.toString())) {
                        log.warn(Messages.cluster_listener_on_received_orchestrator_disconnected(
                            orchestratorAddress));
                        cluster.unsubscribe(getSelf());
                        extensionProvider.fireClusterEvent(EVENT.DISCONNECTED);
                        activeMasterApi = null;
                        hotStandbyApi = null;
                        orchestrator = null;
                        orchestratorAddress = null;
                    }
                }
            }
        } else if (message instanceof ClusterEvent.MemberEvent) {
            ClusterEvent.MemberEvent mEvent = (ClusterEvent.MemberEvent) message;
            log.info(Messages.cluster_listener_on_received_member_event(mEvent));
        } else if (message instanceof ActiveMasterAssignmentMessage) {
            activeMasterApi = ActiveMasterAssignmentMessage.getACTIVE_MASTER_API_CODE();
            hotStandbyApi = null;
            extensionProvider.fireClusterEvent(EVENT.ACTIVE_MASTER_SELECTED);
            log.debug("Active Master Assignment Completed:" + activeMasterApi);

            // Send Secrets and Keys & Credentials (if credentials exist) to Orchestrator
            log.debug("ActiveMaster Assignment" + sender().path());
            getContext().actorSelection(sender().path())
                .tell(credentialsProcessor.CreateOwnSecretsMsg(), getSelf());
            CredentialsCreateMessage credMsg = credentialsProcessor.CredentialsCreateMessage();
            if (credMsg != null && credMsg.isValid()) {
                getContext().actorSelection(sender().path())
                    .tell(credentialsProcessor.CredentialsCreateMessage(), getSelf());
            }
        } else if (message instanceof HotStandbyAssignmentMessage) {
            hotStandbyApi = HotStandbyAssignmentMessage.getHOT_STANDBY_API_CODE();
            activeMasterApi = null;
            extensionProvider.fireClusterEvent(EVENT.ACTIVE_MASTER_UNSELECTED);
            log.debug("Hot Standby Assignment Completed:" + hotStandbyApi);
            clearJobs();
        } else if (message instanceof SecretsAndKeysMessage) {
            SecretsAndKeysMessage msg = (SecretsAndKeysMessage) message;
            SecretsAndKeysMessage ownSecretsKeys = credentialsProcessor.CreateOwnSecretsMsg();
            if (credentialsProcessor.arrayComp(ownSecretsKeys.getKeysFile(), msg.getKeysFile())) {
                log.debug("Keys Match");
                getContext().actorSelection(sender().path())
                    .tell(new SecretsAndKeysMatchMessage(), getSelf());
            } else {
                messageProcessor.process((HajpMessage) message);
                log.debug("Secrets and Keys do not match");
            }
        } else if (message instanceof CredentialsCreateMessage) {
            // If activeMaster or credentials match, send match msg
            // otherwise process credentials
            log.debug(credentialsProcessor.credentialsComp((CredentialsCreateMessage) message));
            if (isActiveMaster() || credentialsProcessor
                .credentialsComp((CredentialsCreateMessage) message)) {
                getContext().actorSelection(sender().path())
                    .tell(new CredentialsMatchMessage(), getSelf());
            } else {
                processMessage((HajpMessage) message);
            }
        } else if (message instanceof ForwardedMessage) {

            log.debug(Messages.cluster_listener_on_received_forward_message(message));
            // Send message to all Members -- Jenkins instances and orchestrator(s)
            sendMessage((ForwardedMessage) message);
        } else if (message instanceof ForwardToOrchestratorMessage) {
            // Send Message to Orchestrator Only
            getContext().actorSelection(orchestratorAddress + ORCHESTRATOR_ACTOR_PATH)
                .tell(((ForwardToOrchestratorMessage) message).getMessageContent(), getSelf());
        } else if (message instanceof HajpMessage)

        {
            log.debug(Messages.cluster_listener_on_received_process_message(message));
            if (((HajpMessage) message).isValid()) {
                processMessage((HajpMessage) message);
            } else {
                log.error(message + " is invalid");
            }
        } else if (message instanceof byte[])

        {
            log.debug(Messages.cluster_listener_on_received_bytes_message());
        } else

        {
            log.debug(Messages.cluster_listener_on_received_unhandled_message(message));
            unhandled(message);
        }

    }

    /**
     * When listener is stopped, ensure unsubscribed.
     */
    @Override public void postStop() {
        cluster.unsubscribe(getSelf());

        // if at any point cluster role was assigned and never removed
        // trigger disconnect event via extension
        if (activeMasterApi != null || hotStandbyApi != null) {
            extensionProvider.fireClusterEvent(EVENT.DISCONNECTED);
        }
        activeMasterApi = null;
        hotStandbyApi = null;
    }

    /**
     * Subscribe to cluster events.
     * InitialState, MemberEvent and UnreachableMember events.
     */
    @Override public void preStart() {
        log.info(messageProcessor);
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
            ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class,
            ClusterEvent.LeaderChanged.class);
    }

    private boolean allowForward(Member member, String destAddress) {
        if (destAddress == null) {
            return true;
        } else if (destAddress.equals(member.address().toString())) {
            return true;
        }
        return false;
    }

    /**
     * Self clearing all jobs
     */
    private void clearJobs() {
        HajpMessage msg = new DeleteAllJobsMessage();
        messageProcessor.process(msg);
    }

    private boolean isActiveMaster() {
        return activeMasterApi != null;
    }

    private boolean isHotStandby() {
        return hotStandbyApi != null;
    }

    /**
     * Messages are processed <b>only</b> by non-leader member.
     *
     * @param hajpMessage the message object as bytes array
     * @throws java.io.IOException    if failed to read the message object from bytes array
     * @throws ClassNotFoundException if failed to cast the stream object to HajpMessage
     */
    private void processMessage(HajpMessage hajpMessage)
        throws IOException, ClassNotFoundException {
        if (isActiveMaster() && (hajpMessage instanceof SendAllJobsMessage)) {
            messageProcessor.process(hajpMessage);
        } else if (isHotStandby() && (hajpMessage instanceof AbstractJobMessage
            || hajpMessage instanceof AbstractBuildMessage
            || hajpMessage instanceof AbstractPluginsMessage
            || hajpMessage instanceof AbstractCredentialsMessage
            || hajpMessage instanceof AbstractGlobalConfigMessage)) {
            messageProcessor.process(hajpMessage);
        } else if (!isActiveMaster() && hajpMessage instanceof AbstractCredentialsMessage) {
            messageProcessor.process(hajpMessage);
        }
    }

    /**
     * Messages are <b>only</b> sent from the leader. Member to member sending is not allowed.
     *
     * @param message the forwarded message that carry the message to be sent
     * @throws java.io.IOException if failed to write the message object into bytes array
     */
    private void sendMessage(ForwardedMessage message) throws IOException {
        if (!isActiveMaster()) {
            return;
        }

        for (Member member : members) {
            if (allowForward(member, message.getDestAddress())) {
                log.debug(Messages.cluster_listener_on_received_forward_message_to_member(message,
                    member.address()));
                getContext().actorSelection(member.address() + JENKINS_ACTOR_PATH)
                    .tell(message.getMessageContent(), getSender());
            }
        }
    }
}
