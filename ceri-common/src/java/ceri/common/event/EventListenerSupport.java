package ceri.common.event;

import java.util.Collection;
import java.util.LinkedHashSet;
import ceri.common.collection.ImmutableUtil;

public class EventListenerSupport<T> implements EventListener<T> {
	private final Collection<EventListener<T>> listeners;

	public static class Builder<T> {
		final Collection<EventListener<T>> listeners = new LinkedHashSet<>();

		Builder() {}

		public Builder<T> listener(EventListener<T> listener) {
			listeners.add(listener);
			return this;
		}

		public EventListenerSupport<T> build() {
			return new EventListenerSupport<>(this);
		}
	}

	EventListenerSupport(Builder<T> builder) {
		listeners = ImmutableUtil.copyAsList(builder.listeners);
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	@SafeVarargs
	public static <T> EventListenerSupport<T> create(EventListener<T>... listeners) {
		Builder<T> builder = builder();
		for (EventListener<T> listener : listeners)
			builder.listener(listener);
		return builder.build();
	}

	public static <T> EventListenerSupport<T> create(Collection<EventListener<T>> listeners) {
		Builder<T> builder = builder();
		for (EventListener<T> listener : listeners)
			builder.listener(listener);
		return builder.build();
	}

	@Override
	public void event(T event) {
		for (EventListener<T> listener : listeners)
			listener.event(event);
	}

}
