package com.github.sarxos.abberwoult.dsl;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Scheduler;
import io.vavr.control.Option;
import scala.concurrent.ExecutionContextExecutor;


public interface Utils extends ActorInternal {

	/**
	 * Send message to a given actor reference.
	 *
	 * @param ref the actor reference
	 * @param message the message
	 */
	default void tell(final ActorRef ref, final Object message) {
		ref.tell(message, self());
	}

	/**
	 * Send message to a given actor selection.
	 *
	 * @param sel the actor selection
	 * @param message the message
	 */
	default void tell(final ActorSelection sel, final Object message) {
		sel.tell(message, self());
	}

	/**
	 * Optionally send message to the given actor reference. This method will send message to actor
	 * reference if and only if {@link Option} has some value. Otherwise it will do nothing.
	 *
	 * @param opt the optional {@link ActorRef}
	 * @param message the message to be send
	 */
	default void tell(final Option<ActorRef> opt, final Object message) {
		opt.forEach(ref -> tell(ref, message));
	}

	/**
	 * Reply given message to the sender.
	 *
	 * @param message the message
	 */
	default void reply(final Object message) {
		tell(sender(), requireNonNull(message, "Reply message must not be null!"));
	}

	/**
	 * Forwards message to the given actor reference.
	 *
	 * @param ref the actor reference
	 * @param message the message to be forwarded
	 */
	default void forward(final ActorRef ref, final Object message) {
		ref.forward(message, getContext());
	}

	/**
	 * Forwards message to the supplied actor reference.
	 *
	 * @param ref the actor reference supplier
	 * @param message the message to be forwarded
	 */
	default void forward(final Supplier<ActorRef> ref, final Object message) {
		forward(ref.get(), message);
	}

	/**
	 * Forwards message to the supplied actor reference.
	 *
	 * @param opt the optional actor reference
	 * @param message the message to be forwarded
	 */
	default void forward(final Option<ActorRef> opt, final Object message) {
		opt.forEach(ref -> forward(ref, message));
	}

	/**
	 * Forwards message to the given actor reference.
	 *
	 * @param sel the actor selection
	 * @param message the message to be forwarded
	 */
	default void forward(final ActorSelection sel, final Object message) {
		sel.forward(message, getContext());
	}

	default ExecutionContextExecutor dispatcher() {
		return getContext().dispatcher();
	}

	default Scheduler scheduler() {
		return getContext().system().getScheduler();
	}
}
