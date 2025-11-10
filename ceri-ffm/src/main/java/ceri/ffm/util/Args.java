package ceri.ffm.util;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable;
import ceri.common.collect.Iterators;
import ceri.common.collect.Sets;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.stream.Streams;
import ceri.common.text.Chars;
import ceri.common.text.Joiner;
import ceri.common.text.Strings;
import ceri.ffm.core.Memory;

/**
 * Utility to create strings from method arguments. Arrays and Iterable types are expanded.
 */
public class Args {
	public static Args DEFAULT = builder().addDefault(true).build();
	private static final List<Class<?>> INT_CLASSES =
		List.of(Byte.class, Short.class, Integer.class, Long.class);
	private static final int DEC_LIMIT = 9;
	private final List<Functions.Function<Object, String>> transforms;
	private final Joiner arrayJoiner;

	/**
	 * Predicate to match an instance of any given of the classes.
	 */
	public static Functions.Predicate<Object> matchClass(Class<?>... classes) {
		return obj -> Reflect.instanceOfAny(obj, classes);
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
		//if (n instanceof IntegerType) n = l;
		return String.format("%1$d|0x%1$x", n);
	}

	/**
	 * Structure to compact string.
	 */
//	public static String string(Structure t) {
//		return Struct.compactString(t);
//	}

	/**
	 * Pointer to compact string.
	 */
//	public static String string(Pointer p) {
//		if (p instanceof Memory m) return string(m);
//		return String.format("@%x", Pointer.nativeValue(p));
//	}

	/**
	 * Memory to compact string.
	 */
	public static String string(MemorySegment m) {
		return String.format("@%x+%x", Memory.address(m), Memory.size(m));
	}

	/**
	 * Memory to compact string.
	 */
//	public static String string(PointerType p) {
//		return String.format("%s(%s)", Reflect.nestedName(p.getClass()),
//			string(p.getPointer()));
//	}

	/**
	 * Callback to compact string.
	 */
//	public static String string(Callback cb) {
//		String s = String.valueOf(cb);
//		return s.substring(s.lastIndexOf(".") + 1);
//	}

	/**
	 * Byte buffer to compact string.
	 */
	public static String string(ByteBuffer b) {
		return String.format("%s(p=%d,l=%d,c=%d)", Reflect.nestedName(b.getClass()),
			b.position(), b.limit(), b.capacity());
	}

	/**
	 * Builder with convenience methods to add transforms.
	 */
	public static class Builder {
		final Set<Functions.Function<Object, String>> transforms = Sets.link();
		int arrayMax = 8;

		Builder() {}

		/**
		 * Adds default transforms.
		 */
		public Builder addDefault(boolean compactStruct) {
			//if (compactStruct) add(Structure.class, Args::string);
			return add(String.class, Chars::escape) //
				.add(matchInt(), n -> stringInt(n)) //
				//.add(IntegerType.class, n -> stringInt(n)) //
				//.add(Pointer.class, Args::string) //
				.add(MemorySegment.class, Args::string) //
				//.add(PointerType.class, Args::string) //
				//.add(Callback.class, Args::string) //
				.add(ByteBuffer.class, Args::string);
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
			return add(arg -> Reflect.castOrNull(cls, arg), fn);
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

		public Args build() {
			return new Args(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Args(Builder builder) {
		transforms = Immutable.list(builder.transforms);
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
		if (arg == null) return Strings.NULL;
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
		int len = RawArray.length(array);
		var iter = Iterators.indexed(len, i -> RawArray.get(array, i));
		return arrayJoiner.join(this::arg, iter, len);
	}
}
