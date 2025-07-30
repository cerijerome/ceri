package ceri.jna.util;

import static ceri.common.text.StringUtil.NULL;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import com.sun.jna.Callback;
import com.sun.jna.IntegerType;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import ceri.common.collection.ImmutableUtil;
import ceri.common.collection.IteratorUtil;
import ceri.common.function.Functions;
import ceri.common.reflect.ReflectUtil;
import ceri.common.stream.Streams;
import ceri.common.text.Joiner;
import ceri.common.text.StringUtil;
import ceri.jna.type.Struct;

/**
 * Utility to create strings from method arguments. Arrays and Iterable types are expanded.
 */
public class JnaArgs {
	public static JnaArgs DEFAULT = builder().addDefault(true).build();
	private static final List<Class<?>> INT_CLASSES =
		List.of(Byte.class, Short.class, Integer.class, Long.class);
	private static final int DEC_LIMIT = 9;
	private final List<Functions.Function<Object, String>> transforms;
	private final Joiner arrayJoiner;

	/**
	 * Predicate to match an instance of any given of the classes.
	 */
	public static Functions.Predicate<Object> matchClass(Class<?>... classes) {
		return obj -> ReflectUtil.instanceOfAny(obj, classes);
	}

	/**
	 * Predicate to match byte/short/int/long primitive or primitive wrapper, and cast to Number.
	 */
	public static Functions.Function<Object, Number> matchInt() {
		return arg -> INT_CLASSES.contains(arg.getClass()) ? (Number) arg : null;
	}

	/**
	 * Int Number type to string. Decimal and hex if within limits. Will throw exception if Number
	 * type is incompatible with %d and %x format conversions.
	 */
	public static String stringInt(Number n) {
		return stringInt(n, -1, DEC_LIMIT);
	}

	/**
	 * Int Number type to string. Decimal and hex if within limits. Will throw exception if Number
	 * type is incompatible with %d and %x format conversions.
	 */
	public static String stringInt(Number n, int hexMin, int hexMax) {
		long l = n.longValue();
		if (l >= hexMin && l <= hexMax) return String.valueOf(n);
		if (n instanceof IntegerType) n = l;
		return String.format("%1$d|0x%1$x", n);
	}

	/**
	 * Structure to compact string.
	 */
	public static String string(Structure t) {
		return Struct.compactString(t);
	}

	/**
	 * Pointer to compact string.
	 */
	public static String string(Pointer p) {
		if (p instanceof Memory m) return string(m);
		return String.format("@%x", Pointer.nativeValue(p));
	}

	/**
	 * Memory to compact string.
	 */
	public static String string(Memory m) {
		return String.format("@%x+%x", Pointer.nativeValue(m), m.size());
	}

	/**
	 * Memory to compact string.
	 */
	public static String string(PointerType p) {
		return String.format("%s(%s)", ReflectUtil.nestedName(p.getClass()),
			string(p.getPointer()));
	}

	/**
	 * Callback to compact string.
	 */
	public static String string(Callback cb) {
		String s = String.valueOf(cb);
		return s.substring(s.lastIndexOf(".") + 1);
	}

	/**
	 * Byte buffer to compact string.
	 */
	public static String string(ByteBuffer b) {
		return String.format("%s(p=%d,l=%d,c=%d)", ReflectUtil.nestedName(b.getClass()),
			b.position(), b.limit(), b.capacity());
	}

	/**
	 * Builder with convenience methods to add transforms.
	 */
	public static class Builder {
		final Collection<Functions.Function<Object, String>> transforms = new LinkedHashSet<>();
		int arrayMax = 8;

		Builder() {}

		/**
		 * Adds default transforms.
		 */
		public Builder addDefault(boolean compactStruct) {
			if (compactStruct) add(Structure.class, JnaArgs::string);
			return add(String.class, StringUtil::escape) //
				.add(matchInt(), n -> stringInt(n)) //
				.add(IntegerType.class, n -> stringInt(n)) //
				.add(Pointer.class, JnaArgs::string) //
				.add(PointerType.class, JnaArgs::string) //
				.add(Callback.class, JnaArgs::string) //
				.add(ByteBuffer.class, JnaArgs::string);
		}

		/**
		 * Adds an argument string transform for matching type.
		 */
		public <T> Builder add(Functions.Function<Object, T> match,
			Functions.Function<T, String> fn) {
			return add(arg -> {
				T t = match.apply(arg);
				return t == null ? null : fn.apply(t);
			});
		}

		/**
		 * Adds an argument string formatter for matching predicate.
		 */
		public Builder add(Functions.Predicate<Object> match, String format) {
			return add(arg -> match.test(arg) ? String.format(format, arg) : null);
		}

		/**
		 * Adds an argument string formatter for matching class.
		 */
		public Builder add(Class<?> cls, String format) {
			return add(cls, arg -> String.format(format, arg));
		}

		/**
		 * Adds an argument string transform for matching class.
		 */
		public <T> Builder add(Class<T> cls, Functions.Function<T, String> fn) {
			return add(arg -> ReflectUtil.castOrNull(cls, arg), fn);
		}

		/**
		 * Adds an argument string transform. Transform should return null if no match.
		 */
		public Builder add(Functions.Function<Object, String> transform) {
			transforms.add(transform);
			return this;
		}

		public Builder arrayMax(int arrayMax) {
			this.arrayMax = arrayMax;
			return this;
		}

		public JnaArgs build() {
			return new JnaArgs(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	JnaArgs(Builder builder) {
		transforms = ImmutableUtil.copyAsList(builder.transforms);
		arrayJoiner = Joiner.builder().prefix("[").separator(",").suffix("]").max(builder.arrayMax)
			.remainder("..").build();
	}

	/**
	 * Creates a comma-separated string from given arguments.
	 */
	public String args(Object... args) {
		return Joiner.COMMA.joinAll(this::arg, args);
	}

	/**
	 * Creates a string from given argument.
	 */
	public String arg(Object arg) {
		if (arg == null) return NULL;
		for (var transform : transforms) {
			var result = transform.apply(arg);
			if (result != null) return result;
		}
		if (arg.getClass().isArray()) return arrayString(arg);
		if (arg instanceof Iterable<?> i) return iterableString(i);
		return String.valueOf(arg);
	}

	private String iterableString(Iterable<?> iterable) {
		return arrayString(Streams.from(iterable).toArray());
	}

	private String arrayString(Object array) {
		int len = Array.getLength(array);
		var iter = IteratorUtil.indexed(len, i -> Array.get(array, i));
		return arrayJoiner.join(this::arg, iter, len);
	}
}
