package ceri.ffm.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.AnnotatedElement;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import ceri.common.array.Dimensions;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.data.Bytes;
import ceri.common.except.Exceptions;
import ceri.common.io.Direction;
import ceri.common.reflect.Annotations;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.type.MultiArray;

/**
 * Support for refining the default behavior of types, methods, parameters and fields.
 */
public class Refine {
	public static final int NUL_MAX_DEF = 0x100;
	public static final int UNSPECIFIED = -1;

	/**
	 * Method marker to capture last error.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface LastError {
		boolean value() default true;
	}

	/**
	 * Byte alignment configuration.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD,
		ElementType.TYPE_USE })
	public @interface Align {
		long value();
	}

	/**
	 * Specifies a byte alignment of 1.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD,
		ElementType.TYPE_USE })
	public @interface Packed {}

	/**
	 * Byte order configuration.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD,
		ElementType.TYPE_USE })
	public @interface Order {
		Bytes.Order value() default Bytes.Order.platform;
	}

	/**
	 * Indicates arrays are nul-terminated.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD,
		ElementType.TYPE_USE })
	public @interface Nul {
		boolean value() default true;
	}

	/**
	 * Indicates unmodifiable pointer data.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD,
		ElementType.TYPE_USE })
	public @interface Const {
		boolean value() default true;
	}

	/**
	 * Unsigned marker for integer types.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	public @interface Unsigned {
		boolean value() default true;
	}

	/**
	 * General size configuration.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD,
		ElementType.TYPE_USE })
	public @interface Size {
		/** The size value. */
		int value() default 0;

		/** The size by registered type name. */
		String type() default "";
	}

	/**
	 * Multi-array dimensions.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD,
		ElementType.TYPE_USE })
	public @interface Dims {
		/** The size value. */
		int[] value() default {};
	}

	/**
	 * String conversion charset.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD,
		ElementType.TYPE_USE })
	public @interface Chars {
		/** The charset used to convert strings. */
		String value() default "";
	}

	/**
	 * Indicates a parameter should write its state for a method call.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.PARAMETER })
	public @interface In {}

	/**
	 * Indicates a parameter should read its state after a method call.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.PARAMETER })
	public @interface Out {}

	/**
	 * Provides contextual configuration for type declarations.
	 */
	public interface Context {
		Context DEFAULT = new Custom(Map.of());

		/**
		 * Gets byte alignment, returning natural if unspecified.
		 */
		default long align() {
			return align(Native.Align.NATURAL);
		}

		/**
		 * Gets byte alignment, returning default if unspecified.
		 */
		default long align(long def) {
			return def;
		}

		/**
		 * Gets byte order, returning native if unspecified.
		 */
		default ByteOrder order() {
			return order(ByteOrder.nativeOrder());
		}

		/**
		 * Gets byte order, returning default if unspecified.
		 */
		default ByteOrder order(ByteOrder def) {
			return def;
		}

		/**
		 * Gets nul-termination directive, returning false if unspecified.
		 */
		default boolean nul() {
			return nul(false);
		}

		/**
		 * Gets nul-termination directive, returning default if unspecified.
		 */
		default boolean nul(boolean def) {
			return def;
		}

		/**
		 * Gets constant directive, returning false if unspecified.
		 */
		default boolean constant() {
			return constant(false);
		}

		/**
		 * Gets constant directive, returning default if unspecified.
		 */
		default boolean constant(boolean def) {
			return def;
		}

		/**
		 * Gets unsigned directive, returning null if unspecified.
		 */
		default boolean unsigned() {
			return unsigned(false);
		}

		/**
		 * Gets unsigned directive, returning false if unspecified.
		 */
		default boolean unsigned(boolean def) {
			return def;
		}

		/**
		 * Gets size, returning zero if unspecified.
		 */
		default int size() {
			return size(0);
		}

		/**
		 * Gets size, returning default if unspecified.
		 */
		default int size(int def) {
			return def;
		}

		/**
		 * Gets multi-array dimensions, returning none if unspecified.
		 */
		default Dimensions dims() {
			return dims(Dimensions.NONE);
		}

		/**
		 * Gets multi-array dimensions, returning default if unspecified.
		 */
		default Dimensions dims(Dimensions def) {
			return def;
		}

		/**
		 * Get fixed dimensions of given count, taking into account nul-termination.
		 */
		default Dimensions dims(int count, boolean nul, int nulMax) {
			if (nulMax <= 0) nulMax = NUL_MAX_DEF;
			return MultiArray.fix(dims(), count, nul, nulMax);
		}

		/**
		 * Gets charset, returning system default if unspecified.
		 */
		default Charset chars() {
			return chars(Charset.defaultCharset());
		}

		/**
		 * Gets charset, returning default if unspecified.
		 */
		default Charset chars(Charset def) {
			return def;
		}

		/**
		 * Gets direction, returning duplex if unspecified.
		 */
		default Direction direction() {
			return direction(Direction.duplex);
		}

		/**
		 * Gets direction, returning default if unspecified.
		 */
		default Direction direction(Direction def) {
			return def;
		}
	}

	/**
	 * Provides contextual configuration from annotations.
	 */
	public record Annotated(AnnotatedElement element) implements Context {
		/**
		 * Gets byte alignment from the element or its parents.
		 */
		@Override
		public long align(long def) {
			return Annotations.resolve(element(), Refine::align, def);
		}

		/**
		 * Gets byte order from the element or its parents.
		 */
		@Override
		public ByteOrder order(ByteOrder def) {
			return Annotations.resolve(element(), Refine::order, def);
		}

		/**
		 * Gets nul-termination directive from the element or its parents.
		 */
		@Override
		public boolean nul(boolean def) {
			return Annotations.resolve(element(), Refine::nul, def);
		}

		/**
		 * Gets nul-termination directive from the element or its parents.
		 */
		@Override
		public boolean constant(boolean def) {
			return Annotations.resolve(element(), Refine::constant, def);
		}

		/**
		 * Gets unsigned directive from the element or its parents.
		 */
		@Override
		public boolean unsigned(boolean def) {
			return Annotations.resolve(element(), Refine::unsigned, def);
		}

		/**
		 * Gets size from the element or its parents.
		 */
		@Override
		public int size(int def) {
			return Annotations.resolve(element(), Refine::size, def);
		}

		/**
		 * Gets dimensions from the element directly.
		 */
		@Override
		public Dimensions dims(Dimensions def) {
			return Refine.dims(element(), def); // Don't resolve to parent
		}

		/**
		 * Gets charset from the element or its parents.
		 */
		@Override
		public Charset chars(Charset def) {
			return Annotations.resolve(element(), Refine::chars, def);
		}

		/**
		 * Gets direction from the element directly.
		 */
		@Override
		public Direction direction(Direction def) {
			return Refine.direction(element(), def); // Don't resolve to parent
		}
	}

	/**
	 * Refinement values.
	 */
	private enum Value {
		align,
		order,
		nul,
		constant,
		unsigned,
		size,
		dims,
		chars,
		direction
	}

	/**
	 * Customizable refinements.
	 */
	private static class Custom implements Context {
		private final Map<Value, Object> values;

		private Custom(Map<Value, Object> values) {
			this.values = values;
		}

		@Override
		public long align(long def) {
			return get(Value.align, def);
		}

		@Override
		public ByteOrder order(ByteOrder def) {
			return get(Value.order, def);
		}

		@Override
		public boolean nul(boolean def) {
			return get(Value.nul, def);
		}

		@Override
		public boolean constant(boolean def) {
			return get(Value.constant, def);
		}

		@Override
		public boolean unsigned(boolean def) {
			return get(Value.unsigned, def);
		}

		@Override
		public int size(int def) {
			return get(Value.size, def);
		}

		@Override
		public Dimensions dims(Dimensions def) {
			return get(Value.dims, def);
		}

		@Override
		public Charset chars(Charset def) {
			return get(Value.chars, def);
		}

		@Override
		public Direction direction(Direction def) {
			return get(Value.direction, def);
		}

		@Override
		public int hashCode() {
			return values.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return (obj instanceof Custom c) && Objects.equals(values, c.values);
		}

		@Override
		public String toString() {
			return values.toString();
		}

		private <T> T get(Value type, Object def) {
			return Reflect.unchecked(values.getOrDefault(type, def));
		}
	}

	/**
	 * A builder for custom refinements.
	 */
	public static class Customizer {
		private final Map<Value, Object> values = Maps.of();

		/**
		 * Override alignment.
		 */
		public Customizer align(long align) {
			return put(Value.align, align);
		}

		/**
		 * Override byte order.
		 */
		public Customizer order(ByteOrder order) {
			return put(Value.order, order);
		}

		/**
		 * Set alignment and order from layout.
		 */
		public Customizer copy(MemoryLayout layout) {
			if (layout == null) return this;
			align(layout.byteAlignment());
			if (layout instanceof ValueLayout v) order(v.order());
			return this;
		}

		/**
		 * Override nul-termination option.
		 */
		public Customizer nul() {
			return nul(Boolean.TRUE);
		}

		/**
		 * Override nul-termination option.
		 */
		public Customizer nul(Boolean nul) {
			return put(Value.nul, nul);
		}

		/**
		 * Override constant directive.
		 */
		public Customizer constant() {
			return constant(Boolean.TRUE);
		}

		/**
		 * Override constant directive.
		 */
		public Customizer constant(Boolean constant) {
			return put(Value.constant, constant);
		}

		/**
		 * Override unsigned option.
		 */
		public Customizer unsigned() {
			return unsigned(Boolean.TRUE);
		}

		/**
		 * Override unsigned option.
		 */
		public Customizer unsigned(Boolean unsigned) {
			return put(Value.unsigned, unsigned);
		}

		/**
		 * Override size.
		 */
		public Customizer size(Integer size) {
			return put(Value.size, size);
		}

		/**
		 * Override dimensions.
		 */
		public Customizer dims(int...dims) {
			return dims(Dimensions.of(dims));
		}

		/**
		 * Override dimensions.
		 */
		public Customizer dims(Dimensions dims) {
			return put(Value.dims, dims);
		}

		/**
		 * Override charset.
		 */
		public Customizer chars(Charset chars) {
			return put(Value.chars, chars);
		}

		/**
		 * Override direction.
		 */
		public Customizer in() {
			return direction(Direction.in);
		}

		/**
		 * Override direction.
		 */
		public Customizer out() {
			return direction(Direction.out);
		}

		/**
		 * Override direction.
		 */
		public Customizer direction(Direction direction) {
			return put(Value.direction, direction);
		}

		/**
		 * Returns the custom context.
		 */
		public Context context() {
			return new Custom(Immutable.map(values));
		}

		private Customizer put(Value type, Object value) {
			if (value == null) values.remove(type);
			else values.put(type, value);
			return this;
		}
	}

	/**
	 * Returns a custom context builder.
	 */
	public static Customizer custom() {
		return new Customizer();
	}

	/**
	 * Returns a custom context builder starting with given context values.
	 */
	public static Customizer custom(AnnotatedElement element) {
		return custom(context(element));
	}

	/**
	 * Returns a custom context builder starting with given context values.
	 */
	public static Customizer custom(Context context) {
		if (context == null) return custom();
		return custom().align(context.align()).order(context.order()).nul(context.nul())
			.unsigned(context.unsigned()).size(context.size()).dims(context.dims())
			.chars(context.chars()).direction(context.direction());
	}

	/**
	 * Creates a new context using annotations.
	 */
	public static Annotated context(AnnotatedElement element) {
		return new Annotated(element);
	}

	/**
	 * Apply context alignment and byte order to the layout.
	 */
	public static <L extends MemoryLayout> L apply(Context context, L layout) {
		if (context == null || layout == null) return layout; 
		return Layouts.set(layout, null, context.align(), context.order());
	}
	
	/**
	 * Extract last error capture directive from annotated type.
	 */
	public static Boolean lastError(AnnotatedElement element, Boolean def) {
		return Annotations.resolve(element, LastError.class, LastError::value, def);
	}

	// support

	static Long align(AnnotatedElement element, Long def) {
		// Look for annotations to the left of arrays
		element = Annotations.component(element);
		boolean packed = Annotations.has(element, Packed.class);
		Long align = Annotations.value(element, Align.class, Align::value);
		if (packed && align != null)
			throw Exceptions.illegalArg("Only one of @%s and @%s may be specified: %s",
				Reflect.name(Align.class), Reflect.name(Packed.class), element);
		if (packed) return 1L;
		if (align != null) return align;
		return def;
	}

	static ByteOrder order(AnnotatedElement element, ByteOrder def) {
		// Look for annotations to the left of arrays
		element = Annotations.component(element);
		var value = Annotations.value(element, Order.class, Order::value, null);
		return value != null ? value.order : def;
	}

	static Boolean nul(AnnotatedElement element, Boolean def) {
		// Look for annotations to the left of arrays
		element = Annotations.component(element);
		return Annotations.value(element, Nul.class, Nul::value, def);
	}

	static Boolean constant(AnnotatedElement element, Boolean def) {
		// Look for annotations to the left of arrays
		element = Annotations.component(element);
		return Annotations.value(element, Const.class, Const::value, def);
	}

	static Boolean unsigned(AnnotatedElement element, Boolean def) {
		// Look for annotations to the left of arrays
		element = Annotations.component(element);
		return Annotations.value(element, Unsigned.class, Unsigned::value, def);
	}

	static Integer size(AnnotatedElement element, Integer def) {
		// Look for annotations to the left of arrays
		element = Annotations.component(element);
		var anno = Annotations.annotation(element, Size.class);
		if (anno == null) return def;
		int size = anno.value();
		if (size > 0) return size;
		if (!Strings.isEmpty(anno.type())) return Native.Size.lookup(anno.type());
		return def;
	}

	static Dimensions dims(AnnotatedElement element, Dimensions def) {
		// Look for annotations to the left of arrays
		element = Annotations.component(element);
		var anno = Annotations.annotation(element, Dims.class);
		if (anno == null) return def;
		int[] dims = anno.value();
		return dims.length == 0 ? def : Dimensions.of(dims);
	}

	static Charset chars(AnnotatedElement element, Charset def) {
		// Look for annotations to the left of arrays
		element = Annotations.component(element);
		var anno = Annotations.annotation(element, Chars.class);
		if (anno == null || Strings.isEmpty(anno.value())) return def;
		return Charset.forName(anno.value());
	}

	static Direction direction(AnnotatedElement element, Direction def) {
		// Look for annotations to the left of arrays
		element = Annotations.component(element);
		boolean in = Annotations.has(element, In.class);
		boolean out = Annotations.has(element, Out.class);
		if (in && out) return Direction.duplex;
		if (in) return Direction.in;
		if (out) return Direction.out;
		return def;
	}
}
