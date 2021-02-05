package ceri.common.color;

import static ceri.common.collection.StreamUtil.first;
import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.math.Bound.Type.inclusive;
import static java.util.Map.entry;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import ceri.common.data.ByteUtil;
import ceri.common.function.BinaryFunction;
import ceri.common.math.MathUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

public class ColorUtil {
	private static final Pattern COLOR_REGEX = Pattern.compile("(?:0x|#)?([0-9a-fA-F]{1,6})");
	private static final int HEX = 16;
	private static final int TRIPLE_HEX_LEN = 3;
	private static final int BITS4 = 4;
	private static final int BITS8 = 8;
	private static final int A_BYTE = 3;
	private static final int R_BYTE = 2;
	private static final int G_BYTE = 1;
	private static final int B_BYTE = 0;
	// private static final int HSB_DECIMALS = 5;
	private static final int RGB_MASK = 0xffffff;
	private static final double HALF = 0.5;
	public static final int CHANNEL_MAX = 0xff;
	private static final Map<Integer, String> awtColorNames = colorMap();

	private ColorUtil() {}

	public static class Fn {

		private Fn() {}

		public interface ChannelAdjuster {
			int applyAsInt(int x, double ratio);

			default ChannelAdjuster bias(Bias bias) {
				return (x, ratio) -> applyAsInt(x, bias.bias(ratio));
			}

			static ChannelAdjuster none() {
				return (x, ratio) -> x;
			}
		}

		public interface ChannelScaler {
			int applyAsInt(int x0, int x1, double ratio);

			static ChannelScaler of() {
				return of(Biases.NONE);
			}

			static ChannelScaler of(Bias bias) {
				return (x0, x1, ratio) -> scaleChannel(x0, x1, bias.bias(ratio));
			}
		}

		public static UnaryOperator<Color> dim(double scale) {
			return c -> ColorUtil.dim(c, scale);
		}

		public static BinaryFunction<Color, List<Color>> fade(int steps) {
			return fade(steps, Biases.NONE);
		}

		public static BinaryFunction<Color, List<Color>> fade(int steps, Bias bias) {
			return (c0, c1) -> ColorUtil.fade(c0, c1, steps, bias);
		}

		public static BinaryFunction<Color, List<Color>> fadeHsb(int steps) {
			return fadeHsb(steps, Biases.NONE);
		}

		public static BinaryFunction<Color, List<Color>> fadeHsb(int steps, Bias bias) {
			return (c0, c1) -> ColorUtil.fadeHsb(c0, c1, steps, bias);
		}

		public static Function<Color, List<Color>> rotateHue(int steps) {
			return rotateHue(steps, Biases.NONE);
		}

		public static Function<Color, List<Color>> rotateHue(int steps, Bias bias) {
			return c -> ColorUtil.rotateHue(c, steps, bias);
		}

		public static BinaryOperator<Color> scale(double ratio) {
			return (c0, c1) -> ColorUtil.scale(c0, c1, ratio);
		}

		public static BinaryOperator<Color> scaleHsb(double ratio) {
			return (c0, c1) -> ColorUtil.scaleHsb(c0, c1, ratio);
		}

		public static Function<Color, List<Color>>
			transform(Function<Colorx, List<Colorx>> rgbxFn) {
			return c -> applyRgbx(c, rgbxFn);
		}

		public static BinaryFunction<Color, List<Color>>
			transform(BinaryFunction<Colorx, List<Colorx>> rgbxFn) {
			return (c0, c1) -> applyRgbx(c0, c1, rgbxFn);
		}

		public static BinaryOperator<Color> transform(BinaryOperator<Colorx> rgbxFn) {
			return (c0, c1) -> applyRgbx(c0, c1, rgbxFn);
		}

		public static UnaryOperator<Color> transform(UnaryOperator<Colorx> rgbxFn) {
			return c -> applyRgbx(c, rgbxFn);
		}

		public static List<Color> applyRgbx(Color color, Function<Colorx, List<Colorx>> rgbxFn) {
			if (color == null) return null;
			if (rgbxFn == null) return Collections.emptyList();
			List<Colorx> colorxs = rgbxFn.apply(Colorx.of(color, 0));
			return toList(colorxs.stream().map(cx -> cx.rgb));
		}

		public static List<Color> applyRgbx(Color c0, Color c1,
			BinaryFunction<Colorx, List<Colorx>> rgbxFn) {
			if (c0 == null || c1 == null) return null;
			if (rgbxFn == null) return Collections.emptyList();
			List<Colorx> colorxs = rgbxFn.apply(Colorx.of(c0, 0), Colorx.of(c1, 0));
			return toList(colorxs.stream().map(cx -> cx.rgb));
		}

		public static Color applyRgbx(Color c0, Color c1, BinaryOperator<Colorx> rgbxFn) {
			if (c0 == null) return c1;
			if (c1 == null || rgbxFn == null) return c0;
			return rgbxFn.apply(Colorx.of(c0, 0), Colorx.of(c1, 0)).rgb;
		}

		public static Color applyRgbx(Color color, UnaryOperator<Colorx> rgbxFn) {
			if (rgbxFn == null || color == null) return color;
			return rgbxFn.apply(Colorx.of(color, 0)).rgb;
		}

		public static List<Color> apply(Color color, Function<Color, List<Color>> rgbFn) {
			if (color == null) return null;
			if (rgbFn == null) return Collections.emptyList();
			return rgbFn.apply(color);
		}

		public static List<Color> apply(Color c0, Color c1,
			BinaryFunction<Color, List<Color>> rgbFn) {
			if (c0 == null || c1 == null) return null;
			if (rgbFn == null) return Collections.emptyList();
			return rgbFn.apply(c0, c1);
		}

		public static Color apply(Color c0, Color c1, BinaryOperator<Color> rgbFn) {
			if (c0 == null) return c1;
			if (c1 == null || rgbFn == null) return c0;
			return rgbFn.apply(c0, c1);
		}

		public static Color apply(Color color, UnaryOperator<Color> rgbFn) {
			if (rgbFn == null || color == null) return color;
			return rgbFn.apply(color);
		}
	}

	public static Color max(Color color) {
		return max(color.getRed(), color.getGreen(), color.getBlue());
	}

	public static Color max(int r, int g, int b) {
		double ratio = MathUtil.max(toRatio(r), toRatio(g), toRatio(b));
		return new Color(divide(r, ratio), divide(g, ratio), divide(b, ratio));
	}

	public static double toRatio(int channel) {
		return MathUtil.limit(((double) channel) / CHANNEL_MAX, 0, 1.0);
	}

	public static int fromRatio(double ratio) {
		return (int) MathUtil.limit(Math.round(ratio * CHANNEL_MAX), 0, CHANNEL_MAX);
	}

	static int divide(int channel, double ratio) {
		if (ratio == 0.0) return CHANNEL_MAX;
		return (int) (channel / ratio);
	}

	public static List<Color> colors(int... rgbs) {
		return toList(IntStream.of(rgbs).mapToObj(Color::new));
	}

	public static List<Color> colors(String... names) {
		return colors(Arrays.asList(names));
	}

	public static List<Color> colors(Collection<String> names) {
		return toList(names.stream().map(ColorUtil::color).filter(Objects::nonNull));
	}

	/**
	 * Returns the given color with modified alpha value.
	 */
	public static Color alphaColor(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	/**
	 * Creates a gray color with given component level.
	 */
	public static Color grayColor(int value) {
		value &= CHANNEL_MAX;
		return new Color(value, value, value);
	}

	public static Color validColor(String name) {
		Color color = color(name);
		if (color != null) return color;
		throw new IllegalArgumentException("Invalid color: " + name);
	}

	public static Color color(String name) {
		Color color = colorFromName(name);
		if (color != null) return color;
		Matcher m = RegexUtil.matched(COLOR_REGEX, name);
		if (m == null) return null;
		String hex = m.group(1);
		int rgb = Integer.valueOf(hex, HEX);
		if (hex.length() == TRIPLE_HEX_LEN) rgb = tripleHexToRgb(rgb);
		return new Color(rgb);
	}

	public static Color awtColor(String name) {
		Integer rgb = first(awtColorNames.entrySet().stream().filter(e -> e.getValue().equals(name))
			.map(Map.Entry::getKey));
		if (rgb == null) return null;
		return new Color(rgb);
	}

	public static boolean isNamedAwtColor(Color color) {
		return isNamedAwtColor(color.getRGB());
	}

	public static boolean isNamedAwtColor(int rgb) {
		return awtColorNames.containsKey(rgb & RGB_MASK);
	}

	public static Color colorFromName(String name) {
		Color color = awtColor(name);
		if (color != null) return color;
		X11Color x11Color = X11Color.from(name);
		if (x11Color != null) return x11Color.color;
		return null;
	}

	public static List<String> toStrings(Color... colors) {
		return toStrings(Arrays.asList(colors));
	}

	public static List<String> toStrings(Collection<Color> colors) {
		return toList(colors.stream().map(ColorUtil::toString));
	}

	public static String toString(Color color) {
		if (color == null) return null;
		return toString(color.getRGB());
	}

	public static String toString(int r, int g, int b) {
		return toString(rgba(r, g, b, 0));
	}

	public static String toString(int rgb) {
		String name = toName(rgb);
		if (name != null) return name;
		return toHex(rgb);
	}

	public static String toName(Color color) {
		if (color == null) return null;
		return toName(color.getRGB());
	}

	public static String toName(int r, int g, int b) {
		return toName(rgba(r, g, b, 0));
	}

	public static String toName(int rgb) {
		rgb = rgb & RGB_MASK;
		String name = awtColorNames.get(rgb);
		if (name != null) return name;
		X11Color x11 = X11Color.from(rgb);
		return x11 == null ? null : x11.name();
	}

	public static String toHex(Color color) {
		if (color == null) return null;
		return toHex(color.getRGB());
	}

	public static String toHex(int r, int g, int b) {
		return toHex(rgba(r, g, b, 0));
	}

	public static String toHex(int rgb) {
		return "#" + StringUtil.toHex(rgb & RGB_MASK, 6);
	}

	public static List<Color> fade(int rgbaMin, int rgbaMax, int steps) {
		return fade(rgbaMin, rgbaMax, steps, Biases.NONE);
	}

	public static List<Color> fade(int rgbaMin, int rgbaMax, int steps, Bias bias) {
		return fade(new Color(rgbaMin), new Color(rgbaMax), steps, bias);
	}

	public static List<Color> fade(Color min, Color max, int steps) {
		return fade(min, max, steps, Biases.NONE);
	}

	public static List<Color> fade(Color min, Color max, int steps, Bias bias) {
		List<Color> colors = new ArrayList<>(steps);
		for (int i = 1; i <= steps; i++)
			colors.add(scale(min, max, bias.bias((double) i / steps)));
		return colors;
	}

	public static List<Color> fadeHsb(int rgbMin, int rgbMax, int steps) {
		return fadeHsb(rgbMin, rgbMax, steps, Biases.NONE);
	}

	public static List<Color> fadeHsb(int rgbMin, int rgbMax, int steps, Bias bias) {
		return fadeHsb(new Color(rgbMin), new Color(rgbMax), steps, bias);
	}

	public static List<Color> fadeHsb(Color min, Color max, int steps) {
		return fadeHsb(min, max, steps, Biases.NONE);
	}

	public static List<Color> fadeHsb(Color min, Color max, int steps, Bias bias) {
		List<Color> colors = new ArrayList<>(steps);
		for (int i = 1; i <= steps; i++)
			colors.add(scaleHsb(min, max, bias.bias((double) i / steps)));
		return colors;
	}

	public static int scaleRgba(int rgbaMin, int rgbaMax, double ratio) {
		int a = scaleChannel(a(rgbaMin), a(rgbaMax), ratio);
		int r = scaleChannel(r(rgbaMin), r(rgbaMax), ratio);
		int g = scaleChannel(g(rgbaMin), g(rgbaMax), ratio);
		int b = scaleChannel(b(rgbaMin), b(rgbaMax), ratio);
		return rgba(r, g, b, a);
	}

	public static Color scaleHsb(Color min, Color max, double ratio) {
		if (min == null) return max;
		if (max == null) return min;
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		HsbColor hsbMin = HsbColor.from(min).normalize();
		HsbColor hsbMax = HsbColor.from(max).normalize();
		double h = scaleHue(hsbMin.h, hsbMax.h, ratio);
		double s = scaleRatio(hsbMin.s, hsbMax.s, ratio);
		double b = scaleRatio(hsbMin.b, hsbMax.b, ratio);
		double a = scaleRatio(hsbMin.a, hsbMax.a, ratio);
		return HsbColor.of(h, s, b, a).asColor();
	}

	public static double scaleHue(double min, double max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		double diff = max - min;
		if (Math.abs(diff) > HALF) diff -= Math.signum(diff);
		double h = min + (ratio * diff);
		return MathUtil.periodicLimit(h, 1, inclusive);
	}

	public static Color scale(Color min, Color max, double ratio) {
		if (min == null) return max;
		if (max == null) return min;
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		int r = scaleChannel(min.getRed(), max.getRed(), ratio);
		int g = scaleChannel(min.getGreen(), max.getGreen(), ratio);
		int b = scaleChannel(min.getBlue(), max.getBlue(), ratio);
		int a = scaleChannel(min.getAlpha(), max.getAlpha(), ratio);
		return new Color(r, g, b, a);
	}

	public static int scaleChannel(int min, int max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		return min + (int) Math.round(ratio * (max - min));
	}

	public static double scaleRatio(double min, double max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		return min + (ratio * (max - min));
	}

	public static List<Color> rotateHue(int rgb, int steps) {
		return rotateHue(rgb, steps, Biases.NONE);
	}

	public static List<Color> rotateHue(int rgb, int steps, Bias bias) {
		return rotateHue(new Color(rgb), steps, bias);
	}

	public static List<Color> rotateHue(Color color, int steps) {
		return rotateHue(color, steps, Biases.NONE);
	}

	public static List<Color> rotateHue(Color color, int steps, Bias bias) {
		HsbColor hsb = HsbColor.from(color);
		List<Color> colors = new ArrayList<>(steps);
		for (int i = 1; i <= steps; i++) {
			double h = hsb.h + bias.bias((double) i / steps);
			if (h >= 1.0) h -= 1.0;
			colors.add(HsbColor.toColor(h, hsb.s, hsb.b));
		}
		return colors;
	}

	public static List<Color> dimAll(double scale, int... rgbs) {
		return dimAll(scale, colors(rgbs));
	}

	public static List<Color> dimAll(double scale, Color... colors) {
		return dimAll(scale, Arrays.asList(colors));
	}

	public static List<Color> dimAll(double scale, Collection<Color> colors) {
		return toList(colors.stream().map(c -> dim(c, scale)));
	}

	public static Color dim(int rgb, double scale) {
		return dim(new Color(rgb), scale);
	}

	public static Color dim(Color color, double scale) {
		return scale(Color.black, color, scale);
	}

	public static int rgba(int r, int g, int b, int a) {
		return (int) (ByteUtil.shiftByteLeft(a, A_BYTE) | ByteUtil.shiftByteLeft(r, R_BYTE) |
			ByteUtil.shiftByteLeft(g, G_BYTE) | ByteUtil.shiftByteLeft(b, B_BYTE));
	}

	/**
	 * Returns rgb components, removing alpha.
	 */
	public static int rgb(Color color) {
		return rgb(color.getRGB());
	}

	public static int a(int rgba) {
		return ByteUtil.ubyteAt(rgba, A_BYTE);
	}

	public static int r(int rgba) {
		return ByteUtil.ubyteAt(rgba, R_BYTE);
	}

	public static int g(int rgba) {
		return ByteUtil.ubyteAt(rgba, G_BYTE);
	}

	public static int b(int rgba) {
		return ByteUtil.ubyteAt(rgba, B_BYTE);
	}

	/**
	 * Returns rgb components, removing alpha.
	 */
	public static int rgb(int rgba) {
		return rgba & RGB_MASK;
	}

	static int tripleHexToRgb(int tripleHex) {
		int r = (tripleHex & 0xf00) << BITS8;
		int g = (tripleHex & 0xf0) << BITS4;
		int b = tripleHex & 0xf;
		return (r | g | b) * (HEX + 1);
	}

	public static Color random() {
		Random rnd = ThreadLocalRandom.current();
		int max = CHANNEL_MAX + 1;
		return new Color(rnd.nextInt(max), rnd.nextInt(max), rnd.nextInt(max));
	}

	private static Map<Integer, String> colorMap() {
		return Map.ofEntries( //
			entry(rgb(Color.black), "black"), //
			entry(rgb(Color.blue), "blue"), //
			entry(rgb(Color.cyan), "cyan"), //
			entry(rgb(Color.darkGray), "darkGray"), //
			entry(rgb(Color.gray), "gray"), //
			entry(rgb(Color.green), "green"), //
			entry(rgb(Color.lightGray), "lightGray"), //
			entry(rgb(Color.magenta), "magenta"), //
			entry(rgb(Color.orange), "orange"), //
			entry(rgb(Color.pink), "pink"), //
			entry(rgb(Color.red), "red"), //
			entry(rgb(Color.white), "white"), //
			entry(rgb(Color.yellow), "yellow"));
	}

}
