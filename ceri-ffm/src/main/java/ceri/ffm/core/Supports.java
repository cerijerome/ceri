package ceri.ffm.core;

import java.nio.Buffer;
import java.util.Map;
import ceri.common.array.Dimensions;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.concurrent.Lazy;
import ceri.common.io.Buffers;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Support.Typed;
import ceri.ffm.reflect.Refine;
import ceri.ffm.reflect.Refine.Const;
import ceri.ffm.reflect.Refine.Dims;
import ceri.ffm.reflect.Refine.Nul;
import ceri.ffm.reflect.Refine.Packed;
import ceri.ffm.reflect.TypeNode;
import ceri.ffm.type.BufferType;
import ceri.ffm.type.IntType;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;
import ceri.ffm.type.RawPointer;
import ceri.ffm.type.StringType;
import ceri.ffm.type.Struct;
import ceri.ffm.type.Union;

/**
 * Lookup for operational support of types and arrays.
 */
public class Supports {
	private static final Lazy.ForClass<Support<?, ?, ?>> cache = Lazy.forClass(c -> fromClass(c));
	public static final Supports DEF = new Supports(Defaults.DEF);
	private final Defaults defaults;

	/**
	 * Provides default configuration for array types.
	 */
	public static class Defaults {
		private static final Value NONE = new Value(0, false);
		private static final Value NUL = new Value(0, true);
		public static final Defaults DEF = new Defaults(Map.of(Category.string, NUL));
		private final Map<Category, Value> values;

		private enum Category {
			string,
			buffer,
			array1d,
			arrayNd
		}

		/**
		 * Array type configuration.
		 */
		public record Value(int count, boolean nul) {}

		/**
		 * Allows customization of default configurations.
		 */
		public static class Builder {
			private final Map<Category, Value> values = Maps.of();

			private Builder() {}

			/**
			 * Sets default string char count and nul-termination.
			 */
			public Builder string(int count, boolean nul) {
				return value(Category.string, count, nul);
			}

			/**
			 * Sets default buffer element count and nul-termination.
			 */
			public Builder buffer(int count, boolean nul) {
				return value(Category.buffer, count, nul);
			}

			/**
			 * Sets default 1-d array size and nul-termination. Use n-d for buffer or string arrays.
			 */
			public Builder array1d(int count, boolean nul) {
				return value(Category.array1d, count, nul);
			}

			/**
			 * Sets default array size and nul-termination for arrays > 1-d, string arrays, and
			 * buffer arrays.
			 */
			public Builder arrayNd(int count, boolean nul) {
				return value(Category.arrayNd, count, nul);
			}

			/**
			 * Provides the defaults instance.
			 */
			public Defaults build() {
				return new Defaults(Immutable.map(values));
			}

			/**
			 * Provides the support lookup with defaults instance.
			 */
			public Supports supports() {
				return new Supports(build());
			}

			private Builder value(Category category, int count, boolean nul) {
				if (count >= 0) values.put(category, new Value(count, nul));
				return this;
			}
		}

		private Defaults(Map<Category, Value> values) {
			this.values = values;
		}

		public Value string() {
			return values.getOrDefault(Category.string, NONE);
		}

		public Value buffer() {
			return values.getOrDefault(Category.buffer, NONE);
		}

		public Value array1d() {
			return values.getOrDefault(Category.array1d, NONE);
		}

		public Value arrayNd() {
			return values.getOrDefault(Category.arrayNd, NONE);
		}

		/**
		 * Returns the array defaults based on component support.
		 */
		public Defaults.Value array(Support<?, ?, ?> support) {
			if (support.isArray()) return arrayNd();
			return switch (support.kind()) {
				case string, buffer -> arrayNd();
				default -> array1d();
			};
		}
		
		@Override
		public String toString() {
			return values.toString();
		}
	}

	/**
	 * Allows customization of array type defaults.
	 */
	public static Defaults.Builder defaults() {
		return new Defaults.Builder();
	}

	// $(int[5!][4][3],<240/1)
	// $(int[5!][4][3],<240/1)
	// $(int[5!][4][3],<240/1)
	// $((const int[3][2]*)[2!][3],<48/1)
	// $(const int[5!][4][3]*,<8/8)
	// $(void*[3!]*,<8/8)

	public static void main(String[] args) {
		var ss = Supports.DEF;
		var s0 = ss.from(int[][][].class, Refine.custom().align(1).dims(5, 4, 3).nul().context());
		var s1 = Primitive.INT.align(1).asArray(5, true).asArray(4).asArray(3);
		var s2 = ss.from(new Generics.Token<@Packed @Dims({ 5, 4, 3 }) @Nul int[][][]>() {});
		var s3 = ss.from(new Generics.Token //
		<@Packed @Dims({ 2, 3 }) @Nul Pointer<@Dims({ 3, 2 }) @Const int[][]>[][]>() {});
		System.out.println(s0);
		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s3);
		System.out.println(s1.asPointer(true));
		System.out.println(Support.VOID.asPointer(false).asArray(3, true).asPointer(false));
	}

	/**
	 * Returns an instance with default array configurations.
	 */
	public static Supports of(Defaults defaults) {
		if (defaults == null || defaults == Defaults.DEF) return DEF;
		return new Supports(defaults);
	}

	private Supports(Defaults defaults) {
		this.defaults = defaults;
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
	public <T> Typed<T, ?> typedFrom(Class<T> cls) {
		return typedFrom(cls, Refine.Context.DEFAULT);
	}

	/**
	 * Finds non-primitive support for the class type, with given refinement context.
	 */
	public <T> Typed<T, ?> typedFrom(Class<T> cls, Refine.Context context) {
		return Reflect.unchecked(from(Reflect.boxed(cls), context));
	}

	/**
	 * Finds support based on a type token and its annotations.
	 */
	public <T> Typed<T, ?> from(Generics.Token<T> token) {
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
		var def = defaults.array(support);
		int size = dims != null ? dims.dim(index) : def.count();
		boolean nul = support.isArray() ? false : context.nul(def.nul());
		return support.asArray(size, nul);
	}

	private Support<?, ?, ?> nonArrayFrom(TypeNode node, Refine.Context context) {
		Class<?> cls = node.typed().cls();
		if (cls == null) return nullCls(node, context);
		var support = cache.get(cls);
		if (support != null) return refine(support, context);
		if (cls == Pointer.class) return pointer(node.type());
		if (cls == String.class) return string(context);
		return buffer(Reflect.unchecked(cls), context);
	}

	private Support<?, ?, ?> nullCls(TypeNode node, Refine.Context context) {
		if (node.isVoid()) return refine(Support.VOID, context);
		throw new IllegalArgumentException("Type not supported: " + node);
	}
	
	private Support<?, ?, ?> pointer(TypeNode node) {
		var context = node.context();
		var support = Pointer.supportFor(node, context.constant());
		return refine(support, context);
	}

	private Support<?, ?, ?> string(Refine.Context context) {
		var chars = context.chars();
		var size = context.size(defaults.string().count());
		var nul = context.nul(defaults.string().nul());
		return refine(StringType.support(chars, size, nul), context);
	}

	private <B extends Buffer> Support<?, ?, ?> buffer(Class<B> cls, Refine.Context context) {
		var size = context.size(defaults.buffer().count());
		var nul = context.nul(defaults.buffer().nul());
		return refine(BufferType.support(cls, size, nul), context);
	}

	private Support<?, ?, ?> refine(Support<?, ?, ?> support, Refine.Context context) {
		if (support == null) return null;
		if (support.kind() == Native.Kind.primitivePointer && context.constant())
			support = ((RawPointer.Supporter<?>) support).asConst();
		return support.align(context.align()).order(context.order());
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
