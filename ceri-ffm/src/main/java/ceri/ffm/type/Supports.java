package ceri.ffm.type;

import java.nio.Buffer;
import java.util.Map;
import java.util.Set;
import ceri.common.array.Dimensions;
import ceri.common.collect.Immutable;
import ceri.common.concurrent.Lazy;
import ceri.common.except.Exceptions;
import ceri.common.io.Buffers;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Native;
import ceri.ffm.reflect.Refine;
import ceri.ffm.reflect.TypeNode;

/**
 * Lookup for operational support of types and arrays.
 */
public class Supports {
	private static final String DIMS_ANNO = "@" + Refine.Dims.class.getSimpleName();
	private static final String SIZE_ANNO = "@" + Refine.Size.class.getSimpleName();
	private static final int SIZE_MAX_DEF = 1024 * 1024;
	private static final Map<Class<?>, Support<?, ?, ?>> MAP = map();
	private static final Lazy.ForClass<Support<?, ?, ?>> cache = Lazy.forClass(c -> fromClass(c));
	private static final Supports DEFAULT = new Supports(Set.of());
	private static final Supports FIXED = new Supports(Set.of(Option.sizeRequired));
	private Set<Option> options;

	/**
	 * Type support options.
	 */
	public enum Option {
		sizeRequired;
	}

	/**
	 * Returns support lookup using default array sizes.
	 */
	public static Supports of() {
		return DEFAULT;
	}

	/**
	 * Returns support lookup requiring explicit array sizes.
	 */
	public static Supports fixed() {
		return FIXED;
	}

	private Supports(Set<Option> options) {
		this.options = options;
	}

	/**
	 * Finds support for the class type, with default refinement context.
	 */
	public Support<?, ?, ?> from(Class<?> cls) {
		return from(TypeNode.of(cls, Refine.Context.DEFAULT));
	}

	/**
	 * Finds support for the class type, with given refinement context.
	 */
	public Support<?, ?, ?> from(Class<?> cls, Refine.Context context) {
		return from(TypeNode.of(cls, context));
	}

	/**
	 * Finds non-primitive support for the class type, with default refinement context.
	 */
	public <T> Support.Typed<T, ?> typedFrom(Class<T> cls) {
		return typedFrom(cls, Refine.Context.DEFAULT);
	}

	/**
	 * Finds non-primitive support for the class type, with given refinement context.
	 */
	public <T> Support.Typed<T, ?> typedFrom(Class<T> cls, Refine.Context context) {
		return Reflect.unchecked(from(Reflect.boxed(cls), context));
	}

	/**
	 * Finds support based on a type token and its annotations.
	 */
	public <T> Support.Typed<T, ?> from(Generics.Token<T> token) {
		return Reflect.unchecked(from(TypeNode.of(token)));
	}

	/**
	 * Finds support based on a type and its annotations.
	 */
	public Support<?, ?, ?> from(TypeNode node) {
		if (node == null) return null;
		return node.isArray() ? arrayFrom(node, null) : nonArrayFrom(node, node.context());
	}

	/**
	 * Finds support and wraps as an array.
	 */
	public Support<?, ?, ?> arrayFrom(TypeNode node, Dimensions dims) {
		if (node == null || !node.isArray()) return null;
		return array(node, dims);
	}

	// support

	private Support<?, ?, ?> array(TypeNode node, Dimensions dims) {
		var context = node.context();
		if (dims == null) dims = context.dims(null);
		var support = array(node.component(), dims, -1, context);
		return refine(support, context);
	}

	private Support<?, ?, ?> array(TypeNode node, Dimensions dims, int index,
		Refine.Context context) {
		var support = node.isArray() ? array(node.component(), dims, index - 1, context) :
			nonArrayFrom(node, context);
		int size = dim(node, dims, index, SIZE_MAX_DEF);
		boolean nul = support.isArray() ? false : context.nul(false);
		return support.asArray(size, nul);
	}

	private Support<?, ?, ?> nonArrayFrom(TypeNode node, Refine.Context context) {
		Class<?> cls = node.typed().cls();
		if (cls == null) return nullCls(node, context);
		var support = cache.get(cls);
		if (support != null) return refine(support, context);
		if (cls == Pointer.class) return pointer(node);
		if (cls == String.class) return string(node, context);
		return buffer(node, Reflect.unchecked(cls), context);
	}

	private static Support<?, ?, ?> nullCls(TypeNode node, Refine.Context context) {
		if (node.isVoid()) return refine(Support.VOID, context);
		throw new IllegalArgumentException("Type not supported: " + node);
	}

	private Support<?, ?, ?> pointer(TypeNode node) {
		var typeNode = node.type();
		if (typeNode == node) typeNode = TypeNode.VOID; // no generic type
		Support.Typed<?, ?> type = Reflect.unchecked(from(typeNode));
		var support = Pointer.support(type, typeNode.context().constant());
		return refine(support, node.context());
	}

	private Support<?, ?, ?> string(TypeNode node, Refine.Context context) {
		var chars = context.chars();
		var size = size(node, context, SIZE_MAX_DEF);
		var nul = context.nul(true);
		return refine(StringType.supportFor(chars, size, nul), context);
	}

	private <B extends Buffer> Support<?, ?, ?> buffer(TypeNode node, Class<B> cls,
		Refine.Context context) {
		var size = size(node, context, SIZE_MAX_DEF);
		var nul = context.nul(false);
		return refine(BufferType.supportFor(cls, size, nul), context);
	}

	private int size(TypeNode node, Refine.Context context, int def) {
		var size = context.size(null);
		if (size != null) return size;
		if (!sizeRequired()) return def;
		throw Exceptions.illegalArg("%s must be specified: %s", SIZE_ANNO, node);
	}

	private int dim(TypeNode node, Dimensions dims, int index, int def) {
		if (dims != null && index < dims.count()) return dims.dim(index);
		if (!sizeRequired()) return def;
		if (dims == null)
			throw Exceptions.illegalArg("%s must be specified on array: %s", DIMS_ANNO, node);
		throw Exceptions.illegalArg("%s requires %d values: %s", DIMS_ANNO, node);
	}

	private boolean sizeRequired() {
		return options.contains(Option.sizeRequired);
	}

	private static Support<?, ?, ?> refine(Support<?, ?, ?> support, Refine.Context context) {
		if (support == null) return null;
		if (support.kind() == Native.Kind.PRIMITIVE_POINTER && context.constant())
			support = ((PointerType.Supporter<?>) support).asConst();
		return support.align(context.align()).order(context.order());
	}

	private static Support<?, ?, ?> fromClass(Class<?> cls) {
		// Populates the cache
		var support = MAP.get(cls);
		if (support != null) return support;
		if (cls == void.class) return Support.VOID;
		if (IntType.class.isAssignableFrom(cls)) return IntType.supportFor(Reflect.unchecked(cls));
		if (Struct.class.isAssignableFrom(cls)) return Struct.supportFor(Reflect.unchecked(cls));
		if (Union.class.isAssignableFrom(cls)) return Union.supportFor(Reflect.unchecked(cls));
		if (cls == Pointer.class) return null; // needs more info
		if (PointerType.class.isAssignableFrom(cls))
			return PointerType.supportFor(Reflect.unchecked(cls));
		// if (Callback.class.isAssignableFrom(cls)) return Upcall.from(Reflect.unchecked(cls));
		if (cls == String.class) return null; // needs more info
		if (Buffers.BASE_TYPES.contains(cls)) return null; // needs more info
		throw new IllegalArgumentException("Type not supported: " + Reflect.simple(cls));
	}

	private static Map<Class<?>, Support<?, ?, ?>> map() {
		return Immutable.convertMapOf(t -> t.type(), t -> t, Support.VOID, Primitive.BOOL,
			Primitive.CHAR, Primitive.BYTE, Primitive.SHORT, Primitive.INT, Primitive.LONG,
			Primitive.FLOAT, Primitive.DOUBLE, Primitive.Box.BOOL, Primitive.Box.CHAR,
			Primitive.Box.BYTE, Primitive.Box.SHORT, Primitive.Box.INT, Primitive.Box.LONG,
			Primitive.Box.FLOAT, Primitive.Box.DOUBLE, PointerType.Raw.$, Pointer.OfVoid.$,
			// Pointer.OfBool.$, Pointer.OfChar.$,
			Pointer.OfByte.$, // Pointer.OfShort.$,
			Pointer.OfInt.$ // Pointer.OfLong.$, Pointer.OfFloat.$, Pointer.OfDouble.$
		);
	}
}
