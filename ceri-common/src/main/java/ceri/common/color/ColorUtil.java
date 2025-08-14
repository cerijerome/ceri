package ceri.common.color;

import static ceri.common.math.MathUtil.roundDiv;
import static ceri.common.text.StringUtil.HEX_RADIX;
import static java.util.stream.Collectors.toList;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.collection.Immutable;
import ceri.common.math.Bound;
import ceri.common.math.MathUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

/**
 * Utilities for handling colors, including 4-byte argb ints, 3-byte rgb ints and Color objects.
 */
public class ColorUtil {
	private static final Pattern HEX_REGEX = Pattern.compile("(0x|0X|#)([0-9a-fA-F]{1,8})");
	public static final Pattern COLOR_REGEX =
		Pattern.compile("(?:[a-zA-Z_][a-zA-Z0-9_]*|(?:0x|0X|#)[0-9a-fA-F]{1,8})");
	public static final Color clear = color(0);
	private static final Immutable.BiMap<Integer, String> colors = colors(); // any alpha
	private static final int HEX_RGB_MAX_LEN = 6;
	private static final int HEX_ARGB_MAX_LEN = 8;
	private static final int HEX3_LEN = 3;
	private static final int HEX3_MASK = 0xf;
	private static final int HEX3_R_SHIFT = 8;
	private static final int HEX3_G_SHIFT = 4;
	private static final double HALF = 0.5;
	public static final double MAX_RATIO = 1.0;
	public static final int MAX_VALUE = 0xff;
	private static final int RND_MAX = MAX_VALUE + 1;

	private ColorUtil() {}

	/* argb int methods */

	/**
	 * Removes alpha component from argb int.
	 */
	public static int rgb(int argb) {
		return argb & ~Component.a.intMask;
	}

	/**
	 * Extracts rgb int without alpha component.
	 */
	public static int rgb(Color color) {
		return rgb(argb(color));
	}

	/**
	 * Extracts argb int (convenience method to avoid confusion).
	 */
	public static int argb(Color color) {
		return color.getRGB();
	}

	/**
	 * Constructs an opaque argb int from rgb int.
	 */
	public static int argb(int rgb) {
		return rgb | Component.a.intMask;
	}

	/**
	 * Constructs an argb int from components.
	 */
	public static int argb(int a, int r, int g, int b) {
		return Component.a.intValue(a) | Component.r.intValue(r) | Component.g.intValue(g)
			| Component.b.intValue(b);
	}

	/**
	 * Constructs an opaque argb int from components.
	 */
	public static int argb(int r, int g, int b) {
		return Component.a.intMask | Component.r.intValue(r) | Component.g.intValue(g)
			| Component.b.intValue(b);
	}

	/**
	 * Returns an argb int from awt name, x11 name, or hex representation. Named colors are opaque,
	 * hex colors may have alpha components, or be opaque. Returns null if no match.
	 */
	public static Integer argb(String text) {
		Integer argb = namedArgb(text);
		return argb != null ? argb : hexArgb(text);
	}

	/**
	 * Returns an argb int from preset name or hex representation. Throws an exception if unable to
	 * parse the text.
	 */
	public static int validArgb(String text) {
		Integer argb = argb(text);
		if (argb != null) return argb;
		throw new IllegalArgumentException("Invalid color: " + text);
	}

	/**
	 * Creates an opaque gray argb int with the same value for all color components.
	 */
	public static int grayArgb(int value) {
		return argb(MAX_VALUE, value, value, value);
	}

	/**
	 * Creates an argb int with maximum color components in the same ratio.
	 */
	public static int maxArgb(int argb) {
		return maxArgb(a(argb), r(argb), g(argb), b(argb));
	}

	/**
	 * Creates an argb int with maximum color components in the same ratio.
	 */
	public static int maxArgb(int a, int r, int g, int b) {
		int max = MathUtil.max(r, g, b);
		if (max == 0 || max == MAX_VALUE) return argb(a, r, g, b);
		return argb(a, roundDiv(r * MAX_VALUE, max), roundDiv(g * MAX_VALUE, max),
			roundDiv(b * MAX_VALUE, max));
	}

	/**
	 * Creates an opaque argb int with random color components.
	 */
	public static int randomRgb() {
		Random rnd = ThreadLocalRandom.current();
		return argb(MAX_VALUE, rnd.nextInt(RND_MAX), rnd.nextInt(RND_MAX), rnd.nextInt(RND_MAX));
	}

	/**
	 * Creates an argb int with random alpha and color components.
	 */
	public static int randomArgb() {
		Random rnd = ThreadLocalRandom.current();
		return argb(rnd.nextInt(RND_MAX), rnd.nextInt(RND_MAX), rnd.nextInt(RND_MAX),
			rnd.nextInt(RND_MAX));
	}

	/**
	 * Flattens the color by applying alpha channel on opaque black.
	 */
	public static int flattenArgb(int argb) {
		return blendArgbs(argb, Colors.black.argb);
	}

	/**
	 * Create a composite from argbs, with lower indexes on top.
	 */
	public static int blendArgbs(int... argbs) {
		if (argbs.length == 0) return 0;
		int argb = argbs[0];
		for (int i = 1; i < argbs.length; i++)
			argb = blendArgb(argb, argbs[i]);
		return argb;
	}

	/**
	 * Provides a component-scaled argb int from given argb int. Alpha value is maintained.
	 */
	public static int dimArgb(int argb, double scale) {
		if (scale == MAX_RATIO || rgb(argb) == 0) return argb;
		return Component.a.set(scaleArgb(0, argb, scale), Component.a.get(argb));
	}

	/**
	 * Provides a component-scaled argb int from min and max argb int values.
	 */
	public static int scaleArgb(int minArgb, int maxArgb, double ratio) {
		if (ratio <= 0.0) return minArgb;
		if (ratio >= MAX_RATIO) return maxArgb;
		int a = scaleValue(a(minArgb), a(maxArgb), ratio);
		int r = scaleValue(r(minArgb), r(maxArgb), ratio);
		int g = scaleValue(g(minArgb), g(maxArgb), ratio);
		int b = scaleValue(b(minArgb), b(maxArgb), ratio);
		return argb(a, r, g, b);
	}

	/**
	 * Provides an hsb component-scaled argb int from min and max argb int values.
	 */
	public static int scaleHsbArgb(int minArgb, int maxArgb, double ratio) {
		if (ratio <= 0.0) return minArgb;
		if (ratio >= MAX_RATIO) return maxArgb;
		return scaleHsb(HsbColor.from(minArgb), HsbColor.from(maxArgb), ratio).argb();
	}

	/* Color methods */

	/**
	 * Creates a color from argb int.
	 */
	public static Color color(int argb) {
		return new Color(argb, true);
	}

	/**
	 * Creates a color from components. Provided for consistency with argb().
	 */
	public static Color color(int a, int r, int g, int b) {
		return color(argb(a, r, g, b));
	}

	/**
	 * Returns a color from awt name, x11 name, or hex representation. Returns null if no match.
	 */
	public static Color color(String text) {
		Integer argb = argb(text);
		return argb == null ? null : color(argb);
	}

	/**
	 * Returns a color from awt name, x11 name, or hex representation. Throws an exception if no
	 * match.
	 */
	public static Color validColor(String text) {
		Color color = color(text);
		if (color != null) return color;
		throw new IllegalArgumentException("Invalid color: " + text);
	}

	/**
	 * Creates an opaque gray color with the same value for all color components.
	 */
	public static Color gray(int value) {
		return color(grayArgb(value));
	}

	/**
	 * Creates a color with maximum color components in the same ratio.
	 */
	public static Color max(Color color) {
		return color(maxArgb(argb(color)));
	}

	/**
	 * Provides an opaque Color with random components.
	 */
	public static Color random() {
		return color(randomRgb());
	}

	/**
	 * Applies alpha component to create a scaled opaque color on black.
	 */
	public static Color flatten(Color color) {
		return blend(color, Color.black);
	}

	/**
	 * Create a composite from colors, with lower indexes on top.
	 */
	public static Color blend(Color... colors) {
		return color(blendArgbs(argbs(colors)));
	}

	/**
	 * Provides a component-scaled color from given color. Alpha value is maintained.
	 */
	public static Color dim(Color color, double scale) {
		if (scale == MAX_RATIO || rgb(color) == 0) return color;
		return color(dimArgb(argb(color), scale));
	}

	/**
	 * Provides a component-scaled color from min and max colors.
	 */
	public static Color scale(Color min, Color max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= MAX_RATIO) return max;
		return color(scaleArgb(argb(min), argb(max), ratio));
	}

	/**
	 * Provides an hsb component-scaled color from min and max colors.
	 */
	public static Color scaleHsb(Color min, Color max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= MAX_RATIO) return max;
		return scaleHsb(HsbColor.from(min), HsbColor.from(max), ratio).color();
	}

	/* other color types */

	/**
	 * Provides a scaled hsb color from min and max.
	 */
	public static HsbColor scaleHsb(HsbColor minHsb, HsbColor maxHsb, double ratio) {
		if (ratio <= 0.0) return minHsb;
		if (ratio >= MAX_RATIO) return maxHsb;
		minHsb = minHsb.normalize();
		maxHsb = maxHsb.normalize();
		return scaleNormHsb(minHsb, maxHsb, ratio);
	}

	/* component functions */

	/**
	 * Extract component from argb int.
	 */
	public static int a(int argb) {
		return Component.a.get(argb);
	}

	/**
	 * Set the argb int component value.
	 */
	public static int a(int argb, int a) {
		return Component.a.set(argb, a);
	}

	/**
	 * Extract component from argb int.
	 */
	public static int r(int argb) {
		return Component.r.get(argb);
	}

	/**
	 * Set the argb int component value.
	 */
	public static int r(int argb, int r) {
		return Component.r.set(argb, r);
	}

	/**
	 * Extract component from argb int.
	 */
	public static int g(int argb) {
		return Component.g.get(argb);
	}

	/**
	 * Set the argb int component value.
	 */
	public static int g(int argb, int g) {
		return Component.g.set(argb, g);
	}

	/**
	 * Extract component from argb int.
	 */
	public static int b(int argb) {
		return Component.b.get(argb);
	}

	/**
	 * Set the argb int component value.
	 */
	public static int b(int argb, int b) {
		return Component.b.set(argb, b);
	}

	/**
	 * Extract component from color.
	 */
	public static int a(Color color) {
		return color.getAlpha();
	}

	/**
	 * Return a color with the component value set.
	 */
	public static Color a(Color color, int a) {
		return a(color) == a ? color : color(a(argb(color), a));
	}

	/**
	 * Extract component from color.
	 */
	public static int r(Color color) {
		return color.getRed();
	}

	/**
	 * Return a color with the component value set.
	 */
	public static Color r(Color color, int r) {
		return r(color) == r ? color : color(r(argb(color), r));
	}

	/**
	 * Extract component from color.
	 */
	public static int g(Color color) {
		return color.getGreen();
	}

	/**
	 * Return a color with the component value set.
	 */
	public static Color g(Color color, int g) {
		return g(color) == g ? color : color(g(argb(color), g));
	}

	/**
	 * Extract component from color.
	 */
	public static int b(Color color) {
		return color.getBlue();
	}

	/**
	 * Return a color with the component value set.
	 */
	public static Color b(Color color, int b) {
		return b(color) == b ? color : color(b(argb(color), b));
	}

	/**
	 * Converts a component value to a 0-1 (inclusive) ratio.
	 */
	public static double ratio(int component) {
		return (double) MathUtil.ubyte(component) / MAX_VALUE;
	}

	/**
	 * Converts a 0-1 (inclusive) ratio to a component value.
	 */
	public static int value(double ratio) {
		return MathUtil.limit((int) Math.round(ratio * MAX_VALUE), 0, MAX_VALUE);
	}

	/**
	 * Limits a ratio to 0-1 (inclusive).
	 */
	public static double limit(double ratio) {
		return MathUtil.limit(ratio, 0, MAX_RATIO);
	}

	/**
	 * Adjust hue to the range 0-1 (inclusive).
	 */
	public static double limitHue(double h) {
		return MathUtil.periodicLimit(h, MAX_RATIO, Bound.Type.inclusive);
	}

	/**
	 * Scales a 0-1 (inclusive) hue component, finding the shortest cyclic path.
	 */
	public static double scaleHue(double min, double max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= MAX_RATIO) return max;
		double diff = max - min;
		if (Math.abs(diff) > HALF) diff -= Math.signum(diff);
		return limitHue(min + ratio * diff);
	}

	/**
	 * Scales a component value between min and max values.
	 */
	public static int scaleValue(int min, int max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= MAX_RATIO) return max;
		return min + MathUtil.intRoundExact(ratio * (max - min));
	}

	/**
	 * Scales a 0-1 component ratio value between min and max values.
	 */
	public static double scaleRatio(double min, double max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= MAX_RATIO) return max;
		return min + (ratio * (max - min));
	}

	/* string methods */

	/**
	 * Returns the hex string, with name if rgb matches an awt or x11 color.
	 */
	public static String toString(Color color) {
		return toString(argb(color));
	}

	/**
	 * Returns the hex string, with name if rgb matches an awt or x11 color.
	 */
	public static String toString(int argb) {
		String name = name(argb);
		String hex = hex(argb);
		return name == null ? hex : hex + "(" + name + ")";
	}

	/**
	 * Looks up the awt or x11 color name for given color, ignoring alpha. Returns null if no match.
	 */
	public static String name(Color color) {
		return name(argb(color));
	}

	/**
	 * Looks up the Colors preset name for given argb int, ignoring alpha. Returns null if no match.
	 */
	public static String name(int argb) {
		String name = colors.keys.get(argb);
		if (name == null) name = Colors.name(argb);
		return name;
	}

	/**
	 * Creates a hex string from argb int. Uses 6 digits if opaque, otherwise 8.
	 */
	public static String hex(Color color) {
		return hex(argb(color));
	}

	/**
	 * Creates a hex string from argb int. Uses 6 digits if opaque, otherwise 8.
	 */
	public static String hex(int argb) {
		int digits = a(argb) == MAX_VALUE ? HEX_RGB_MAX_LEN : HEX_ARGB_MAX_LEN;
		return "#" + StringUtil.toHex(argb, digits);
	}

	/* stream methods */

	/**
	 * Convert colors to argb array.
	 */
	public static int[] argbs(Color... colors) {
		return stream(colors).toArray();
	}

	/**
	 * Convert color preset name or hex strings to argb array. Throws an exception if unable to
	 * parse text.
	 */
	public static int[] argbs(String... strings) {
		return stream(strings).toArray();
	}

	/**
	 * Convert color name/hex strings to color array. Throws an exception if unable to parse text.
	 */
	public static Color[] colors(String... strings) {
		return colors(stream(strings));
	}

	/**
	 * Collect argb int stream as a color array.
	 */
	public static Color[] colors(IntStream argbStream) {
		return argbStream.mapToObj(ColorUtil::color).toArray(Color[]::new);
	}

	/**
	 * Collect argb int stream as a list.
	 */
	public static List<Integer> argbList(IntStream argbStream) {
		return argbStream.boxed().collect(toList());
	}

	/**
	 * Collect argb int stream as a color list.
	 */
	public static List<Color> colorList(IntStream argbStream) {
		return argbStream.mapToObj(ColorUtil::color).collect(toList());
	}

	/**
	 * Create a stream of opaque argb ints from rgb ints.
	 */
	public static IntStream rgbStream(int... rgbs) {
		return IntStream.of(rgbs).map(ColorUtil::argb);
	}

	/**
	 * Create a stream of argb ints from colors.
	 */
	public static IntStream stream(Color... colors) {
		return Stream.of(colors).mapToInt(ColorUtil::argb);
	}

	/**
	 * Create a stream of argb ints from preset name or hex strings. Throws an exception if unable
	 * to parse the text.
	 */
	public static IntStream stream(String... strings) {
		return Stream.of(strings).mapToInt(ColorUtil::validArgb);
	}

	/**
	 * Create a stream of argb ints by fading in steps.
	 */
	public static IntStream fadeStream(Color min, Color max, int steps, Bias bias) {
		return fadeStream(argb(min), argb(max), steps, bias);
	}

	/**
	 * Create a stream of argb ints by fading in steps.
	 */
	public static IntStream fadeStream(int minArgb, int maxArgb, int steps, Bias bias) {
		return IntStream.rangeClosed(1, steps)
			.map(i -> scaleArgb(minArgb, maxArgb, bias.bias((double) i / steps)));
	}

	/**
	 * Create a stream of argb ints by fading hue/saturation/brightness in steps.
	 */
	public static IntStream fadeHsbStream(Color min, Color max, int steps, Bias bias) {
		return fadeHsbStream(argb(min), argb(max), steps, bias);
	}

	/**
	 * Create a stream of argb ints by fading hue/saturation/brightness in steps.
	 */
	public static IntStream fadeHsbStream(int minArgb, int maxArgb, int steps, Bias bias) {
		return fadeHsbStream(HsbColor.from(minArgb), HsbColor.from(maxArgb), steps, bias);
	}

	/**
	 * Create a stream of argb ints by fading hue/saturation/brightness in steps.
	 */
	public static IntStream fadeHsbStream(HsbColor min, HsbColor max, int steps, Bias bias) {
		return IntStream.rangeClosed(1, steps)
			.mapToObj(i -> scaleNormHsb(min, max, bias.bias((double) i / steps)))
			.mapToInt(HsbColor::argb);
	}

	/**
	 * Create a stream of argb ints by rotating hue 360 degrees in steps.
	 */
	public static IntStream rotateHueStream(Color color, int steps, Bias bias) {
		return rotateHueStream(argb(color), steps, bias);
	}

	/**
	 * Create a stream of argb ints by rotating hue 360 degrees in steps.
	 */
	public static IntStream rotateHueStream(int argb, int steps, Bias bias) {
		return rotateHueStream(HsbColor.from(argb), steps, bias);
	}

	/**
	 * Create a stream of argb ints by rotating hue 360 degrees in steps.
	 */
	public static IntStream rotateHueStream(HsbColor hsb, int steps, Bias bias) {
		return IntStream.rangeClosed(1, steps)
			.mapToObj(i -> hsb.shiftHue(bias.bias((double) i / steps))).mapToInt(HsbColor::argb);
	}

	/* support methods */

	/**
	 * Blends argb0 on top of argb1 using alpha values.
	 */
	private static int blendArgb(int argb0, int argb1) {
		int a0 = a(argb0);
		if (a0 == MAX_VALUE) return argb0;
		if (a0 == 0) return argb1;
		int a1 = a(argb1);
		if (a1 == 0) return argb0;
		int a = blendAlpha(a0, a1);
		int argb = Component.a.set(0, a);
		for (var component : Component.RGB) {
			int c = blendComponent(a, a0, a1, component.get(argb0), component.get(argb1));
			argb = component.set(argb, c);
		}
		return argb;
	}

	private static int blendAlpha(int a0, int a1) {
		return a0 + a1 - roundDiv(a0 * a1, MAX_VALUE);
	}

	private static int blendComponent(int a, int a0, int a1, int c0, int c1) {
		return roundDiv(MAX_VALUE * (a0 * c0 + a1 * c1) - (a0 * a1 * c1), MAX_VALUE * a);
	}

	/**
	 * Converts hex string to argb int. The value must be prefixed with '#' or '0x', and contain
	 * 1..8 hex digits. If <= 6 digits, the value is treated as opaque, otherwise the alpha value is
	 * captured. Triple hex '#rgb' values will be treated as opaque 'rrggbb' hex values. Returns
	 * null if no match.
	 */
	private static Integer hexArgb(String text) {
		Matcher m = RegexUtil.matched(HEX_REGEX, text);
		if (m == null) return null;
		String prefix = m.group(1);
		String hex = m.group(2);
		int argb = Integer.parseUnsignedInt(hex, HEX_RADIX);
		int len = hex.length();
		return hexArgb(prefix, len, argb);
	}

	private static HsbColor scaleNormHsb(HsbColor minHsb, HsbColor maxHsb, double ratio) {
		// if (ratio <= 0.0) return minHsb;
		if (ratio >= MAX_RATIO) return maxHsb;
		double a = scaleRatio(minHsb.a(), maxHsb.a(), ratio);
		double h = scaleHue(minHsb.h(), maxHsb.h(), ratio);
		double s = scaleRatio(minHsb.s(), maxHsb.s(), ratio);
		double b = scaleRatio(minHsb.b(), maxHsb.b(), ratio);
		return HsbColor.of(a, h, s, b);
	}

	private static Integer namedArgb(String name) {
		Integer argb = colors.values.get(name);
		if (argb == null) argb = Colors.argb(name);
		return argb;
	}

	private static int hexArgb(String prefix, int len, int argb) {
		if (len > HEX_RGB_MAX_LEN) return argb; // argb
		if (!"#".equals(prefix) || len != HEX3_LEN) return argb(argb); // rgb
		int r = Component.r.intValue((argb >>> HEX3_R_SHIFT) & HEX3_MASK);
		int g = Component.g.intValue((argb >>> HEX3_G_SHIFT) & HEX3_MASK);
		int b = (argb) & HEX3_MASK;
		return argb((r | g | b) * (HEX_RADIX + 1)); // triple-hex #rgb
	}

	private static Immutable.BiMap<Integer, String> colors() {
		// Placeholder for custom colors
		return Immutable.wrapBiMap(Map.of(argb(clear), "clear"));
	}
}
