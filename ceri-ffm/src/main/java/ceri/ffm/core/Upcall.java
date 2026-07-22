package ceri.ffm.core;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import ceri.common.collect.Collectable;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.function.Functions;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Handles;
import ceri.common.reflect.Reflect;

/**
 * Encapsulates a native callback.
 */
public class Upcall {
	private static final MethodHandle INVOKE = Handles.staticMethod(Upcall.class, "invoke",
		Object.class, Upcall.class, Callback.class, Object[].class);
	private static final Return<?, ?> VOID =
		new Return<>(Generics.Typed.VOID, void.class, null, (_, _) -> null);
	private static final Map<Callback, MemorySegment> stubs = Maps.syncWeak();
	private final Linker linker;
	private final Method callback;
	private final MethodHandle callbackHandle;
	private final FunctionDescriptor upcallDesc;
	private final MethodHandle upcallHandle;
	private final MethodType upcallType;
	private final Functions.BiFunction<SegmentAllocator, ?, ?> rtn;
	private final List<Functions.Function<?, ?>> args;

	/**
	 * Provides a memory layout and type adapter for a callback argument.
	 */
	private record Arg<T, R>(Generics.Typed localType, Class<?> nativeType, MemoryLayout layout,
		Functions.Function<T, R> adapter) {}

	/**
	 * Provides a memory layout and type adapter for a callback response.
	 */
	private record Return<T, R>(Generics.Typed localType, Class<?> nativeType, MemoryLayout layout,
		Functions.BiFunction<SegmentAllocator, T, R> adapter) {}

	/**
	 * Builds a callback.
	 */
	public static class Builder {
		private Return<?, ?> rtn = VOID;
		private final List<Arg<?, ?>> args = Lists.of();

		private Builder() {}

		/**
		 * Specifies the return type.
		 */
		public <T, R> Builder rtn(Generics.Typed localType, Class<?> nativeType,
			MemoryLayout layout, Functions.BiFunction<SegmentAllocator, T, R> adapter) {
			this.rtn = new Return<>(localType, nativeType, layout, adapter);
			return this;
		}

		/**
		 * Specifies the next argument type.
		 */
		public <T, R> Builder arg(Generics.Typed localType, Class<?> nativeType,
			MemoryLayout layout, Functions.Function<T, R> adapter) {
			args.add(new Arg<>(localType, nativeType, layout, adapter));
			return this;
		}

		public Upcall build(Linker linker, Method callback) {
			return new Upcall(linker, callback, rtn, args);
		}
	}

	/**
	 * Start building the call for the native method.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Handler for all native callbacks; delegates to the given upcall.
	 */
	public static Object invoke(Upcall upcall, Callback callback, Object... nativeArgs) {
		return upcall.invoke(Segments.lazyAuto(), callback, nativeArgs);
	}

	private Upcall(Linker linker, Method callback, Return<?, ?> rtn, List<Arg<?, ?>> args) {
		this.linker = linker;
		this.callback = callback;
		this.rtn = rtn.adapter();
		this.args = Immutable.adaptList(Arg::adapter, args);
		callbackHandle = Handles.method(callback);
		upcallDesc = upcallDesc(rtn, args);
		upcallType = upcallType(rtn, args);
		upcallHandle = INVOKE.bindTo(this);
	}

	public MemorySegment stub(Arena arena, Callback callback) {
		return stubs.computeIfAbsent(callback, _ -> createStub(arena, callback));
	}

	@Override
	public String toString() {
		return Reflect.descriptor(callback);
	}

	// support

	private MemorySegment createStub(Arena arena, Callback callback) {
		var handle =
			upcallHandle.bindTo(callback).asVarargsCollector(Object[].class).asType(upcallType);
		return linker.upcallStub(handle, upcallDesc, arena);
	}

	private Object invoke(SegmentAllocator allocator, Callback callback, Object[] nativeArgs) {
		var localArgs = localArgs(callback, nativeArgs);
		var localRtn = Handles.invoke(callbackHandle, localArgs);
		return rtn.apply(allocator, Reflect.unchecked(localRtn));
	}

	private Object[] localArgs(Callback callback, Object[] nativeArgs) {
		var localArgs = new Object[nativeArgs.length + 1];
		localArgs[0] = callback;
		for (int i = 0; i < nativeArgs.length; i++)
			localArgs[i + 1] = args.get(i).apply(Reflect.unchecked(nativeArgs[i]));
		return localArgs;
	}

	private static FunctionDescriptor upcallDesc(Return<?, ?> rtn, List<Arg<?, ?>> args) {
		var argLayouts = Collectable.adaptToArray(args, MemoryLayout[]::new, Arg::layout);
		if (rtn.layout() == null) return FunctionDescriptor.ofVoid(argLayouts);
		return FunctionDescriptor.of(rtn.layout(), argLayouts);
	}

	private static MethodType upcallType(Return<?, ?> rtn, List<Arg<?, ?>> args) {
		var argTypes = Collectable.adaptToArray(args, Class[]::new, Arg::nativeType);
		return MethodType.methodType(rtn.nativeType(), argTypes);
	}
}
