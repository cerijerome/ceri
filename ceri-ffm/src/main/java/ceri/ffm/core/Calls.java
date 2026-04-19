package ceri.ffm.core;

import java.io.IOException;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.Buffer;
import java.util.List;
import ceri.common.array.Array;
import ceri.common.array.Dimensions;
import ceri.common.collect.Lists;
import ceri.common.except.Exceptions;
import ceri.common.io.Buffers;
import ceri.common.io.Direction;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.ffm.clib.ffm.CStdLib;
import ceri.ffm.reflect.Refine;
import ceri.ffm.type.BufferType;
import ceri.ffm.type.IntType;
import ceri.ffm.type.MultiArray;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;
import ceri.ffm.type.StringType;
import ceri.ffm.type.Support;

/**
 * Builds native calls from interface methods and annotated configuration.
 */
public class Calls {
	private static final int NUL_TERM_MAX_DEF = 0x100;
	private final Linker linker;
	private final SymbolLookup lookup;

	public static void main(String[] args) throws IOException {
		System.out.println(CStdLib.getenv("USER"));
		System.out.println(CStdLib.getenv("TESTXXX"));
		CStdLib.setenv("TESTXXX", "xxx", false);
		System.out.println(CStdLib.getenv("TESTXXX"));
		CStdLib.setenv("TESTXXX", "yyy", false);
		System.out.println(CStdLib.getenv("TESTXXX"));
		CStdLib.setenv("TESTXXX", "yyy", true);
		System.out.println(CStdLib.getenv("TESTXXX"));
	}

	public static Calls of(Linker linker, SymbolLookup lookup) {
		return new Calls(linker, lookup);
	}

	private Calls(Linker linker, SymbolLookup lookup) {
		this.linker = linker;
		this.lookup = lookup;
	}

	/**
	 * Creates a call from the method.
	 */
	public Call call(Method method) {
		var b = Call.builder(method.getName());
		rtn(b, method);
		var parameters = method.getParameters();
		for (int i = 0; i < paramCount(method); i++)
			arg(b, method, parameters[i]);
		if (Refine.lastError(method, false)) b.lastError();
		return b.build(linker, lookup);
	}

	/**
	 * Extends a call with varargs.
	 */
	public Call varArgsCall(Call call, Method method, List<Class<?>> varArgTypes) {
		var b = Call.builder(call).varArg();
		var context = Refine.context(method);
		var parameter = Array.last(method.getParameters());
		for (int i = 0; i < varArgTypes.size(); i++)
			varArg(b, context, method, parameter, varArgTypes, i);
		return b.build(linker, lookup);
	}

	// support

	private static void arg(Call.Builder b, Method method, Parameter parameter) {
		var spec = Native.Kind.validSpec(Generics.typed(parameter));
		var context = Refine.context(parameter);
		var arg = arg(context, spec);
		if (arg != null) b.arg(spec.typed(), arg);
		else throw Exceptions.illegalArg("Unsupported arg: %s.%s (%s)", method.getName(),
			parameter.getName(), spec.typed());
	}

	private static void varArg(Call.Builder b, Refine.Context context, Method method,
		Parameter parameter, List<Class<?>> varArgs, int i) {
		var cls = Lists.at(varArgs, i);
		var spec = Native.Kind.validSpec(cls);
		var arg = arg(context, spec);
		if (arg != null) b.arg(spec.typed(), arg);
		else throw Exceptions.illegalArg("Unsupported vararg: %s.%s[%d] (%s)", method.getName(),
			parameter.getName(), i, spec.typed());
	}

	private static void rtn(Call.Builder b, Method method) {
		if (Reflect.isVoid(method)) return;
		var spec = Native.Kind.validSpec(Generics.typedReturn(method));
		var rtn = rtn(Refine.context(method), spec);
		if (rtn != null) b.rtn(spec.typed(), rtn);
		else throw Exceptions.illegalArg("Unsupported return type: %s (%s)", method.getName(),
			spec.typed());
	}

	private static Call.Arg<?, ?> arg(Refine.Context context, Native.Kind.Spec spec) {
		return switch (spec.kind()) {
			case primitive, boxed -> primitiveArg(context, spec);
			case intType -> intArg(context, spec);
			case string -> stringArg(context, spec);
			case buffer -> bufferArg(context, spec);
			case pointer -> pointerArg(context, spec);
			default -> null;
		};
	}

	private static Call.Arg<?, ?> primitiveArg(Refine.Context context, Native.Kind.Spec spec) {
		var support = support(Primitive.of(spec.component()), context);
		if (spec.array().isArray()) return arrayArg(support, context);
		return new Call.Arg<>(support.layout(), (_, t) -> t, null);
	}

	private static <T extends IntType<T>> Call.Arg<?, ?> intArg(Refine.Context context,
		Native.Kind.Spec spec) {
		var support = support(IntType.<T>support(spec.component()), context);
		if (spec.array().isArray()) return arrayArg(support, context);
		return new Call.Arg<T, Number>(support.layout(), (_, t) -> t.nativeValue(), null);
	}

	private static Call.Arg<?, ?> stringArg(Refine.Context context, Native.Kind.Spec spec) {
		var support = StringType.of(context.chars(null));
		if (spec.array().isArray()) return stringArrayArg(support, context);
		return new Call.Arg<String, MemorySegment>(Layouts.POINTER,
			(a, s) -> support.alloc(a, s, true), null);
	}

	private static <B extends Buffer> Call.Arg<?, ?> bufferArg(Refine.Context context,
		Native.Kind.Spec spec) {
		var support = BufferType.<B>of(spec.component());
		if (spec.array().isArray()) return bufferArrayArg(support, context);
		var dir = context.direction(Direction.duplex);
		var nul = context.nul();
		return new Call.Arg<B, MemorySegment>(Layouts.POINTER,
			(a, b) -> Buffers.isDirect(b) ? support.ofBuffer(b) : support.alloc(a, b, nul),
			Direction.out(dir) ? (b, m) -> {
				if (!Buffers.isDirect(b)) support.read(m, b, nul);
			} : null);
	}

	private static <T> Call.Arg<?, ?> pointerArg(Refine.Context context, Native.Kind.Spec spec) {
		var type = Pointer.Type.of(spec.typed().type(0));
		var support = support(Pointer.Support.VOID.as(type), context);
		if (spec.array().isArray()) return arrayArg(support, context);
		return new Call.Arg<Pointer<T>, MemorySegment>(support.layout(), (_, p) -> p.memory(),
			null);
	}

	private static Call.Arg<Object, MemorySegment> arrayArg(Support<?, ?, ?> support,
		Refine.Context context) {
		var dir = context.direction(Direction.duplex);
		var nul = context.nul();
		return new Call.Arg<>(Layouts.POINTER,
			Direction.in(dir) ? (a, t) -> support.deepAlloc(a, t, nul) :
				(a, t) -> support.deepAllocEmpty(a, t, nul),
			Direction.out(dir) ? (t, m) -> support.deepRead(m, t, nul) : null);
	}

	private static Call.Arg<Object, MemorySegment> stringArrayArg(StringType support,
		Refine.Context context) {
		var dir = context.direction(Direction.duplex);
		var count = context.size(NUL_TERM_MAX_DEF);
		return new Call.Arg<>(Layouts.POINTER, (a, t) -> support.deepAlloc(a, t, true),
			Direction.out(dir) ? (t, m) -> support.deepRead(m, t, count, true) : null);
	}

	private static <B extends Buffer> Call.Arg<Object, MemorySegment>
		bufferArrayArg(BufferType<B, ?, ?, ?> support, Refine.Context context) {
		var dir = context.direction(Direction.duplex);
		var nul = context.nul();
		var count = context.size(NUL_TERM_MAX_DEF);
		return new Call.Arg<>(Layouts.POINTER,
			Direction.in(dir) ? (a, t) -> support.deepAlloc(a, t, nul) :
				(a, t) -> support.deepAllocEmpty(a, t, nul),
			Direction.out(dir) ? (t, m) -> support.deepRead(m, t, count, nul) : null);
	}

	private static Call.Return<?, ?> rtn(Refine.Context context, Native.Kind.Spec spec) {
		return switch (spec.kind()) {
			case primitive, boxed -> primitiveRtn(context, spec);
			case intType -> intRtn(context, spec);
			case string -> stringRtn(context, spec);
			case buffer -> bufferRtn(context, spec);
			case pointer -> pointerRtn(context, spec);
			default -> null;
		};
	}

	private static Call.Return<?, ?> primitiveRtn(Refine.Context context, Native.Kind.Spec spec) {
		var support = support(Primitive.of(spec.component()), context);
		if (spec.array().isArray()) return arrayRtn(support, context, spec);
		return new Call.Return<>(support.layout(), t -> t);
	}

	private static <T extends IntType<T>> Call.Return<?, ?> intRtn(Refine.Context context,
		Native.Kind.Spec spec) {
		var support = support(IntType.<T>support(spec.component()), context);
		if (spec.array().isArray()) return arrayRtn(support, context, spec);
		return new Call.Return<Number, T>(support.layout(), n -> IntType.of(spec.component(), n));
	}

	private static Call.Return<?, ?> stringRtn(Refine.Context context, Native.Kind.Spec spec) {
		var support = StringType.of(context.chars(null));
		if (spec.array().isArray()) return stringArrayRtn(support, context, spec);
		var size = support.size(context.size(NUL_TERM_MAX_DEF));
		return new Call.Return<MemorySegment, String>(Layouts.POINTER,
			m -> support.get(Segments.reslice(m, 0L, size), true));
	}

	private static <B extends Buffer> Call.Return<?, ?> bufferRtn(Refine.Context context,
		Native.Kind.Spec spec) {
		var support = BufferType.<B>of(spec.component());
		if (spec.array().isArray()) return bufferArrayRtn(support, context, spec);
		var nul = context.nul();
		var size = support.size(context.size(NUL_TERM_MAX_DEF), nul);
		return new Call.Return<MemorySegment, B>(Layouts.POINTER,
			m -> support.asBuffer(Segments.reslice(m, 0L, size), nul));
	}

	private static <T> Call.Return<?, ?> pointerRtn(Refine.Context context, Native.Kind.Spec spec) {
		var type = Pointer.Type.<T>of(spec.typed().type(0));
		var support = support(Pointer.Support.VOID.as(type), context);
		if (spec.array().isArray()) return arrayRtn(support, context, spec);
		return new Call.Return<MemorySegment, Pointer<T>>(support.layout(),
			m -> Pointer.of(type, Segments.reslice(m, 0L, support.layoutSize())));
	}

	private static Call.Return<MemorySegment, Object> arrayRtn(Support<?, ?, ?> support,
		Refine.Context context, Native.Kind.Spec spec) {
		var nul = context.nul();
		var dims = dims(context, spec, nul);
		var size = support.size(dims.total());
		return new Call.Return<>(Layouts.POINTER,
			m -> support.deepGet(Segments.reslice(m, 0L, size), dims, nul));
	}

	private static Call.Return<MemorySegment, Object> stringArrayRtn(StringType support,
		Refine.Context context, Native.Kind.Spec spec) {
		var dims = dims(context, spec, false);
		var count = context.size(NUL_TERM_MAX_DEF);
		var size = support.size(dims.total() * count);
		return new Call.Return<>(Layouts.POINTER,
			m -> support.deepGet(Segments.reslice(m, 0L, size), dims, count, true));
	}

	private static <B extends Buffer> Call.Return<MemorySegment, Object> bufferArrayRtn(
		BufferType<B, ?, ?, ?> support, Refine.Context context, Native.Kind.Spec spec) {
		var nul = context.nul();
		var dims = dims(context, spec, false);
		var count = context.size(NUL_TERM_MAX_DEF);
		var size = support.size(dims.total() * count);
		return new Call.Return<>(Layouts.POINTER,
			m -> support.deepGet(Segments.reslice(m, 0L, size), dims, count, nul));
	}

	private static Dimensions dims(Refine.Context context, Native.Kind.Spec spec, boolean nul) {
		return MultiArray.fix(context.dims(Dimensions.NONE), spec.array().dimensions(), nul,
			NUL_TERM_MAX_DEF);
	}

	private static int paramCount(Method method) {
		return method.isVarArgs() ? method.getParameterCount() - 1 : method.getParameterCount();
	}

	private static <T extends Support<?, ?, ?>> T support(T support, Refine.Context context) {
		return Support.with(support, null, context.align(), context.order());
	}
}
