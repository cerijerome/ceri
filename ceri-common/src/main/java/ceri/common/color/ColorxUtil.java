package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_RATIO;
import static ceri.common.color.ColorUtil.MAX_VALUE;
import static ceri.common.color.ColorUtil.scaleValue;
import static ceri.common.color.Component.X_COUNT;
import static ceri.common.color.Component.X_MASK;
import static ceri.common.math.MathUtil.intRound;
import static ceri.common.math.MathUtil.limit;
import static ceri.common.math.MathUtil.min;
import static ceri.common.math.MathUtil.uint;
import static ceri.common.text.StringUtil.HEX_RADIX;
import static java.util.stream.Collectors.toList;
import java.awt.Color;
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
import ceri.common.data.IntProvider;
import ceri.common.math.MathUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

public class ColorxUtil {
	private static final Pattern HEX_REGEX = Pattern.compile("(?:0x|0X|#)([0-9a-fA-F]{1,16})");
	public static final Pattern COLORX_REGEX =
		Pattern.compile("(?:[a-zA-Z_][a-zA-Z0-9_]*|(?:0x|0X|#)[0-9a-fA-F]{1,16})");
	private static final BiMap<Long, String> colorxs = colorxs(); // full alpha

	private ColorxUtil() {}

	/* xargb long methods */

	/**
	 * Determine if xargb long has any x component.
	 */
	public static boolean hasX(long xargb) {
		return (xargb & X_MASK) != 0L;
	}

	/**
	 * Removes the alpha component from xargb long.
	 */
	public static long xrgb(long xargb) {
		return xargb & ~Component.a.mask;
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
		long xargb = uint(argb);
		for (int i = 0; i < xs.length; i++)
			xargb |= Component.x(i).longValue(xs[i]);
		return xargb;
	}

	/**
	 * Constructs an xargb long from color and x component.
	 */
	public static long xargb(Color color, int... xs) {
		return xargb(color.getRGB(), xs);
	}

	/**
	 * Returns an xargb long from colorx name, or hex representation. Returns null if no match.
	 */
	public static Long xargb(String text) {
		Integer argb = ColorUtil.argb(text);
		if (argb != null) return xargb(argb);
		Long xargb = namedArgbx(text);
		return xargb != null ? xargb : hexXargb(text);
	}

	/**
	 * Returns an xargb long from colorx name, or hex representation. Throws an exception if no
	 * match.
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
		int max = MathUtil.max(r, g, b, MathUtil.max(xs));
		if (max == 0 || max == MAX_VALUE) return xargb(argb, xs);
		argb = ColorUtil.argb(a, roundDiv(r * MAX_VALUE, max), roundDiv(g * MAX_VALUE, max),
			roundDiv(b * MAX_VALUE, max));
		for (int i = 0; i < xs.length; i++)
			xs[i] = roundDiv(xs[i] * MAX_VALUE, max);
		return xargb(argb, xs);
	}

	/**
	 * Creates an opaque xargb long with random components.
	 */
	public static long randomXargb() {
		return randomXargb(X_COUNT);
	}

	/**
	 * Creates an opaque xargb long with random components. nx specifies how many x values to
	 * generate.
	 */
	public static long randomXargb(int nx) {
		Random rnd = ThreadLocalRandom.current();
		int max = MAX_VALUE + 1;
		int argb = ColorUtil.argb(MAX_VALUE, rnd.nextInt(max), rnd.nextInt(max), rnd.nextInt(max));
		int[] xs = IntStream.range(0, nx).map(i -> rnd.nextInt(max)).toArray();
		return xargb(argb, xs);
	}

	/**
	 * Flattens the color by applying alpha channel on opaque black.
	 */
	public static long flattenXargb(long xargb) {
		return blendXargbs(xargb, Colorx.black.xargb);
	}

	/**
	 * Create a composite from xargbs, with lower indexes on top.
	 */
	public static long blendXargbs(long... xargbs) {
		if (xargbs.length == 0) return 0L;
		long xargb = xargbs[0];
		for (int i = 1; i < xargbs.length; i++)
			xargb = blendXargb(xargb, xargbs[i]);
		return xargb;
	}

	/**
	 * Provides a component-scaled xargb long from given xargb long. Alpha value is maintained.
	 */
	public static long dimXargb(long xargb, double scale) {
		if (scale == MAX_RATIO || xrgb(xargb) == 0L) return xargb;
		return Component.a.set(scaleXargb(0, xargb, scale), Component.a.get(xargb));
	}

	/**
	 * Provides a component-scaled xargb long from min and max xargb long values.
	 */
	public static long scaleXargb(long minXargb, long maxXargb, double ratio) {
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
	public static Colorx random() {
		return random(X_COUNT);
	}

	/**
	 * Provides an opaque colorx with random components. nx specifies how many x values to generate.
	 */
	public static Colorx random(int nx) {
		return Colorx.of(randomXargb(nx));
	}

	/**
	 * Create a composite from colors, with lower indexes on top.
	 */
	public static Colorx blend(Colorx... colorxs) {
		return Colorx.of(blendXargbs(xargbs(colorxs)));
	}

	/**
	 * Provides a component-scaled colorx from given colorx. Alpha value is maintained.
	 */
	public static Colorx dim(Colorx colorx, double scale) {
		if (scale == MAX_RATIO) return colorx;
		return Colorx.of(dimXargb(colorx.xargb, scale));
	}

	/**
	 * Provides a component-scaled colorx from min and max colorxs.
	 */
	public static Colorx scale(Colorx min, Colorx max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= MAX_RATIO) return max;
		return Colorx.of(scaleXargb(min.xargb, max.xargb, ratio));
	}

	/* component functions */

	/**
	 * Extract component from xargb long.
	 */
	public static int a(long xargb) {
		return ColorUtil.a((int) xargb);
	}

	/**
	 * Set the xargb long component value.
	 */
	public static long a(long xargb, int a) {
		return Component.a.set(xargb, a);
	}

	/**
	 * Extract component from xargb long.
	 */
	public static int r(long xargb) {
		return ColorUtil.r((int) xargb);
	}

	/**
	 * Set the xargb long component value.
	 */
	public static long r(long xargb, int r) {
		return Component.r.set(xargb, r);
	}

	/**
	 * Extract component from xargb long.
	 */
	public static int g(long xargb) {
		return ColorUtil.g((int) xargb);
	}

	/**
	 * Set the xargb long component value.
	 */
	public static long g(long xargb, int g) {
		return Component.g.set(xargb, g);
	}

	/**
	 * Extract component from xargb long.
	 */
	public static int b(long xargb) {
		return ColorUtil.b((int) xargb);
	}

	/**
	 * Set the xargb long component value.
	 */
	public static long b(long xargb, int b) {
		return Component.b.set(xargb, b);
	}

	/**
	 * Extract x[i] component from xargb long.
	 */
	public static int x(long xargb, int i) {
		return Component.x(i).get(xargb);
	}

	/**
	 * Set the xargb long x[i] component value.
	 */
	public static long x(long xargb, int i, int x) {
		return Component.x(i).set(xargb, x);
	}

	/**
	 * Extract all x components from xargb long.
	 */
	public static int[] xs(long xargb) {
		return IntStream.range(0, X_COUNT).map(i -> x(xargb, i)).toArray();
	}

	/* string methods */

	/**
	 * Returns the hex string, with name if colorx matches a named colorx.
	 */
	public static String toString(Colorx color) {
		return toString(color.xargb);
	}

	/**
	 * Returns the hex string, with name if xargb matches a named colorx.
	 */
	public static String toString(long xargb) {
		return hasX(xargb) ? toStringX(xargb) : ColorUtil.toString((int) xargb);
	}

	/**
	 * Looks up the colorx name. Returns null if no match.
	 */
	public static String name(Colorx colorx) {
		return name(colorx.xargb);
	}

	/**
	 * Looks up the colorx name. Returns null if no match.
	 */
	public static String name(long xargb) {
		return hasX(xargb) ? nameX(xargb) : ColorUtil.name((int) xargb);
	}

	/**
	 * Creates a hex string from colorx. Returns color hex if no x component.
	 */
	public static String hex(Colorx colorx) {
		return hex(colorx.xargb);
	}

	/**
	 * Creates a hex string from xargb long. Returns argb hex if no x component.
	 */
	public static String hex(long xargb) {
		return hasX(xargb) ? hexX(xargb) : ColorUtil.hex((int) xargb);
	}

	/* stream methods */

	/**
	 * Convert colorxs to xargb array.
	 */
	public static long[] xargbs(Colorx... colorxs) {
		return stream(colorxs).toArray();
	}

	/**
	 * Returns an array of xargb longs from preset name or hex strings. Throws an exception if
	 * unable to parse text.
	 */
	public static long[] xargbs(String... strings) {
		return stream(strings).toArray();
	}

	/**
	 * Returns an array of colorxs from preset name or hex strings. Throws an exception if unable to
	 * parse text.
	 */
	public static Colorx[] colorxs(String... strings) {
		return colorxs(stream(strings));
	}

	/**
	 * Collect xargb stream as a colorx array.
	 */
	public static Colorx[] colorxs(LongStream xargbStream) {
		return xargbStream.mapToObj(Colorx::of).toArray(Colorx[]::new);
	}

	/**
	 * Collect xargb stream as a list.
	 */
	public static List<Long> xargbList(LongStream argbStream) {
		return argbStream.boxed().collect(toList());
	}

	/**
	 * Collect xargb stream as a colorx list.
	 */
	public static List<Colorx> colorxList(LongStream xargbStream) {
		return xargbStream.mapToObj(Colorx::of).collect(toList());
	}

	/**
	 * Create a stream of xargbs longs, with 0 x components, from argb ints.
	 */
	public static LongStream argbStream(int... argbs) {
		return IntStream.of(argbs).mapToLong(MathUtil::uint);
	}

	/**
	 * Create a stream of xargb longs from colorxs.
	 */
	public static LongStream stream(Colorx... colorxs) {
		return Stream.of(colorxs).mapToLong(cx -> cx.xargb);
	}

	/**
	 * Create a stream of xargb longs from preset name or hex strings. Throws an exception if unable
	 * to parse the text.
	 */
	public static LongStream stream(String... strings) {
		return Stream.of(strings).mapToLong(ColorxUtil::validXargb);
	}

	/**
	 * Convert argb int stream to xargb long stream, applying x components to all values.
	 */
	public static LongStream stream(IntStream argbStream, int... xs) {
		long x = xargb(0, xs);
		return argbStream.mapToLong(argb -> x | uint(argb));
	}

	/**
	 * Extract sequence of rgb values from each argb to determine x components.
	 */
	public static LongStream denormalize(IntStream argbStream, Color... xcolors) {
		return denormalize(argbStream, ColorUtil.argbs(xcolors));
	}

	/**
	 * Extract sequence of rgb values from each argb to determine x components.
	 */
	public static LongStream denormalize(IntStream argbStream, int... xrgbs) {
		return denormalize(argbStream, IntProvider.of(xrgbs));
	}

	/**
	 * Extract sequence of rgb values from each argb to determine x components.
	 */
	public static LongStream denormalize(IntStream argbStream, IntProvider xrgbs) {
		return argbStream.mapToLong(argb -> denormalizeXargb(argb, xrgbs));
	}

	/**
	 * For each xargb, combine x-scaled rgb values with argb, scaling to fit within argb bounds.
	 */
	public static IntStream normalize(LongStream xargbStream, Color... xcolors) {
		return normalize(xargbStream, ColorUtil.argbs(xcolors));
	}

	/**
	 * For each xargb, combine x-scaled rgb values with argb, scaling to fit within argb bounds.
	 */
	public static IntStream normalize(LongStream xargbStream, int... xrgbs) {
		return normalize(xargbStream, IntProvider.of(xrgbs));
	}

	/**
	 * For each xargb, combine x-scaled rgb values with argb, scaling to fit within argb bounds.
	 */
	public static IntStream normalize(LongStream xargbStream, IntProvider xrgbs) {
		return xargbStream.mapToInt(xargb -> normalizeArgb(xargb, xrgbs));
	}

	/**
	 * Apply an argb int operation to an xargb long.
	 */
	public static LongStream applyArgb(LongStream xargbStream, IntUnaryOperator argbFn) {
		return xargbStream.map(xargb -> applyXargb(xargb, argbFn));
	}

	/**
	 * Create a stream of xargb longs by fading in steps.
	 */
	public static LongStream fadeStream(Colorx min, Colorx max, int steps, Bias bias) {
		return fadeStream(min.xargb, max.xargb, steps, bias);
	}

	/**
	 * Create a stream of xargb longs by fading in steps.
	 */
	public static LongStream fadeStream(long minArgbx, long maxArgbx, int steps, Bias bias) {
		return IntStream.rangeClosed(1, steps)
			.mapToLong(i -> scaleXargb(minArgbx, maxArgbx, bias.bias((double) i / steps)));
	}

	/* adapter methods */

	/**
	 * Extract sequence of color values from color to determine x components.
	 */
	public static Colorx denormalize(Color color, Color... xcolors) {
		return Colorx.of(denormalizeXargb(color.getRGB(), ColorUtil.argbs(xcolors)));
	}

	/**
	 * Extract sequence of rgb values from argb to determine x components.
	 */
	public static long denormalizeXargb(int argb, int... xrgbs) {
		return denormalizeXargb(argb, IntProvider.of(xrgbs));
	}

	/**
	 * Extract sequence of rgb values from argb to determine x components.
	 */
	public static long denormalizeXargb(int argb, IntProvider xrgbs) {
		if (xrgbs.length() == 0) return xargb(argb);
		int[] rgb = { ColorUtil.r(argb), ColorUtil.g(argb), ColorUtil.b(argb) };
		int[] xs = new int[Math.min(X_COUNT, xrgbs.length())];
		for (int i = 0; i < xs.length; i++) {
			int xrgb = xrgbs.getInt(i);
			if (ColorUtil.rgb(xrgb) != 0)
				xs[i] = denormalize(rgb, ColorUtil.r(xrgb), ColorUtil.g(xrgb), ColorUtil.b(xrgb));
		}
		argb = ColorUtil.argb(ColorUtil.a(argb), rgb[0], rgb[1], rgb[2]);
		return xargb(argb, xs);
	}

	/**
	 * Combine x-scaled rgb values with argb, scaling to fit within argb bounds.
	 */
	public static Color normalize(Colorx colorx, Color... xcolors) {
		return ColorUtil.color(normalizeArgb(colorx.xargb, ColorUtil.argbs(xcolors)));
	}

	/**
	 * Combine x-scaled rgb values with argb, scaling to fit within argb bounds.
	 */
	public static int normalizeArgb(long xargb, int... xrgbs) {
		return normalizeArgb(xargb, IntProvider.of(xrgbs));
	}

	/**
	 * Combine x-scaled rgb values with argb, scaling to fit within argb bounds.
	 */
	public static int normalizeArgb(long xargb, IntProvider xrgbs) {
		if (xrgbs.length() == 0) return (int) xargb;
		int[] rgb = { r(xargb), g(xargb), b(xargb) };
		for (int i = 0; i < Math.min(X_COUNT, xrgbs.length()); i++)
			normalize(rgb, xrgbs.getInt(i), x(xargb, i));
		int max = MathUtil.max(rgb);
		if (max <= MAX_VALUE) return ColorUtil.argb(a(xargb), rgb[0], rgb[1], rgb[2]);
		return ColorUtil.argb(a(xargb), roundDiv(rgb[0] * MAX_VALUE, max),
			roundDiv(rgb[1] * MAX_VALUE, max), roundDiv(rgb[2] * MAX_VALUE, max));
	}

	/**
	 * Apply an argb int operation to an xargb long.
	 */
	public static long applyXargb(long xargb, IntUnaryOperator argbFn) {
		return xargb & X_MASK | uint(argbFn.applyAsInt((int) xargb));
	}

	/**
	 * Apply a color operation to a colorx.
	 */
	public static Colorx apply(Colorx colorx, UnaryOperator<Color> colorFn) {
		int argb = colorFn.apply(colorx.color()).getRGB();
		return Colorx.of(colorx.xargb & X_MASK | uint(argb));
	}

	/* support methods */

	/**
	 * Blends xargb0 on top of xargb1 using alpha values.
	 */
	private static long blendXargb(long xargb0, long xargb1) {
		int a0 = a(xargb0);
		if (a0 == MAX_VALUE) return xargb0;
		if (a0 == 0) return xargb1;
		int a1 = a(xargb1);
		if (a1 == 0) return xargb0;
		int a = blendAlpha(a0, a1);
		long xargb = Component.a.set(0L, a);
		for (var component : Component.XRGB) {
			int c = blendComponent(a, a0, a1, component.get(xargb0), component.get(xargb1));
			xargb = component.set(xargb, c);
		}
		return xargb;
	}

	private static int blendAlpha(int a0, int a1) {
		return a0 + a1 - roundDiv(a0 * a1, MAX_VALUE);
	}

	private static int blendComponent(int a, int a0, int a1, int c0, int c1) {
		return roundDiv(MAX_VALUE * (a0 * c0 + a1 * c1) - (a0 * a1 * c1), MAX_VALUE * a);
	}

	private static int roundDiv(int x, int y) {
		return (x + (y >> 1)) / y;
	}

	private static String toStringX(long xargb) {
		String name = nameX(xargb);
		String hex = hexX(xargb);
		return name == null ? hex : hex + "(" + name + ")";
	}

	private static String nameX(long xargb) {
		return colorxs.keys.get(xargb | Component.a.mask);
	}

	private static String hexX(long xargb) {
		return "#" + StringUtil.toHex(xargb, Component.count(xargb) << 1);
	}

	private static Long namedArgbx(String name) {
		return colorxs.values.get(name);
	}

	private static Long hexXargb(String text) {
		Matcher m = RegexUtil.matched(HEX_REGEX, text);
		if (m == null) return null;
		String hex = m.group(1);
		return Long.parseUnsignedLong(hex, HEX_RADIX);
	}

	private static int denormalize(int[] rgb, int r, int g, int b) {
		double x = limit(min(ratio(rgb[0], r), ratio(rgb[1], g), ratio(rgb[2], b)), 0, 1);
		if (x == 0) return 0;
		rgb[0] -= intRound(x * r);
		rgb[1] -= intRound(x * g);
		rgb[2] -= intRound(x * b);
		return ColorUtil.value(x);
	}

	private static void normalize(int[] rgb, int xrgb, int x) {
		rgb[0] += roundDiv(x * r(xrgb), MAX_VALUE);
		rgb[1] += roundDiv(x * g(xrgb), MAX_VALUE);
		rgb[2] += roundDiv(x * b(xrgb), MAX_VALUE);
	}

	private static double ratio(int c, int c0) {
		if (c0 == 0) return MAX_RATIO;
		return (double) c / c0;
	}

	private static BiMap<Long, String> colorxs() {
		return BiMap.<Long, String>builder() //
			.put(Colorx.black.xargb, "black") //
			.put(Colorx.fullX0.xargb, "fullX0") //
			.put(Colorx.fullX01.xargb, "fullX01") //
			.put(Colorx.fullX012.xargb, "fullX012") //
			.put(Colorx.full.xargb, "full") //
			.build();
	}
}
