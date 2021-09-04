package ceri.serial.jna;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.collection.ArrayUtil.shorts;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.collection.ImmutableUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.StringUtil;

/**
 * Utility to create strings from methods arguments. Arrays and Iterable types are expanded.
 */
public class JnaArgs {
	public static JnaArgs DEFAULT = builder().addDefault().build();
	private static final List<Class<?>> INT_CLASSES = List.of(Byte.TYPE, Short.TYPE, Integer.TYPE,
		Long.TYPE, Byte.class, Short.class, Integer.class, Long.class);
	private static final String NULL_STRING = "null";
	private static final int HEX_LIMIT = 0xf;
	private final List<Function<Object, String>> transforms;

	public static void main(String[] args) {
		System.out.println(DEFAULT.string("x", -1, bytes(2, -3), List.of(-4L, 5L, 0x10L), -1L, 1.1f,
			2.2, shorts()));
	}

	/**
	 * Predicate to match an instance of any given of the classes.
	 */
	public static Predicate<Object> matchClass(Class<?>... classes) {
		return obj -> ReflectUtil.instanceOfAny(obj, classes);
	}

	/**
	 * Predicate to match byte/short/int/long primitive or primitive wrapper, and cast to Number.
	 */
	public static Function<Object, Number> matchInt() {
		return arg -> INT_CLASSES.contains(arg.getClass()) ? (Number) arg : null;
	}

	/**
	 * Int Number type to string. Decimal and hex if within limits. Will throw exception if Number
	 * type is incompatible with %d and %x format conversions.
	 */
	public static String intString(Number n, int hexMin, int hexMax) {
		int i = n.intValue();
		return i >= hexMin && i <= hexMax ? String.valueOf(n) : String.format("%1$d/0x%1$x", n);
	}

	/**
	 * Structure to compact string.
	 */
	public static String string(Structure t) {
		return String.format("%s@%x+%x", ReflectUtil.name(t.getClass()),
			Pointer.nativeValue(t.getPointer()), t.size());
	}

	/**
	 * Pointer to compact string.
	 */
	public static String string(Pointer p) {
		if (p == null) return NULL_STRING;
		if (p instanceof Memory m) return string(m);
		return String.format("@%x", Pointer.nativeValue(p));
	}

	/**
	 * Memory to compact string.
	 */
	public static String string(Memory m) {
		if (m == null) return String.valueOf(m);
		return String.format("@%x+%x", Pointer.nativeValue(m), m.size());
	}

	/**
	 * Builder with convenience methods to add transforms.
	 */
	public static class Builder {
		final Collection<Function<Object, String>> transforms = new LinkedHashSet<>();

		Builder() {}

		/**
		 * Adds default transforms.
		 */
		public Builder addDefault() {
			return add(String.class, StringUtil::escape) //
				.add(matchInt(), n -> intString(n, -HEX_LIMIT, HEX_LIMIT)) //
				.add(Structure.class, JnaArgs::string) //
				.add(Pointer.class, JnaArgs::string);
		}

		/**
		 * Adds an argument string transform for matching type.
		 */
		public <T> Builder add(Function<Object, T> match, Function<T, String> fn) {
			return add(arg -> {
				T t = match.apply(arg);
				return t == null ? null : fn.apply(t);
			});
		}

		/**
		 * Adds an argument string formatter for matching predicate.
		 */
		public Builder add(Predicate<Object> match, String format) {
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
		public <T> Builder add(Class<T> cls, Function<T, String> fn) {
			return add(arg -> ReflectUtil.castOrNull(cls, arg), fn);
		}

		/**
		 * Adds an argument string transform. Transform should return null if no match.
		 */
		public Builder add(Function<Object, String> transform) {
			transforms.add(transform);
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
	}

	/**
	 * Creates a comma-separated string from given arguments.
	 */
	public String string(Object... args) {
		return StringUtil.joinAll(", ", transformArgs(args));
	}

	private Object[] transformArgs(Object... args) {
		for (int i = 0; i < args.length; i++)
			args[i] = transformArg(args[i]);
		return args;
	}

	private String transformArg(Object arg) {
		if (arg == null) return NULL_STRING;
		if (arg instanceof Iterable<?> i) return iterableString(i);
		if (arg.getClass().isArray()) return arrayString(arg);
		for (var transform : transforms) {
			var result = transform.apply(arg);
			if (result != null) return result;
		}
		return String.valueOf(arg);
	}

	private String iterableString(Iterable<?> iterable) {
		return arrayString(StreamUtil.stream(iterable.iterator()).toArray());
	}

	private String arrayString(Object obj) {
		return "[" + string(array(obj)) + "]";
	}

	private static Object[] array(Object obj) {
		Object[] array = new Object[Array.getLength(obj)];
		for (int i = 0; i < array.length; i++)
			array[i] = Array.get(obj, i);
		return array;
	}

}
