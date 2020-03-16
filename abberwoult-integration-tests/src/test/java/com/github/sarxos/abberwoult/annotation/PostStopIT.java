package com.github.sarxos.abberwoult.annotation;

import static akka.actor.ActorRef.noSender;
import static com.github.sarxos.abberwoult.deployment.ActorLifecycleRegistry.getPostStopsFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.annotation.PostStopTesting.PostStopActorImplementingInterface;
import com.github.sarxos.abberwoult.annotation.PostStopTesting.PostStopActorSubClassWithOverridenBinding;
import com.github.sarxos.abberwoult.annotation.PostStopTesting.PostStopActorSubClassWithSingleBinding;
import com.github.sarxos.abberwoult.annotation.PostStopTesting.PostStopActorWithMultipleBindings;
import com.github.sarxos.abberwoult.annotation.PostStopTesting.PostStopActorWithSingleBinding;
import com.github.sarxos.abberwoult.annotation.PostStopTesting.PostStopInterfaceWithSingleBinding;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class PostStopIT {

	@Inject
	ActorUniverse universe;

	@Test
	public void test_ifCorrectNumberOfPostStopsIsRegistered() {
		assertThat(getPostStopsFor(PostStopActorWithSingleBinding.class)).hasSize(1);
		assertThat(getPostStopsFor(PostStopActorWithMultipleBindings.class)).hasSize(2);
		assertThat(getPostStopsFor(PostStopActorSubClassWithSingleBinding.class)).hasSize(1);
		assertThat(getPostStopsFor(PostStopActorSubClassWithOverridenBinding.class)).hasSize(1);
		assertThat(getPostStopsFor(PostStopActorImplementingInterface.class)).hasSize(1);
	}

	@Test
	public void test_ifSinglePostStopIsInvoked() {

		final AtomicBoolean teardowned = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(PostStopActorWithSingleBinding.class)
			.withArguments(teardowned)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(teardowned);
	}

	@Test
	public void test_ifMultiplePostStopsAreInvoked() {

		final AtomicBoolean teardowned1 = new AtomicBoolean(false);
		final AtomicBoolean teardowned2 = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(PostStopActorWithMultipleBindings.class)
			.withArguments(teardowned1, teardowned2)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(teardowned1);
		await().untilTrue(teardowned2);
	}

	@Test
	public void test_ifSinglePostStopIsInvokedInSuperclass() {

		final AtomicBoolean teardowned = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(PostStopActorSubClassWithSingleBinding.class)
			.withArguments(teardowned)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(teardowned);
	}

	@Test
	public void test_ifSinglePostStopIsInvokedWhenOverride() {

		final AtomicBoolean teardowned1 = new AtomicBoolean(false);
		final AtomicBoolean teardowned2 = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(PostStopActorSubClassWithOverridenBinding.class)
			.withArguments(teardowned1, teardowned2)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(teardowned2);

		assertThat(teardowned1).isFalse();
	}

	@Test
	public void test_ifSinglePostStopIsInvokedInInterface() {

		PostStopInterfaceWithSingleBinding.teardowned.set(false);

		final ActorRef ref = universe.actor()
			.of(PostStopActorImplementingInterface.class)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(PostStopInterfaceWithSingleBinding.teardowned);
	}
}
