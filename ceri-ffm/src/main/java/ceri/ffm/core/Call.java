package ceri.ffm.core;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.util.List;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.stream.Streams;
import ceri.common.text.Strings;
import ceri.common.text.ToString;

/**
 * Encapsulates a native call.
 */
public class Call {
	public final String name;
	private final MethodHandle handle;
	private final Return rtn;
	private final List<Arg> args;
	private final boolean lastError;

	/**
	 * An action that creates one value from another.
	 */
	@FunctionalInterface
	public interface Adapter {
		static Adapter NULL = (_, _, o) -> o;

		Object apply(Arena arena, MemoryLayout layout, Object from);
	}

	/**
	 * An action that resolves two existing values.
	 */
	@FunctionalInterface
	public interface Resolver {
		static Resolver NULL = (_, _, _, _) -> {};

		void accept(Arena arena, MemoryLayout layout, Object from, Object to);
	}

	/**
	 * Represents method argument actions.
	 */
	public record Arg(String name, MemoryLayout layout, Adapter adapter, Resolver resolver) {
		public static Arg of(MemoryLayout layout, Adapter adapter, Resolver resolver, String format,
			Object... args) {
			return new Arg(Strings.format(format, args), layout, adapter, resolver);
		}

		public Object adapt(Arena arena, Object from) {
			if (adapter() == null) return from;
			return adapter().apply(arena, layout(), from);
		}

		public void resolve(Arena arena, Object from, Object to) {
			if (resolver() != null) resolver().accept(arena, layout(), from, to);
		}

		@Override
		public final String toString() {
			return name();
		}
	}

	/**
	 * Represents method return actions.
	 */
	public record Return(String name, MemoryLayout layout, Adapter adapter) {
		public static final Return VOID = new Return("void", null, null);

		public static Return of(MemoryLayout layout, Adapter adapter, String format,
			Object... args) {
			return new Return(Strings.format(format, args), layout, adapter);
		}

		public Object adapt(Arena arena, Object from) {
			if (adapter() == null) return from;
			return adapter().apply(arena, layout(), from);
		}

		@Override
		public final String toString() {
			return name();
		}
	}

	public static class Builder {
		private final String name;
		private Return rtn = Return.VOID;
		private final List<Arg> args = Lists.of();
		private int varArg = -1;
		private boolean lastError = false;

		private Builder(String name) {
			this.name = name;
		}

		public Builder rtn(Return rtn) {
			this.rtn = rtn;
			return this;
		}

		public Builder arg(Arg arg) {
			args.add(arg);
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
			if (lastError) options.add(CaptureState.OPTION);
			return options.toArray(Linker.Option[]::new);
		}

		private FunctionDescriptor funcDesc() {
			var argLayouts = Streams.from(args).map(Arg::layout).toArray(MemoryLayout[]::new);
			if (rtn.layout() == null) return FunctionDescriptor.ofVoid(argLayouts);
			return FunctionDescriptor.of(rtn.layout(), argLayouts);
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

	private Call(String name, MethodHandle handle, Return rtn, List<Arg> args, boolean lastError) {
		this.name = name;
		this.handle = handle;
		this.rtn = rtn;
		this.args = args;
		this.lastError = lastError;
	}

	public int argCount() {
		return args.size();
	}

	public Object invoke(Arena arena, Object[] args) throws Throwable {
		int index = lastError ? 1 : 0;
		var nativeArgs = nativeArgs(arena, args, index);
		var rtn = handle.invokeWithArguments(nativeArgs); // invokeExact(Object[]) fails
		var result = this.rtn.adapt(arena, rtn);
		if (lastError) CaptureState.validate(result, args);
		resolveArgs(arena, args, nativeArgs, index);
		return result;
	}

	@Override
	public String toString() {
		var t = ToString.ofName(rtn.name() + " " + name);
		for (var arg : args)
			t.values(arg.name());
		return lastError ? t + "!" : t.toString();
	}

	// support

	public Object[] nativeArgs(Arena arena, Object[] args, int index) {
		var nativeArgs = new Object[index + args.length];
		if (index > 0) nativeArgs[0] = CaptureState.capture(arena);
		for (int i = 0; i < argCount(); i++)
			nativeArgs[index + i] = arg(i).adapt(arena, args[i]);
		return nativeArgs;
	}

	public void resolveArgs(Arena arena, Object[] args, Object[] nativeArgs, int index) {
		for (int i = 0; i < argCount(); i++)
			arg(i).resolve(arena, args[i], nativeArgs[index + i]);
	}

	private Arg arg(int i) {
		return Lists.at(args, i);
	}
}
