package ceri.ffm.core;

import java.lang.foreign.SymbolLookup;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import ceri.common.array.Array;
import ceri.common.collect.Maps;
import ceri.common.function.Enclosure;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;

/**
 * Provides access to native library calls by creating a proxy instance. Allows overrides for
 * testing.
 */
public class Library<T> implements Functions.Supplier<T> {
	private final Map<Call.Key, Call.Down> cache = Maps.concurrent();
	private final Class<T> cls;
	private final SymbolLookup lookup;
	private final T proxy;
	private volatile T override = null;

	/**
	 * A wrapper for repeatedly overriding the native library.
	 */
	public static class Ref<T> implements Functions.Closeable, Supplier<T> {
		private final Enclosure.Repeater<RuntimeException, T> repeater;

		private Ref(Library<? super T> library, Supplier<? extends T> constructor) {
			repeater = Enclosure.Repeater.unsafe(() -> library.enclosed(constructor.get()));
		}

		/**
		 * Re-initializes the override.
		 */
		public T init() {
			return repeater.init();
		}

		@Override
		public T get() {
			return repeater.get();
		}

		/**
		 * Returns the current value of the override, which may be null.
		 */
		public T lib() {
			return repeater.ref();
		}

		@Override
		public void close() {
			repeater.close();
		}
	}

	/**
	 * Creates an instance for the native interface, with default call and upcall builders.
	 */
	public static <T> Library<T> of(Class<T> cls) {
		return of(Native.LOOKUP, cls);
	}

	/**
	 * Creates an instance for the native interface, with given call and upcall builders.
	 */
	public static <T> Library<T> of(SymbolLookup lookup, Class<T> cls) {
		return new Library<>(lookup, cls);
	}

	private Library(SymbolLookup lookup, Class<T> cls) {
		this.cls = cls;
		this.lookup = lookup;
		proxy = Reflect.unchecked(Proxy.newProxyInstance(cls.getClassLoader(),
			new Class<?>[] { cls }, (_, m, args) -> invokeMethod(m, args)));
	}

	/**
	 * Loads typed native library, or returns the override if set.
	 */
	@Override
	public T get() {
		return Basics.def(override, proxy);
	}

	/**
	 * Temporarily override the native library.
	 */
	public <U extends T> Enclosure<U> enclosed(U override) {
		set(override);
		return Enclosure.of(override, _ -> set(null));
	}

	/**
	 * A wrapper for repeatedly overriding the native library.
	 */
	public <U extends T> Ref<U> ref(Supplier<U> constructor) {
		return new Ref<>(this, constructor);
	}

	/**
	 * Returns a copy of the current method cache.
	 */
	public Map<Call.Key, Call.Down> methods() {
		return new TreeMap<>(cache);
	}

	@Override
	public String toString() {
		return Reflect.name(cls) + (override == null ? "" : "*");
	}

	// support

	private void set(T override) {
		this.override = override; // null to clear
	}

	private Object invokeMethod(Method method, Object[] args) throws Throwable {
		if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args);
		args = Basics.def(args, Array.OBJECT.empty);
		var call = call(method, args);
		return call.invoke(args);
	}

	private Call.Down call(Method method, Object[] args) {
		var call = cachedCall(method);
		if (!method.isVarArgs() || args.length == 0) return call;
		return cachedVarArgsCall(call, args);
	}

	private Call.Down cachedCall(Method method) {
		var key = Call.Key.of(method);
		return cache.computeIfAbsent(key, _ -> {
			var pointer = lookup.findOrThrow(method.getName());
			return Call.config(method).down(pointer);
		});
	}

	private Call.Down cachedVarArgsCall(Call.Down call, Object[] args) {
		var key = Call.Key.from(call.config().method(), args);
		if (key.varArgTypes().isEmpty()) return call;
		return cache.computeIfAbsent(key, _ -> {
			var config = call.config().withVarArgs(key.varArgTypes());
			return config.down(call.pointer());
		});
	}
}
