package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_VALUE;
import static ceri.common.math.MathUtil.ubyte;
import java.util.List;
import ceri.common.data.TypeTranscoder;

/**
 * Color components. Provides logic to get and set components from argb and xargb colors.
 */
public enum Component {
	b(0),
	g(1),
	r(2),
	a(3),
	// used in xargb longs:
	x0(4),
	x1(5),
	x2(6),
	x3(7);

	/**
	 * The list of x components in order.
	 */
	public static final List<Component> RGB = List.of(r, g, b);
	public static final List<Component> ARGB = List.of(a, r, g, b);
	public static final List<Component> XRGB = List.of(x3, x2, x1, x0, r, g, b);
	public static final List<Component> XARGB = List.of(x3, x2, x1, x0, a, r, g, b);
	public static final List<Component> XS = List.of(x0, x1, x2, x3);
	public static final long X_MASK = 0xffffffff00000000L;
	public static final int X_COUNT = 4;
	private static final TypeTranscoder<Component> xcoder =
		TypeTranscoder.of(t -> t.index, Component.class);
	public final int index;
	public final int shift;
	public final long mask;
	public final int intMask;

	/**
	 * Counts up to the last non-zero component.
	 */
	public static int count(long xargb) {
		for (int i = 0; i < Long.BYTES; i++) {
			if (xargb == 0L) return i;
			xargb >>>= Byte.SIZE;
		}
		return Long.BYTES;
	}

	/**
	 * Look up component from byte index.
	 */
	public static Component from(int index) {
		return xcoder.decode(index);
	}

	/**
	 * Get x component by index.
	 */
	public static Component x(int xIndex) {
		return switch (xIndex) {
			case 0 -> x0;
			case 1 -> x1;
			case 2 -> x2;
			case 3 -> x3;
			default -> null;
		};
	}

	/**
	 * Extract component values from argb int.
	 */
	public static int[] getAll(int argb, Component... components) {
		int[] values = new int[components.length];
		for (int i = 0; i < values.length; i++)
			values[i] = components[i].get(argb);
		return values;
	}

	/**
	 * Extract component values from xargb long.
	 */
	public static int[] getAll(long xargb, Component... components) {
		int[] values = new int[components.length];
		for (int i = 0; i < values.length; i++)
			values[i] = components[i].get(xargb);
		return values;
	}

	private Component(int i) {
		index = i;
		shift = Byte.SIZE * i;
		mask = 0xffL << shift;
		intMask = (int) mask;
	}

	/**
	 * Returns true if component fits in argb int.
	 */
	public boolean isInt() {
		return shift < Integer.SIZE;
	}

	/**
	 * Extract component value from argb int.
	 */
	public int get(int argb) {
		return isInt() ? ubyte(argb >>> shift) : 0;
	}

	/**
	 * Extract component value from xargb long.
	 */
	public int get(long xargb) {
		return ubyte(xargb >>> shift);
	}

	/**
	 * Set component value in argb int.
	 */
	public int set(int argb, int value) {
		return (argb & (int) ~mask) | intValue(value);
	}

	/**
	 * Set component value in xargb long.
	 */
	public long set(long xargb, int value) {
		return (xargb & ~mask) | longValue(value);
	}

	/**
	 * Shift value to its position in argb int.
	 */
	public int intValue(int value) {
		return isInt() ? ubyte(value) << shift : 0;
	}

	/**
	 * Shift value to its position in xargb long.
	 */
	public long longValue(int value) {
		return (long) ubyte(value) << shift;
	}

	/**
	 * Convert ratio to value, and shift to its position in argb int.
	 */
	public int intValue(double ratio) {
		return intValue(ColorUtil.value(ratio));
	}

	/**
	 * Convert ratio to value, and shift to its position in xargb long.
	 */
	public long longValue(double ratio) {
		return longValue(ColorUtil.value(ratio));
	}

	/**
	 * Extract component value as a ratio.
	 */
	public double ratio(int argb) {
		return (double) get(argb) / MAX_VALUE;
	}

	/**
	 * Extract component value as a ratio.
	 */
	public double ratio(long xargb) {
		return (double) get(xargb) / MAX_VALUE;
	}

}
