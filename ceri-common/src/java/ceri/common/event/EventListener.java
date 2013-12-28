package ceri.common.event;

/**
 * Interface to listen for an event.
 */
public interface EventListener<T> {
	void event(T event);
}
