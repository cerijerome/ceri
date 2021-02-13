package ceri.common.color;

import static ceri.common.collection.StreamUtil.first;
import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.color.ColorUtil.MAX_VALUE;
import static ceri.common.color.ColorUtil.divide;
import static ceri.common.color.ColorUtil.scaleValue;
import static ceri.common.color.ColorUtil.ratio;
import static ceri.common.color.ColorUtil.tripleHexToRgb;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import ceri.common.data.ByteUtil;
import ceri.common.math.MathUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

public class ColorxUtil {
	private static final Pattern COLORX_REGEX = Pattern.compile("(?:0x|#)?([0-9a-fA-F]{1,8})");
	private static final int HEX = 16;
	private static final int TRIPLE_HEX_LEN = 3;
	private static final int QUAD_HEX_LEN = 4;
	private static final int BITS4 = 4;
	private static final int BITS8 = 8;
	private static final int BITS12 = 12;
	
	private static final Map<Integer, String> colorNames = colorMap();

	private ColorxUtil() {}

	public static Colorx max(Colorx colorx) {
		return max(colorx.r(), colorx.g(), colorx.b(), colorx.x());
	}

	public static Colorx max(int r, int g, int b, int x) {
		double ratio = MathUtil.max(ratio(r), ratio(g), ratio(b), ratio(x));
		return Colorx.of(divide(r, ratio), divide(g, ratio), divide(b, ratio), divide(x, ratio));
	}

	public static List<Colorx> colors(long... argbxs) {
		return toList(LongStream.of(argbxs).mapToObj(Colorx::of));
	}

	public static List<Colorx> colors(int... rgbxs) {
		return toList(IntStream.of(rgbxs).mapToObj(Colorx::of));
	}

	public static List<Colorx> colors(String... names) {
		return colors(Arrays.asList(names));
	}

	public static List<Colorx> colors(Collection<String> names) {
		return toList(names.stream().map(ColorxUtil::color).filter(Objects::nonNull));
	}

	/**
	 * Returns the given color with modified alpha value.
	 */
	public static Colorx alphaColor(Colorx colorx, int alpha) {
		return Colorx.of(alpha, colorx.r(), colorx.g(), colorx.b(), colorx.x());
	}

	public static Colorx validColor(String name) {
		Colorx colorx = color(name);
		if (colorx != null) return colorx;
		throw new IllegalArgumentException("Invalid colorx: " + name);
	}

	public static Colorx color(String name) {
		Colorx colorx = colorFromXName(name);
		if (colorx != null) return colorx;
		Matcher m = RegexUtil.matched(COLORX_REGEX, name);
		if (m == null) return rgbColor(name);
		String hex = m.group(1);
		int rgbx = (int) Long.parseLong(hex, HEX);
		if (hex.length() == TRIPLE_HEX_LEN) return Colorx.of(tripleHexToRgb(rgbx), 0);
		if (hex.length() == QUAD_HEX_LEN) rgbx = quadHexToRgbx(rgbx);
		return Colorx.of(rgbx);
	}

	private static Colorx rgbColor(String name) {
		Color color = ColorUtil.color(name);
		if (color != null) return Colorx.of(color, 0);
		return null;
	}

	public static Colorx colorFromName(String name) {
		Colorx colorx = colorFromXName(name);
		if (colorx != null) return colorx;
		Color color = ColorUtil.colorFromName(name);
		if (color != null) return Colorx.of(color, 0);
		return null;
	}

	private static Colorx colorFromXName(String name) {
		Integer rgbx = first(colorNames.entrySet().stream().filter(e -> e.getValue().equals(name))
			.map(Map.Entry::getKey));
		if (rgbx != null) return Colorx.of(rgbx);
		return null;
	}

	public static List<String> toStrings(Colorx... colorxs) {
		return toStrings(Arrays.asList(colorxs));
	}

	public static List<String> toStrings(Collection<Colorx> colorxs) {
		return toList(colorxs.stream().map(ColorxUtil::toString));
	}

	public static String toString(Colorx colorx) {
		if (colorx == null) return null;
		return toString(colorx.rgbx());
	}

	public static String toString(int r, int g, int b, int x) {
		return toString(rgbx(r, g, b, x));
	}

	public static String toString(int rgbx) {
		String name = toName(rgbx);
		if (name != null) return name;
		return toHex(rgbx);
	}

	public static String toName(Colorx colorx) {
		if (colorx == null) return null;
		return toName(colorx.rgbx());
	}

	public static String toName(int r, int g, int b, int x) {
		return toName(rgbx(r, g, b, x));
	}

	public static String toName(int rgbx) {
		String name = colorNames.get(rgbx);
		if (name != null) return name;
		if (ByteUtil.byteAt(rgbx, 0) != 0) return null;
		return ColorUtil.toName(ByteUtil.shift(rgbx, 1));
	}

	public static String toHex(Colorx colorx) {
		if (colorx == null) return null;
		return toHex(colorx.rgbx());
	}

	public static String toHex(int r, int g, int b, int x) {
		return toHex(rgbx(r, g, b, x));
	}

	public static String toHex(int rgbx) {
		return "#" + StringUtil.toHex(rgbx, 8);
	}

	public static List<Colorx> fade(int rgbxMin, int rgbxMax, int steps) {
		return fade(rgbxMin, rgbxMax, steps, Biases.NONE);
	}

	public static List<Colorx> fade(int rgbxMin, int rgbxMax, int steps, Bias bias) {
		return fade(Colorx.of(rgbxMin), Colorx.of(rgbxMax), steps, bias);
	}

	public static List<Colorx> fade(Colorx min, Colorx max, int steps) {
		return fade(min, max, steps, Biases.NONE);
	}

	public static List<Colorx> fade(Colorx min, Colorx max, int steps, Bias bias) {
		List<Colorx> colorxs = new ArrayList<>(steps);
		for (int i = 1; i <= steps; i++)
			colorxs.add(scale(min, max, bias.bias((double) i / steps)));
		return colorxs;
	}

	public static List<Colorx> fadeHsbx(int rgbxMin, int rgbxMax, int steps) {
		return fadeHsbx(rgbxMin, rgbxMax, steps, Biases.NONE);
	}

	public static List<Colorx> fadeHsbx(int rgbxMin, int rgbxMax, int steps, Bias bias) {
		return fadeHsbx(Colorx.of(rgbxMin), Colorx.of(rgbxMax), steps, bias);
	}

	public static List<Colorx> fadeHsbx(Colorx min, Colorx max, int steps) {
		return fadeHsbx(min, max, steps, Biases.NONE);
	}

	public static List<Colorx> fadeHsbx(Colorx min, Colorx max, int steps, Bias bias) {
		List<Colorx> colors = new ArrayList<>(steps);
		for (int i = 1; i <= steps; i++)
			colors.add(scaleHsbx(min, max, bias.bias((double) i / steps)));
		return colors;
	}

	public static Colorx scaleHsbx(int rgbxMin, int rgbxMax, double ratio) {
		return scaleHsbx(Colorx.of(rgbxMin), Colorx.of(rgbxMax), ratio);
	}

	public static Colorx scaleHsbx(Colorx min, Colorx max, double ratio) {
		if (min == null) return max;
		if (max == null) return min;
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		Color rgb = ColorUtil.scaleHsb(min.color(), max.color(), ratio);
		int x = scaleValue(min.x(), max.x(), ratio);
		return Colorx.of(rgb, x);
	}

	public static Colorx scale(int rgbxMin, int rgbxMax, double ratio) {
		return scale(Colorx.of(rgbxMin), Colorx.of(rgbxMax), ratio);
	}

	public static Colorx scale(Colorx min, Colorx max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		int r = scaleValue(min.r(), max.r(), ratio);
		int g = scaleValue(min.g(), max.g(), ratio);
		int b = scaleValue(min.b(), max.b(), ratio);
		int x = scaleValue(min.x(), max.x(), ratio);
		int a = scaleValue(min.a(), max.a(), ratio);
		return Colorx.of(a, r, g, b, x);
	}

	public static List<Colorx> rotateHuex(int rgbx, int steps) {
		return rotateHuex(rgbx, steps, Biases.NONE);
	}

	public static List<Colorx> rotateHuex(int rgbx, int steps, Bias bias) {
		return rotateHuex(Colorx.of(rgbx), steps, bias);
	}

	public static List<Colorx> rotateHuex(Colorx colorx, int steps) {
		return rotateHuex(colorx, steps, Biases.NONE);
	}

	public static List<Colorx> rotateHuex(Colorx colorx, int steps, Bias bias) {
		List<Color> rgbs = ColorUtil.rotateHue(colorx.color(), steps, bias);
		return toList(rgbs.stream().map(rgb -> Colorx.of(rgb, colorx.x())));
	}

	public static List<Colorx> dimAll(double scale, int... rgbxs) {
		return dimAll(scale, colors(rgbxs));
	}

	public static List<Colorx> dimAll(double scale, Colorx... colorxs) {
		return dimAll(scale, Arrays.asList(colorxs));
	}

	public static List<Colorx> dimAll(double scale, Collection<Colorx> colorxs) {
		return toList(colorxs.stream().map(c -> dim(c, scale)));
	}

	public static Colorx dim(int rgbx, double scale) {
		return dim(Colorx.of(rgbx), scale);
	}

	public static Colorx dim(Colorx color, double scale) {
		return scale(Colorx.black, color, scale);
	}

	public static int rgbx(int r, int g, int b, int x) {
		return Colorx.of(r, g, b, x).rgbx();
	}

	public static int quadHexToRgbx(int quadHex) {
		int r = (quadHex & 0xf000) << BITS12;
		int g = (quadHex & 0xf00) << BITS8;
		int b = (quadHex & 0xf0) << BITS4;
		int x = quadHex & 0xf;
		return (r | g | b | x) * (HEX + 1);
	}

	public static Colorx random() {
		Random rnd = ThreadLocalRandom.current();
		int max = MAX_VALUE + 1;
		return Colorx.of(rnd.nextInt(max), rnd.nextInt(max), rnd.nextInt(max), rnd.nextInt(max));
	}

	private static int averageInt(int... values) {
		return (int) Math.round(MathUtil.mean(values));
	}

	private static Map<Integer, String> colorMap() {
		return Map.of(Colorx.black.rgbx(), "black", Colorx.full.rgbx(), "full");
	}
}
