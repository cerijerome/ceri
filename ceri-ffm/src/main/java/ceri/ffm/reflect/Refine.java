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
import ceri.common.array.Dimensions;
import ceri.common.data.Bytes;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.io.Direction;
import ceri.common.reflect.Annotations;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;
import ceri.common.util.Basics;
import ceri.ffm.core.Native;

/**
 * Support for refining the default behavior of types, methods, parameters and fields.
 */
public class Refine {
	public static final int UNSPECIFIED = -1;

	public static void main(String[] args) {
		int[][] ii = new int[0][0];
		System.out.println(ii);
	}
	
	/**
	 * String conversion charset.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Chars {
		/** The charset used to convert strings. */
		String value() default "";
	}

	/**
	 * General size configuration.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Size {
		/** The size value. */
		int value() default 0;

		/** The size by registered type name. */
		String type() default "";
	}

	/**
	 * Maximum value configuration.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Max {
		/** The size value. */
		int value() default 0;
	}

	/**
	 * Minimum value configuration.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Min {
		/** The value. */
		int value() default 0;
	}

	/**
	 * Array dimensions.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Dims {
		/** The size value. */
		int[] value() default {};
	}

	/**
	 * Method marker to capture last error.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface LastError {
		boolean value() default true;
	}

	/**
	 * Unsigned marker.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Unsigned {
		boolean value() default true;
	}

	/**
	 * Indicates arrays are nul-terminated.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
	public @interface Nul {}

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
		Bytes.Order value() default Bytes.Order.platform;
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
	 * Provides contextual configuration for calls and structs.
	 */
	public record Context(AnnotatedElement element) {
		public Charset chars(Charset def) {
			return Refine.resolveChars(element, def);
		}

		public Integer size(Integer def) {
			return Refine.resolveSize(element, def);
		}

		public Dimensions dims(Dimensions def) {
			return Refine.dims(element, def); // don't check parent
		}

		public boolean nul() {
			return Refine.nul(element);
		}

		public Direction direction(Direction def) {
			return Refine.direction(element, def); // don't check parent
		}

		public long align() {
			return Refine.resolveAlign(element, Native.Align.NATURAL);
		}

		public ByteOrder order() {
			return Refine.resolveOrder(element, ByteOrder.nativeOrder());
		}
	}

	/**
	 * Creates a new context from element.
	 */
	public static Context context(AnnotatedElement element) {
		return new Context(element);
	}

	/**
	 * Extract char configuration from annotated type and parents.
	 */
	public static Charset resolveChars(AnnotatedElement element, Charset def) {
		return resolve(element, Refine::chars, def);
	}

	/**
	 * Extract char configuration from annotated type.
	 */
	public static Charset chars(AnnotatedElement element, Charset def) {
		var anno = Annotations.annotation(element, Chars.class);
		if (anno == null || Strings.isEmpty(anno.value())) return def;
		return Charset.forName(anno.value());
	}

	/**
	 * Extract size from annotated type and parents.
	 */
	public static Integer resolveSize(AnnotatedElement element, Integer def) {
		return resolve(element, Refine::size, def);
	}

	/**
	 * Extract size from annotated type.
	 */
	public static Integer size(AnnotatedElement element, Integer def) {
		var anno = Annotations.annotation(element, Size.class);
		if (anno == null) return def;
		int size = anno.value();
		if (size > 0) return size;
		if (!Strings.isEmpty(anno.type())) return Native.Size.lookup(anno.type());
		return def;
	}

	/**
	 * Extract dimensions from annotated type and parents.
	 */
	public static Dimensions resolveDims(AnnotatedElement element, Dimensions def) {
		return resolve(element, Refine::dims, def);
	}

	/**
	 * Extract dimensions from annotated type.
	 */
	public static Dimensions dims(AnnotatedElement element, Dimensions def) {
		var anno = Annotations.annotation(element, Dims.class);
		if (anno == null) return def;
		int[] dims = anno.value();
		return dims.length == 0 ? def : Dimensions.of(dims);
	}

	/**
	 * Extract last error capture directive from annotated type and parents.
	 */
	public static Boolean resolveLastError(AnnotatedElement element, Boolean def) {
		return resolve(element, Refine::lastError, def);
	}

	/**
	 * Extract last error capture directive from annotated type.
	 */
	public static Boolean lastError(AnnotatedElement element, Boolean def) {
		return Annotations.value(element, LastError.class, LastError::value, def);
	}

	/**
	 * Extract signedness from annotated type.
	 */
	public static Boolean resolveUnsigned(AnnotatedElement element, Boolean def) {
		return resolve(element, Refine::unsigned, def);
	}

	/**
	 * Extract signedness from annotated type.
	 */
	public static Boolean unsigned(AnnotatedElement element, Boolean def) {
		return Annotations.value(element, Unsigned.class, Unsigned::value, def);
	}

	/**
	 * Extract nul-termination from annotated type.
	 */
	public static boolean nul(AnnotatedElement element) {
		return Annotations.has(element, Nul.class);
	}

	/**
	 * Extract alignment from annotated type and parents.
	 */
	public static Long resolveAlign(AnnotatedElement element, Long def) {
		return resolve(element, Refine::align, def);
	}

	/**
	 * Extract alignment from annotated type.
	 */
	public static Long align(AnnotatedElement element, Long def) {
		boolean packed = Annotations.has(element, Packed.class);
		Long align = Annotations.value(element, Align.class, Align::value);
		if (packed && align != null)
			throw Exceptions.illegalArg("Only one of @%s and @%s may be specified: %s",
				Reflect.name(Align.class), Reflect.name(Packed.class), element);
		if (packed) return 1L;
		if (align != null) return align;
		return def;
	}

	/**
	 * Extract byte order from annotated type and parents.
	 */
	public static ByteOrder resolveOrder(AnnotatedElement element, ByteOrder def) {
		return resolve(element, Refine::order, def);
	}

	/**
	 * Extract byte order from annotated type.
	 */
	public static ByteOrder order(AnnotatedElement element, ByteOrder def) {
		var value = Annotations.value(element, Order.class, Order::value, null);
		return value != null ? value.order : def;
	}

	/**
	 * Extract in/out configuration from annotated type.
	 */
	public static Direction direction(AnnotatedElement element, Direction def) {
		boolean in = Annotations.has(element, In.class);
		boolean out = Annotations.has(element, Out.class);
		if (in && out) return Direction.duplex;
		if (in) return Direction.in;
		if (out) return Direction.out;
		return def;
	}

	// support

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
}
