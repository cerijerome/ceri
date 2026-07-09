package ceri.ffm.core;

import java.io.IOException;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import ceri.common.array.Array;
import ceri.common.collect.Lists;
import ceri.common.except.Exceptions;
import ceri.common.reflect.Reflect;
import ceri.ffm.clib.ffm.CStdLib;
import ceri.ffm.reflect.Refine;
import ceri.ffm.reflect.TypeNode;
import ceri.ffm.type.IntType;
import ceri.ffm.type.PointerType;
import ceri.ffm.type.Support;
import ceri.ffm.type.Supports;

/**
 * Builds native calls from interface methods and annotated configuration.
 */
public class Calls {
	private static final int ARG_ARRAY_LEN = 1024 * 1024; // max array length
	private static final int RTN_ARRAY_LEN = 256; // max array length
	private static final Supports.Defaults ARG_DEFS =
		Supports.defaults().string(ARG_ARRAY_LEN, true).buffer(ARG_ARRAY_LEN, false)
			.array1d(ARG_ARRAY_LEN, false).arrayNd(ARG_ARRAY_LEN, false).build();
	private static final Supports.Defaults RTN_DEFS =
		Supports.defaults().string(RTN_ARRAY_LEN, true).buffer(RTN_ARRAY_LEN, true)
			.array1d(RTN_ARRAY_LEN, true).arrayNd(0, false).build();
	public static final Calls DEF = builder().build();
	private final Linker linker;
	private final SymbolLookup lookup;
	private final Supports argSupports;
	private final Supports rtnSupports;

	public static void main(String[] args) throws IOException {
		System.out.println(CStdLib.getenv("USER"));
		System.out.println(CStdLib.getenv("TESTXXX"));
		CStdLib.setenv("TESTXXX", "xxx", false);
		System.out.println(CStdLib.getenv("TESTXXX"));
		CStdLib.setenv("TESTXXX", "yyy", false);
		System.out.println(CStdLib.getenv("TESTXXX"));
		CStdLib.setenv("TESTXXX", "yyy", true);
		System.out.println(CStdLib.getenv("TESTXXX"));
		System.out.println("Expecting: <user>, null, xxx, xxx, yyy");
	}

	public static class Builder {
		private Linker linker = Linker.nativeLinker();
		private SymbolLookup lookup = null;
		private Supports.Defaults argDefs = ARG_DEFS;
		private Supports.Defaults rtnDefs = RTN_DEFS;

		public Builder linker(Linker linker) {
			this.linker = linker;
			return this;
		}

		public Builder lookup(SymbolLookup lookup) {
			this.lookup = lookup;
			return this;
		}

		public Builder argDefs(Supports.Defaults argDefs) {
			this.argDefs = argDefs;
			return this;
		}

		public Builder rtnDefs(Supports.Defaults rtnDefs) {
			this.rtnDefs = rtnDefs;
			return this;
		}

		public Calls build() {
			return new Calls(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private Calls(Builder builder) {
		linker = builder.linker;
		lookup = builder.lookup != null ? builder.lookup : linker.defaultLookup();
		argSupports = Supports.of(builder.argDefs);
		rtnSupports = Supports.of(builder.rtnDefs);
	}

	/**
	 * Creates a call from the method.
	 */
	public Call call(Method method) {
		var b = Call.builder(method);
		rtn(b, method);
		var parameters = method.getParameters();
		int paramCount = paramCount(method);
		for (int i = 0; i < paramCount; i++)
			arg(b, parameters[i]);
		if (Refine.lastError(method, true)) b.lastError();
		return b.build(linker, lookup);
	}

	/**
	 * Extends a call with varargs.
	 */
	public Call varArgsCall(Call call, List<Class<?>> varArgTypes) {
		var b = Call.builder(call).varArg();
		var parameter = Array.last(call.method.getParameters());
		for (int i = 0; i < varArgTypes.size(); i++)
			varArg(b, parameter, varArgTypes, i);
		return b.build(linker, lookup);
	}

	// support

	private void arg(Call.Builder b, Parameter parameter) {
		var node = TypeNode.of(parameter);
		if (arg(b, node) == null) throw Exceptions.illegalArg("Unsupported arg type: %s (%s)",
			Reflect.localName(parameter), node.typed());
	}

	private void varArg(Call.Builder b, Parameter parameter, List<Class<?>> varArgs, int i) {
		var node = TypeNode.of(parameter).sub(Lists.at(varArgs, i));
		if (arg(b, node) == null)
			throw Exceptions.illegalArg("Unsupported vararg type: %s[%d] (%s)",
				Reflect.localName(parameter), i, node.typed());
	}

	private Call.Builder arg(Call.Builder b, TypeNode node) {
		var support = argSupports.from(node);
		if (support.isArray()) return byRefArg(b, support, node);
		return switch (support.kind()) {
			case PRIMITIVE, BOXED -> primitiveArg(b, support, node);
			case INT_TYPE -> intTypeArg(b, Reflect.unchecked(support), node);
			case POINTER, PRIMITIVE_POINTER -> pointerArg(b, Reflect.unchecked(support), node);
			case STRING, BUFFER -> byRefArg(b, support, node);
			case null -> null;
			default -> byValArg(b, support, node);
		};
	}

	private void rtn(Call.Builder b, Method method) {
		if (Reflect.isVoid(method)) return;
		var node = TypeNode.ofReturn(method);
		if (rtn(b, node) == null) throw Exceptions.illegalArg("Unsupported return type: %s (%s)",
			method.getName(), node.typed());
	}

	private Call.Builder rtn(Call.Builder b, TypeNode node) {
		var support = rtnSupports.from(node);
		if (support.isArray()) return byRefRtn(b, support, node);
		return switch (support.kind()) {
			case PRIMITIVE, BOXED -> primitiveRtn(b, support, node);
			case INT_TYPE -> intTypeRtn(b, Reflect.unchecked(support), node);
			case POINTER, PRIMITIVE_POINTER -> pointerRtn(b, Reflect.unchecked(support), node);
			case STRING, BUFFER -> byRefRtn(b, support, node);
			case null -> null;
			default -> byValRtn(b, support, node);
		};
	}

	private static Call.Builder primitiveArg(Call.Builder b, Support<?, ?, ?> support,
		TypeNode node) {
		return b.arg(node.typed(), support.layout(), (_, t) -> Native.Adapted.of(t));
	}

	private static <T extends IntType<T>> Call.Builder intTypeArg(Call.Builder b,
		IntType.Supporter<T> support, TypeNode node) {
		return b.<T, Number>arg(node.typed(), support.layout(),
			(_, t) -> Native.Adapted.of(t.nativeValue()));
	}

	private static <P extends PointerType.Raw> Call.Builder pointerArg(Call.Builder b,
		PointerType.Supporter<P> support, TypeNode node) {
		return b.<P, MemorySegment>arg(node.typed(), support.layout(),
			(_, t) -> Native.Adapted.of(t.memory()));
	}

	private static <T> Call.Builder byValArg(Call.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		return b.<T, MemorySegment>arg(node.typed(), support.layout(),
			(a, t) -> Native.Adapted.of(support.alloc(a, t)));
	}

	private static <T> Call.Builder byRefArg(Call.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		var direction = node.context().direction();
		return b.<T, MemorySegment>arg(node.typed(), Layouts.POINTER,
			(a, t) -> support.encode(direction, a, t));
	}

	private static Call.Builder primitiveRtn(Call.Builder b, Support<?, ?, ?> support,
		TypeNode node) {
		return b.rtn(node.typed(), support.layout(), t -> t);
	}

	private static <T extends IntType<T>> Call.Builder intTypeRtn(Call.Builder b,
		IntType.Supporter<T> support, TypeNode node) {
		return b.<Number, T>rtn(node.typed(), support.layout(), n -> support.of(n));
	}

	private static <P extends PointerType.Raw> Call.Builder pointerRtn(Call.Builder b,
		PointerType.Supporter<P> support, TypeNode node) {
		return b.<MemorySegment, P>rtn(node.typed(), support.layout(), m -> support.of(m));
	}

	private static <T> Call.Builder byValRtn(Call.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		return b.<MemorySegment, T>rtn(node.typed(), support.layout(),
			m -> support.get(Segments.reslice(m, support.layout())));
	}

	private static <T> Call.Builder byRefRtn(Call.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		return b.<MemorySegment, T>rtn(node.typed(), Layouts.POINTER,
			m -> support.decode(Segments.reslice(m, support.layout())));
	}

	private static int paramCount(Method method) {
		return method.isVarArgs() ? method.getParameterCount() - 1 : method.getParameterCount();
	}
}
