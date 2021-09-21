package ceri.serial.jna;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;

/**
 * A cache to prevent gc of callbacks. Generates a new id when a callback is registered. Typically
 * used as a static final field.
 * 
 * <pre>
 * public interface MyCallback extends Callback {
 * 	public static final CallbackRegistry&lt;MyCallback> registry = CallbackRegistry.of();
 * 
 * 	void invoke(String s, int i);
 * 
 * 	public static MyCallback register(MyCallback cb, int n) {
 * 		return registry.register(id -> (s, i) -> {
 * 			registry.remove(id);
 * 			cb.invoke(s, i);
 * 		}, n);
 * 	}
 * }
 * </pre>
 */
public class CallbackRegistry<T> {
	private final Map<Long, Refs<T>> callbacks = new ConcurrentHashMap<>();
	private final AtomicLong id = new AtomicLong();

	private static class Refs<T> {
		private final T callback;
		private int count = 0;

		private Refs(T callback, int count) {
			this.callback = callback;
			this.count = count;
		}
	}

	public static <T> CallbackRegistry<T> of() {
		return new CallbackRegistry<>();
	}

	private CallbackRegistry() {}

	/**
	 * Registers a callback function, generating a new id, and passing to the given function.
	 */
	public T register(LongFunction<T> fn) {
		return register(1, fn);
	}

	/**
	 * Registers a callback function and ref count. A new id is generated, and passed to the given
	 * function. When the ref count is 0, the callback is removed from the map.
	 */
	public T register(int n, LongFunction<T> fn) {
		long id = this.id.incrementAndGet();
		Refs<T> refs = new Refs<>(fn.apply(id), n);
		callbacks.put(id, refs);
		return refs.callback;
	}

	/**
	 * Reduces the ref count by 1 for the registered callback, and removes it if the ref count
	 * reaches 0.
	 */
	public void remove(long id) {
		callbacks.computeIfPresent(id, (i, refs) -> --refs.count <= 0 ? null : refs);
	}

	/**
	 * Returns the number of registered callbacks.
	 */
	public int size() {
		return callbacks.size();
	}

	/**
	 * Clears all registered callbacks.
	 */
	public void clear() {
		callbacks.clear();
	}
}
