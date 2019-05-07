package com.github.sarxos.abberwoult;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import akka.event.EventStream;


@Singleton
public class EventsBypass {

	private final EventStream stream;

	@Inject
	public EventsBypass(final ActorSystem system) {
		this.stream = system.getEventStream();
	}

	void bypass(@Observes Object event) {
		stream.publish(event);
	}
}
