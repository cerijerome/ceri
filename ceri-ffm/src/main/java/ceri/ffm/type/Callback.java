package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import ceri.common.except.Exceptions;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;

/**
 * Marker interface for native callbacks. Callbacks must extend this interface with a single invoke
 * method with desired return type/void and argument types.
 */
public interface Callback {
	String METHOD_NAME = "invoke";

	/**
	 * Operational support for callback types.
	 */
	static class Supporter<C extends Callback> extends Support.Typed<C, AddressLayout> {
		private final Class<C> cls;

		Supporter(Class<C> cls) {
			super(Layouts.POINTER);
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
			return this;
		}

		@Override
		public Typed<C, AddressLayout> order(ByteOrder order) {
			return this;
		}

		@Override
		public C val() {
			return null;
		}

		@Override
		C rawGet(MemorySegment memory, long offset, long length) {
			return null;
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, C value) {}
	}

	/**
	 * Returns the callback class type for the instance.
	 */
	static Class<? extends Callback> classOf(Callback callback) {
		if (callback == null) return null;
		var ifaces = callback.getClass().getInterfaces();
		if (ifaces.length == 1) return Reflect.unchecked(ifaces[0]);
		for (var iface : ifaces)
			if (Callback.class.isAssignableFrom(iface)) return Reflect.unchecked(ifaces[0]);
		return null; // should not happen
	}

	/**
	 * Returns the callback method for the instance.
	 */
	static Method methodOf(Callback callback) {
		return method(classOf(callback));
	}

	/**
	 * Returns the callback method for the class type.
	 */
	static Method method(Class<? extends Callback> cls) {
		var method = Reflect.publicMethod(cls, METHOD_NAME);
		if (method != null) return method;
		throw Exceptions.illegalArg("%s.%s(...) method not found", Reflect.name(cls), METHOD_NAME);
	}
}
