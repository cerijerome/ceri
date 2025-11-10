package ceri.ffm.core;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
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
import ceri.common.util.Validate;
import ceri.ffm.clib.ffm.CException;
import ceri.ffm.clib.ffm.CFcntl;
import ceri.ffm.clib.ffm.CLib;
import ceri.ffm.clib.ffm.CStdLib;
import ceri.ffm.clib.ffm.CUnistd;

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

	// TODO:
	// call args:
	// - pointers
	// - structs and unions
	// - work through clib

	private record Key(Method method, List<Class<?>> varArgTypes) {
		@Override
		public final String toString() {
			return method().getName() + Lists.adapt(Reflect::name, varArgTypes());
		}
	}

	public static void main(String[] args) throws Exception {
		int fd1 = CFcntl.open("file.txt", CFcntl.Open.O_RDONLY.value);
		System.out.println("fd1 = " + fd1);
		int fd2 = CFcntl.open("file.txt", CFcntl.Open.O_RDONLY.value, 0777);
		System.out.println("fd2 = " + fd2);
		CUnistd.close(fd2);
		CUnistd.close(fd1);
		main0(args);
		CLib.library.methods.forEach((k, c) -> {
			System.out.println();
			System.out.println(k);
			System.out.println(c);
		});
	}

	public static void main0(String[] args) throws CException {
		var key = "CERI_TEST";
		System.out.printf("\"%s\" = %s%n", key, CStdLib.getenv(key));
		CStdLib.setenv(key, "hello1", false);
		System.out.printf("\"%s\" = %s%n", key, CStdLib.getenv(key));
		CStdLib.setenv(key, "hello2", true);
		System.out.printf("\"%s\" = %s%n", key, CStdLib.getenv(key));
		CStdLib.setenv(key, "hello3", false);
		System.out.printf("\"%s\" = %s%n", key, CStdLib.getenv(key));
		key = "";
		System.out.printf("\"%s\" = %s%n", key, CStdLib.getenv(key));
		try {
			CStdLib.setenv(key, "hello4", true);
		} catch (Exception e) {
			e.printStackTrace();
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

	public static <T> Library<T> of(Class<T> cls) {
		var linker = Linker.nativeLinker();
		var lookup = linker.defaultLookup();
		return new Library<>(linker, lookup, cls);
	}

	private Library(Linker linker, SymbolLookup lookup, Class<T> cls) {
		this.cls = cls;
		calls = Calls.of(linker, lookup);
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

	/**
	 * Overrides the library; use null to clear. Useful for testing.
	 */
	private void set(T override) {
		this.override = override;
	}

	// support

	private Object invokeMethod(Method method, Object[] args) throws Throwable {
		if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args);
		var call = call(method);
		if (!method.isVarArgs()) return invokeCall(call, args);
		return invokeVarArgs(method, call, args);
	}

	private Object invokeVarArgs(Method method, Call call, Object[] args) throws Throwable {
		var varArgs = (Object[]) Array.last(args);
		var flatArgs = flatten(args, varArgs);
		if (Array.isEmpty(varArgs)) return invokeCall(call, flatArgs);
		call = varArgsCall(method, call, varArgs);
		return invokeCall(call, flatArgs);
	}

	private Call call(Method method) {
		var key = key(method);
		return methods.computeIfAbsent(key, _ -> calls.call(method));
	}

	private Call varArgsCall(Method method, Call call, Object[] varArgs) {
		var key = key(method, varArgs);
		return methods.computeIfAbsent(key,
			_ -> calls.varArgsCall(call, method, key.varArgTypes()));
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

	@SuppressWarnings("resource")
	private static Object invokeCall(Call call, Object[] args) throws Throwable {
		return call.invoke(Arena.ofAuto(), args);
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
