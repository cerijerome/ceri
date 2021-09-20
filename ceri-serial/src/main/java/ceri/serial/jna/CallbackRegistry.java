package ceri.serial.jna;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import com.sun.jna.Callback;

/**
 * A cache to prevent gc of callbacks. 
 * Generates a new id when a callback is registered.
 * Typically used as a static final field.
 * 
 * <pre>
 * public interface MyCallback extends Callback {
 * 	public static final CallbackRegistry&lt;MyCallback> registry = CallbackRegistry.of();
 * 
 * 	void invoke(String s, int i);
 * 
 * 	public static MyCallback register(MyCallback cb) {
 * 		return registry.register(id -> (s, i) -> {
 * 			registry.remove(id);
 * 			cb.invoke(s, i);
 * 		});
 * 	}
 * }
 * </pre>
 */
public class CallbackRegistry<T extends Callback> {
	private final Map<Integer, T> callbacks = new ConcurrentHashMap<>();
	private final AtomicInteger id = new AtomicInteger();

	public static <T extends Callback> CallbackRegistry<T> of() {
		return new CallbackRegistry<>();
	}

	private CallbackRegistry() {}

	/**
	 * Registers a callback function, generating a new id, and passing to the given function.
	 */
	public T register(IntFunction<T> fn) {
		int id = this.id.incrementAndGet();
		T t = fn.apply(id);
		callbacks.put(id, t);
		return t;
	}

	/**
	 * Removes a registered callback by id.
	 */
	public boolean remove(int id) {
		return callbacks.remove(id) != null;
	}

	/**
	 * Returns the number of registered callbacks.
	 */
	public int size() {
		return callbacks.size();
	}

	/**
	 * Returns the number of registered callbacks.
	 */
	public void clear() {
		callbacks.clear();
	}
}
