package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getLabel;
import static com.github.sarxos.abberwoult.util.ActorUtils.DEFAULT_TIMEOUT_PROP;
import static com.github.sarxos.abberwoult.util.ActorUtils.DEFAULT_TIMEOUT_SECONDS;
import static com.github.sarxos.abberwoult.util.ActorUtils.durationOf;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.github.sarxos.abberwoult.annotation.Labeled;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterSharding;
import akka.util.Timeout;


/**
 * This is factory bean which create {@link Shard} instances which were annotated with
 * {@link Labeled} annotation. A {@link Labeled} annotation acts as a name of shard to be injected.
 * The resultant {@link Shard} is {@link Askable}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class ShardFactory {

	/**
	 * The {@link ClusterSharding} from {@link ActorSystem}.
	 */
	private final ClusterSharding sharding;

	@ConfigProperty(name = DEFAULT_TIMEOUT_PROP, defaultValue = DEFAULT_TIMEOUT_SECONDS)
	Timeout timeout;

	@Inject
	public ShardFactory(final ClusterSharding sharding) {
		this.sharding = sharding;
	}

	/**
	 * Create labeled {@link Shard} instance with {@link Dependent} scope. This method is meant to
	 * be invoked by the CDI SPI.
	 *
	 * @param injection the {@link InjectionPoint} provided by CDI SPI
	 * @return New {@link Shard} instance
	 */
	@Produces
	@Labeled
	public Shard create(final InjectionPoint injection) {

		final String name = getLabel(injection);
		final ActorRef region = sharding.shardRegion(name);
		final Shard shard = new Shard(region, durationOf(timeout));

		return shard;
	}
}
