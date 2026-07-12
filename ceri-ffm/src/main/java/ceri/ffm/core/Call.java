package ceri.ffm.core;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.function.Functions;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.common.stream.Streams;
import ceri.common.text.Joiner;

/**
 * Encapsulates a native call.
 */
public class Call {
	private static final Return<?, ?> VOID = new Return<>(Generics.Typed.VOID, null, _ -> null);
	public final Method method;
	private final MethodHandle handle;
	private final Return<?, ?> rtn;
	private final List<Arg<?, ?>> args;
	private final boolean lastError;

	/**
	 * Provides a memory layout and type adapter for a call argument.
	 */
	private record Arg<T, R>(Generics.Typed type, MemoryLayout layout,
		Functions.BiFunction<SegmentAllocator, T, Native.Adapted<R>> adapter) {

		/**
		 * Applies the adapter to the value, with allocator if needed.
		 */
		Native.Adapted<R> apply(SegmentAllocator allocator, T value) {
			return adapter().apply(allocator, value);
		}

		@Override
		public final String toString() {
			return type.toString();
		}
	}

	/**
	 * Provides a memory layout and type adapter for a call response.
	 */
	private record Return<T, R>(Generics.Typed type, MemoryLayout layout,
		Functions.Function<T, R> adapter) {
		/**
		 * Adapts the return value with allocator.
		 */
		R apply(T from) {
			return adapter().apply(from);
		}

		@Override
		public final String toString() {
			return type.toString();
		}
	}

	/**
	 * Builds a call.
	 */
	public static class Builder {
		private final Method method;
		private Return<?, ?> rtn = VOID;
		private final List<Arg<?, ?>> args = Lists.of();
		private int varArg = -1;
		private boolean lastError = false;

		private Builder(Method method) {
			this.method = method;
		}

		/**
		 * Specifies the return type. 
		 */
		public <T, R> Builder rtn(Generics.Typed type, MemoryLayout layout,
			Functions.Function<T, R> adapter) {
			this.rtn = new Return<>(type, layout, adapter);
			return this;
		}

		/**
		 * Specifies the next argument type.
		 */
		public <T, R> Builder arg(Generics.Typed type, MemoryLayout layout,
			Functions.BiFunction<SegmentAllocator, T, Native.Adapted<R>> adapter) {
			args.add(new Arg<>(type, layout, adapter));
			return this;
		}

		/**
		 * Mark the call as var-arg.
		 */
		public Builder varArg() {
			varArg = args.size();
			return this;
		}

		/**
		 * Specifies the call to capture the last error code.
		 */
		public Builder lastError() {
			lastError = true;
			return this;
		}

		/**
		 * Builds the call with given linker and symbol lookup.
		 */
		public Call build(Linker linker, SymbolLookup lookup) {
			var pointer = lookup.findOrThrow(method.getName());
			var funcDesc = funcDesc();
			var handle = linker.downcallHandle(pointer, funcDesc, options());
			return new Call(method, handle, rtn, Immutable.wrap(args), lastError);
		}

		private Linker.Option[] options() {
			var options = Lists.<Linker.Option>of();
			if (varArg >= 0) options.add(Linker.Option.firstVariadicArg(varArg));
			if (lastError) options.add(LastError.OPTION);
			return options.toArray(Linker.Option[]::new);
		}

		private FunctionDescriptor funcDesc() {
			var argLayouts = Streams.from(args).map(a -> a.layout()).toArray(MemoryLayout.class);
			if (rtn.layout() == null) return FunctionDescriptor.ofVoid(argLayouts);
			return FunctionDescriptor.of(rtn.layout(), argLayouts);
		}
	}

	/**
	 * Start building the call for the native method.
	 */
	public static Builder builder(Method method) {
		return new Builder(method);
	}

	/**
	 * Start building an extension to the given call.
	 */
	public static Builder builder(Call call) {
		var b = new Builder(call.method);
		b.rtn = call.rtn;
		b.args.addAll(call.args);
		b.lastError = call.lastError;
		return b;
	}

	private Call(Method method, MethodHandle handle, Return<?, ?> rtn, List<Arg<?, ?>> args,
		boolean lastError) {
		this.method = method;
		this.handle = handle;
		this.rtn = rtn;
		this.args = args;
		this.lastError = lastError;
	}

	/**
	 * Returns the number of call arguments.
	 */
	public int argCount() {
		return args.size();
	}

	/**
	 * Invokes the call with given allocator and java arguments.
	 */
	public Object invoke(SegmentAllocator allocator, Object[] args) throws Throwable {
		var adaptedArgs = adaptArgs(allocator, args);
		var nativeArgs = nativeArgs(allocator, adaptedArgs);
		var rtn = handle.invokeWithArguments(nativeArgs); // invokeExact(Object[]) fails
		var result = this.rtn.apply(Reflect.unchecked(rtn));
		resolveArgs(adaptedArgs, nativeArgs);
		return result;
	}

	@Override
	public String toString() {
		return rtn + " " + method.getName() + Joiner.PARAM.join(args) + (lastError ? "!" : "");
	}

	// support

	private List<Native.Adapted<?>> adaptArgs(SegmentAllocator allocator, Object[] args) {
		var adaptedArgs = new ArrayList<Native.Adapted<?>>(args.length);
		for (int i = 0; i < args.length; i++)
			adaptedArgs.add(this.args.get(i).apply(allocator, Reflect.unchecked(args[i])));
		return adaptedArgs;
	}

	private Object[] nativeArgs(SegmentAllocator allocator, List<Native.Adapted<?>> adaptedArgs) {
		var nativeArgs = new Object[adaptedArgs.size() + (lastError ? 1 : 0)];
		int index = 0;
		if (lastError) nativeArgs[index++] = LastError.capture(allocator);
		for (var adaptedArg : adaptedArgs)
			nativeArgs[index++] = adaptedArg.value();
		return nativeArgs;
	}

	private void resolveArgs(List<Native.Adapted<?>> adaptedArgs, Object[] nativeArgs) {
		if (lastError) LastError.save((MemorySegment) nativeArgs[0]);
		for (var adaptedArg : adaptedArgs)
			adaptedArg.resolve();
	}
}
