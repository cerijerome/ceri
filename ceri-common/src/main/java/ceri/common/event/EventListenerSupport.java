package ceri.common.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import ceri.common.collection.ImmutableUtil;

/**
 * Captures event listeners and notifies them of events.
 */
public class EventListenerSupport<T> implements EventListener<T> {
	private final Collection<EventListener<T>> listeners;

	public static class Builder<T> {
		final Collection<EventListener<T>> listeners = new LinkedHashSet<>();

		Builder() {}

		/**
		 * Add a listener.
		 */
		public Builder<T> listener(EventListener<T> listener) {
			listeners.add(listener);
			return this;
		}

		/**
		 * Builds the listener support object.
		 */
		public EventListenerSupport<T> build() {
			return new EventListenerSupport<>(this);
		}
	}

	EventListenerSupport(Builder<T> builder) {
		listeners = ImmutableUtil.copyAsList(builder.listeners);
	}

	/**
	 * Creates the builder to add listeners.
	 */
	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	/**
	 * Creates the support object for given listeners.
	 */
	@SafeVarargs
	public static <T> EventListenerSupport<T> create(EventListener<T>... listeners) {
		return create(Arrays.asList(listeners));
	}

	/**
	 * Creates the support object for given listeners.
	 */
	public static <T> EventListenerSupport<T> create(Collection<EventListener<T>> listeners) {
		Builder<T> builder = builder();
		for (EventListener<T> listener : listeners)
			builder.listener(listener);
		return builder.build();
	}

	/**
	 * Notifies listeners of an event.
	 */
	@Override
	public void event(T event) {
		for (EventListener<T> listener : listeners)
			listener.event(event);
	}

}
