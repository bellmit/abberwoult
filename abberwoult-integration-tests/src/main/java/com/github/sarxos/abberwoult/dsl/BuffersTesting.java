package com.github.sarxos.abberwoult.dsl;

import static java.time.Duration.ofMillis;

import java.util.Deque;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import io.vavr.control.Option;


public class BuffersTesting {

	static final class SomeException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public SomeException(Throwable cause) {
			super(cause);
		}
	}

	public static class UntilReceivedMessageOfTestActor extends SimpleActor implements Buffers, Utils {

		final ActorRef probe;

		public UntilReceivedMessageOfTestActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void init() {
			bufferUntilReceivedMessageOf(Integer.class)
				.onSuccess(this::doIntegerStuff)
				.onFailure(this::doErrorStuff)
				.onTimeout(ofMillis(500), this::doTimeoutStuff);
		}

		private void doIntegerStuff(final Integer i) {
			forward(probe, i);
		}

		private void doErrorStuff(final Throwable t) {
			forward(probe, new SomeException(t));
		}

		private void doTimeoutStuff(final Deque<BufferMessage> messages) {
			forward(probe, messages);
		}

		public void onMessage(@Receives final Object message) {
			forward(probe, message);
		}
	}

	public static class UntilReceivedMessageTestActor extends SimpleActor implements Buffers, Utils {

		final ActorRef probe;

		public UntilReceivedMessageTestActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void init() {
			bufferUntilReceivedMessage(333)
				.onSuccess(this::doIntegerStuff)
				.onFailure(this::doErrorStuff)
				.onTimeout(ofMillis(500), this::doTimeoutStuff);
		}

		private void doIntegerStuff(final Integer i) {
			forward(probe, i);
		}

		private void doErrorStuff(final Throwable t) {
			forward(probe, new SomeException(t));
		}

		private void doTimeoutStuff(final Deque<BufferMessage> messages) {
			forward(probe, messages);
		}

		public void onMessage(@Receives final Object message) {
			forward(probe, message);
		}
	}

	public static class UntilTestActor extends SimpleActor implements Buffers, Utils {

		final ActorRef probe;

		public UntilTestActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void init() {
			bufferUntil(this::isMagic)
				.onSuccess(this::doMagicStuff)
				.onFailure(this::doErrorStuff)
				.onTimeout(ofMillis(500), this::doTimeoutStuff);
		}

		private boolean isMagic(final Object message) {
			return message == "m";
		}

		private void doMagicStuff(final Object i) {
			forward(probe, i);
		}

		private void doErrorStuff(final Throwable t) {
			forward(probe, new SomeException(t));
		}

		private void doTimeoutStuff(final Deque<BufferMessage> messages) {
			forward(probe, messages);
		}

		public void onMessage(@Receives final Object message) {
			forward(probe, message);
		}
	}

	public static class UntilDecidedByTestActor extends SimpleActor implements Buffers, Utils {

		class CountingDecider extends Decider<Object> {

			int count = 0;

			// expect 3 integers smaller than 5, then return true to stop buffer

			@Override
			public boolean test(final Object message) {

				final boolean smallerThanFive = Option.of(message)
					.filter(Integer.class::isInstance)
					.map(Integer.class::cast)
					.filter(i -> i < 5)
					.isDefined();

				if (smallerThanFive) {
					count++;
					discard(message);
				}

				return count == 3;
			}
		}

		final ActorRef probe;

		public UntilDecidedByTestActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void init() {
			bufferUntilDecidedBy(new CountingDecider())
				.onSuccess(this::doSuccessStuff)
				.onFailure(this::doErrorStuff)
				.onTimeout(ofMillis(500), this::doTimeoutStuff);
		}

		private void doSuccessStuff(final Object i) {
			forward(probe, "DONE");
		}

		private void doErrorStuff(final Throwable t) {
			forward(probe, new SomeException(t));
		}

		private void doTimeoutStuff(final Deque<BufferMessage> messages) {
			forward(probe, messages);
		}

		public void onMessage(@Receives final Object message) {
			forward(probe, message);
		}
	}

	public static class StackedBuffersTestActor extends SimpleActor implements Buffers, Utils {

		final ActorRef probe;

		public StackedBuffersTestActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void init() {
			bufferUntilReceivedMessage(8).onSuccess(this::doIntegerStuff);
			bufferUntilReceivedMessage(5).onSuccess(this::doIntegerStuff);
			bufferUntilReceivedMessage(3).onSuccess(this::doIntegerStuff);
		}

		private void doIntegerStuff(final Integer i) {
			forward(probe, i);
		}

		public void onMessage(@Receives final Object message) {
			forward(probe, message);
		}
	}

}
