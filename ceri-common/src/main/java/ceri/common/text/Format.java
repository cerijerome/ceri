package ceri.common.text;

import ceri.common.array.ArrayUtil;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.math.Maths;
import ceri.common.math.Radix;
import ceri.common.util.Basics;

/**
 * Number formatting.
 */
public class Format {
	public static final OfLong HEX = ofLong(Radix.HEX, 0, 0);
	public static final OfLong HEX_BYTE = ofLong(Radix.HEX, 2, 2);
	public static final OfLong HEX_SHORT = ofLong(Radix.HEX, 4, 4);
	public static final OfLong HEX_INT = ofLong(Radix.HEX, 8, 8);
	public static final OfLong DEC = new OfLong(false, "", Radix.DEC.n, 0, 0, null);
	public static final OfLong UDEC = new OfLong(true, "", Radix.DEC.n, 0, 0, null);
	public static final LongFunction DEC_UBYTE = DEC::ubyte;
	public static final LongFunction DEC_USHORT = DEC::ushort;
	public static final LongFunction DEC_UINT = DEC::uint;
	public static final LongFunction UDEC_HEX = Format::udecHex;
	public static final LongFunction UDEC_OR_HEX = Format::udecOrHex;
	public static final OfLong BIN =
		new OfLong(true, Radix.BIN.prefix(), Radix.BIN.n, 0, 0, Separator._8);
	public static final OfDouble FP = new OfDouble(0, 0);
	public static final OfDouble FP1 = new OfDouble(1, 1);
	public static final OfDouble FP3 = new OfDouble(3, 3);
	public static final OfDouble FP03 = new OfDouble(0, 3);
	public static final DoubleFunction ROUND = d -> DEC.apply(Math.round(d));

	private Format() {}

	/**
	 * Long formatter interface.
	 */
	public interface LongFunction
		extends Functions.LongFunction<String>, Functions.IntFunction<String> {
		@Override
		default String apply(int i) {
			return apply((long) i);
		}

		/**
		 * Returns the formatted unsigned number.
		 */
		default String ubyte(long value) {
			return apply(Maths.ubyte(value));
		}

		/**
		 * Returns the formatted unsigned number, or null string.
		 */
		default String ubyte(Number value) {
			return value == null ? Strings.NULL : ubyte(value.longValue());
		}

		/**
		 * Returns the formatted unsigned number.
		 */
		default String ushort(long value) {
			return apply(Maths.ushort(value));
		}

		/**
		 * Returns the formatted unsigned number, or null string.
		 */
		default String ushort(Number value) {
			return value == null ? Strings.NULL : ushort(value.longValue());
		}

		/**
		 * Returns the formatted unsigned number.
		 */
		default String uint(long value) {
			return apply(Maths.uint(value));
		}

		/**
		 * Returns the formatted unsigned number, or null string.
		 */
		default String uint(Number value) {
			return value == null ? Strings.NULL : uint(value.longValue());
		}

		/**
		 * Returns the formatted number, or null string.
		 */
		default String apply(Number value) {
			return value == null ? Strings.NULL : apply(value.longValue());
		}
	}

	/**
	 * Double formatter interface.
	 */
	public interface DoubleFunction extends Functions.DoubleFunction<String> {}

	/**
	 * Integral formatter with accessible arguments.
	 */
	public record OfLong(boolean unsigned, String prefix, int radix, int minDigits, int maxDigits,
		Separator separator) implements LongFunction {
		@Override
		public String apply(long l) {
			return format(l, unsigned(), prefix(), radix(), minDigits(), maxDigits(), separator());
		}
	}

	/**
	 * Floating point formatter with accessible arguments.
	 */
	public record OfDouble(int minDec, int maxDec) implements DoubleFunction {
		@Override
		public String apply(double d) {
			return Format.format(d, minDec(), maxDec());
		}
	}

	/**
	 * Logic for adding separators during formatting, based on significant digit index.
	 */
	public interface Separator {
		public static final Separator NONE = Lambdas.register((_, _) -> null, "none");
		public static final Separator _8 = Lambdas.register(of(8, "_"), "_8");
		public static final Separator _4 = Lambdas.register(of(4, "_"), "_4");

		public static Separator of(int count, String separator) {
			if (count <= 0) return NONE;
			return of(i -> (i % count) == 0, separator);
		}

		/**
		 * Creates an instance based on predicate
		 */
		public static Separator of(Functions.IntPredicate predicate, String separator) {
			if (predicate == null || separator == null) return NONE;
			return (i, n) -> (i > 0 && i < n && predicate.test(i)) ? separator : null;
		}

		/**
		 * Provide separator for current digit out of total digits, or null if none.
		 */
		String accept(int digit, int digits);
	}

	/**
	 * Utility to modify a string, optimized to only build if changes are made.
	 */
	private static class Appender {
		private final Separator sep;
		private final int digits;
		private final StringBuilders.State b;
		private int digit;

		private Appender(CharSequence s, int digits, Separator sep) {
			this.digits = digits;
			this.sep = Basics.def(sep, Separator.NONE);
			b = StringBuilders.State.of(s);
			digit = digits;
		}

		private Appender neg() {
			b.append('-');
			return this;
		}

		private Appender append(CharSequence s) {
			b.append(s);
			return this;
		}

		private Appender zeros(int n) {
			while (n-- > 0)
				digit('0');
			return this;
		}

		private Appender digits(CharSequence s) {
			return digits(s, 0, Integer.MAX_VALUE);
		}

		private Appender digits(CharSequence s, int offset, int length) {
			return ArrayUtil.applySlice(Strings.length(s), offset, length, (o, l) -> {
				for (int i = 0; i < l; i++)
					digit(s.charAt(o + i));
				return this;
			});
		}

		private Appender digit(char c) {
			b.append(sep.accept(digit--, digits)).append(c);
			return this;
		}

		@Override
		public String toString() {
			return b.toString();
		}
	}

	/**
	 * Returns a formatter with prefix, radix, and digit padding/truncation.
	 */
	public static OfLong ofLong(Radix radix, int minDigits, int maxDigits) {
		return new OfLong(true, radix.prefix(), radix.n, minDigits, maxDigits, null);
	}

	/**
	 * Returns a formatter with prefix, radix, and digit padding/truncation.
	 */
	public static OfLong ofLong(String prefix, int radix, int minDigits, int maxDigits) {
		return new OfLong(true, prefix, radix, minDigits, maxDigits, null);
	}

	/**
	 * Returns the unsigned formatted number with prefix.
	 */
	public static String hex(long value) {
		return hex(value, 0, 0);
	}

	/**
	 * Returns the unsigned formatted number with prefix and exact digits.
	 */
	public static String hex(long value, int digits) {
		return hex(value, digits, digits);
	}

	/**
	 * Returns the unsigned formatted number with prefix and digit padding/truncation.
	 */
	public static String hex(long value, int minDigits, int maxDigits) {
		return format(value, Radix.HEX, minDigits, maxDigits);
	}

	/**
	 * Returns the unsigned formatted number with prefix and exact digits.
	 */
	public static String hex(long value, String prefix, int digits) {
		return hex(value, prefix, digits, digits);
	}

	/**
	 * Returns the unsigned formatted number with prefix and digit padding/truncation.
	 */
	public static String hex(long value, String prefix, int minDigits, int maxDigits) {
		return format(value, true, prefix, Radix.HEX.n, minDigits, maxDigits);
	}

	/**
	 * Returns the unsigned decimal number and hex if not from 0 to 9.
	 */
	public static String udecHex(long value) {
		if (value >= 0L && value <= 9L) return Long.toUnsignedString(value);
		return Long.toUnsignedString(value) + "|" + hex(value);
	}

	/**
	 * Returns the unsigned decimal number and hex if not from 0 to 9.
	 */
	public static String udecOrHex(long value) {
		if (value >= 0L && value <= 9L) return Long.toUnsignedString(value);
		return hex(value);
	}

	/**
	 * Returns the unsigned formatted number with prefix.
	 */
	public static String bin(long value) {
		return bin(value, 0, 0);
	}

	/**
	 * Returns the unsigned formatted number with prefix and exact digits.
	 */
	public static String bin(long value, int digits) {
		return bin(value, digits, digits);
	}

	/**
	 * Returns the unsigned formatted number with prefix and digit padding/truncation.
	 */
	public static String bin(long value, int minDigits, int maxDigits) {
		return format(value, Radix.BIN, minDigits, maxDigits);
	}

	/**
	 * Returns the unsigned formatted number with prefix and exact digits.
	 */
	public static String bin(long value, String prefix, int digits) {
		return bin(value, prefix, digits, digits);
	}

	/**
	 * Returns the unsigned formatted number with prefix and digit padding/truncation.
	 */
	public static String bin(long value, String prefix, int minDigits, int maxDigits) {
		return format(value, true, prefix, Radix.BIN.n, minDigits, maxDigits);
	}

	/**
	 * Returns the formatted number with prefix, radix, and digit padding/truncation.
	 */
	public static String format(long value, boolean unsigned, String prefix, int radix,
		int minDigits, int maxDigits) {
		return format(value, unsigned, prefix, radix, minDigits, maxDigits, null);
	}

	/**
	 * Returns the formatted number with prefix, radix, digit padding/truncation, and separation.
	 */
	public static String format(long value, boolean unsigned, String prefix, int radix,
		int minDigits, int maxDigits, Separator sep) {
		if (unsigned || value >= 0) return formatPositive(Long.toUnsignedString(value, radix),
			prefix, minDigits, maxDigits, sep);
		return formatNegative(Long.toString(value, radix), prefix, minDigits, maxDigits, sep);
	}

	/**
	 * Returns the unsigned formatted number with prefix, radix, and digit padding/truncation.
	 */
	public static String format(long value, Radix radix, int minDigits, int maxDigits) {
		return format(value, radix, minDigits, maxDigits, null);
	}

	/**
	 * Returns the unsigned formatted number with prefix, radix, digit padding/truncation, and
	 * separation.
	 */
	public static String format(long value, Radix radix, int minDigits, int maxDigits,
		Separator sep) {
		return format(value, true, radix.prefix(), radix.n, minDigits, maxDigits, sep);
	}

	/**
	 * Returns the formatted number, with decimal places rounded within range.
	 */
	public static String format(double value, int minDec, int maxDec) {
		if (!Double.isFinite(value)) return Double.toString(value);
		var s = maxDec <= 0 ? Double.toString(value) : String.format("%." + maxDec + "f", value);
		int len = s.length();
		int p = s.indexOf('.');
		while (len - p - 1 > minDec && Chars.at(s, len - 1) == '0')
			len--;
		if (Chars.at(s, len - 1) == '.') return s.substring(0, len - 1);
		if (len != s.length()) s = s.substring(0, len);
		if (len - p - 1 < minDec) s += Strings.repeat('0', minDec + p + 1 - len);
		return s;
	}

	// support

	private static String formatNegative(String s, String prefix, int min, int max, Separator sep) {
		int len = s.length() - 1;
		int n = n(len, min, max);
		var b = new Appender(s, n, sep).neg().append(prefix);
		if (n == len) b.digits(s, 1, len);
		else if (n < len) b.digits(s, len + 1 - n, n);
		else b.zeros(n - len).digits(s, 1, len);
		return b.toString();
	}

	private static String formatPositive(String s, String prefix, int min, int max, Separator sep) {
		int len = s.length();
		int n = n(len, min, max);
		var b = new Appender(s, n, sep).append(prefix);
		if (n == len) b.digits(s);
		else if (n < len) b.digits(s, len - n, n);
		else b.zeros(n - len).digits(s);
		return b.toString();
	}

	private static int n(int len, int min, int max) {
		if (len < min) return min;
		if (max > 0 && len > max) return max;
		return len;
	}
}
