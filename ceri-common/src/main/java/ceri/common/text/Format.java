package ceri.common.text;

import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.math.MathUtil;
import ceri.common.math.Radix;
import ceri.common.util.BasicUtil;

/**
 * Number formatting support.
 */
public record Format(int radix, String prefix, int minDigits, int maxDigits,
	Functions.ObjIntConsumer<StringBuilder> separation) {

	public static final Format HEX = of(Radix.HEX, 0);
	public static final Format HEX2 = of(Radix.HEX, 2);
	public static final Format HEX4 = of(Radix.HEX, 4);
	public static final Format HEX8 = of(Radix.HEX, 8);
	public static final Format HEX16 = of(Radix.HEX, 16);
	public static final Format OCT = of(Radix.OCT, 0);
	public static final Format BIN = of(Radix.BIN, 0);
	public static final Format BIN4_4 = of(Radix.BIN, 4, 0, Separation._4);
	public static final Format BIN8_4 = of(Radix.BIN, 4, 0, Separation._4);
	public static final Format BIN16_4 = of(Radix.BIN, 4, 0, Separation._4);

	/**
	 * Logic for adding separators between digits.
	 */
	public interface Separation extends Functions.ObjIntConsumer<StringBuilder> {
		public static final Separation NONE = Lambdas.register((_, _) -> {}, "none");
		public static final Separation _8 = Lambdas.register(of(8, "_"), "_8");
		public static final Separation _4 = Lambdas.register(of(4, "_"), "_4");

		public static Separation of(int count, String separator) {
			if (count <= 0) return NONE;
			return of(i -> (i % count) == 0, separator);
		}

		/**
		 * Creates an instance based on predicate
		 */
		public static Separation of(Functions.IntPredicate predicate, String separator) {
			if (predicate == null || separator == null) return NONE;
			return (b, i) -> {
				if (i > 0 && predicate.test(i)) b.append(separator);
			};
		}
	}

	/**
	 * Returns the formatted number with no prefix.
	 */
	public static String hex(long value) {
		return hex(value, 0);
	}

	/**
	 * Returns the formatted number with no prefix and the exact number of digits.
	 */
	public static String hex(long value, int digits) {
		return apply(value, Radix.HEX.n, digits, digits);
	}

	/**
	 * Returns the formatted number with no prefix.
	 */
	public static String bin(long value) {
		return bin(value, 0);
	}

	/**
	 * Returns the formatted number with no prefix and the exact number of digits.
	 */
	public static String bin(long value, int digits) {
		return apply(value, Radix.BIN.n, digits, digits);
	}

	/**
	 * Returns the formatted number.
	 */
	public static String apply(long value, int radix, int minDigits, int maxDigits) {
		return apply(value, radix, minDigits, maxDigits, Separation.NONE);
	}

	/**
	 * Returns the formatted number.
	 */
	public static String apply(long value, int radix, int minDigits, int maxDigits,
		Separation separation) {
		return append(new StringBuilder(), value, radix, minDigits, maxDigits, separation)
			.toString();
	}

	/**
	 * Appends the formatted number.
	 */
	public static StringBuilder append(StringBuilder b, long value, int radix, int minDigits,
		int maxDigits, Separation separation) {
		if (b == null) return b;
		separation = BasicUtil.def(separation, Separation.NONE);
		var s = Long.toUnsignedString(value, radix);
		int len = s.length();
		int n = n(len, minDigits, maxDigits);
		while (n > len)
			separation.accept(b.append('0'), --n);
		while (n > 0)
			separation.accept(b.append(s.charAt(len - n)), --n);
		return b;
	}

	/**
	 * Creates an unsigned instance with no separation.
	 */
	public static Format of(Radix radix, int minDigits) {
		return of(radix, minDigits, 0);
	}

	/**
	 * Creates an unsigned instance with no separation.
	 */
	public static Format of(Radix radix, int minDigits, int maxDigits) {
		return of(radix, minDigits, maxDigits, Separation.NONE);
	}

	/**
	 * Creates an unsigned instance with no separation.
	 */
	public static Format of(Radix radix, int minDigits, int maxDigits, Separation separation) {
		return new Format(radix.n, radix.prefix(), minDigits, maxDigits, separation);
	}

	/**
	 * Creates an unsigned instance with no separation.
	 */
	public static Format of(int radix, String prefix, int minDigits) {
		return of(radix, prefix, minDigits, 0);
	}

	/**
	 * Creates an unsigned instance.
	 */
	public static Format of(int radix, String prefix, int minDigits, int maxDigits) {
		return new Format(radix, prefix, minDigits, maxDigits, Separation.NONE);
	}

	/**
	 * Returns the formatted unsigned number.
	 */
	public String ubyte(long value) {
		return apply(MathUtil.ubyte(value));
	}

	/**
	 * Returns the formatted unsigned number, or null string.
	 */
	public String ubyte(Number value) {
		if (value == null) return Strings.NULL;
		return ubyte(value.longValue());
	}

	/**
	 * Returns the formatted unsigned number.
	 */
	public String ushort(long value) {
		return apply(MathUtil.ushort(value));
	}

	/**
	 * Returns the formatted unsigned number, or null string.
	 */
	public String ushort(Number value) {
		if (value == null) return Strings.NULL;
		return ushort(value.longValue());
	}

	/**
	 * Returns the formatted unsigned number.
	 */
	public String uint(long value) {
		return apply(MathUtil.uint(value));
	}

	/**
	 * Returns the formatted unsigned number, or null string.
	 */
	public String uint(Number value) {
		if (value == null) return Strings.NULL;
		return uint(value.longValue());
	}

	/**
	 * Returns the formatted number.
	 */
	public String apply(long value) {
		return append(new StringBuilder(), value).toString();
	}

	/**
	 * Returns the formatted number, or null string.
	 */
	public String apply(Number value) {
		if (value == null) return Strings.NULL;
		return apply(value.longValue());
	}

	/**
	 * Appends the formatted number.
	 */
	public StringBuilder append(StringBuilder b, long value) {
		if (b == null) return b;
		b.append(prefix());
		var separation = BasicUtil.def(separation(), Separation.NONE);
		var s = Long.toUnsignedString(value, radix());
		int len = s.length();
		int n = n(len, minDigits(), maxDigits());
		while (n > len)
			separation.accept(b.append('0'), --n);
		while (n > 0)
			separation.accept(b.append(s.charAt(len - n)), --n);
		return b;
	}

	/**
	 * Appends the formatted number if not null.
	 */
	public StringBuilder append(StringBuilder b, Number value) {
		if (value == null) return b;
		return append(b, value.longValue());
	}

	/**
	 * Return the prefix or empty string.
	 */
	public String prefix() {
		return BasicUtil.def(prefix, "");
	}

	@Override
	public final String toString() {
		return ToString.forClass(this, radix(), prefix(), minDigits(), maxDigits(),
			Lambdas.name(separation()));
	}

	private static int n(int len, int min, int max) {
		if (len < min) return min;
		if (max > 0 && len > max) return max;
		return len;
	}
}
