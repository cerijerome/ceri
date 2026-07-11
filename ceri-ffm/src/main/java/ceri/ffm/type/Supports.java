package ceri.ffm.type;

import java.nio.Buffer;
import java.util.Map;
import ceri.common.array.Dimensions;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.concurrent.Lazy;
import ceri.common.io.Buffers;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Native;
import ceri.ffm.reflect.Refine;
import ceri.ffm.reflect.Refine.Const;
import ceri.ffm.reflect.Refine.Dims;
import ceri.ffm.reflect.Refine.Nul;
import ceri.ffm.reflect.Refine.Packed;
import ceri.ffm.reflect.TypeNode;
import ceri.ffm.type.Support.Typed;

/**
 * Lookup for operational support of types and arrays.
 */
public class Supports {
	private static final Map<Class<?>, Support<?, ?, ?>> MAP = map();
	private static final Lazy.ForClass<Support<?, ?, ?>> cache = Lazy.forClass(c -> fromClass(c));
	public static final Supports DEF = new Supports(Defaults.DEF);
	private final Defaults defaults;

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
		System.out.println(Support.VOID.asPointer(true).asArray(3, true).asPointer());
		System.out.println(Primitive.BYTE.asPointer(true).asArray(3, true).asArray(2).asPointer());
		System.out.println(
			Primitive.CHAR.asArray(5, true).asPointer().asArray(3, true).asArray(2).asPointer());
	}

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

		/**
		 * Provides the array type configuration. Defaults to none.
		 */
		public Value string() {
			return values.getOrDefault(Category.string, NONE);
		}

		/**
		 * Provides the array type configuration. Defaults to none.
		 */
		public Value buffer() {
			return values.getOrDefault(Category.buffer, NONE);
		}

		/**
		 * Provides the array type configuration. Defaults to none.
		 */
		public Value array1d() {
			return values.getOrDefault(Category.array1d, NONE);
		}

		/**
		 * Provides the array type configuration. Defaults to none.
		 */
		public Value arrayNd() {
			return values.getOrDefault(Category.arrayNd, NONE);
		}

		/**
		 * Returns the array defaults based on component support.
		 */
		public Defaults.Value array(Support<?, ?, ?> support) {
			if (support.isArray()) return arrayNd();
			return switch (support.kind()) {
				case STRING, BUFFER -> arrayNd();
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
		return refine(StringType.supportFor(chars, size, nul), context);
	}

	private <B extends Buffer> Support<?, ?, ?> buffer(Class<B> cls, Refine.Context context) {
		var size = context.size(defaults.buffer().count());
		var nul = context.nul(defaults.buffer().nul());
		return refine(BufferType.supportFor(cls, size, nul), context);
	}

	private Support<?, ?, ?> refine(Support<?, ?, ?> support, Refine.Context context) {
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
		// TODO: function pointer
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
