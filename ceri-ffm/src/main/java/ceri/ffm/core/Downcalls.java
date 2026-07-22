package ceri.ffm.core;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import ceri.common.array.Array;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.except.Exceptions;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;
import ceri.ffm.reflect.Refine;
import ceri.ffm.reflect.TypeNode;
import ceri.ffm.type.IntType;
import ceri.ffm.type.PointerType;
import ceri.ffm.type.Support;
import ceri.ffm.type.Supports;

/**
 * Creates, caches, and invokes native downcalls. Downcalls only support local to native argument
 * adapters and cannot be used for function pointers.
 */
public class Downcalls {
	private final Map<Call.Key, Call> cache = Maps.concurrent();
	private final SymbolLookup lookup;

	/**
	 * Creates an cache instance.
	 */
	public static Downcalls of(SymbolLookup lookup) {
		return new Downcalls(lookup);
	}

	private Downcalls(SymbolLookup lookup) {
		this.lookup = lookup;
	}

	/**
	 * Returns a copy of the current method cache.
	 */
	public Map<Call.Key, Call> methods() {
		return new TreeMap<>(cache);
	}

	/**
	 * Invokes the native call for the method. The downcall is created on first call, then cached.
	 */
	public Object invoke(SegmentAllocator allocator, Method method, Object[] args)
		throws Throwable {
		args = Basics.def(args, Array.OBJECT.empty);
		var call = call(method, args);
		return call.invoke(allocator, args);
	}

	// support

	private Call call(Method method, Object[] args) {
		var call = call(method);
		if (!method.isVarArgs() || args.length == 0) return call;
		return varArgsCall(call, args);
	}

	private Call call(Method method) {
		var key = Call.Key.of(method);
		return cache.computeIfAbsent(key, _ -> createCall(method));
	}

	private Call varArgsCall(Call call, Object[] args) {
		var key = Call.Key.from(call.config().method(), args);
		return cache.computeIfAbsent(key, _ -> createVarArgsCall(call, key));
	}

	private Call createCall(Method method) {
		var pointer = lookup.findOrThrow(method.getName());
		var config = config(method);
		return Call.of(config, pointer);
	}

	private Call createVarArgsCall(Call call, Call.Key key) {
		var config = varArgsConfig(call.config(), key.varArgTypes());
		return Call.of(config, call.pointer());
	}

	private static Call.Config config(Method method) {
		var b = Call.Config.builder(method);
		rtn(b, method);
		var parameters = method.getParameters();
		int paramCount = parameters.length;
		if (method.isVarArgs()) paramCount--;
		for (int i = 0; i < paramCount; i++)
			arg(b, parameters[i]);
		if (Refine.lastError(method, true)) b.lastError();
		return b.build();
	}

	private static Call.Config varArgsConfig(Call.Config config, List<Class<?>> varArgTypes) {
		var b = config.builder().varArg();
		var parameter = Array.last(config.method().getParameters());
		for (int i = 0; i < varArgTypes.size(); i++)
			varArg(b, parameter, varArgTypes, i);
		return b.build();
	}

	private static void arg(Call.Config.Builder b, Parameter parameter) {
		var node = TypeNode.of(parameter);
		if (arg(b, node) == null) throw Exceptions.illegalArg("Unsupported arg type: %s (%s)",
			Reflect.localName(parameter), node.typed());
	}

	private static void varArg(Call.Config.Builder b, Parameter parameter, List<Class<?>> varArgs,
		int i) {
		var node = TypeNode.of(parameter).sub(Lists.at(varArgs, i));
		if (arg(b, node) == null)
			throw Exceptions.illegalArg("Unsupported vararg type: %s[%d] (%s)",
				Reflect.localName(parameter), i, node.typed());
	}

	private static Call.Config.Builder arg(Call.Config.Builder b, TypeNode node) {
		var support = Supports.of().from(node);
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

	private static void rtn(Call.Config.Builder b, Method method) {
		if (Reflect.isVoid(method)) return;
		var node = TypeNode.ofReturn(method);
		if (rtn(b, node) == null) throw Exceptions.illegalArg("Unsupported return type: %s (%s)",
			method.getName(), node.typed());
	}

	private static Call.Config.Builder rtn(Call.Config.Builder b, TypeNode node) {
		var support = Supports.of().from(node);
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

	private static Call.Config.Builder primitiveArg(Call.Config.Builder b, Support<?, ?, ?> support,
		TypeNode node) {
		return b.arg(node.typed(), support.layout(), (_, t) -> Native.Adapted.of(t));
	}

	private static <T extends IntType<T>> Call.Config.Builder intTypeArg(Call.Config.Builder b,
		IntType.Supporter<T> support, TypeNode node) {
		return b.<T, Number>arg(node.typed(), support.layout(),
			(_, t) -> Native.Adapted.of(t.nativeValue()));
	}

	private static <P extends PointerType.Raw> Call.Config.Builder pointerArg(Call.Config.Builder b,
		PointerType.Supporter<P> support, TypeNode node) {
		return b.<P, MemorySegment>arg(node.typed(), support.layout(),
			(_, t) -> Native.Adapted.of(t.memory()));
	}

	private static <T> Call.Config.Builder byValArg(Call.Config.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		return b.<T, MemorySegment>arg(node.typed(), support.layout(),
			(a, t) -> Native.Adapted.of(support.alloc(a, t)));
	}

	private static <T> Call.Config.Builder byRefArg(Call.Config.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		var direction = node.context().direction();
		return b.<T, MemorySegment>arg(node.typed(), Layouts.POINTER,
			(a, t) -> support.encode(direction, a, t));
	}

	private static Call.Config.Builder primitiveRtn(Call.Config.Builder b, Support<?, ?, ?> support,
		TypeNode node) {
		return b.rtn(node.typed(), support.layout(), t -> t);
	}

	private static <T extends IntType<T>> Call.Config.Builder intTypeRtn(Call.Config.Builder b,
		IntType.Supporter<T> support, TypeNode node) {
		return b.<Number, T>rtn(node.typed(), support.layout(), n -> support.of(n));
	}

	private static <P extends PointerType.Raw> Call.Config.Builder pointerRtn(Call.Config.Builder b,
		PointerType.Supporter<P> support, TypeNode node) {
		return b.<MemorySegment, P>rtn(node.typed(), support.layout(), m -> support.of(m));
	}

	private static <T> Call.Config.Builder byValRtn(Call.Config.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		return b.<MemorySegment, T>rtn(node.typed(), support.layout(),
			m -> support.get(Segments.reslice(m, support.layout())));
	}

	private static <T> Call.Config.Builder byRefRtn(Call.Config.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		return b.<MemorySegment, T>rtn(node.typed(), Layouts.POINTER,
			m -> support.decode(Segments.reslice(m, support.layout())));
	}
}
