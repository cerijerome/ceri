package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.util.Map;
import ceri.common.collect.Maps;
import ceri.common.concurrent.Locker;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Call;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;

/**
 * Marker interface for native callbacks. Callbacks must extend this interface with a single invoke
 * method, with desired return type/void and argument types.
 */
public interface Callback extends Functions.Closeable {
	/** Required callback invocation method name. */
	String METHOD_NAME = "invoke";

	// TODO: handle callbacks with vararg params
	// - need config for each vararg type combination
	// - upcall need to have map of multiple configs?
	// - move cache from library to downcall? reuse for callback?

	/**
	 * Frees resources used by the callback.
	 */
	@Override
	default void close() {
		Cache.remove(this);
	}

	/**
	 * Returns a function pointer for the callback instance. Callbacks and their pointers are
	 * cached, so subsequent calls return the same pointer.
	 */
	static MemorySegment pointer(Callback callback) {
		if (callback == null) return null;
		return Cache.pointer(callback);
	}

	/**
	 * Returns a callback instance for the function pointer. Callbacks and their pointers are
	 * cached, so subsequent calls return the same callback.
	 */
	static <C extends Callback> C callback(Class<C> cls, MemorySegment pointer) {
		if (cls == null || pointer == null) return null;
		return Cache.callback(cls, pointer);
	}

	/**
	 * Operational support for callback types.
	 */
	static class Supporter<C extends Callback> extends Support.Typed<C, AddressLayout> {
		private final Class<C> cls;

		public static <C extends Callback> Supporter<C> of(Class<C> cls) {
			return new Supporter<>(cls, Layouts.POINTER);
		}

		Supporter(Class<C> cls, AddressLayout layout) {
			super(layout);
			this.cls = cls;
		}

		@Override
		public Native.Kind kind() {
			return Native.Kind.CALLBACK;
		}

		@Override
		public Class<C> type() {
			return cls;
		}

		@Override
		public Supporter<C> align(long align) {
			var layout = Layouts.align(layout(), align);
			return layout == layout() ? this : new Supporter<>(cls, layout);
		}

		@Override
		public Supporter<C> order(ByteOrder order) {
			var layout = Layouts.order(layout(), order);
			return layout == layout() ? this : new Supporter<>(cls, layout);
		}

		@Override
		public C val() {
			return null; // create no-op instance for class?
		}

		@Override
		public String typeDesc() {
			return cls.getSimpleName() + "*";
		}

		@Override
		C rawGet(MemorySegment memory, long offset, long length) {
			var pointer = memory.get(layout(), offset);
			return Callback.callback(cls, pointer);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, C value) {
			var pointer = Callback.pointer(value);
			memory.set(layout(), offset, pointer);
		}
	}

	/**
	 * Cached callback and function pointer mappings.
	 */
	static class Cache {
		private static final Map<Class<? extends Callback>, Call.Config> configs = Maps.of();
		private static final Map<Callback, Call.Up> callbacks = Maps.of();
		private static final Map<MemorySegment, Call.Up> pointers = Maps.of();
		private static final Locker locker = Locker.of();

		private Cache() {}

		@SuppressWarnings("resource")
		private static MemorySegment pointer(Callback callback) {
			return locker.get(() -> {
				var upcall = callbacks.get(callback);
				if (upcall != null) return upcall.pointer();
				var config = config(Callback.classOf(callback));
				upcall = config.up(callback);
				return add(upcall).pointer();
			});
		}

		@SuppressWarnings("resource")
		private static <C extends Callback> C callback(Class<C> cls, MemorySegment pointer) {
			return locker.get(() -> {
				var upcall = pointers.get(pointer);
				if (upcall != null) return Reflect.unchecked(upcall.callback());
				var config = config(cls);
				upcall = config.up(pointer);
				return Reflect.unchecked(add(upcall).callback());
			});
		}

		@SuppressWarnings("resource")
		private static void remove(Callback callback) {
			locker.run(() -> close(callbacks.get(callback)));
		}

		@SuppressWarnings("resource")
		private static Call.Up add(Call.Up upcall) {
			System.out.println("add-to-cache: " + upcall);
			callbacks.put(upcall.callback(), upcall);
			pointers.put(upcall.pointer(), upcall);
			return upcall;
		}

		@SuppressWarnings("resource")
		private static void close(Call.Up upcall) {
			if (upcall == null) return;
			System.out.println("remove-from-cache: " + upcall);
			callbacks.remove(upcall.callback());
			pointers.remove(upcall.pointer());
			upcall.close();
		}

		private static Call.Config config(Class<? extends Callback> cls) {
			return configs.computeIfAbsent(cls, _ -> Call.config(Callback.method(cls)));
		}
	}

	// support

	private static Class<? extends Callback> classOf(Callback callback) {
		if (callback == null) return null;
		var ifaces = callback.getClass().getInterfaces();
		if (ifaces.length == 1) return Reflect.unchecked(ifaces[0]);
		for (var iface : ifaces)
			if (Callback.class.isAssignableFrom(iface)) return Reflect.unchecked(ifaces[0]);
		return null; // should not happen
	}

	private static Method method(Class<? extends Callback> cls) {
		var method = Reflect.publicMethod(cls, METHOD_NAME);
		if (method != null) return method;
		throw Exceptions.illegalArg("%s.%s(...) method not found", Reflect.name(cls), METHOD_NAME);
	}
}
