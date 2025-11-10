package ceri.ffm.core;

import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ceri.common.array.Array;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.ffm.reflect.Refine;

/**
 * Builds native calls from interface methods and annotated configuration.
 */
public class Calls {
	private static final Set<Class<?>> VALUES = Set.of(boolean.class, Boolean.class, char.class,
		Character.class, byte.class, Byte.class, short.class, Short.class, int.class, Integer.class,
		long.class, Long.class, float.class, Float.class, double.class, Double.class);
	private static final Map<Class<?>, Arg> ARGS = args();
	private static final Map<Class<?>, Return> RETURNS = returns();
	private final Linker linker;
	private final SymbolLookup lookup;

	public record Context(Refine.Context refine) {}

	private interface Arg extends Functions.Function<Context, Call.Arg> {}

	private interface Return extends Functions.Function<Context, Call.Return> {}

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
		var b = Call.builder(method.getName()).rtn(rtn(method));
		var parameters = method.getParameters();
		for (int i = 0; i < paramCount(method); i++) {
			var context = context(parameters[i]);
			b.arg(arg(context, method, parameters[i]));
		}
		if (lastError(method)) b.lastError();
		return b.build(linker, lookup);
	}

	/**
	 * Extends a call with varargs.
	 */
	public Call varArgsCall(Call call, Method method, List<Class<?>> varArgTypes) {
		var b = Call.builder(call).varArg();
		var context = context(method);
		var parameter = Array.last(method.getParameters());
		for (int i = 0; i < varArgTypes.size(); i++)
			b.arg(varArg(context, method, parameter, varArgTypes, i));
		return b.build(linker, lookup);
	}

	// support

	private Call.Return rtn(Method method) {
		if (Reflect.isVoid(method)) return Call.Return.VOID;
		var context = context(method);
		var type = method.getReturnType();
		var rtn = rtn(context, type);
		if (rtn != null) return rtn;
		throw Exceptions.illegalArg("Unsupported return type: %s (%s)", method.getName(), type);
	}

	private Call.Arg arg(Context context, Method method, Parameter parameter) {
		var type = parameter.getType();
		var arg = arg(context, type);
		if (arg != null) return arg;
		throw Exceptions.illegalArg("Unsupported arg: %s.%s (%s)", method.getName(),
			parameter.getName(), type);
	}

	private Call.Arg varArg(Context context, Method method, Parameter parameter,
		List<Class<?>> varArgs, int i) {
		var type = Lists.at(varArgs, i);
		var arg = arg(context, type);
		if (arg != null) return arg;
		throw Exceptions.illegalArg("Unsupported vararg: %s.%s[%d] (%s)", method.getName(),
			parameter.getName(), i, type);
	}

	private static int paramCount(Method method) {
		return method.isVarArgs() ? method.getParameterCount() - 1 : method.getParameterCount();
	}

	private static boolean lastError(Method method) {
		for (var type : method.getExceptionTypes())
			if (LastErrorException.class.isAssignableFrom(type)) return true;
		return false;
	}

	private static Context context(AnnotatedElement e) {
		return new Context(Refine.Context.from(e));
	}

	private static Call.Arg arg(Context context, Class<?> cls) {
		var arg = ARGS.get(cls);
		if (arg != null) return arg.apply(context);
		if (IntType.class.isAssignableFrom(cls))
			return Adapters.Arg.intType(context, Reflect.unchecked(cls));
		return null;
	}

	private static Call.Return rtn(Context context, Class<?> cls) {
		var rtn = RETURNS.get(cls);
		if (rtn != null) return rtn.apply(context);
		if (IntType.class.isAssignableFrom(cls))
			return Adapters.Return.intType(context, Reflect.unchecked(cls));
		return null;
	}

	private static Map<Class<?>, Arg> args() {
		var map = Maps.<Class<?>, Arg>of();
		for (var cls : VALUES)
			map.put(cls, c -> Adapters.Arg.value(c, cls));
		map.put(String.class, c -> Adapters.Arg.string(c));
		map.put(byte[].class, c -> Adapters.Arg.array(c, Allocators.BYTES));
		map.put(short[].class, c -> Adapters.Arg.array(c, Allocators.SHORTS));
		map.put(int[].class, c -> Adapters.Arg.array(c, Allocators.INTS));
		map.put(long[].class, c -> Adapters.Arg.array(c, Allocators.LONGS));
		map.put(float[].class, c -> Adapters.Arg.array(c, Allocators.FLOATS));
		map.put(double[].class, c -> Adapters.Arg.array(c, Allocators.DOUBLES));
		return Immutable.wrap(map);
	}

	private static Map<Class<?>, Return> returns() {
		var map = Maps.<Class<?>, Return>of();
		for (var cls : VALUES)
			map.put(cls, c -> Adapters.Return.value(c, cls));
		map.put(String.class, c -> Adapters.Return.string(c));
		map.put(byte[].class, c -> Adapters.Return.array(c, Allocators.BYTES));
		map.put(short[].class, c -> Adapters.Return.array(c, Allocators.SHORTS));
		map.put(int[].class, c -> Adapters.Return.array(c, Allocators.INTS));
		map.put(long[].class, c -> Adapters.Return.array(c, Allocators.LONGS));
		map.put(float[].class, c -> Adapters.Return.array(c, Allocators.FLOATS));
		map.put(double[].class, c -> Adapters.Return.array(c, Allocators.DOUBLES));
		return Immutable.wrap(map);
	}
}
