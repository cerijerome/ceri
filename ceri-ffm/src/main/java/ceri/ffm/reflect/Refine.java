package ceri.ffm.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import ceri.common.data.Bytes;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.io.Direction;
import ceri.common.reflect.Annotations;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;
import ceri.ffm.core.Native;
import ceri.ffm.reflect.Refine.Chars.Value;

/**
 * Support for refining the default behavior of types, methods, parameters and fields.
 */
public class Refine {
	public static final int UNSPECIFIED = -1;

	/**
	 * A context that provides refinement settings.
	 */
	public interface Context {
		/** String conversion configuration. */
		Chars.Value chars();
		/** General size configuration. */
		int size();
		/** Byte alignment. */
		long align();
		/** Byte order. */
		ByteOrder order();
		/** Parameter direction. */
		Direction direction();

		/**
		 * Returns annotated refinements on demand.
		 */
		static Context from(AnnotatedElement element) {
			return Refine.context(() -> resolveChars(element), () -> resolveSize(element),
				() -> resolveAlignment(element), () -> resolveOrder(element),
				() -> resolveDirection(element));
		}
	}

	/**
	 * String conversion charset, and max bytes for reading.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Chars {
		/** The charset used to convert strings. */
		String value() default "";

		/** Max memory segment length. */
		int max() default Short.MAX_VALUE;

		record Value(Charset charset, int max) {
			public static final Value DEF = new Value(Charset.defaultCharset(), Short.MAX_VALUE);

			public static Value from(Chars c) {
				return c == null ? null :
					new Value(ceri.common.text.Chars.charset(c.value()), c.max());
			}
		}
	}

	/**
	 * General size configuration.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Size {
		int value();
	}

	/**
	 * Byte alignment configuration.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Align {
		long value();
	}

	/**
	 * Specifies a byte alignment of 1.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Packed {}

	/**
	 * Byte order configuration.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Order {
		Bytes.Order value();
	}

	/**
	 * Indicates a parameter should write its state for a method call, but not read its state after
	 * the call.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.PARAMETER })
	public @interface In {}

	/**
	 * Indicates a parameter should not write its state for a method call, and only read its state
	 * after the call.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.PARAMETER })
	public @interface Out {}

	/**
	 * Extract char configuration from annotated type and parents.
	 */
	public static Chars.Value resolveChars(AnnotatedElement element) {
		return resolve(element, Refine::chars, Chars.Value.DEF);
	}

	/**
	 * Extract char configuration from annotated type.
	 */
	public static Chars.Value chars(AnnotatedElement element) {
		return chars(element, Chars.Value.DEF);
	}

	/**
	 * Extract size from annotated type and parents.
	 */
	public static int resolveSize(AnnotatedElement element) {
		return Basics.def(size(element, null), UNSPECIFIED);
	}

	/**
	 * Extract size from annotated type.
	 */
	public static int size(AnnotatedElement element) {
		return size(element, UNSPECIFIED);
	}

	/**
	 * Extract alignment from annotated type and parents.
	 */
	public static long resolveAlignment(AnnotatedElement element) {
		return resolve(element, Refine::alignment, Native.ALIGN_NATURAL);
	}

	/**
	 * Extract alignment from annotated type.
	 */
	public static long alignment(AnnotatedElement element) {
		return alignment(element, Native.ALIGN_NATURAL);
	}

	/**
	 * Extract byte order from annotated type and parents.
	 */
	public static ByteOrder resolveOrder(AnnotatedElement element) {
		return Bytes.Order.order(resolve(element, Refine::order, Bytes.Order.platform));
	}

	/**
	 * Extract byte order from annotated type.
	 */
	public static Bytes.Order order(AnnotatedElement element) {
		return order(element, Bytes.Order.platform);
	}

	/**
	 * Extract in/out configuration from annotated type and parents.
	 */
	public static Direction resolveDirection(AnnotatedElement element) {
		return Basics.def(direction(element), Direction.duplex);
	}

	/**
	 * Extract in/out configuration from annotated type.
	 */
	public static Direction direction(AnnotatedElement element) {
		return direction(element, Direction.duplex);
	}

	// support

	private static Chars.Value chars(AnnotatedElement element, Chars.Value def) {
		return Annotations.value(element, Chars.class, Chars.Value::from, def);
	}

	private static Integer size(AnnotatedElement element, Integer def) {
		return Annotations.value(element, Size.class, Size::value, def);
	}

	private static Long alignment(AnnotatedElement element, Long def) {
		boolean packed = Annotations.has(element, Packed.class);
		Long align = Annotations.value(element, Align.class, Align::value);
		if (packed && align != null)
			throw Exceptions.illegalArg("Only one of @%s and @%s may be specified: %s",
				Reflect.name(Align.class), Reflect.name(Packed.class), element);
		if (packed) return 1L;
		if (align != null) return align;
		return def;
	}

	private static Bytes.Order order(AnnotatedElement element, Bytes.Order def) {
		return Annotations.value(element, Order.class, Order::value, def);
	}

	private static Direction direction(AnnotatedElement element, Direction def) {
		boolean in = Annotations.has(element, In.class);
		boolean out = Annotations.has(element, Out.class);
		if (in && out) throw Exceptions.illegalArg("Only one of @%s and @%s may be specified: %s",
			Reflect.name(In.class), Reflect.name(Out.class), element);
		if (in) return Direction.in;
		if (out) return Direction.out;
		return def;
	}

	private static <T> T resolve(AnnotatedElement element,
		Functions.BiFunction<AnnotatedElement, T, T> func, T def) {
		T value = switch (element) {
			case Parameter p -> resolveParameter(p, func);
			case Executable e -> resolveExecutable(e, func);
			case Class<?> c -> func.apply(c, null);
			case null -> null;
			default -> null;
		};
		return Basics.def(value, def);
	}

	private static <T> T resolveParameter(Parameter parameter,
		Functions.BiFunction<AnnotatedElement, T, T> func) {
		var t = func.apply(parameter, null);
		if (t != null) return t;
		return resolve(parameter.getDeclaringExecutable(), func, null);
	}

	private static <T> T resolveExecutable(Executable executable,
		Functions.BiFunction<AnnotatedElement, T, T> func) {
		var t = func.apply(executable, null);
		if (t != null) return t;
		return resolve(executable.getDeclaringClass(), func, null);
	}

	private static Context context(Functions.Supplier<Chars.Value> chars,
		Functions.IntSupplier size, Functions.LongSupplier align,
		Functions.Supplier<ByteOrder> order, Functions.Supplier<Direction> direction) {
		return new Context() {
			@Override
			public Value chars() {
				return chars.get();
			}

			@Override
			public int size() {
				return size.getAsInt();
			}

			@Override
			public long align() {
				return align.getAsLong();
			}

			@Override
			public ByteOrder order() {
				return order.get();
			}

			@Override
			public Direction direction() {
				return direction.get();
			}
		};
	}
}
