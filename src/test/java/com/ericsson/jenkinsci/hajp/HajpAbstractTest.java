package com.ericsson.jenkinsci.hajp;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import com.ericsson.jenkinsci.hajp.actors.HajpClusterListener;
import com.ericsson.jenkinsci.hajp.actors.HajpClusterSender;
import com.ericsson.jenkinsci.hajp.actors.HajpDependencyInjector;
import com.ericsson.jenkinsci.hajp.extensions.HajpClusterExtension;
import com.ericsson.jenkinsci.hajp.messages.HajpMessage;
import com.ericsson.jenkinsci.hajp.messages.orchestration.HotStandbyAssignmentMessage;
import com.google.inject.Guice;
import com.google.inject.Injector;
import hudson.ExtensionList;
import jenkins.model.Jenkins;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Iterator;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This the base class to be extended for unit tests that require to mock the clustering. It uses
 * the HajpTestContext class for Guice injection.
 */
public abstract class HajpAbstractTest {

    protected static ActorSystem system;
    protected static HajpClusterListener actor;
    protected static TestActorRef<HajpClusterListener> ref;

    protected static HajpClusterSender mockSender;

    protected static HajpTestContext context;
    protected static Injector injector;
    protected static Jenkins mockJenkins;

    @BeforeClass public static void setup() {
        try {
            context = new HajpTestContext();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mockJenkins = context.getMockJenkins();
        injector = Guice.createInjector(context);

        ExtensionList<HajpClusterExtension> list = mock(ExtensionList.class);
        Iterator<HajpClusterExtension> iterator = Collections.emptyIterator();
        when(list.iterator()).thenReturn(iterator);
        when(mockJenkins.getExtensionList(same(HajpClusterExtension.class))).thenReturn(list);

        final Props props = Props.create(HajpDependencyInjector.class, injector);
        system = ActorSystem.create();
        ref = TestActorRef.create(system, props, "clusterListenerTest");
        actor = ref.underlyingActor();

        mockSender = new HajpClusterSenderMock(actor);
        HotStandbyAssignmentMessage hsMessage = new HotStandbyAssignmentMessage();
        mockSender.send(hsMessage);
    }

    @AfterClass public static void teardown() {
        actor.postStop();
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    private static class HajpClusterSenderMock extends HajpClusterSender {
        HajpClusterListener clusterListener;

        public HajpClusterSenderMock(HajpClusterListener clusterListener) {
            this.clusterListener = clusterListener;
        }

        @Override public boolean send(HajpMessage message) {
            try {
                clusterListener.onReceive(message);
                return true;
            } catch (IOException | ClassNotFoundException e) {
            }
            return false;
        }
    }

}
