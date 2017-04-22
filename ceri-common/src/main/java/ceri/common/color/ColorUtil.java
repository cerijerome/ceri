package ceri.common.color;

import static ceri.common.collection.StreamUtil.first;
import static ceri.common.collection.StreamUtil.toList;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import ceri.common.data.ByteUtil;
import ceri.common.math.MathUtil;
import ceri.common.text.StringUtil;

public class ColorUtil {
	private static final Pattern COLOR_REGEX = Pattern.compile("(?:0x|#)?([0-9a-fA-F]{1,6})");
	private static final int HEX = 16;
	private static final int TRIPLE_HEX_LEN = 3;
	private static final int BITS4 = 4;
	private static final int BITS8 = 8;
	//private static final int HSB_DECIMALS = 5;
	private static final int RGB_MASK = 0xffffff;
	private static final Map<Integer, String> awtColorNames = colorMap();

	private ColorUtil() {}

	public static Color max(Color color) {
		return max(color.getRed(), color.getGreen(), color.getBlue());
	}

	public static Color max(int r, int g, int b) {
		double ratio = MathUtil.max(ratio(r), ratio(g), ratio(b));
		return new Color(divide(r, ratio), divide(g, ratio), divide(b, ratio));
	}

	private static double ratio(int channel) {
		return ((double) channel) / 0xff;
	}

	private static int divide(int channel, double ratio) {
		if (ratio == 0.0) return channel;
		return (int) (channel / ratio);
	}

	public static Color colorFromName(String name) {
		Color color = awtColor(name);
		if (color != null) return color;
		X11Color x11Color = X11Color.from(name);
		if (x11Color != null) return x11Color.color;
		return null;
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
	
	public static Color color(String name) {
		Color color = colorFromName(name);
		if (color != null) return color;
		Matcher m = COLOR_REGEX.matcher(name);
		if (!m.find()) return null;
		String hex = m.group(1);
		int rgb = Integer.valueOf(hex, HEX);
		if (hex.length() == TRIPLE_HEX_LEN) rgb = tripleHexToRgb(rgb);
		return new Color(rgb);
	}

	public static Color awtColor(String name) {
		Integer rgb =
			first(colorMap().entrySet().stream().filter(e -> e.getValue().equals(name)).map(
				Map.Entry::getKey));
		if (rgb == null) return null;
		return new Color(rgb);
	}

	public static boolean isNamedAwtColor(Color color) {
		return isNamedAwtColor(color.getRGB());
	}

	public static boolean isNamedAwtColor(int rgb) {
		return awtColorNames.containsKey(rgb & RGB_MASK);
	}

	public static String toString(Collection<Color> colors) {
		return colors.stream().map(ColorUtil::toString).collect(Collectors.joining(", ", "[", "]"));
	}

	public static String toString(Color color) {
		return toString(color.getRGB());
	}

	public static String toString(int r, int g, int b) {
		return toString(rgb(r, g, b));
	}

	public static String toString(int rgb) {
		rgb = rgb & RGB_MASK;
		String name = awtColorNames.get(rgb);
		if (name != null) return name;
		X11Color x11 = X11Color.from(rgb);
		if (x11 != null) return x11.name();
		return "#" + StringUtil.toHex(rgb, 6);
	}

	public static List<Color> fade(int rgbMin, int rgbMax, int steps) {
		return fade(rgbMin, rgbMax, steps, Biases.NONE);
	}

	public static List<Color> fade(int rgbMin, int rgbMax, int steps, Bias bias) {
		return fade(new Color(rgbMin), new Color(rgbMax), steps, bias);
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

	public static Color scale(int rgbMin, int rgbMax, double ratio) {
		return scale(new Color(rgbMin), new Color(rgbMax), ratio);
	}

	public static Color scale(Color min, Color max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		int r = scaleChannel(min.getRed(), max.getRed(), ratio);
		int g = scaleChannel(min.getGreen(), max.getGreen(), ratio);
		int b = scaleChannel(min.getBlue(), max.getBlue(), ratio);
		return new Color(r, g, b);
	}

	public static int scaleChannel(int min, int max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		return min + (int) Math.round(ratio * (max - min));
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

	public static int rgb(int r, int g, int b) {
		return ByteUtil.shiftLeft(r, 2) | ByteUtil.shiftLeft(g, 1) | b & 0xff;
	}

	public static int rgb(Color color) {
		return color.getRGB() & RGB_MASK;
	}

	public static int tripleHexToRgb(int tripleHex) {
		int r = (tripleHex & 0xf00) << BITS8;
		int g = (tripleHex & 0xf0) << BITS4;
		int b = tripleHex & 0xf;
		return (r | g | b) * (HEX + 1);
	}

	private static Map<Integer, String> colorMap() {
		Map<Integer, String> map = new HashMap<>();
		map.put(rgb(Color.black), "black");
		map.put(rgb(Color.blue), "blue");
		map.put(rgb(Color.cyan), "cyan");
		map.put(rgb(Color.darkGray), "darkGray");
		map.put(rgb(Color.gray), "gray");
		map.put(rgb(Color.green), "green");
		map.put(rgb(Color.lightGray), "lightGray");
		map.put(rgb(Color.magenta), "magenta");
		map.put(rgb(Color.orange), "orange");
		map.put(rgb(Color.pink), "pink");
		map.put(rgb(Color.red), "red");
		map.put(rgb(Color.white), "white");
		map.put(rgb(Color.yellow), "yellow");
		return Collections.unmodifiableMap(map);
	}

}
