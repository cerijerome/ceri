package ceri.ffm.core;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.util.List;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.function.Functions;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.common.stream.Streams;
import ceri.common.text.ToString;
import ceri.ffm.reflect.Refine;

/**
 * Encapsulates a native call.
 */
public class Call {
	private static final Typed<Return<?, ?>> VOID =
		new Typed<>(Generics.Typed.VOID, new Return<>(null, _ -> null));
	public final String name;
	private final MethodHandle handle;
	private final Typed<Return<?, ?>> rtn;
	private final List<Typed<Arg<?, ?>>> args;
	private final boolean lastError;

	/**
	 * Applies a type to the reference.
	 */
	private record Typed<T>(Generics.Typed type, T ref) {
		@Override
		public final String toString() {
			return type.toString();
		}
	}

	/**
	 * Represents method argument actions.
	 */
	public record Arg<T, R>(MemoryLayout layout,
		Functions.BiFunction<SegmentAllocator, T, R> adapter, Functions.BiConsumer<T, R> resolver) {
		/**
		 * Provides an argument handler based on context, type, and array dimensions (zero for
		 * non-array).
		 */
		public interface Handler {
			Arg<?, ?> arg(Refine.Context context, Generics.Array array);
		}

		/**
		 * Adapts the argument with allocator.
		 */
		public R adapt(SegmentAllocator allocator, T from) {
			return adapter().apply(allocator, from);
		}

		/**
		 * Updates the argument after the call, if a resolver is specified.
		 */
		public void resolve(T from, R to) {
			if (resolver() != null) resolver().accept(from, to);
		}
	}

	/**
	 * Represents method return actions.
	 */
	public record Return<T, R>(MemoryLayout layout, Functions.Function<T, R> adapter) {
		/**
		 * Provides a return value handler based on context, type, and array dimensions (zero for
		 * non-array).
		 */
		public interface Handler {
			Return<?, ?> rtn(Refine.Context context, Generics.Array array);
		}

		/**
		 * Adapts the return value with allocator.
		 */
		public R adapt(T from) {
			return adapter().apply(from);
		}
	}

	/**
	 * Builds a call.
	 */
	public static class Builder {
		private final String name;
		private Typed<Return<?, ?>> rtn = VOID;
		private final List<Typed<Arg<?, ?>>> args = Lists.of();
		private int varArg = -1;
		private boolean lastError = false;

		private Builder(String name) {
			this.name = name;
		}

		public Builder rtn(Generics.Typed type, Return<?, ?> rtn) {
			this.rtn = new Typed<>(type, rtn);
			return this;
		}

		public Builder arg(Generics.Typed type, Arg<?, ?> arg) {
			args.add(new Typed<>(type, arg));
			return this;
		}

		public Builder varArg() {
			varArg = args.size();
			return this;
		}

		public Builder lastError() {
			lastError = true;
			return this;
		}

		public Call build(Linker linker, SymbolLookup lookup) {
			var pointer = lookup.findOrThrow(name);
			var funcDesc = funcDesc();
			var handle = linker.downcallHandle(pointer, funcDesc, options());
			return new Call(name, handle, rtn, Immutable.wrap(args), lastError);
		}

		private Linker.Option[] options() {
			var options = Lists.<Linker.Option>of();
			if (varArg >= 0) options.add(Linker.Option.firstVariadicArg(varArg));
			if (lastError) options.add(LastError.OPTION);
			return options.toArray(Linker.Option[]::new);
		}

		private FunctionDescriptor funcDesc() {
			var argLayouts =
				Streams.from(args).map(a -> a.ref().layout()).toArray(MemoryLayout.class);
			if (rtn.ref().layout() == null) return FunctionDescriptor.ofVoid(argLayouts);
			return FunctionDescriptor.of(rtn.ref().layout(), argLayouts);
		}
	}

	public static Builder builder(String name) {
		return new Builder(name);
	}

	public static Builder builder(Call call) {
		var b = new Builder(call.name);
		b.rtn = call.rtn;
		b.args.addAll(call.args);
		b.lastError = call.lastError;
		return b;
	}

	private Call(String name, MethodHandle handle, Typed<Return<?, ?>> rtn,
		List<Typed<Arg<?, ?>>> args, boolean lastError) {
		this.name = name;
		this.handle = handle;
		this.rtn = rtn;
		this.args = args;
		this.lastError = lastError;
	}

	public int argCount() {
		return args.size();
	}

	public Object invoke(SegmentAllocator allocator, Object[] args) throws Throwable {
		int index = lastError ? 1 : 0;
		var nativeArgs = nativeArgs(allocator, args, index);
		var rtn = handle.invokeWithArguments(nativeArgs); // invokeExact(Object[]) fails
		var result = this.rtn.ref().adapt(Reflect.unchecked(rtn));
		if (lastError) LastError.save((MemorySegment) nativeArgs[0]);
		resolveArgs(args, nativeArgs, index);
		return result;
	}

	@Override
	public String toString() {
		var t = ToString.ofName(rtn + " " + name);
		for (var arg : args)
			t.values(arg);
		return lastError ? t + "!" : t.toString();
	}

	// support

	private Object[] nativeArgs(SegmentAllocator allocator, Object[] args, int index) {
		var nativeArgs = new Object[index + args.length];
		if (index > 0) nativeArgs[0] = LastError.capture(allocator);
		for (int i = 0; i < argCount(); i++)
			nativeArgs[index + i] = arg(i).adapt(allocator, Reflect.unchecked(args[i]));
		return nativeArgs;
	}

	private void resolveArgs(Object[] args, Object[] nativeArgs, int index) {
		for (int i = 0; i < argCount(); i++)
			arg(i).resolve(Reflect.unchecked(args[i]), Reflect.unchecked(nativeArgs[index + i]));
	}

	private Arg<?, ?> arg(int i) {
		return Lists.at(args, i).ref();
	}
}
