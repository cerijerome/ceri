package ceri.common.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.function.Throws;

/**
 * Support for method and var handles.
 */
public class Handles {
	private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

	/**
	 * Math-based handles.
	 */
	public static class Math {
		private static final Class<?> CLASS = java.lang.Math.class;
		public static final MethodHandle ADD_EXACT_INT =
			staticMethod(CLASS, "addExact", int.class, int.class, int.class);
		public static final MethodHandle ADD_EXACT_LONG =
			staticMethod(CLASS, "addExact", long.class, long.class, long.class);

		private Math() {}

		/**
		 * Returns a method handle {@code (int) -> int} that adds a value to the argument.
		 */
		public static MethodHandle addExact(int value) {
			return MethodHandles.insertArguments(ADD_EXACT_INT, 1, value);
		}

		/**
		 * Returns a method handle {@code (long) -> long} that adds a value to the argument.
		 */
		public static MethodHandle addExact(long value) {
			return MethodHandles.insertArguments(ADD_EXACT_LONG, 1, value);
		}
	}

	/**
	 * Method handle invocation function.
	 */
	public interface VarArgFunction<E extends Exception, T> {
		T apply(Object... args) throws E;
	}

	private Handles() {}

	/**
	 * Gets a typed value from the handle.
	 */
	public static <T> T get(VarHandle handle) {
		if (handle == null) return null;
		return Reflect.unchecked(handle.get());
	}

	/**
	 * Gets a typed value from the handle.
	 */
	public static <T> T get(VarHandle handle, Object arg) {
		if (handle == null) return null;
		return Reflect.unchecked(handle.get(arg));
	}

	/**
	 * Returns a var handle for field access.
	 */
	public static VarHandle handle(Class<?> cls, String fieldName) {
		return handle(Reflect.publicField(cls, fieldName));
	}

	/**
	 * Returns a var handle for field access.
	 */
	public static VarHandle handle(Field field) {
		if (field == null) return null;
		return get(() -> LOOKUP.unreflectVarHandle(field));
	}

	/**
	 * Adapts a var handle get into a method handle.
	 */
	public static MethodHandle getter(VarHandle handle) {
		if (handle == null) return null;
		return handle.toMethodHandle(VarHandle.AccessMode.GET);
	}

	/**
	 * Adapts a method handle to a supplier.
	 */
	public static <T> Functions.Supplier<T> asSupplier(MethodHandle handle) {
		if (handle == null) return null;
		return () -> invoke(handle);
	}

	/**
	 * Adapts a method handle to a supplier.
	 */
	public static <E extends Exception, T> Excepts.Supplier<E, T>
		asSupplier(ExceptionAdapter<? extends E> except, MethodHandle handle) {
		if (handle == null || except == null) return null;
		return () -> invoke(except, handle);
	}

	/**
	 * Adapts a method handle to a function.
	 */
	public static <T, R> Functions.Function<T, R> asFunction(MethodHandle handle) {
		if (handle == null) return null;
		return t -> invoke(handle, t);
	}

	/**
	 * Adapts a method handle to a function.
	 */
	public static <E extends Exception, T, R> Excepts.Function<E, T, R>
		asFunction(ExceptionAdapter<? extends E> except, MethodHandle handle) {
		if (handle == null || except == null) return null;
		return t -> invoke(except, handle, t);
	}

	/**
	 * Adapts a method handle to a var-arg function.
	 */
	public static <T> VarArgFunction<RuntimeException, T> asVargArg(MethodHandle handle) {
		return asVarArg(ExceptionAdapter.illegalArg, handle);
	}

	/**
	 * Adapts a method handle to a var-arg function.
	 */
	public static <E extends Exception, T> VarArgFunction<E, T>
		asVarArg(ExceptionAdapter<? extends E> except, MethodHandle handle) {
		if (handle == null) return null;
		return args -> invoke(except, handle, args);
	}

	/**
	 * Invokes the method, converting exceptions to runtime.
	 */
	public static <T> T invoke(MethodHandle handle, Object... args) {
		return invoke(ExceptionAdapter.illegalArg, handle, args);
	}

	/**
	 * Invokes the method, converting exceptions.
	 */
	public static <E extends Exception, T> T invoke(ExceptionAdapter<? extends E> except,
		MethodHandle handle, Object... args) throws E {
		if (handle == null || except == null) return null;
		return Reflect.unchecked(except.get(() -> handle.invokeWithArguments(args)));
	}

	/**
	 * Looks up a constructor.
	 */
	public static MethodHandle constructor(Class<?> cls, Class<?>... args) {
		if (cls == null || args == null) return null;
		return get(() -> LOOKUP.findConstructor(cls, MethodType.methodType(void.class, args)));
	}

	/**
	 * Looks up a non-static method.
	 */
	public static MethodHandle method(Class<?> cls, String name, Class<?> rtn, Class<?>... args) {
		if (cls == null || name == null || rtn == null || args == null) return null;
		return get(() -> LOOKUP.findVirtual(cls, name, MethodType.methodType(rtn, args)));
	}

	/**
	 * Looks up a static method.
	 */
	public static MethodHandle staticMethod(Class<?> cls, String name, Class<?> rtn,
		Class<?>... args) {
		if (cls == null || name == null || rtn == null || args == null) return null;
		return get(() -> LOOKUP.findStatic(cls, name, MethodType.methodType(rtn, args)));
	}

	// support

	private static <T> T get(Throws.Supplier<T> supplier) {
		return ExceptionAdapter.illegalArg.get(supplier);
	}
}
