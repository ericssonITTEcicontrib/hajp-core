package com.ericsson.jenkinsci.hajp.actors;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import com.google.inject.Injector;
import lombok.Getter;

/**
 * Factory class designed to combine Akka object lifecycle with
 * Guice. Methods below need overriding, please refer to Akka
 * documentation Dependency Injection (Section 3.1) of 2.3.9 Version.
 */
@SuppressWarnings("") public class HajpDependencyInjector implements IndirectActorProducer {

    @Getter private Injector injector;

    /**
     * Create a HajpDependencyInjector with given Guice injector
     *
     * @param inj Guice injector
     */
    public HajpDependencyInjector(Injector inj) {
        injector = inj;
    }

    /**
     * Responsible for producing the Actor.
     */
    @Override public Actor produce() {
        return injector.getInstance(HajpClusterListener.class);
    }

    /**
     * Responsible for providing the Class of the Actor.
     */
    @Override public Class<? extends Actor> actorClass() {
        return HajpClusterListener.class;
    }
}
