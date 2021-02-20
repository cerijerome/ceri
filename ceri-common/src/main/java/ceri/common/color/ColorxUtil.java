package ceri.common.color;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.color.ColorUtil.MAX_RATIO;
import static ceri.common.color.ColorUtil.MAX_VALUE;
import static ceri.common.color.ColorUtil.divide;
import static ceri.common.color.ColorUtil.scaleValue;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.uint;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import ceri.common.collection.BiMap;
import ceri.common.math.MathUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

public class ColorxUtil {
	private static final Pattern ARGBX_REGEX = Pattern.compile("(0x|#)?([0-9a-fA-F]{1,10})");
	private static final BiMap<Long, String> colors = colors();
	private static final int HEX = 16;
	private static final int HEX4_LEN = 4;
	private static final int HEX_RGBX_MAX_LEN = 8;
	private static final int HEX_ARGBX_MAX_LEN = 10;
	private static final int A_SHIFT = 32;
	private static final int R_SHIFT = 24;
	private static final int G_SHIFT = 16;
	private static final int B_SHIFT = 8;
	public static final long MASK = 0xffffffffffL;
	public static final long A_MASK = 0xff00000000L;
	public static final long RGBX_MASK = 0xffffffffL;
	private static final int HEX4_MASK = 0xf;
	private static final int HEX4_R_SHIFT = 12;
	private static final int HEX4_G_SHIFT = 8;
	private static final int HEX4_B_SHIFT = 4;

	private ColorxUtil() {}

	/* argbx long methods */

	public static void main(String[] args) {
		System.out.printf("0x%x%n", argbx(0x12345678, 0x9a));
		System.out.printf("0x%x%n", alphaArgbx(0x12, 0x3456789a));
	}

	/**
	 * Replaces the alpha value for an argbx long.
	 */
	public static long alphaArgbx(int a, long argbx) {
		return ((long) ubyte(a) << A_SHIFT) | (argbx & RGBX_MASK);
	}

	/**
	 * Removes the alpha component from argbx long.
	 */
	public static int rgbx(long argbx) {
		return (int) argbx;
	}

	/**
	 * Constructs an argb int by removing the x component from an argbx long.
	 */
	public static int argb(long argbx) {
		return (int) (argbx >>> B_SHIFT);
	}

	/**
	 * Constructs an opaque argbx long from rgbx int.
	 */
	public static long argbx(int rgbx) {
		return alphaArgbx(MAX_VALUE, rgbx);
	}

	/**
	 * Constructs an argbx long from argb and x.
	 */
	public static long argbx(int argb, int x) {
		return (uint(argb) << B_SHIFT) | ubyte(x);
	}

	/**
	 * Constructs an argbx long from components.
	 */
	public static long argbx(int a, int r, int g, int b, int x) {
		return alphaArgbx(a, r << R_SHIFT | ubyte(g) << G_SHIFT | ubyte(b) << B_SHIFT | ubyte(x));
	}

	/**
	 * Constructs an opaque argbx long from components.
	 */
	public static long argbx(int r, int g, int b, int x) {
		return argbx(MAX_VALUE, r, g, b, x);
	}

	/**
	 * Constructs an argbx long from color and x component.
	 */
	public static long argbx(Color color, int x) {
		return argbx(color.getRGB(), x);
	}

	/**
	 * Returns an argbx long from colorx name, or hex representation. Named colorxs are opaque, hex
	 * colorxs may have alpha components, or be opaque. Returns null if no match.
	 */
	public static Long argbx(String text) {
		Long argbx = namedArgbx(text);
		return argbx != null ? argbx : hexArgbx(text);
	}

	/**
	 * Returns an argbx long from colorx name, or hex representation. Named colorxs are opaque, hex
	 * colorxs may have alpha components, or be opaque. Throws an exception if no match.
	 */
	public static long validArgbx(String text) {
		Long argbx = argbx(text);
		if (argbx != null) return argbx;
		throw new IllegalArgumentException("Invalid colorx: " + text);
	}

	/**
	 * Creates an opaque gray argbx long with the same value for all color components.
	 */
	public static long grayArgb(int value) {
		return argbx(MAX_VALUE, value, value, value, value);
	}

	/**
	 * Creates an argbx long with maximum color components in the same ratio.
	 */
	public static long maxArgbx(long argbx) {
		return maxArgbx(a(argbx), r(argbx), g(argbx), b(argbx), x(argbx));
	}

	/**
	 * Creates an argbx long with maximum color components in the same ratio.
	 */
	public static long maxArgbx(int a, int r, int g, int b, int x) {
		double ratio = MathUtil.max(ColorUtil.ratio(r), ColorUtil.ratio(g), ColorUtil.ratio(b),
			ColorUtil.ratio(x));
		return argbx(a, divide(r, ratio), divide(g, ratio), divide(b, ratio));
	}

	/**
	 * Creates an opaque argbx long with random color components.
	 */
	public static long randomArgbx() {
		Random rnd = ThreadLocalRandom.current();
		int max = MAX_VALUE + 1;
		return argbx(MAX_VALUE, rnd.nextInt(max), rnd.nextInt(max), rnd.nextInt(max),
			rnd.nextInt(max));
	}

	/**
	 * Provides a component-scaled argbx long from given argbx long. Alpha value is maintained.
	 */
	public static long dimArgbx(long argbx, double scale) {
		if (scale == MAX_RATIO || rgbx(argbx) == 0) return argbx;
		return alphaArgbx(a(argbx), scaleArgbx(0, argbx, scale));
	}

	/**
	 * Provides a component-scaled argbx long from min and max argbx long values.
	 */
	public static long scaleArgbx(long minArgbx, long maxArgbx, double ratio) {
		if (ratio <= 0.0) return minArgbx;
		if (ratio >= 1.0) return maxArgbx;
		int a = scaleValue(a(minArgbx), a(maxArgbx), ratio);
		int r = scaleValue(r(minArgbx), r(maxArgbx), ratio);
		int g = scaleValue(g(minArgbx), g(maxArgbx), ratio);
		int b = scaleValue(b(minArgbx), b(maxArgbx), ratio);
		int x = scaleValue(x(minArgbx), x(maxArgbx), ratio);
		return argbx(a, r, g, b, x);
	}

	/* Colorx methods */

	/**
	 * Replaces the alpha component of the colorx.
	 */
	public static Colorx alpha(int a, Colorx colorx) {
		return Colorx.of(alphaArgbx(a, colorx.argbx()));
	}

	/**
	 * Returns the colorx by name or hex representation. Returns null if no match.
	 */
	public static Colorx colorx(String text) {
		Long argbx = argbx(text);
		return argbx == null ? null : Colorx.of(argbx);
	}

	/**
	 * Returns a colorx from colorx name, or hex representation. Named colorxs are opaque, hex
	 * colorxs may have alpha components, or be opaque. Throws an exception if no match.
	 */
	public static Colorx validColorx(String text) {
		Colorx colorx = colorx(text);
		if (colorx != null) return colorx;
		throw new IllegalArgumentException("Invalid colorx: " + text);
	}

	/**
	 * Creates an opaque gray colorx with the same value for all color components.
	 */
	public static Colorx gray(int value) {
		return Colorx.of(grayArgb(value));
	}

	/**
	 * Creates a colorx with maximum color components in the same ratio.
	 */
	public static Colorx max(Colorx colorx) {
		return Colorx.of(maxArgbx(colorx.argbx()));
	}

	/**
	 * Provides an opaque colorx with random components.
	 */
	public static Colorx random() {
		return Colorx.of(randomArgbx());
	}

	/**
	 * Provides a component-scaled colorx from given colorx. Alpha value is maintained.
	 */
	public static Colorx dim(Colorx colorx, double scale) {
		if (scale == MAX_RATIO || colorx.rgbx() == 0) return colorx;
		return Colorx.of(dimArgbx(colorx.argbx(), scale));
	}

	/**
	 * Provides a component-scaled colorx from min and max colorxs.
	 */
	public static Colorx scale(Colorx min, Colorx max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= MAX_RATIO) return max;
		return Colorx.of(scaleArgbx(min.argbx(), max.argbx(), ratio));
	}

	/* component functions */

	/**
	 * Extract component from argbx long.
	 */
	public static int a(long argbx) {
		return ubyte(argbx >>> A_SHIFT);
	}

	/**
	 * Extract component from argbx long.
	 */
	public static int r(long argbx) {
		return ubyte(argbx >>> R_SHIFT);
	}

	/**
	 * Extract component from argbx long.
	 */
	public static int g(long argbx) {
		return ubyte(argbx >>> G_SHIFT);
	}

	/**
	 * Extract component from argbx long.
	 */
	public static int b(long argbx) {
		return ubyte(argbx >>> B_SHIFT);
	}

	/**
	 * Extract component from argbx long.
	 */
	public static int x(long argbx) {
		return ubyte(argbx);
	}

	/* string methods */

	/**
	 * Returns the hex string, with name if rgbx matches a named colorx.
	 */
	public static String toString(Colorx color) {
		return toString(color.argbx());
	}

	/**
	 * Returns the hex string, with name if rgbx matches a named colorx.
	 */
	public static String toString(long argbx) {
		String name = name(argbx);
		String hex = hex(argbx);
		return name == null ? hex : hex + "(" + name + ")";
	}

	/**
	 * Looks up the colorx name, ignoring alpha. Returns null if no match.
	 */
	public static String name(Colorx colorx) {
		return name(colorx.argbx());
	}

	/**
	 * Looks up the colorx name, ignoring alpha. Returns null if no match.
	 */
	public static String name(long argbx) {
		return colors.keys.get(argbx | A_MASK);
	}

	/**
	 * Creates a hex string from colorx. Uses 8 digits if opaque, otherwise 10.
	 */
	public static String hex(Colorx colorx) {
		return hex(colorx.argbx());
	}

	/**
	 * Creates a hex string from argbx long. Uses 8 digits if opaque, otherwise 10.
	 */
	public static String hex(long argbx) {
		int digits = a(argbx) == MAX_VALUE ? HEX_RGBX_MAX_LEN : HEX_ARGBX_MAX_LEN;
		return "#" + StringUtil.toHex(argbx, digits);
	}

	/* List methods */

	/**
	 * Returns a list of opaque argbx longs from rgbx ints.
	 */
	public static List<Long> argbxs(int... rgbxs) {
		return toList(IntStream.of(rgbxs).mapToLong(ColorxUtil::argbx).boxed());
	}

	/**
	 * Returns a list of opaque argbx longs from name/hex strings. Throws an exception if unable to
	 * map all strings.
	 */
	public static List<Long> argbxs(String... names) {
		return argbxs(Arrays.asList(names));
	}

	/**
	 * Returns a list of opaque argbx longs from name/hex strings. Throws an exception if unable to
	 * map all strings.
	 */
	public static List<Long> argbxs(Collection<String> names) {
		return toList(names.stream().map(ColorxUtil::validArgbx));
	}

	/**
	 * Returns a list of colorxs from argbx longs.
	 */
	public static List<Colorx> colorxs(long... argbxs) {
		return toList(LongStream.of(argbxs).mapToObj(Colorx::of));
	}

	/**
	 * Returns a list of opaque colorxs from rgbx ints.
	 */
	public static List<Colorx> colorxsRgbx(int... rgbxs) {
		return toList(IntStream.of(rgbxs).mapToObj(Colorx::of));
	}

	/**
	 * Returns a list of opaque colorxs from name/hex strings. Throws an exception if unable to map
	 * all strings.
	 */
	public static List<Colorx> colorxs(String... names) {
		return colorxs(Arrays.asList(names));
	}

	/**
	 * Returns a list of opaque colorxs from name/hex strings. Throws an exception if unable to map
	 * all strings.
	 */
	public static List<Colorx> colorxs(Collection<String> names) {
		return toList(names.stream().map(ColorxUtil::validColorx));
	}

	/**
	 * Create a list of scaled argbx longs from min to max, in steps using bias.
	 */
	public static List<Long> fadeArgbx(long minArgbx, long maxArgbx, int steps, Bias bias) {
		return toList(streamFade(minArgbx, maxArgbx, steps, bias).boxed());
	}

	/**
	 * Create a list of scaled argbx longs from min to max, in steps using bias.
	 */
	public static List<Colorx> fade(Colorx min, Colorx max, int steps, Bias bias) {
		return fade(min.argbx(), max.argbx(), steps, bias);
	}

	/**
	 * Create a list of scaled colorxs from min to max, in steps using bias.
	 */
	public static List<Colorx> fade(long minArgbx, long maxArgbx, int steps, Bias bias) {
		return toList(streamFade(minArgbx, maxArgbx, steps, bias).mapToObj(Colorx::of));
	}

	/* adapter methods */

	/**
	 * Return an argb int by normalizing an argbx long x value with the given x rgb int. Keeps the
	 * color component ratio, and reduces all values if any component exceeds its max.
	 */
	public static int normalizeArgb(long argbx, int xRgb) {
		double xRatio = ColorUtil.ratio(x(argbx));
		double r = r(argbx) + xRatio * ColorUtil.r(xRgb);
		double g = g(argbx) + xRatio * ColorUtil.g(xRgb);
		double b = b(argbx) + xRatio * ColorUtil.b(xRgb);
		double div = MathUtil.max(r, g, b, 1.0); // adjust so no component is > max value
		return ColorUtil.argb(a(argbx), (int) (r / div), (int) (g / div), (int) (b / div));
	}

	/**
	 * Extracts the given x rgb int from the argb int, to create a denormalized argbx long.
	 */
	public static long denormalizeArgbx(int argb, int xRgb) {
		int a = ColorUtil.a(argb);
		int r = ColorUtil.r(argb);
		int g = ColorUtil.g(argb);
		int b = ColorUtil.b(argb);
		int xr = ColorUtil.r(xRgb);
		int xg = ColorUtil.g(xRgb);
		int xb = ColorUtil.b(xRgb);
		// Calculate ratio of xRgb within argb, 0..1 (inclusive)
		double xRatio = MathUtil.min(MAX_RATIO, ratio(r, xr), ratio(g, xg), ratio(b, xb));
		int r0 = (int) (r - xRatio * xr);
		int g0 = (int) (g - xRatio * xg);
		int b0 = (int) (b - xRatio * xb);
		int x0 = (int) (xRatio * MAX_VALUE);
		return argbx(a, r0, g0, b0, x0);
	}

	/**
	 * Apply an argb int operation to an argbx long.
	 */
	public static long applyArgbx(long argbx, IntUnaryOperator argbFn) {
		return argbx(argbFn.applyAsInt(argb(argbx)), x(argbx));
	}

	/**
	 * Apply a color operation to a colorx.
	 */
	public static Colorx apply(Colorx colorx, UnaryOperator<Color> colorFn) {
		return Colorx.of(colorFn.apply(colorx.color()), colorx.x());
	}

	/* support methods */

	/**
	 * Converts hex string to argbx long. The value must be prefixed with '#' or '0x', and contain
	 * 1..10 hex digits. If <= 8 digits, the value is treated as opaque, otherwise the alpha value
	 * is captured. Quad hex '#rgbx' values will be treated as opaque 'rrggbbxx' hex values. Returns
	 * null if no match.
	 */
	private static Long hexArgbx(String text) {
		Matcher m = RegexUtil.matched(ARGBX_REGEX, text);
		if (m == null) return null;
		String prefix = m.group(1);
		String hex = m.group(2);
		long argb = Long.valueOf(hex, HEX);
		int len = hex.length();
		return hexArgbx(prefix, len, argb);
	}

	private static double ratio(int value, int max) {
		if (max == 0) return MAX_RATIO;
		return MathUtil.limit((double) value / max, 0, MAX_RATIO);
	}

	private static LongStream streamFade(long minArgbx, long maxArgbx, int steps, Bias bias) {
		return IntStream.rangeClosed(1, steps)
			.mapToLong(i -> scaleArgbx(minArgbx, maxArgbx, bias.bias((double) i / steps)));
	}

	private static Long namedArgbx(String name) {
		return colors.values.get(name);
	}

	private static long hexArgbx(String prefix, int len, long argbx) {
		if (len > HEX_RGBX_MAX_LEN) return argbx & MASK; // argbx
		if (!"#".equals(prefix) || len != HEX4_LEN) return argbx((int) argbx); // rgbx
		int r = (int) ((argbx >>> HEX4_R_SHIFT) & HEX4_MASK) << R_SHIFT;
		int g = (int) ((argbx >>> HEX4_G_SHIFT) & HEX4_MASK) << G_SHIFT;
		int b = (int) ((argbx >>> HEX4_B_SHIFT) & HEX4_MASK) << B_SHIFT;
		int x = (int) argbx & HEX4_MASK;
		return argbx((r | g | b | x) * (HEX + 1)); // quad-hex #rgbx
	}

	private static BiMap<Long, String> colors() {
		return BiMap.<Long, String>builder() //
			.put(Colorx.black.argbx(), "black") //
			.put(Colorx.full.argbx(), "full").build();
	}

}
