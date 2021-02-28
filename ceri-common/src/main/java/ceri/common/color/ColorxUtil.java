package ceri.common.color;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.color.ColorUtil.MAX_RATIO;
import static ceri.common.color.ColorUtil.MAX_VALUE;
import static ceri.common.color.ColorUtil.scaleValue;
import static ceri.common.math.MathUtil.limit;
import static ceri.common.math.MathUtil.min;
import static ceri.common.math.MathUtil.ubyte;
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
import java.util.stream.Stream;
import ceri.common.collection.BiMap;
import ceri.common.math.MathUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

public class ColorxUtil {
	private static final Pattern XARGB_REGEX = Pattern.compile("(0x|#)?([0-9a-fA-F]{1,16})");
	private static final BiMap<Long, String> colors = colors();
	private static final int HEX = 16;
	public static final int X_COUNT = 4;
	private static final long X_MASK = 0xffffffff00000000L;
	private static final long A_MASK = 0xff000000L;

	private ColorxUtil() {}

	/* xargb long methods */

	/**
	 * Replaces the alpha value for an xargb long.
	 */
	public static long alphaXargb(int a, long xargb) {
		return (xargb & X_MASK) | ColorUtil.alphaArgb(a, (int) xargb);
	}

	/**
	 * Removes the alpha component from xargb long.
	 */
	public static long xrgb(long xargb) {
		return (xargb & ~A_MASK);
	}

	/**
	 * Returns an argb int by removing the x components.
	 */
	public static int argb(long xargb) {
		return (int) xargb;
	}

	/**
	 * Constructs an xargb long from argb and x components.
	 */
	public static long xargb(int argb, int... xs) {
		long xargb = argb;
		for (int i = 0; i < xs.length; i++)
			xargb |= xEncode(xs[i], i);
		return xargb;
	}

	/**
	 * Constructs an xargb long from color and x component.
	 */
	public static long xargb(Color color, int... xs) {
		return xargb(color.getRGB(), xs);
	}

	/**
	 * Returns an xargb long from colorx name, or hex representation. Named colorxs are opaque, hex
	 * colorxs have alpha components. Returns null if no match.
	 */
	public static Long xargb(String text) {
		Long xargb = namedArgbx(text);
		return xargb != null ? xargb : hexXargb(text);
	}

	/**
	 * Returns an xargb long from colorx name, or hex representation. Named colorxs are opaque, hex
	 * colorxs have alpha components. Throws an exception if no match.
	 */
	public static long validXargb(String text) {
		Long xargb = xargb(text);
		if (xargb != null) return xargb;
		throw new IllegalArgumentException("Invalid colorx: " + text);
	}

	/**
	 * Creates an xargb long with max components in the same ratio.
	 */
	public static long maxXargb(long xargb) {
		return maxXargb((int) xargb, xs(xargb));
	}

	/**
	 * Creates an xargb long with max components in the same ratio.
	 */
	public static long maxXargb(int argb, int... xs) {
		int a = ColorUtil.a(argb);
		int r = ColorUtil.r(argb);
		int g = ColorUtil.g(argb);
		int b = ColorUtil.b(argb);
		double ratio = ColorUtil.ratio(MathUtil.max(r, g, b, MathUtil.max(xs)));
		argb = ColorUtil.argb(a, divide(r, ratio), divide(g, ratio), divide(b, ratio));
		for (int i = 0; i < xs.length; i++)
			xs[i] = divide(xs[i], ratio);
		return xargb(argb, xs);
	}

	/**
	 * Creates an opaque xargb long with random components. nx specifies how many x values to
	 * generate.
	 */
	public static long randomXargb(int nx) {
		Random rnd = ThreadLocalRandom.current();
		int max = MAX_VALUE + 1;
		int[] xs = IntStream.range(0, nx).map(i -> rnd.nextInt(max)).toArray();
		return xargb(ColorUtil.randomArgb(), xs);
	}

	/**
	 * Provides a component-scaled xargb long from given xargb long. Alpha value is maintained.
	 */
	public static long dimArgbx(long xargb, double scale) {
		if (scale == MAX_RATIO || xrgb(xargb) == 0L) return xargb;
		return alphaXargb(a(xargb), scaleArgbx(0, xargb, scale));
	}

	/**
	 * Provides a component-scaled xargb long from min and max xargb long values.
	 */
	public static long scaleArgbx(long minXargb, long maxXargb, double ratio) {
		if (ratio <= 0.0) return minXargb;
		if (ratio >= 1.0) return maxXargb;
		int a = scaleValue(a(minXargb), a(maxXargb), ratio);
		int r = scaleValue(r(minXargb), r(maxXargb), ratio);
		int g = scaleValue(g(minXargb), g(maxXargb), ratio);
		int b = scaleValue(b(minXargb), b(maxXargb), ratio);
		int[] xs = new int[X_COUNT];
		for (int i = 0; i < xs.length; i++)
			xs[i] = scaleValue(x(minXargb, i), x(maxXargb, i), ratio);
		return xargb(ColorUtil.argb(a, r, g, b), xs);
	}

	/* Colorx methods */

	/**
	 * Replaces the alpha component of the colorx.
	 */
	public static Colorx alpha(int a, Colorx colorx) {
		return Colorx.of(alphaXargb(a, colorx.xargb));
	}

	/**
	 * Returns the colorx by name or hex representation. Returns null if no match.
	 */
	public static Colorx colorx(String text) {
		Long xargb = xargb(text);
		return xargb == null ? null : Colorx.of(xargb);
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
	 * Creates a colorx with maximum color components in the same ratio.
	 */
	public static Colorx max(Colorx colorx) {
		return Colorx.of(maxXargb(colorx.xargb));
	}

	/**
	 * Provides an opaque colorx with random components.
	 */
	public static Colorx random(int nx) {
		return Colorx.of(randomXargb(nx));
	}

	/**
	 * Provides a component-scaled colorx from given colorx. Alpha value is maintained.
	 */
	public static Colorx dim(Colorx colorx, double scale) {
		if (scale == MAX_RATIO) return colorx;
		return Colorx.of(dimArgbx(colorx.xargb, scale));
	}

	/**
	 * Provides a component-scaled colorx from min and max colorxs.
	 */
	public static Colorx scale(Colorx min, Colorx max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= MAX_RATIO) return max;
		return Colorx.of(scaleArgbx(min.xargb, max.xargb, ratio));
	}

	/* component functions */

	/**
	 * Extract component from xargb long.
	 */
	public static int a(long xargb) {
		return ColorUtil.a((int) xargb);
	}

	/**
	 * Extract component from xargb long.
	 */
	public static int r(long xargb) {
		return ColorUtil.r((int) xargb);
	}

	/**
	 * Extract component from xargb long.
	 */
	public static int g(long xargb) {
		return ColorUtil.g((int) xargb);
	}

	/**
	 * Extract component from xargb long.
	 */
	public static int b(long xargb) {
		return ColorUtil.b((int) xargb);
	}

	/**
	 * Extract x[i] component from xargb long.
	 */
	public static int x(long xargb, int i) {
		return ubyte(xargb >>> xShift(i));
	}

	/**
	 * Extract all x components from xargb long.
	 */
	public static int[] xs(long xargb) {
		return IntStream.range(0, X_COUNT).map(i -> x(xargb, i)).toArray();
	}

	/* string methods */

	/**
	 * Returns the hex string, with name if rgbx matches a named colorx.
	 */
	public static String toString(Colorx color) {
		return toString(color.xargb);
	}

	/**
	 * Returns the hex string, with name if rgbx matches a named colorx.
	 */
	public static String toString(long xargb) {
		String name = name(xargb);
		String hex = hex(xargb);
		return name == null ? hex : hex + "(" + name + ")";
	}

	/**
	 * Looks up the colorx name, ignoring alpha. Returns null if no match.
	 */
	public static String name(Colorx colorx) {
		return name(colorx.xargb);
	}

	/**
	 * Looks up the colorx name, ignoring alpha. Returns null if no match.
	 */
	public static String name(long xargb) {
		return colors.keys.get(xargb | A_MASK);
	}

	/**
	 * Creates a hex string from colorx. Uses 8 digits if opaque, otherwise 10.
	 */
	public static String hex(Colorx colorx) {
		return hex(colorx.xargb);
	}

	/**
	 * Creates a hex string from xargb long. Drops leading zero x values.
	 */
	public static String hex(long xargb) {
		int digits = (xCount(xargb) + Integer.BYTES) << 1;
		return "#" + StringUtil.toHex(xargb, digits);
	}

	/* Collection methods */

	/**
	 * Convert colorxs to xargb array.
	 */
	public static long[] xargbArray(Colorx... colorxs) {
		return Stream.of(colorxs).mapToLong(cx -> cx.xargb).toArray();
	}

	/**
	 * Returns a list of xargb longs from name/hex strings. Throws an exception if unable to map all
	 * strings.
	 */
	public static List<Long> xargbs(String... names) {
		return xargbs(Arrays.asList(names));
	}

	/**
	 * Returns a list of xargb longs from name/hex strings. Throws an exception if unable to map all
	 * strings.
	 */
	public static List<Long> xargbs(Collection<String> names) {
		return toList(names.stream().map(ColorxUtil::validXargb));
	}

	/**
	 * Returns a list of colorxs from xargb longs.
	 */
	public static List<Colorx> colorxs(long... xargbs) {
		return toList(LongStream.of(xargbs).mapToObj(Colorx::of));
	}

	/**
	 * Returns a list of colorxs from name/hex strings. Throws an exception if unable to map all
	 * strings.
	 */
	public static List<Colorx> colorxs(String... names) {
		return colorxs(Arrays.asList(names));
	}

	/**
	 * Returns a list of colorxs from name/hex strings. Throws an exception if unable to map all
	 * strings.
	 */
	public static List<Colorx> colorxs(Collection<String> names) {
		return toList(names.stream().map(ColorxUtil::validColorx));
	}

	/**
	 * Create a list of scaled xargb longs from min to max, in steps using bias.
	 */
	public static List<Long> fadeArgbx(long minXargb, long maxXargb, int steps, Bias bias) {
		return toList(streamFade(minXargb, maxXargb, steps, bias).boxed());
	}

	/**
	 * Create a list of scaled colorxs from min to max, in steps using bias.
	 */
	public static List<Colorx> fade(Colorx min, Colorx max, int steps, Bias bias) {
		return fade(min.xargb, max.xargb, steps, bias);
	}

	/**
	 * Create a list of scaled colorxs from min to max, in steps using bias.
	 */
	public static List<Colorx> fade(long minXargb, long maxXargb, int steps, Bias bias) {
		return toList(streamFade(minXargb, maxXargb, steps, bias).mapToObj(Colorx::of));
	}

	/* adapter methods */

	/**
	 * Converts hex string to argb int. The value must be prefixed with '#' or '0x', and contain
	 * 1..8 hex digits. If <= 6 digits, the value is treated as opaque, otherwise the alpha value is
	 * captured. Triple hex '#rgb' values will be treated as opaque 'rrggbb' hex values. Returns
	 * null if no match.
	 */
	private static Long hexXargb(String text) {
		Matcher m = RegexUtil.matched(XARGB_REGEX, text);
		if (m == null) return null;
		String hex = m.group(2);
		return Long.valueOf(hex, HEX);
	}

	/**
	 * Extract sequence of rgb values from argb to determine x components.
	 */
	public static long denormalizeXargb(int argb, int... xrgbs) {
		int[] rgb = { ColorUtil.r(argb), ColorUtil.g(argb), ColorUtil.b(argb) };
		int[] xs = new int[Math.min(X_COUNT, xrgbs.length)];
		for (int i = 0; i < xs.length; i++)
			xs[i] = denormalize(rgb, ColorUtil.r(xrgbs[i]), ColorUtil.g(xrgbs[i]),
				ColorUtil.b(xrgbs[i]));
		argb = ColorUtil.argb(ColorUtil.a(argb), rgb[0], rgb[1], rgb[2]);
		return xargb(argb, xs);
	}

	/**
	 * Combines x-scaled rgb values with argb, scaling to fit within argb bounds if needed.
	 */
	public static int normalizeArgb(long xargb, int... xrgbs) {
		int r = r(xargb);
		int g = g(xargb);
		int b = b(xargb);
		for (int i = 0; i < Math.min(X_COUNT, xrgbs.length); i++) {
			double ratio = ColorUtil.ratio(x(xargb, i));
			r += (ratio * r(xrgbs[i]));
			g += (ratio * g(xrgbs[i]));
			b += (ratio * b(xrgbs[i]));
		}
		int max = MathUtil.max(r, g, b, MAX_VALUE);
		if (max > MAX_VALUE) {
			r = (r * MAX_VALUE) / max;
			g = (g * MAX_VALUE) / max;
			b = (b * MAX_VALUE) / max;
		}
		return ColorUtil.argb(a(xargb), r, g, b);
	}

	/**
	 * Apply an argb int operation to an xargb long.
	 */
	public static long applyArgbx(long xargb, IntUnaryOperator argbFn) {
		return argbFn.applyAsInt((int) xargb) | (xargb & X_MASK);
	}

	/**
	 * Apply a color operation to a colorx.
	 */
	public static Colorx apply(Colorx colorx, UnaryOperator<Color> colorFn) {
		int argb = colorFn.apply(colorx.color()).getRGB();
		return Colorx.of(argb | (colorx.xargb & X_MASK));
	}

	/* support methods */

	private static LongStream streamFade(long minArgbx, long maxArgbx, int steps, Bias bias) {
		return IntStream.rangeClosed(1, steps)
			.mapToLong(i -> scaleArgbx(minArgbx, maxArgbx, bias.bias((double) i / steps)));
	}

	private static Long namedArgbx(String name) {
		return colors.values.get(name);
	}

	private static int denormalize(int[] rgb, int r, int g, int b) {
		double x = limit(min((double) rgb[0] / r, (double) rgb[1] / g, (double) rgb[2] / b), 0, 1);
		if (x == 0) return 0;
		rgb[0] -= (x * r);
		rgb[1] -= (x * g);
		rgb[2] -= (x * b);
		return ColorUtil.value(x);
	}

	private static int xCount(long xargb) {
		for (int i = X_COUNT - 1; i >= 0; i--)
			if (x(xargb, i) != 0) return i + 1;
		return 0;
	}

	private static long xEncode(int x, int i) {
		return ubyte(x) << xShift(i);
	}

	private static int xShift(int i) {
		return Integer.SIZE + (Byte.SIZE * i);
	}

	private static int divide(int x, double ratio) {
		if (ratio == 0.0) return 0;
		return (int) (x / ratio);
	}

	private static BiMap<Long, String> colors() {
		return BiMap.<Long, String>builder() //
			.put(Colorx.black.xargb, "black") //
			.put(Colorx.full0.xargb, "full0") //
			.put(Colorx.full1.xargb, "full1") //
			.put(Colorx.full2.xargb, "full2") //
			.put(Colorx.full.xargb, "full") //
			.build();
	}
}
