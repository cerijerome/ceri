package ceri.ffm.core;

import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import ceri.common.except.Exceptions;
import ceri.common.io.Direction;
import ceri.common.reflect.Reflect;
import ceri.ffm.reflect.TypeNode;
import ceri.ffm.type.IntType;
import ceri.ffm.type.PointerType;
import ceri.ffm.type.Support;
import ceri.ffm.type.Supports;

/**
 * Builds native upcall handlers from callback interface methods and annotated configuration.
 */
public class Upcalls {
	private static final int ARG_ARRAY_LEN = 1024 * 1024; // max array length
	private static final int RTN_ARRAY_LEN = 256; // max array length
	private static final Supports.Defaults ARG_DEFS =
		Supports.defaults().string(ARG_ARRAY_LEN, true).buffer(ARG_ARRAY_LEN, false)
			.array1d(ARG_ARRAY_LEN, false).arrayNd(ARG_ARRAY_LEN, false).build();
	private static final Supports.Defaults RTN_DEFS =
		Supports.defaults().string(RTN_ARRAY_LEN, true).buffer(RTN_ARRAY_LEN, true)
			.array1d(RTN_ARRAY_LEN, true).arrayNd(0, false).build();
	/** Default call builder instance. */
	public static final Upcalls DEF = builder().build();
	private final Linker linker;
	private final Supports argSupports;
	private final Supports rtnSupports;

	/**
	 * Builder to specify linker and type defaults.
	 */
	public static class Builder {
		private Linker linker = Linker.nativeLinker();
		private Supports.Defaults argDefs = ARG_DEFS;
		private Supports.Defaults rtnDefs = RTN_DEFS;

		/**
		 * Specify the native call linker.
		 */
		public Builder linker(Linker linker) {
			this.linker = linker;
			return this;
		}

		/**
		 * Specify call argument type defaults.
		 */
		public Builder argDefs(Supports.Defaults argDefs) {
			this.argDefs = argDefs;
			return this;
		}

		/**
		 * Specify call return type defaults.
		 */
		public Builder rtnDefs(Supports.Defaults rtnDefs) {
			this.rtnDefs = rtnDefs;
			return this;
		}

		/**
		 * Creates the call builder instance.
		 */
		public Upcalls build() {
			return new Upcalls(this);
		}
	}

	/**
	 * Start configuring a upcall builder. 
	 */
	public static Builder builder() {
		return new Builder();
	}

	private Upcalls(Builder builder) {
		linker = builder.linker;
		argSupports = Supports.of(builder.argDefs);
		rtnSupports = Supports.of(builder.rtnDefs);
	}

	/**
	 * Creates an upcall handler from the callback type.
	 */
	public Upcall upcall(Class<? extends Callback> callbackCls) {
		var callback = Callback.method(callbackCls);
		var b = Upcall.builder();
		rtn(b, callback);
		for (var parameter : callback.getParameters())
			arg(b, parameter);
		return b.build(linker, callback);
	}

	// support

	private void arg(Upcall.Builder b, Parameter parameter) {
		var node = TypeNode.of(parameter);
		if (arg(b, node) == null) throw Exceptions.illegalArg("Unsupported arg type: %s (%s)",
			Reflect.localName(parameter), node.typed());
	}

	private Upcall.Builder arg(Upcall.Builder b, TypeNode node) {
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

	private void rtn(Upcall.Builder b, Method method) {
		if (Reflect.isVoid(method)) return;
		var node = TypeNode.ofReturn(method);
		if (rtn(b, node) == null) throw Exceptions.illegalArg("Unsupported return type: %s (%s)",
			method.getName(), node.typed());
	}

	private Upcall.Builder rtn(Upcall.Builder b, TypeNode node) {
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

	private static Upcall.Builder primitiveArg(Upcall.Builder b, Support<?, ?, ?> support,
		TypeNode node) {
		return b.arg(node.typed(), support.type(), support.layout(), t -> t);
	}

	private static <T extends IntType<T>> Upcall.Builder intTypeArg(Upcall.Builder b,
		IntType.Supporter<T> support, TypeNode node) {
		return b.<Number, T>arg(node.typed(), support.nativeType(), support.layout(),
			n -> support.of(n));
	}

	private static <P extends PointerType.Raw> Upcall.Builder pointerArg(Upcall.Builder b,
		PointerType.Supporter<P> support, TypeNode node) {
		return b.<MemorySegment, P>arg(node.typed(), MemorySegment.class, support.layout(),
			m -> support.of(m));
	}

	private static <T> Upcall.Builder byValArg(Upcall.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		return b.<MemorySegment, T>arg(node.typed(), MemorySegment.class, support.layout(),
			m -> support.get(Segments.reslice(m, support.layout())));
	}

	private static <T> Upcall.Builder byRefArg(Upcall.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		return b.<MemorySegment, T>arg(node.typed(), MemorySegment.class, Layouts.POINTER,
			m -> support.decode(Segments.reslice(m, support.layout())));
	}

	private static Upcall.Builder primitiveRtn(Upcall.Builder b, Support<?, ?, ?> support,
		TypeNode node) {
		return b.rtn(node.typed(), support.type(), support.layout(), (_, t) -> t);
	}

	private static <T extends IntType<T>> Upcall.Builder intTypeRtn(Upcall.Builder b,
		IntType.Supporter<T> support, TypeNode node) {
		return b.<T, Number>rtn(node.typed(), support.nativeType(), support.layout(),
			(_, t) -> t.nativeValue());
	}

	private static <P extends PointerType.Raw> Upcall.Builder pointerRtn(Upcall.Builder b,
		PointerType.Supporter<P> support, TypeNode node) {
		return b.<P, MemorySegment>rtn(node.typed(), MemorySegment.class, support.layout(),
			(_, t) -> t.memory());
	}

	private static <T> Upcall.Builder byValRtn(Upcall.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		return b.<T, MemorySegment>rtn(node.typed(), MemorySegment.class, support.layout(),
			(a, t) -> support.alloc(a, t));
	}

	private static <T> Upcall.Builder byRefRtn(Upcall.Builder b, Support<T, ?, ?> support,
		TypeNode node) {
		return b.<T, MemorySegment>rtn(node.typed(), MemorySegment.class, Layouts.POINTER,
			(a, t) -> support.encode(Direction.in, a, t).value());
	}
}
