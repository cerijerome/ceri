package ceri.ffm.core;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.array.Array;
import ceri.common.collect.Collectable;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.reflect.Handles;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;
import ceri.common.util.Validate;
import ceri.ffm.reflect.Refine;
import ceri.ffm.reflect.TypeNode;
import ceri.ffm.type.Callback;
import ceri.ffm.type.IntType;
import ceri.ffm.type.PointerType;
import ceri.ffm.type.Support;
import ceri.ffm.type.Supports;

/**
 * Types for encapsulation of native upcalls and downcalls.
 */
public class Call {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final MethodHandle LOCAL_CALLBACK =
		Handles.staticMethod(Call.class, "localCallback", Object.class, Down.class, Object[].class);
	private static final MethodHandle NATIVE_CALLBACK =
		Handles.staticMethod(Call.class, "nativeCallback", Object.class, Up.class, Object[].class);
	private static final Linker.Option[] NO_OPTIONS = new Linker.Option[0];

	private Call() {}

	/**
	 * Method cache lookup key. Uses method and var-arg types.
	 */
	public record Key(Method method, List<Class<?>> varArgTypes) implements Comparable<Key> {
		private static final Comparator<Key> COMPARATOR =
			Comparator.<Key, String>comparing(k -> k.method().getName())
				.thenComparingInt(k -> k.varArgTypes().size());

		/**
		 * Returns a lookup key for a method without var-args.
		 */
		public static Key of(Method method) {
			return new Key(method, Immutable.list());
		}

		/**
		 * Returns a lookup key for a method with possible var-args.
		 */
		public static Key from(Method method, Object[] args) {
			if (!method.isVarArgs() || args.length == 0) return of(method);
			return new Key(method, Call.varArgTypes(args));
		}

		@Override
		public int compareTo(Key key) {
			return COMPARATOR.compare(this, key);
		}

		@Override
		public final String toString() {
			return Reflect.name(method.getDeclaringClass()) + "." + method().getName()
				+ Lists.adapt(Reflect::name, varArgTypes());
		}
	}

	/**
	 * Call configuration.
	 */
	public static class Config {
		private final Method method;
		private final Native.Adapter<?, ?> rtn;
		private final List<Native.Adapter<?, ?>> args;
		private final boolean groupReturn;
		private final int varArg;
		private final boolean lastError;
		private final MethodType localMethodType;
		private final MethodHandle localHandle;
		private final FunctionDescriptor nativeFuncDesc;
		private final MethodType nativeMethodType;
		private final MethodHandle nativeHandle;

		private static class Builder {
			private final Method method;
			private Native.Adapter<?, ?> rtn = Native.Adapter.VOID;
			private final List<Native.Adapter<?, ?>> args = Lists.of();
			private int varArg = -1;
			private boolean lastError = false;

			private Builder(Method method) {
				this.method = method;
			}

			private Builder rtn(Native.Adapter<?, ?> rtn) {
				this.rtn = rtn;
				return this;
			}

			private Builder arg(Native.Adapter<?, ?> arg) {
				args.add(arg);
				return this;
			}

			private Builder varArg() {
				varArg = args.size();
				return this;
			}

			private Builder lastError() {
				lastError = true;
				return this;
			}

			private Config build() {
				return new Config(this);
			}
		}

		private Config(Builder builder) {
			method = builder.method;
			rtn = builder.rtn;
			groupReturn = (rtn.layout() instanceof GroupLayout);
			args = Immutable.wrap(builder.args);
			varArg = builder.varArg;
			lastError = builder.lastError;
			localMethodType = localMethodType(); // local types
			localHandle = Handles.method(method); // class + local types
			nativeFuncDesc = nativeFuncDesc();
			nativeMethodType = nativeMethodType();
			nativeHandle = Native.LINKER.downcallHandle(nativeFuncDesc, options(varArg, lastError));
		}

		/**
		 * Extends the configuration with var-arg argument types.
		 */
		public Config withVarArgs(List<Class<?>> varArgTypes) {
			var b = builder().varArg();
			var parameter = Array.last(method.getParameters());
			for (int i = 0; i < varArgTypes.size(); i++)
				b.arg(varArg(parameter, varArgTypes, i));
			return b.build();
		}

		/**
		 * Returns the method that generated this configuration.
		 */
		public Method method() {
			return method;
		}

		/**
		 * Returns a native downcall for this method at given pointer.
		 */
		public Down down(MemorySegment pointer) {
			return new Call.Down(this, pointer);
		}

		/**
		 * Returns an upcall with local callback instance for the given function pointer.
		 */
		@SuppressWarnings("resource")
		public Up up(MemorySegment pointer) {
			return new Up(this, callback(pointer), pointer);
		}

		/**
		 * Returns an upcall with function pointer for the given local callback instance.
		 */
		public Call.Up up(Callback callback) {
			return new Up(this, callback);
		}

		@Override
		public String toString() {
			return Reflect.descriptor(method) + (lastError ? "!" : "");
		}

		// support

		private Builder builder() {
			var b = new Config.Builder(method);
			b.rtn = rtn;
			b.args.addAll(args);
			b.varArg = varArg;
			b.lastError = lastError;
			return b;
		}

		private Callback callback(MemorySegment pointer) {
			var downcall = down(pointer);
			var downHandle = LOCAL_CALLBACK.bindTo(downcall).asVarargsCollector(Object[].class)
				.asType(localMethodType);
			return Reflect.unchecked(Handles.proxy(method.getDeclaringClass(), downHandle));
		}

		private MemorySegment pointer(Up upcall) {
			var handle = NATIVE_CALLBACK.bindTo(upcall).asVarargsCollector(Object[].class)
				.asType(nativeMethodType);
			return Native.LINKER.upcallStub(handle, nativeFuncDesc, upcall.arena,
				options(varArg, false));
		}

		private Object invokeLocal(Callback callback, SegmentAllocator allocator,
			Object[] nativeArgs) {
			try {
				var localArgs = localArgs(callback, nativeArgs);
				var localRtn = localHandle.invokeWithArguments(localArgs);
				return rtn.toNative(allocator, Reflect.unchecked(localRtn)).value();
			} catch (Throwable t) {
				logger.catching(t);
				var def = rtn.nativeDef();
				logger.info("Returning default: %s", def);
				return def;
			}
		}

		private Object[] localArgs(Callback callback, Object[] nativeArgs) {
			var localArgs = new Object[1 + nativeArgs.length];
			localArgs[0] = callback;
			for (int i = 0; i < nativeArgs.length; i++)
				localArgs[i + 1] = args.get(i).toLocal(Reflect.unchecked(nativeArgs[i]));
			return localArgs;
		}

		private Object invokeNative(SegmentAllocator allocator, MemorySegment pointer,
			Object[] localArgs) throws Throwable {
			var adaptedArgs = adaptLocalArgs(allocator, flatten(localArgs));
			var nativeArgs = nativeArgs(allocator, pointer, adaptedArgs);
			var nativeRtn = nativeHandle.invokeWithArguments(nativeArgs); // invokeExact(...) fails
			resolveArgs(adaptedArgs, nativeArgs);
			return rtn.toLocal(Reflect.unchecked(nativeRtn));
		}

		private Object[] flatten(Object[] localArgs) {
			if (localArgs.length == 0 || varArg < 0) return localArgs;
			var varArgs = (Object[]) Array.last(localArgs);
			if (varArgs.length == 1) {
				localArgs[localArgs.length - 1] = varArgs[0];
				return localArgs;
			}
			var flat = new Object[localArgs.length - 1 + varArgs.length];
			Array.copy(localArgs, 0, flat, 0, localArgs.length - 1);
			Array.copy(varArgs, 0, flat, localArgs.length - 1, varArgs.length);
			return flat;
		}

		private List<Native.Adapted<?>> adaptLocalArgs(SegmentAllocator allocator,
			Object[] localArgs) {
			var adaptedArgs = new ArrayList<Native.Adapted<?>>(args.size());
			for (int i = 0; i < args.size(); i++)
				adaptedArgs.add(args.get(i).toNative(allocator, Reflect.unchecked(localArgs[i])));
			return adaptedArgs;
		}

		private Object[] nativeArgs(SegmentAllocator allocator, MemorySegment pointer,
			List<Native.Adapted<?>> adaptedArgs) {
			var nativeArgs = new Object[nativeArgCount(adaptedArgs)];
			int index = 0;
			nativeArgs[index++] = pointer;
			if (groupReturn) nativeArgs[index++] = allocator;
			if (lastError) nativeArgs[index++] = LastError.capture(allocator);
			for (var adaptedArg : adaptedArgs)
				nativeArgs[index++] = adaptedArg.value();
			return nativeArgs;
		}

		private int nativeArgCount(List<Native.Adapted<?>> adaptedArgs) {
			return 1 + (groupReturn ? 1 : 0) + (lastError ? 1 : 0) + adaptedArgs.size();
		}

		private void resolveArgs(List<Native.Adapted<?>> adaptedArgs, Object[] nativeArgs) {
			if (lastError) LastError.save((MemorySegment) nativeArgs[lastErrorIndex()]);
			for (var adaptedArg : adaptedArgs)
				adaptedArg.resolve();
		}

		private int lastErrorIndex() {
			return 1 + (groupReturn ? 1 : 0);
		}

		private FunctionDescriptor nativeFuncDesc() {
			var argLayouts =
				Collectable.adaptToArray(args, MemoryLayout[]::new, Native.Adapter::layout);
			if (rtn.localType().isVoid()) return FunctionDescriptor.ofVoid(argLayouts);
			return FunctionDescriptor.of(rtn.layout(), argLayouts);
		}

		private MethodType localMethodType() {
			var localArgTypes =
				Collectable.adaptToArray(args, Class[]::new, Native.Adapter::localCls);
			return MethodType.methodType(rtn.localCls(), localArgTypes);
		}

		private MethodType nativeMethodType() {
			var nativeArgTypes =
				Collectable.adaptToArray(args, Class[]::new, Native.Adapter::nativeCls);
			return MethodType.methodType(rtn.nativeCls(), nativeArgTypes);
		}
	}

	/**
	 * Encapsulates a native downcall.
	 */
	public static class Down {
		private final Config config;
		private final MemorySegment pointer;
		
		private Down(Config config, MemorySegment pointer) {
			this.config = config;
			this.pointer = pointer;
		}
		
		/**
		 * Returns the method configuration.
		 */
		public Config config() {
			return config;
		}
		
		/**
		 * Returns the function pointer.
		 */
		public MemorySegment pointer() {
			return pointer;
		}
		
		/**
		 * Invokes the native call with given allocator and local arguments.
		 */
		public Object invoke(Object[] localArgs) throws Throwable {
			try (var allocator = Arena.ofConfined()) {
				return config.invokeNative(allocator, pointer, localArgs);
			}
		}

		@Override
		public String toString() {
			return String.format("downcall%s/%s", Segments.addressString(pointer), config);
		}
	}

	/**
	 * Encapsulates a local callback.
	 */
	public static class Up implements Functions.Closeable {
		private final Config config;
		private final Callback callback;
		private final MemorySegment pointer;
		private final Arena arena;

		private Up(Config config, Callback callback, MemorySegment pointer) {
			this.config = config;
			this.callback = callback;
			arena = Arena.ofShared();
			this.pointer = pointer;
		}

		private Up(Config config, Callback callback) {
			this.config = config;
			this.callback = callback;
			arena = Arena.ofShared();
			pointer = config.pointer(this);
		}

		/**
		 * Returns the method configuration.
		 */
		public Config config() {
			return config;
		}
		
		/**
		 * Returns the callback instance.
		 */
		public Callback callback() {
			return callback;
		}

		/**
		 * Returns the function pointer.
		 */
		public MemorySegment pointer() {
			return pointer;
		}

		@Override
		public void close() {
			arena.close();
		}

		@Override
		public String toString() {
			return String.format("%s%s/%s", Basics.def(Lambdas.registered(callback), "upcall"),
				Reflect.nameHash(callback), config);
		}

		/**
		 * Invokes the local callback method with native arguments adapted to local arguments, and
		 * the local return value adapted to a native return value. Must not throw an exception.
		 */
		private Object invokeCallback(Object[] nativeArgs) {
			// how/when to free memory allocated for return values?
			// will accumulate until callback is garbage collected
			return config.invokeLocal(callback, arena, nativeArgs);
		}
	}

	/**
	 * Creates the native call configuration for the method.
	 */
	public static Config config(Method method) {
		System.out.println("config: " + method);
		var b = new Config.Builder(method).rtn(rtn(method));
		addArgs(b, method);
		if (Refine.lastError(method, true)) b.lastError();
		return b.build();
	}

	/**
	 * Common entry point for local invocation of a callback. Returns the local return value.
	 */
	public static Object localCallback(Down downcall, Object[] localArgs) throws Throwable {
		return downcall.invoke(localArgs);
	}

	/**
	 * Common entry point for native invocation of a callback. Returns the native return value. Must
	 * not throw an exception, or the JVM will terminate.
	 */
	public static Object nativeCallback(Up upcall, Object[] nativeArgs) {
		return upcall.invokeCallback(nativeArgs);
	}

	// support

	private static void addArgs(Config.Builder b, Method method) {
		var params = method.getParameters();
		int paramCount = params.length;
		if (method.isVarArgs()) paramCount--;
		for (int i = 0; i < paramCount; i++)
			b.arg(arg(params[i]));
	}

	private static Native.Adapter<?, ?> arg(Parameter parameter) {
		var node = TypeNode.of(parameter);
		var adapter = adapter(node);
		if (adapter != null) return adapter;
		throw Exceptions.illegalArg("Unsupported arg type: %s (%s)", Reflect.localName(parameter),
			node.typed());
	}

	private static Native.Adapter<?, ?> varArg(Parameter parameter, List<Class<?>> varArgs, int i) {
		var node = TypeNode.of(parameter).sub(Lists.at(varArgs, i));
		var adapter = adapter(node);
		if (adapter != null) return adapter;
		throw Exceptions.illegalArg("Unsupported vararg type: %s[%d] (%s)",
			Reflect.localName(parameter), i, node.typed());
	}

	private static Native.Adapter<?, ?> rtn(Method method) {
		if (Reflect.isVoid(method)) return Native.Adapter.VOID;
		var node = TypeNode.ofReturn(method);
		var adapter = adapter(node);
		if (adapter != null) return adapter;
		throw Exceptions.illegalArg("Unsupported return type: %s (%s)", method.getName(),
			node.typed());
	}

	private static Native.Adapter<?, ?> adapter(TypeNode node) {
		var support = Supports.of().from(node);
		if (support.isArray()) return byRef(node, support);
		return switch (support.kind()) {
			case PRIMITIVE, BOXED -> primitive(node, support);
			case INT_TYPE -> intType(node, Reflect.unchecked(support));
			case POINTER, PRIMITIVE_POINTER -> pointer(node, Reflect.unchecked(support));
			case STRING, BUFFER -> byRef(node, support);
			case null -> null;
			default -> byVal(node, support);
		};
	}

	private static Native.Adapter<?, ?> primitive(TypeNode node, Support<?, ?, ?> support) {
		return new Native.Adapter<>(node.typed(), support.type(), support.val(), support.layout(),
			(_, t) -> Native.Adapted.of(t), t -> t);
	}

	private static <T extends IntType<T>> Native.Adapter<T, Number> intType(TypeNode node,
		IntType.Supporter<T> support) {
		return new Native.Adapter<>(node.typed(), support.nativeType(), support.val().nativeValue(),
			support.layout(), (_, t) -> Native.Adapted.of(t.nativeValue()), n -> support.of(n));
	}

	private static <P extends PointerType.Raw> Native.Adapter<P, MemorySegment>
		pointer(TypeNode node, PointerType.Supporter<P> support) {
		return new Native.Adapter<>(node.typed(), MemorySegment.class, MemorySegment.NULL,
			support.layout(), (_, t) -> Native.Adapted.of(t.memory()), m -> support.of(m));
	}

	private static <T> Native.Adapter<T, MemorySegment> byVal(TypeNode node,
		Support<T, ?, ?> support) {
		return new Native.Adapter<>(node.typed(), MemorySegment.class, MemorySegment.NULL,
			support.layout(), (a, t) -> Native.Adapted.of(support.alloc(a, t)),
			m -> support.get(Segments.reslice(m, support.layout())));
	}

	private static <T> Native.Adapter<T, MemorySegment> byRef(TypeNode node,
		Support<T, ?, ?> support) {
		var direction = node.context().direction();
		return new Native.Adapter<>(node.typed(), MemorySegment.class, MemorySegment.NULL,
			Layouts.POINTER, (a, t) -> support.encode(direction, a, t),
			m -> support.decode(Segments.reslice(m, support.layout())));
	}

	private static Linker.Option[] options(int varArg, boolean lastError) {
		int count = (varArg >= 0 ? 1 : 0) + (lastError ? 1 : 0);
		if (count == 0) return NO_OPTIONS;
		var options = new Linker.Option[count];
		if (varArg >= 0) options[--count] = Linker.Option.firstVariadicArg(varArg);
		if (lastError) options[--count] = LastError.OPTION;
		return options;
	}

	private static List<Class<?>> varArgTypes(Object[] args) {
		var varArgs = (Object[]) Array.last(args);
		var types = Lists.<Class<?>>of();
		for (int i = 0; i < varArgs.length; i++)
			types.add(varArgType(varArgs, i));
		return Immutable.wrap(types);
	}

	private static Class<?> varArgType(Object[] varArgs, int i) {
		var value = Validate.nonNull(varArgs[i], "vararg[%d]", i);
		return Native.promote(value.getClass());
	}
}
