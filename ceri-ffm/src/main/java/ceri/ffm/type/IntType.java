package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import ceri.common.array.Array;
import ceri.common.collect.Maps;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.text.Format;
import ceri.ffm.reflect.Refine;
import ceri.ffm.reflect.Refine.Size;
import ceri.ffm.reflect.Refine.Unsigned;
import ceri.ffm.type.Support.Typed;

/**
 * Represents an immutable integer type of desired size and signedness.
 */
public abstract class IntType<T extends IntType<T>> implements Comparable<T> {
	private static final Map<Class<?>, Support<?>> cache = Maps.syncWeak();
	private final Support<T> support;
	private final Number nativeValue; // signed

	/**
	 * C signed long type.
	 */
	@Size(type = "long")
	public static class CLong extends IntType<CLong> {
		public static final Support<CLong> TYPE = support(CLong.class);

		public CLong(Number value) {
			super(value);
		}
	}

	/**
	 * C unsigned long type.
	 */
	@Unsigned
	@Size(type = "long")
	public static class CUlong extends IntType<CUlong> {
		public static final Support<CUlong> TYPE = support(CUlong.class);

		public CUlong(Number value) {
			super(value);
		}
	}

	/**
	 * C size_t unsigned type.
	 */
	@Unsigned
	@Size(type = "size_t")
	public static class size_t extends IntType<size_t> {
		public static final Support<size_t> TYPE = support(size_t.class);

		public size_t(Number value) {
			super(value);
		}
	}

	/**
	 * C ssize_t signed type.
	 */
	@Size(type = "size_t")
	public static class ssize_t extends IntType<ssize_t> {
		public static final Support<ssize_t> TYPE = support(ssize_t.class);

		public ssize_t(Number value) {
			super(value);
		}
	}

	/**
	 * C wchar_t signed type. Native type is usually unsigned, but not always.
	 */
	@Size(type = "wchar_t")
	public static class wchar_t extends IntType<wchar_t> {
		public static final Support<wchar_t> TYPE = support(wchar_t.class);
		public static final wchar_t TERM = new wchar_t(0);
		public static final Charset CHARSET = charset(TYPE.spec().size());

		public wchar_t(Number value) {
			super(value);
		}
	}

	/**
	 * Int type specification.
	 */
	public record Spec(int size, boolean unsigned) implements Comparator<Number> {
		/**
		 * Returns true if this is unsigned long; negative longs should be treated as positive.
		 */
		public boolean isUlong() {
			return size() == Long.BYTES && unsigned();
		}

		/**
		 * Returns the native value object for the given number.
		 */
		public Number nativeValue(Number n) {
			long value = 0L;
			if (n != null) value = unsigned() ? unsignedValue(n) : n.longValue();
			return switch (size()) {
				case Byte.BYTES -> (byte) value;
				case Short.BYTES -> (short) value;
				case Integer.BYTES -> (int) value;
				default -> value;
			};
		}

		/**
		 * Returns a long value for the given native value object.
		 */
		public long value(Number nativeValue) {
			return unsigned() ? unsignedValue(nativeValue) : nativeValue.longValue();
		}

		/**
		 * Compares numbers based on signedness.
		 */
		@Override
		public int compare(Number lhs, Number rhs) {
			if (lhs == null) lhs = 0L;
			if (rhs == null) rhs = 0L;
			return unsigned() ? Long.compareUnsigned(lhs.longValue(), rhs.longValue()) :
				Long.compare(lhs.longValue(), rhs.longValue());
		}

		@Override
		public String toString() {
			return (unsigned() ? "u" : "s") + (size << 3);
		}

		private static long unsignedValue(Number n) {
			return switch (n) {
				case Byte b -> Maths.ubyte(b);
				case Short s -> Maths.ushort(s);
				case Integer i -> Maths.uint(i);
				default -> n.longValue();
			};
		}
	}

	/**
	 * Support for int type operations.
	 */
	public static class Support<T extends IntType<T>> extends Typed<T, ValueLayout> {
		private final Config<T> config;
		private final Typed<? extends Number, ? extends ValueLayout> boxed;

		private record Config<T>(Class<T> type, Spec spec,
			Functions.Function<Number, T> constructor) {}

		private Support(Config<T> config, Typed<? extends Number, ? extends ValueLayout> boxed) {
			super(boxed.layout());
			this.config = config;
			this.boxed = boxed;
		}

		@Override
		public Class<T> type() {
			return config.type();
		}

		/**
		 * Returns the type specification.
		 */
		public Spec spec() {
			return config.spec();
		}

		/**
		 * Creates an instance from the given value.
		 */
		public T of(Number n) {
			if (n == null) return null;
			return config.constructor().apply(n);
		}

		/**
		 * Creates an array from given values.
		 */
		public T[] ofAll(Number[] ns) {
			if (ns == null) return null;
			var array = Array.ofType(type(), ns.length);
			for (int i = 0; i < ns.length; i++)
				array[i] = of(ns[i]);
			return array;
		}

		/**
		 * Creates an array from given values.
		 */
		public T[] ofAll(long... ns) {
			if (ns == null) return null;
			var array = Array.ofType(type(), ns.length);
			for (int i = 0; i < ns.length; i++)
				array[i] = of(ns[i]);
			return array;
		}

		@Override
		public Support<T> with(String name, long align, ByteOrder order) {
			var boxed = this.boxed.with(name, align, order);
			return boxed == this.boxed ? this : new Support<>(config, boxed);
		}

		@Override
		public T val() {
			return of(boxed.val());
		}

		/**
		 * Allocates memory and copies values from the array within bounds.
		 */
		@SafeVarargs
		public final MemorySegment allocAll(SegmentAllocator allocator, boolean nul,
			Number... array) {
			return allocArray(allocator, ofAll(array), nul);
		}

		/**
		 * Copies values to memory within bounds; returns the number of values copied.
		 */
		@SafeVarargs
		public final int writeAll(MemorySegment memory, boolean nul, Number... array) {
			return writeAll(memory, 0L, nul, array);
		}

		/**
		 * Copies values to memory within bounds; returns the number of values copied.
		 */
		@SafeVarargs
		public final int writeAll(MemorySegment memory, long offset, boolean nul, Number... array) {
			return writeAll(memory, offset, Long.MAX_VALUE, nul, array);
		}

		/**
		 * Copies values to memory within bounds; returns the number of values copied.
		 */
		@SafeVarargs
		public final int writeAll(MemorySegment memory, long offset, long length, boolean nul,
			Number... array) {
			return writeArray(ofAll(array), 0, Integer.MAX_VALUE, memory, offset, length, nul);
		}

		@Override
		T rawGet(MemorySegment memory, long offset) {
			return of(boxed.get(memory, offset));
		}

		@Override
		void rawWrite(T value, MemorySegment memory, long offset) {
			boxed.write(Reflect.unchecked(init(value).nativeValue()), memory);
		}
	}

	/**
	 * Returns true if the class is an int type.
	 */
	public static boolean is(Class<?> cls) {
		return cls != null && IntType.class.isAssignableFrom(cls);
	}

	/**
	 * Casts the given object to an int type.
	 */
	public static <T extends IntType<T>> T cast(Object intType) {
		return Reflect.unchecked(intType);
	}

	/**
	 * Returns operational support for the type.
	 */
	public static <T extends IntType<T>> Support<T> support(Class<T> cls) {
		return Reflect.unchecked(cache.computeIfAbsent(cls, _ -> supportFor(cls)));
	}

	/**
	 * Returns cached specification for the type.
	 */
	public static <T extends IntType<T>> Spec spec(Class<T> cls) {
		var support = support(cls);
		return support == null ? null : support.spec();
	}

	/**
	 * Returns the default layout for the type.
	 */
	public static <T extends IntType<T>> ValueLayout layout(Class<T> cls) {
		var support = support(cls);
		return support == null ? null : support.layout();
	}

	/**
	 * Creates an instance with given value.
	 */
	public static <T extends IntType<T>> T of(Class<T> cls, Number n) {
		var support = support(cls);
		return support == null ? null : support.of(n);
	}

	protected IntType(Number n) {
		support = support(Reflect.getClass(this));
		nativeValue = spec().nativeValue(n);
	}

	/**
	 * Returns type support.
	 */
	public Support<T> support() {
		return support;
	}

	/**
	 * Returns the specification for this type.
	 */
	public Spec spec() {
		return support().spec();
	}

	/**
	 * Returns the long value. This will be negative for unsigned long.
	 */
	public long value() {
		return spec().value(nativeValue);
	}

	/**
	 * Returns the native value object.
	 */
	public Number nativeValue() {
		return nativeValue;
	}

	/**
	 * Returns the value cast to int.
	 */
	public int intValue() {
		return (int) value();
	}

	/**
	 * Applies an operator to the long value, and returns a new instance if changed.
	 */
	public T apply(Functions.LongOperator operator) {
		if (operator == null) return Reflect.unchecked(this);
		return create(operator.applyAsLong(value()));
	}

	/**
	 * Applies an operator to the native value, and returns a new instance if changed.
	 */
	public T applyNative(Functions.Operator<Number> operator) {
		if (operator == null) return Reflect.unchecked(this);
		return create(operator.apply(nativeValue));
	}

	/**
	 * Returns true if the int type represents the same value.
	 */
	public boolean sameAs(IntType<?> i) {
		return equals(spec(), nativeValue(), i.spec(), i.nativeValue());
	}

	@Override
	public int compareTo(T t) {
		return support().spec().compare(nativeValue, t == null ? null : t.nativeValue());
	}

	@Override
	public int hashCode() {
		return Objects.hash(nativeValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof IntType<?> i)) return false;
		return Objects.equals(nativeValue, i.nativeValue);
	}

	@Override
	public String toString() {
		var spec = spec();
		long value = spec.value(nativeValue());
		return (spec.isUlong() ? Long.toUnsignedString(value) : value) + "|"
			+ Format.hex(value, "0x", 0, spec.size() << 1);
	}

	// support

	private T create(Number n) {
		if (Objects.equals(nativeValue, n)) return Reflect.unchecked(this);
		return of(Reflect.unchecked(getClass()), n);
	}

	private static boolean equals(Spec lc, Number ln, Spec rc, Number rn) {
		long lv = lc.value(ln);
		long rv = rc.value(rn);
		var lul = lc.isUlong();
		var rul = rc.isUlong();
		if (lul && !rul && lv < 0) return false;
		if (rul && !lul && rv < 0) return false;
		return lv == rv;
	}

	private static Charset charset(int size) {
		return switch (size) {
			case Byte.BYTES -> StandardCharsets.UTF_8;
			case Short.BYTES -> StandardCharsets.UTF_16;
			default -> StandardCharsets.UTF_32;
		};
	}

	private static <T extends IntType<T>> Support<T> supportFor(Class<T> cls) {
		var spec = specFor(cls);
		var constructor = constructorFor(cls);
		var boxed = boxed(spec.size());
		return new Support<>(new Support.Config<>(cls, spec, constructor), boxed);
	}

	private static Spec specFor(Class<?> cls) {
		int size = Refine.resolveSize(cls, 0);
		if (size <= 0) throw Exceptions.illegalArg("%s must specify @%s > 0: %d",
			Reflect.simple(cls), Reflect.simple(Refine.Size.class), size);
		boolean unsigned = Refine.resolveUnsigned(cls, false);
		return new Spec(size, unsigned);
	}

	private static <T extends IntType<T>> Functions.Function<Number, T>
		constructorFor(Class<T> cls) {
		var constructor = Reflect.constructor(cls, Number.class);
		if (constructor != null) return n -> Reflect.create(constructor, n);
		throw Exceptions.illegalArg("Missing constructor %s(Number n)", Reflect.name(cls));
	}

	private static <N extends Number, L extends ValueLayout> Primitive.Box<N, L> boxed(int size) {
		var box = switch (size) {
			case Byte.BYTES -> Primitive.Box.BYTE;
			case Short.BYTES -> Primitive.Box.SHORT;
			case Integer.BYTES -> Primitive.Box.INT;
			default -> Primitive.Box.LONG;
		};
		return Reflect.unchecked(box);
	}
}
