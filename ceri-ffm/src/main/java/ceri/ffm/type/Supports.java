package ceri.ffm.type;

import ceri.common.array.Dimensions;
import ceri.common.concurrent.Lazy;
import ceri.common.io.Buffers;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.ffm.reflect.Refine;
import ceri.ffm.reflect.Refine.Dims;
import ceri.ffm.reflect.Refine.Nul;
import ceri.ffm.reflect.Refine.Packed;
import ceri.ffm.reflect.TypeNode;
import ceri.ffm.type.Support.Typed;

/**
 * Lookup for operational support of types and arrays.
 */
public class Supports {
	private static final Lazy.ForClass<Support<?, ?, ?>> cache = Lazy.forClass(c -> fromClass(c));
	private static final int STRING_LEN_DEF = 32;
	private static final int BUFFER_LEN_DEF = 32;

	private Supports() {}

	public static void main(String[] args) {
		var s0 = from(int[][][].class, Refine.custom().dims(5, 4, 3).nul().context());
		var s1 = Primitive.INT.asArray(5, true).asArray(4).asArray(3);
		var s2 = from(new Generics.Token<@Packed @Dims({ 5, 4, 3 }) @Nul int[][][]>() {});
		System.out.println(s0);
		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s1.asPointer(true));
		System.out.println(Support.VOID.asPointer(false).asArray(3, true).asPointer(false));
	}

	/**
	 * Finds support for the class type, with default refinement context.
	 */
	public static Support<?, ?, ?> from(Class<?> cls) {
		return from(TypeNode.of(cls, Refine.Context.DEFAULT));
	}

	/**
	 * Finds support for the class type, with given refinement context.
	 */
	public static Support<?, ?, ?> from(Class<?> cls, Refine.Context context) {
		return from(TypeNode.of(cls, context));
	}

	/**
	 * Finds non-primitive support for the class type, with default refinement context.
	 */
	public static <T> Typed<T, ?> typedFrom(Class<T> cls) {
		return typedFrom(cls, Refine.Context.DEFAULT);
	}

	/**
	 * Finds non-primitive support for the class type, with given refinement context.
	 */
	public static <T> Typed<T, ?> typedFrom(Class<T> cls, Refine.Context context) {
		return Reflect.unchecked(from(Reflect.boxed(cls), context));
	}

	/**
	 * Finds support based on a type token and its annotations.
	 */
	public static <T> Typed<T, ?> from(Generics.Token<T> token) {
		return Reflect.unchecked(from(TypeNode.of(token)));
	}

	/**
	 * Finds support based on a type and its annotations.
	 */
	public static Support<?, ?, ?> from(TypeNode node) {
		if (node == null) return null;
		var support = node.isArray() ? fromArray(node) : fromComponent(node);
		var context = node.context();
		return support.with(context.align(), context.order());
	}

	private static Support<?, ?, ?> fromArray(TypeNode node) {
		var context = node.context();
		var dims = context.dims();
		var nul = context.nul();
		return fromArray(node.component(), dims, nul);
	}

	public static Support<?, ?, ?> fromArray(TypeNode node, Dimensions dims, boolean nul) {
		return fromArray(node, dims, -1, nul);
	}

	private static Support<?, ?, ?> fromArray(TypeNode node, Dimensions dims, int index,
		boolean nul) {
		int size = dims.dim(index);
		if (!node.isArray()) return from(node).asArray(size, nul);
		return fromArray(node.component(), dims, index - 1, nul).asArray(size, false);
	}

	private static Support<?, ?, ?> fromComponent(TypeNode node) {
		Class<?> cls = node.typed().cls();
		var support = cache.get(cls);
		if (support != null) return support;
		if (cls == Pointer.class) return Pointer.supportFor(node.type());
		var context = node.context();
		if (cls == String.class) return StringType.support(context.chars(),
			context.size(STRING_LEN_DEF), context.nul(true));
		return BufferType.support(Reflect.unchecked(cls), context.size(BUFFER_LEN_DEF),
			context.nul());
	}

	private static Support<?, ?, ?> fromClass(Class<?> cls) {
		// Populates the cache
		if (cls == void.class || cls == Void.class) return Support.VOID;
		if (cls == boolean.class) return Primitive.BOOL;
		if (cls == char.class) return Primitive.CHAR;
		if (cls == byte.class) return Primitive.BYTE;
		if (cls == short.class) return Primitive.SHORT;
		if (cls == int.class) return Primitive.INT;
		if (cls == long.class) return Primitive.LONG;
		if (cls == float.class) return Primitive.FLOAT;
		if (cls == double.class) return Primitive.DOUBLE;
		if (cls == Boolean.class) return Primitive.Box.BOOL;
		if (cls == Character.class) return Primitive.Box.CHAR;
		if (cls == Byte.class) return Primitive.Box.BYTE;
		if (cls == Short.class) return Primitive.Box.SHORT;
		if (cls == Integer.class) return Primitive.Box.INT;
		if (cls == Long.class) return Primitive.Box.LONG;
		if (cls == Float.class) return Primitive.Box.FLOAT;
		if (cls == Double.class) return Primitive.Box.DOUBLE;
		if (IntType.class.isAssignableFrom(cls)) return IntType.supportFor(Reflect.unchecked(cls));
		if (Struct.class.isAssignableFrom(cls)) return Struct.supportFor(Reflect.unchecked(cls));
		if (Union.class.isAssignableFrom(cls)) return Union.supportFor(Reflect.unchecked(cls));
		if (cls == Pointer.OfVoid.class) return Pointer.OfVoid.$;
		// if (cls == Pointer.OfBool.class) return Pointer.OfBool.$;
		// if (cls == Pointer.OfChar.class) return Pointer.OfChar.$;
		if (cls == Pointer.OfByte.class) return Pointer.OfByte.$;
		// if (cls == Pointer.OfShort.class) return Pointer.OfShort.$;
		if (cls == Pointer.OfInt.class) return Pointer.OfInt.$;
		// if (cls == Pointer.OfLong.class) return Pointer.OfLong.$;
		// if (cls == Pointer.OfFloat.class) return Pointer.OfFloat.$;
		// if (cls == Pointer.OfDouble.class) return Pointer.OfDouble.$;
		if (cls == Pointer.class) return null; // needs more info
		// TODO: pointer type
		// TODO: function pointer
		if (cls == String.class) return null; // needs more info
		if (Buffers.BASE_TYPES.contains(cls)) return null; // needs more info
		throw new IllegalArgumentException("Type not supported: " + Reflect.simple(cls));
	}
}
