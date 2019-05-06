package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.util.ReflectionUtils.getAnnotatedParameterPosition;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.getObjectDistance;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.hasValidableParameters;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.unreflect;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.util.ReflectionUtils;

import io.vavr.control.Option;


public class MessageHandlerRegistry {

	private static final Logger LOG = LoggerFactory.getLogger(MessageHandlerRegistry.class);

	/**
	 * A static map where all {@link Receives} annotation points are stored. This map is populated
	 * by a {@link ActorRegistryTemplate} recorded in the compile time.
	 */
	private static final Map<String, Map<String, MessageHandlerMethod>> RECORDS = new HashMap<>();

	private static final Predicate<Method> HAS_ARGUMENTS = m -> m.getParameterCount() > 0;

	/**
	 * Comparator to sort {@link MessageHandlerMethod} by a distance between a message type and an
	 * {@link Object} class. The more specialized methods will be listed first.
	 */
	private static final Comparator<MessageHandlerMethod> BY_OBJECT_DISTANCE = (a, b) -> {
		final int distanceA = getObjectDistance(a.getMessageClass());
		final int distanceB = getObjectDistance(b.getMessageClass());
		return Integer.compare(distanceA, distanceB);
	};

	/**
	 * Where to stop inheritance tree scanning.
	 */
	private static final Class<?> STOP_CLASS = SimpleActor.class.getSuperclass();

	/**
	 * Register given class as a {@link Receives} definer. Given class inheritance tree will be
	 * scanned and all methods which contains at least one parameter annotated with {@link Receives}
	 * annotations will be stored in internal handlers registry,
	 *
	 * @param clazz the class to scan
	 */
	static void register(final Class<?> clazz) {

		// allow concrete classes only

		if (clazz.isAnonymousClass() || Modifier.isAbstract(clazz.getModifiers())) {
			return;
		}

		final List<MessageHandlerMethod> methods = ReflectionUtils
			.getAnnotatedParameterMethodsFromClass(clazz, Receives.class, STOP_CLASS)
			.stream()
			.filter(HAS_ARGUMENTS)
			.map(MessageHandlerMethod::new)
			.collect(toList());

		if (methods.isEmpty()) {
			return;
		} else {
			methods.sort(BY_OBJECT_DISTANCE);
		}

		LOG.debug("Register {} message handlers for actor {}", methods.size(), clazz);

		final String classKey = clazz.getName();
		final Map<String, MessageHandlerMethod> handlers = RECORDS.computeIfAbsent(classKey, $ -> new LinkedHashMap<>());

		methods
			.stream()
			.peek(entry -> LOG.trace("Register message handler {}", entry.getName()))
			.forEach(entry -> handlers.putIfAbsent(entry.getMessageKey(), entry));
	}

	/**
	 * Return a mapping between message class and corresponding handlers.
	 *
	 * @param clazz a declaring class
	 * @return A mapping between message class and corresponding handlers
	 */
	public static Option<Map<String, MessageHandlerMethod>> getMessageHandlersFor(final Class<?> clazz) {
		return Option.of(RECORDS.get(clazz.getName()));
	}

	/**
	 * This method will return a {@link MessageHandlerMethod} for a given message class in a given
	 * declaring class.
	 *
	 * @param declaringClass a declaring class
	 * @param messageClass a message class
	 * @return A {@link MessageHandlerMethod} entry
	 */
	public static Option<MessageHandlerMethod> getHandlerFor(final Class<?> declaringClass, final Class<?> messageClass) {
		return getMessageHandlersFor(declaringClass).map(mapping -> mapping.get(messageClass.getName()));
	}

	public static class MessageHandlerMethod {

		private final Method method;
		private final Class<?> messageClass;
		private final int messageParameterPosition;
		private final MethodHandle handle;
		private final boolean validable;
		private final boolean injectable;

		public MessageHandlerMethod(final Method method) {
			this.method = method;
			this.messageClass = method.getParameterTypes()[0];
			this.messageParameterPosition = getAnnotatedParameterPosition(method, Receives.class);
			this.handle = unreflect(getDeclaringClass(), method);
			this.validable = hasValidableParameters(method);
			this.injectable = method.isAnnotationPresent(Inject.class);
		}

		public Class<?> getDeclaringClass() {
			return method.getDeclaringClass();
		}

		public Class<?> getMessageClass() {
			return messageClass;
		}

		public int getMessageParameterPosition() {
			return messageParameterPosition;
		}

		public MethodHandle getHandle() {
			return handle;
		}

		public boolean isValidable() {
			return validable;
		}

		public boolean isInjectable() {
			return injectable;
		}

		public String getMessageKey() {
			return getMessageClass().getName();
		}

		/**
		 * @return Interned method name
		 */
		public String getName() {
			return method.getName();
		}

		public Class<?> getReturnedType() {
			return method.getReturnType();
		}
	}
}
