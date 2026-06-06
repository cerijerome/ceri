package ceri.ffm.core;

import java.io.IOException;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.Buffer;
import java.util.List;
import ceri.common.array.Array;
import ceri.common.collect.Lists;
import ceri.common.except.Exceptions;
import ceri.common.io.Buffers;
import ceri.common.io.Direction;
import ceri.common.reflect.Reflect;
import ceri.ffm.clib.ffm.CStdLib;
import ceri.ffm.reflect.Refine;
import ceri.ffm.reflect.TypeNode;
import ceri.ffm.type.BufferType;
import ceri.ffm.type.IntType;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;
import ceri.ffm.type.StringType;
import ceri.ffm.type.Struct;
import ceri.ffm.type.Support;
import ceri.ffm.type.Union;

/**
 * Builds native calls from interface methods and annotated configuration.
 */
public class Calls {
	private static final String RETURN = "return";
	private static final AddressLayout POINTER_RETURN = Layouts.POINTER.withName(RETURN);
	private final Linker linker;
	private final SymbolLookup lookup;

	// string with fixed size (+/-nul) => support
	// string no fixed size (+nul) => stringtype

	// buffer with fixed size (+/-nul) => support
	// buffer no fixed size (+nul) => buffertype

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
		var b = rtn(Call.builder(method), method);
		var parameters = method.getParameters();
		int paramCount = paramCount(method);
		for (int i = 0; i < paramCount; i++)
			arg(b, parameters[i]);
		if (Refine.lastError(method, false)) b.lastError();
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

	private static void arg(Call.Builder b, Parameter parameter) {
		var node = TypeNode.of(parameter);
		var arg = arg(parameter.getName(), node);
		if (arg != null) b.arg(node.typed(), arg);
		else throw Exceptions.illegalArg("Unsupported arg type: %s (%s)",
			Reflect.localName(parameter), node.typed());
	}

	private static void varArg(Call.Builder b, Parameter parameter, List<Class<?>> varArgs, int i) {
		var node = TypeNode.of(parameter).sub(Lists.at(varArgs, i));
		var arg = arg(parameter.getName() + "[" + i + "]", node);
		if (arg != null) b.arg(node.typed(), arg);
		else throw Exceptions.illegalArg("Unsupported vararg type: %s[%d] (%s)",
			Reflect.localName(parameter), i, node.typed());
	}

	private static Call.Builder rtn(Call.Builder b, Method method) {
		if (Reflect.isVoid(method)) return b;
		var node = TypeNode.ofReturn(method);
		var rtn = rtn(node);
		if (rtn != null) return b.rtn(node.typed(), rtn);
		throw Exceptions.illegalArg("Unsupported return type: %s (%s)", method.getName(),
			node.typed());
	}

	private static Call.Arg<?, ?> arg(String name, TypeNode node) {
		var spec = node.spec();
		return switch (spec.kind()) {
			case primitive, boxed -> primitiveArg(name, spec, node.context());
			case intType -> intArg(name, spec, node.context());
			case struct -> structArg(name, spec, node.context());
			case union -> unionArg(name, spec, node.context());
			case pointer -> pointerArg(name, node);
			case primitivePointer -> null; // TODO
			case pointerType -> null; // TODO
			case functionPointer -> null; // TODO
			case string -> stringArg(name, spec, node.context());
			case buffer -> bufferArg(name, spec, node.context());
			default -> null;
		};
	}

	private static Call.Arg<?, ?> primitiveArg(String name, Native.Spec spec,
		Refine.Context context) {
		var support = Support.with(Primitive.of(spec.component()), context);
		if (spec.isArray()) return arrayArg(name, support, context, context.nul());
		return new Call.Arg<>(support.layout().withName(name), (_, t) -> t, null);
	}

	private static <T extends IntType<T>> Call.Arg<?, ?> intArg(String name, Native.Spec spec,
		Refine.Context context) {
		var support = Support.with(IntType.support(spec.<T>component()), context);
		if (spec.isArray()) return arrayArg(name, support, context, context.nul());
		return new Call.Arg<T, Number>(support.layout().withName(name), (_, t) -> t.nativeValue(),
			null);
	}

	private static Call.Arg<?, ?> structArg(String name, Native.Spec spec, Refine.Context context) {
		var support = Struct.support(spec.component());
		return byValArg(name, support, spec, context);
	}

	private static Call.Arg<?, ?> unionArg(String name, Native.Spec spec, Refine.Context context) {
		var support = Union.support(spec.component());
		return byValArg(name, support, spec, context);
	}

	private static Call.Arg<?, ?> pointerArg(String name, TypeNode node) {
		var support = Pointer.support(node);
		return byValArg(name, support, node.spec(), node.context());
	}

	private static Call.Arg<?, ?> stringArg(String name, Native.Spec spec, Refine.Context context) {
		var string = StringType.of(context.chars());
		var nul = context.nul(true);
		var count = context.size(); // fixed size if > 0
		if (count > 0) return byRefArg(name, string.support(count, nul), spec, context);
		if (spec.isArray()) return stringNulArrayArg(name, string, context);
		return new Call.Arg<String, MemorySegment>(Layouts.POINTER.withName(name),
			(a, s) -> string.alloc(a, s, nul), null);
	}

	private static <B extends Buffer> Call.Arg<?, ?> bufferArg(String name, Native.Spec spec,
		Refine.Context context) {
		var buffer = BufferType.<B>of(spec.component());
		var nul = context.nul();
		var count = context.size(); // fixed size if > 0
		if (count > 0) return byRefArg(name, buffer.support(count, nul), spec, context);
		if (spec.isArray()) return bufferArrayArg(name, buffer, context, nul);
		var direction = context.direction();
		return new Call.Arg<B, MemorySegment>(Layouts.POINTER.withName(name),
			(a, b) -> Buffers.isDirect(b) ? buffer.ofBuffer(b) : buffer.alloc(a, b, nul),
			Direction.out(direction) ? (b, m) -> {
				if (!Buffers.isDirect(b)) buffer.read(m, b, nul);
			} : null);
	}

	private static <T> Call.Arg<?, ?> byValArg(String name, Support<T, ?, ?> support,
		Native.Spec spec, Refine.Context context) {
		var s = Support.with(support, context);
		if (spec.isArray()) return arrayArg(name, s, context, context.nul());
		return new Call.Arg<T, MemorySegment>(s.layout().withName(name), (a, t) -> s.alloc(a, t),
			null);
	}

	private static <T> Call.Arg<?, ?> byRefArg(String name, Support<T, ?, ?> support,
		Native.Spec spec, Refine.Context context) {
		var s = Support.with(support, context);
		if (spec.isArray()) return arrayArg(name, s, context, false);
		return new Call.Arg<T, MemorySegment>(Layouts.POINTER.withName(name),
			(a, t) -> s.alloc(a, t), null);
	}

	private static Call.Arg<Object, MemorySegment> arrayArg(String name, Support<?, ?, ?> support,
		Refine.Context context, boolean nul) {
		var direction = context.direction();
		return new Call.Arg<>(Layouts.POINTER.withName(name),
			Direction.in(direction) ? (a, t) -> support.deepAlloc(a, t, nul) :
				(a, t) -> support.deepAllocEmpty(a, t, nul),
			Direction.out(direction) ? (t, m) -> support.deepRead(m, t, nul) : null);
	}

	private static Call.Arg<Object, MemorySegment> stringNulArrayArg(String name,
		StringType support, Refine.Context context) {
		var direction = context.direction();
		var count = context.size(Refine.NUL_MAX_DEF);
		return new Call.Arg<>(Layouts.POINTER.withName(name),
			(a, t) -> support.deepAlloc(a, t, true),
			Direction.out(direction) ? (t, m) -> support.deepRead(m, t, count, true) : null);
	}

	private static <B extends Buffer> Call.Arg<Object, MemorySegment> bufferArrayArg(String name,
		BufferType<B, ?, ?, ?> support, Refine.Context context, boolean nul) {
		var direction = context.direction();
		var count = context.size(Refine.NUL_MAX_DEF);
		return new Call.Arg<>(Layouts.POINTER.withName(name),
			Direction.in(direction) ? (a, t) -> support.deepAlloc(a, t, nul) :
				(a, t) -> support.deepAllocEmpty(a, t, nul),
			Direction.out(direction) ? (t, m) -> support.deepRead(m, t, count, nul) : null);
	}

	private static Call.Return<?, ?> rtn(TypeNode node) {
		var spec = node.spec();
		return switch (spec.kind()) {
			case primitive, boxed -> primitiveRtn(spec, node.context());
			case intType -> intRtn(spec, node.context());
			case struct -> structRtn(spec, node.context());
			case union -> unionRtn(spec, node.context());
			case pointer -> pointerRtn(node);
			case primitivePointer -> null; // TODO
			case pointerType -> null; // TODO
			case functionPointer -> null; // TODO
			case string -> stringRtn(spec, node.context());
			case buffer -> bufferRtn(spec, node.context());
			default -> null;
		};
	}

	private static Call.Return<?, ?> primitiveRtn(Native.Spec spec, Refine.Context context) {
		var support = Support.with(Primitive.of(spec.component()), context);
		if (spec.isArray()) return arrayRtn(support, spec, context, context.nul());
		return new Call.Return<>(support.layout().withName(RETURN), t -> t);
	}

	private static <T extends IntType<T>> Call.Return<?, ?> intRtn(Native.Spec spec,
		Refine.Context context) {
		var support = Support.with(IntType.support(spec.<T>component()), context);
		if (spec.isArray()) return arrayRtn(support, spec, context, context.nul());
		return new Call.Return<Number, T>(support.layout().withName(RETURN), n -> support.of(n));
	}

	private static Call.Return<?, ?> structRtn(Native.Spec spec, Refine.Context context) {
		var support = Struct.support(spec.component());
		return byValRtn(support, spec, context);
	}

	private static Call.Return<?, ?> unionRtn(Native.Spec spec, Refine.Context context) {
		var support = Union.support(spec.component());
		return byValRtn(support, spec, context);
	}

	private static Call.Return<?, ?> pointerRtn(TypeNode node) {
		var support = Pointer.support(node);
		return byValRtn(support, node.spec(), node.context());
	}

	private static Call.Return<?, ?> stringRtn(Native.Spec spec, Refine.Context context) {
		var string = StringType.of(context.chars());
		var nul = context.nul(true);
		var count = context.size(Refine.NUL_MAX_DEF);
		if (nul && spec.isArray()) return stringNulArrayRtn(string, spec, context, count);
		return byRefRtn(string.support(count, nul), spec, context);
	}

	private static <B extends Buffer> Call.Return<?, ?> bufferRtn(Native.Spec spec,
		Refine.Context context) {
		var buffer = BufferType.<B>of(spec.component());
		var nul = context.nul();
		var count = context.size(Refine.NUL_MAX_DEF);
		if (nul && spec.isArray()) return bufferNulArrayRtn(buffer, spec, context, count);
		return byRefRtn(buffer.support(count, nul), spec, context);
	}

	private static <T> Call.Return<?, ?> byValRtn(Support<T, ?, ?> support, Native.Spec spec,
		Refine.Context context) {
		var s = Support.with(support, context);
		if (spec.isArray()) return arrayRtn(s, spec, context, context.nul());
		return new Call.Return<MemorySegment, T>(s.layout().withName(RETURN),
			m -> s.get(Segments.reslice(m, s.layout())));
	}

	private static <T> Call.Return<?, ?> byRefRtn(Support<T, ?, ?> support, Native.Spec spec,
		Refine.Context context) {
		var s = Support.with(support, context);
		if (spec.isArray()) return arrayRtn(s, spec, context, false);
		return new Call.Return<MemorySegment, T>(POINTER_RETURN,
			m -> s.get(Segments.reslice(m, s.layout())));
	}

	private static Call.Return<MemorySegment, Object> arrayRtn(Support<?, ?, ?> support,
		Native.Spec spec, Refine.Context context, boolean nul) {
		var dims = context.dims(spec.dimensions(), nul, 0);
		var size = support.size(dims.total());
		return new Call.Return<>(POINTER_RETURN,
			m -> support.deepGet(Segments.reslice(m, 0L, size), dims, nul));
	}

	private static Call.Return<MemorySegment, Object> stringNulArrayRtn(StringType string,
		Native.Spec spec, Refine.Context context, int count) {
		var dims = context.dims(spec.dimensions(), false, 0);
		var size = string.size(dims.total() * count);
		return new Call.Return<>(POINTER_RETURN,
			m -> string.deepGet(Segments.reslice(m, 0L, size), dims, count, true));
	}

	private static <B extends Buffer> Call.Return<MemorySegment, Object> bufferNulArrayRtn(
		BufferType<B, ?, ?, ?> buffer, Native.Spec spec, Refine.Context context, int count) {
		var dims = context.dims(spec.dimensions(), false, 0);
		var size = buffer.size(dims.total() * count);
		return new Call.Return<>(POINTER_RETURN,
			m -> buffer.deepGet(Segments.reslice(m, 0L, size), dims, count, true));
	}

	private static int paramCount(Method method) {
		return method.isVarArgs() ? method.getParameterCount() - 1 : method.getParameterCount();
	}
}
