package ceri.ffm.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import ceri.common.array.Array;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.function.Enclosure;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;
import ceri.common.util.Validate;

/**
 * Provides access to native library calls by creating a proxy instance. Also allows overrides for
 * testing.
 */
public class Library<T> {
	private final Class<T> cls;
	private final Calls calls;
	private final Map<Key, Call> methods = Maps.concurrent();
	private volatile T proxy = null;
	private volatile T override = null;

	/**
	 * Method lookup key.
	 */
	record Key(Method method, List<Class<?>> varArgTypes) {
		@Override
		public final String toString() {
			return method().getName() + Lists.adapt(Reflect::name, varArgTypes());
		}
	}

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
	 * Creates an instance for the native interface, with the given call constructor.
	 */
	public static <T> Library<T> of(Class<T> cls) {
		return new Library<>(Calls.DEF, cls);
	}

	/**
	 * Creates an instance for the native interface, with the given call constructor.
	 */
	public static <T> Library<T> of(Calls calls, Class<T> cls) {
		return new Library<>(calls, cls);
	}

	private Library(Calls calls, Class<T> cls) {
		this.calls = calls;
		this.cls = cls;
	}

	/**
	 * Loads typed native library, or returns override if set.
	 */
	public T get() {
		if (override != null) return override;
		return ensureProxy();
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

	@Override
	public String toString() {
		return Reflect.name(cls) + (override == null ? "" : "*");
	}

	// support

	Map<Key, Call> methods() {
		return methods;
	}

	/**
	 * Overrides the library; use null to clear. Useful for testing.
	 */
	private void set(T override) {
		this.override = override;
	}

	private Object invokeMethod(Method method, Object[] args) throws Throwable {
		if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args);
		args = Basics.def(args, Array.OBJECT.empty);
		var call = call(method);
		if (method.isVarArgs()) return invokeVarArgs(call, args);
		return invokeCall(call, args);
	}

	private Object invokeVarArgs(Call call, Object[] args) throws Throwable {
		var varArgs = (Object[]) Array.last(args);
		var flatArgs = flatten(args, varArgs);
		if (Array.isEmpty(varArgs)) return invokeCall(call, flatArgs);
		call = varArgsCall(call, varArgs);
		return invokeCall(call, flatArgs);
	}

	private Call call(Method method) {
		var key = key(method);
		return methods.computeIfAbsent(key, _ -> calls.call(method));
	}

	private Call varArgsCall(Call call, Object[] varArgs) {
		var key = key(call.method, varArgs);
		return methods.computeIfAbsent(key, _ -> calls.varArgsCall(call, key.varArgTypes()));
	}

	private T ensureProxy() {
		if (proxy == null) synchronized (this) {
			proxy = createProxy(cls);
		}
		return proxy;
	}

	private T createProxy(Class<T> cls) {
		return Reflect.unchecked(Proxy.newProxyInstance(cls.getClassLoader(),
			new Class<?>[] { cls }, (_, m, args) -> invokeMethod(m, args)));
	}

	private static Object invokeCall(Call call, Object[] args) throws Throwable {
		return call.invoke(Segments.auto(), args);
	}

	private static Object[] flatten(Object[] args, Object[] varArgs) {
		if (varArgs.length == 1) {
			args[args.length - 1] = varArgs[0];
			return args;
		}
		var flat = new Object[args.length - 1 + varArgs.length];
		Array.copy(args, 0, flat, 0, args.length - 1);
		Array.copy(varArgs, 0, flat, args.length - 1, varArgs.length);
		return flat;
	}

	private static Key key(Method method) {
		return new Key(method, Immutable.list());
	}

	private static Key key(Method method, Object[] varArgs) {
		var types = Lists.<Class<?>>of();
		for (int i = 0; i < varArgs.length; i++)
			types.add(varArgType(varArgs, i));
		return new Key(method, Immutable.wrap(types));
	}

	private static Class<?> varArgType(Object[] varArgs, int i) {
		var value = Validate.nonNull(varArgs[i], "vararg[%d]", i);
		return Native.promote(value.getClass());
	}
}
