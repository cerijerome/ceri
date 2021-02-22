package ceri.common.color;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.math.Bound.Type.inclusive;
import static ceri.common.math.MathUtil.ubyte;
import static java.lang.Math.round;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.math.MathUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

/**
 * Utilities for handling colors, including 4-byte argb ints, 3-byte rgb ints and Color objects.
 */
public class ColorUtil {
	private static final Pattern ARGB_REGEX = Pattern.compile("(0x|#)?([0-9a-fA-F]{1,8})");
	public static final Color clear = color(0);
	private static final int HEX = 16;
	private static final int HEX3_LEN = 3;
	private static final int HEX_RGB_MAX_LEN = 6;
	private static final int HEX_ARGB_MAX_LEN = 8;
	private static final int A_SHIFT = 24;
	private static final int R_SHIFT = 16;
	private static final int G_SHIFT = 8;
	public static final double MAX_RATIO = 1.0;
	private static final double HALF = 0.5;
	public static final int A_MASK = 0xff000000;
	public static final int RGB_MASK = 0xffffff;
	public static final int MAX_VALUE = 0xff;
	private static final int HEX3_MASK = 0xf;
	private static final int HEX3_R_SHIFT = 8;
	private static final int HEX3_G_SHIFT = 4;

	private ColorUtil() {}

	/* argb int methods */

	/**
	 * Replaces the alpha component for an argb int.
	 */
	public static int alphaArgb(int a, int argb) {
		return (a << A_SHIFT) | (argb & RGB_MASK);
	}

	/**
	 * Removes alpha component from argb int.
	 */
	public static int rgb(int argb) {
		return argb & RGB_MASK;
	}

	/**
	 * Extracts rgb int without alpha component.
	 */
	public static int rgb(Color color) {
		return rgb(color.getRGB());
	}

	/**
	 * Constructs an opaque argb int from rgb int.
	 */
	public static int argb(int rgb) {
		return alphaArgb(MAX_VALUE, rgb);
	}

	/**
	 * Constructs an argb int from components.
	 */
	public static int argb(int a, int r, int g, int b) {
		return alphaArgb(a, ubyte(r) << R_SHIFT | ubyte(g) << G_SHIFT | ubyte(b));
	}

	/**
	 * Constructs an opaque argb int from components.
	 */
	public static int argb(int r, int g, int b) {
		return argb(MAX_VALUE, r, g, b);
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
	 * Returns an argb int from awt name, x11 name, or hex representation. Throws an exception if no
	 * match.
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
		double ratio = MathUtil.max(ratio(r), ratio(g), ratio(b));
		return argb(a, divide(r, ratio), divide(g, ratio), divide(b, ratio));
	}

	/**
	 * Creates an opaque argb int with random color components.
	 */
	public static int randomArgb() {
		Random rnd = ThreadLocalRandom.current();
		int max = MAX_VALUE + 1;
		return argb(MAX_VALUE, rnd.nextInt(max), rnd.nextInt(max), rnd.nextInt(max));
	}

	/**
	 * Provides a component-scaled argb int from given argb int. Alpha value is maintained.
	 */
	public static int dimArgb(int argb, double scale) {
		if (scale == MAX_RATIO || rgb(argb) == 0) return argb;
		return alphaArgb(a(argb), scaleArgb(0, argb, scale));
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
	 * Replaces the alpha component of the color.
	 */
	public static Color alpha(int a, Color color) {
		return color(alphaArgb(a, color.getRGB()));
	}

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
		return color(maxArgb(color.getRGB()));
	}

	/**
	 * Provides an opaque Color with random components.
	 */
	public static Color random() {
		return color(randomArgb());
	}

	/**
	 * Provides a component-scaled color from given color. Alpha value is maintained.
	 */
	public static Color dim(Color color, double scale) {
		if (scale == MAX_RATIO || rgb(color) == 0) return color;
		return color(dimArgb(color.getRGB(), scale));
	}

	/**
	 * Provides a component-scaled color from min and max colors.
	 */
	public static Color scale(Color min, Color max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= MAX_RATIO) return max;
		return color(scaleArgb(min.getRGB(), max.getRGB(), ratio));
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
		return ubyte(argb >>> A_SHIFT);
	}

	/**
	 * Extract component from argb int.
	 */
	public static int r(int argb) {
		return ubyte(argb >>> R_SHIFT);
	}

	/**
	 * Extract component from argb int.
	 */
	public static int g(int argb) {
		return ubyte(argb >>> G_SHIFT);
	}

	/**
	 * Extract component from argb int.
	 */
	public static int b(int argb) {
		return ubyte(argb);
	}

	/**
	 * Extract component from color.
	 */
	public static int a(Color color) {
		return color.getAlpha();
	}

	/**
	 * Extract component from color.
	 */
	public static int r(Color color) {
		return color.getRed();
	}

	/**
	 * Extract component from color.
	 */
	public static int g(Color color) {
		return color.getGreen();
	}

	/**
	 * Extract component from color.
	 */
	public static int b(Color color) {
		return color.getBlue();
	}

	/**
	 * Converts a component value to a 0-1 (inclusive) ratio.
	 */
	public static double ratio(int component) {
		return (double) ubyte(component) / MAX_VALUE;
	}

	/**
	 * Converts a 0-1 (inclusive) ratio to a component value.
	 */
	public static int value(double ratio) {
		return MathUtil.limit((int) round(ratio * MAX_VALUE), 0, MAX_VALUE);
	}

	/**
	 * Limits a ratio to 0-1 (inclusive).
	 */
	public static double limit(double ratio) {
		return MathUtil.limit(ratio, 0, MAX_VALUE);
	}

	/**
	 * Adjust hue to the range 0-1 (inclusive).
	 */
	public static double limitHue(double h) {
		return MathUtil.periodicLimit(h, MAX_RATIO, inclusive);
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
		return min + (int) Math.round(ratio * (max - min));
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
		return toString(color.getRGB());
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
		return name(color.getRGB());
	}

	/**
	 * Looks up the Colors preset name for given argb int, ignoring alpha. Returns null if no match.
	 */
	public static String name(int argb) {
		Colors preset = Colors.from(argb);
		if (preset != null) return preset.name();
		Colors x11 = Colors.from(argb);
		return x11 == null ? null : x11.name();
	}

	/**
	 * Creates a hex string from argb int. Uses 6 digits if opaque, otherwise 8.
	 */
	public static String hex(Color color) {
		return hex(color.getRGB());
	}

	/**
	 * Creates a hex string from argb int. Uses 6 digits if opaque, otherwise 8.
	 */
	public static String hex(int argb) {
		int digits = a(argb) == MAX_VALUE ? HEX_RGB_MAX_LEN : HEX_ARGB_MAX_LEN;
		return "#" + StringUtil.toHex(argb, digits);
	}

	/* List methods */

	/**
	 * Returns a list of opaque argb ints from rgb ints.
	 */
	public static List<Integer> argbs(int... rgbs) {
		return toList(IntStream.of(rgbs).map(ColorUtil::argb).boxed());
	}

	/**
	 * Returns a list of opaque argb ints from name/hex strings. Throws an exception if unable to
	 * map all strings.
	 */
	public static List<Integer> argbs(String... names) {
		return argbs(Arrays.asList(names));
	}

	/**
	 * Returns a list of opaque argb ints from name/hex strings. Throws an exception if unable to
	 * map all strings.
	 */
	public static List<Integer> argbs(Collection<String> names) {
		return toList(names.stream().map(ColorUtil::validArgb));
	}

	/**
	 * Returns a list of colors from argb ints.
	 */
	public static List<Color> colors(int... argbs) {
		return toList(IntStream.of(argbs).mapToObj(ColorUtil::color));
	}

	/**
	 * Returns a list of opaque colors from rgb ints.
	 */
	public static List<Color> colorsRgb(int... rgbs) {
		return toList(IntStream.of(rgbs).mapToObj(Color::new));
	}

	/**
	 * Returns a list of opaque colors from name/hex strings. Throws an exception if unable to map
	 * all strings.
	 */
	public static List<Color> colors(String... names) {
		return colors(Arrays.asList(names));
	}

	/**
	 * Returns a list of opaque colors from name/hex strings. Throws an exception if unable to map
	 * all strings.
	 */
	public static List<Color> colors(Collection<String> names) {
		return toList(names.stream().map(ColorUtil::validColor));
	}

	/**
	 * Create a list of scaled argb ints from min to max, in steps using bias.
	 */
	public static List<Integer> fadeArgb(int minArgb, int maxArgb, int steps, Bias bias) {
		return toList(streamFade(minArgb, maxArgb, steps, bias).boxed());
	}

	/**
	 * Create a list of scaled argb ints from min to max, in steps using bias.
	 */
	public static List<Color> fade(Color min, Color max, int steps, Bias bias) {
		return fade(min.getRGB(), max.getRGB(), steps, bias);
	}

	/**
	 * Create a list of scaled colors from min to max, in steps using bias.
	 */
	public static List<Color> fade(int minArgb, int maxArgb, int steps, Bias bias) {
		return toList(streamFade(minArgb, maxArgb, steps, bias).mapToObj(ColorUtil::color));
	}

	/**
	 * Create a list of hsb-scaled argb ints from min to max, in steps using bias.
	 */
	public static List<Integer> fadeHsbArgb(int minArgb, int maxArgb, int steps, Bias bias) {
		return toList(streamFadeHsb(HsbColor.from(minArgb), HsbColor.from(maxArgb), steps, bias)
			.map(HsbColor::argb));
	}

	/**
	 * Create a list of hsb-scaled colors from min to max, in steps using bias.
	 */
	public static List<Color> fadeHsb(Color min, Color max, int steps, Bias bias) {
		return fadeHsb(min.getRGB(), max.getRGB(), steps, bias);
	}

	/**
	 * Create a list of hsb-scaled colors from min to max, in steps using bias.
	 */
	public static List<Color> fadeHsb(int minArgb, int maxArgb, int steps, Bias bias) {
		return toList(streamFadeHsb(HsbColor.from(minArgb), HsbColor.from(maxArgb), steps, bias)
			.map(HsbColor::color));
	}

	/**
	 * Create a list of hsb-scaled colors from min to max, in steps using bias.
	 */
	public static List<HsbColor> fadeHsb(HsbColor minHsb, HsbColor maxHsb, int steps, Bias bias) {
		return toList(streamFadeHsb(minHsb.normalize(), maxHsb.normalize(), steps, bias));
	}

	/**
	 * Create a list of argb ints for 1 full hsb rotation, in steps using bias.
	 */
	public static List<Integer> rotateHueArgb(int argb, int steps, Bias bias) {
		return toList(streamRotateHue(HsbColor.from(argb), steps, bias).map(HsbColor::argb));
	}

	/**
	 * Create a list of colors for 1 full hsb rotation, in steps using bias.
	 */
	public static List<Color> rotateHue(Color color, int steps, Bias bias) {
		return rotateHue(color.getRGB(), steps, bias);
	}

	/**
	 * Create a list of colors for 1 full hsb rotation, in steps using bias.
	 */
	public static List<Color> rotateHue(int argb, int steps, Bias bias) {
		return toList(streamRotateHue(HsbColor.from(argb), steps, bias).map(HsbColor::color));
	}

	/* support methods */

	/**
	 * Converts hex string to argb int. The value must be prefixed with '#' or '0x', and contain
	 * 1..8 hex digits. If <= 6 digits, the value is treated as opaque, otherwise the alpha value is
	 * captured. Triple hex '#rgb' values will be treated as opaque 'rrggbb' hex values. Returns
	 * null if no match.
	 */
	private static Integer hexArgb(String text) {
		Matcher m = RegexUtil.matched(ARGB_REGEX, text);
		if (m == null) return null;
		String prefix = m.group(1);
		String hex = m.group(2);
		int argb = Integer.valueOf(hex, HEX);
		int len = hex.length();
		return hexArgb(prefix, len, argb);
	}

	static int divide(int component, double ratio) {
		if (ratio == 0.0) return MAX_VALUE;
		return (int) (component / ratio);
	}

	private static IntStream streamFade(int minArgb, int maxArgb, int steps, Bias bias) {
		return IntStream.rangeClosed(1, steps)
			.map(i -> scaleArgb(minArgb, maxArgb, bias.bias((double) i / steps)));
	}

	private static Stream<HsbColor> streamFadeHsb(HsbColor minHsb, HsbColor maxHsb, int steps,
		Bias bias) {
		return IntStream.rangeClosed(1, steps)
			.mapToObj(i -> scaleNormHsb(minHsb, maxHsb, bias.bias((double) i / steps)));
	}

	private static HsbColor scaleNormHsb(HsbColor minHsb, HsbColor maxHsb, double ratio) {
		if (ratio <= 0.0) return minHsb;
		if (ratio >= MAX_RATIO) return maxHsb;
		double a = scaleRatio(minHsb.a, maxHsb.a, ratio);
		double h = scaleHue(minHsb.h, maxHsb.h, ratio);
		double s = scaleRatio(minHsb.s, maxHsb.s, ratio);
		double b = scaleRatio(minHsb.b, maxHsb.b, ratio);
		return HsbColor.of(a, h, s, b);
	}

	private static Stream<HsbColor> streamRotateHue(HsbColor hsb, int steps, Bias bias) {
		return IntStream.rangeClosed(1, steps)
			.mapToObj(i -> hsb.shiftHue(bias.bias((double) i / steps)));
	}

	private static Integer namedArgb(String name) {
		Colors preset = Colors.from(name);
		return preset == null ? null : preset.argb;
	}

	private static int hexArgb(String prefix, int len, int argb) {
		if (len > HEX_RGB_MAX_LEN) return argb; // argb
		if (!"#".equals(prefix) || len != HEX3_LEN) return argb(argb); // rgb
		int r = ((argb >>> HEX3_R_SHIFT) & HEX3_MASK) << R_SHIFT;
		int g = ((argb >>> HEX3_G_SHIFT) & HEX3_MASK) << G_SHIFT;
		int b = (argb) & HEX3_MASK;
		return argb((r | g | b) * (HEX + 1)); // triple-hex #rgb
	}
}
