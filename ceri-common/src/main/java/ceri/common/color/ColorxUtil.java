package ceri.common.color;

import static ceri.common.collection.StreamUtil.first;
import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.color.ColorUtil.CHANNEL_MAX;
import static ceri.common.color.ColorUtil.divide;
import static ceri.common.color.ColorUtil.scaleChannel;
import static ceri.common.color.ColorUtil.toRatio;
import static ceri.common.color.ColorUtil.tripleHexToRgb;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import ceri.common.color.ColorUtil.Fn.ChannelAdjuster;
import ceri.common.color.ColorUtil.Fn.ChannelScaler;
import ceri.common.data.ByteUtil;
import ceri.common.function.BinaryFunction;
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

	public static class Fn {

		private Fn() {}

		public static UnaryOperator<Colorx> dim(double scale) {
			return cx -> ColorxUtil.dim(cx, scale);
		}

		public static BinaryFunction<Colorx, List<Colorx>> fade(int steps) {
			return fade(steps, Biases.NONE);
		}

		public static BinaryFunction<Colorx, List<Colorx>> fade(int steps, Bias bias) {
			return (cx0, cx1) -> ColorxUtil.fade(cx0, cx1, steps, bias);
		}

		public static BinaryFunction<Colorx, List<Colorx>> fadeHsbx(int steps) {
			return fadeHsbx(steps, Biases.NONE);
		}

		public static BinaryFunction<Colorx, List<Colorx>> fadeHsbx(int steps, Bias bias) {
			return (cx0, cx1) -> ColorxUtil.fadeHsbx(cx0, cx1, steps, bias);
		}

		public static Function<Colorx, List<Colorx>> rotateHuex(int steps) {
			return rotateHuex(steps, Biases.NONE);
		}

		public static Function<Colorx, List<Colorx>> rotateHuex(int steps, Bias bias) {
			return cx -> ColorxUtil.rotateHuex(cx, steps, bias);
		}

		public static BinaryOperator<Colorx> scale(double ratio) {
			return (cx0, cx1) -> ColorxUtil.scale(cx0, cx1, ratio);
		}

		public static BinaryOperator<Colorx> scaleHsbx(double ratio) {
			return (cx0, cx1) -> ColorxUtil.scaleHsbx(cx0, cx1, ratio);
		}

		public static Function<Colorx, List<Colorx>> transform(Function<Color, List<Color>> rgbFn) {
			return cx -> applyRgb(cx, rgbFn);
		}

		public static Function<Colorx, List<Colorx>> transform(Function<Color, List<Color>> rgbFn,
			ChannelAdjuster xFn) {
			return cx -> applyRgb(cx, rgbFn, xFn);
		}

		public static BinaryFunction<Colorx, List<Colorx>>
			transform(BinaryFunction<Color, List<Color>> rgbFn) {
			return (cx0, cx1) -> applyRgb(cx0, cx1, rgbFn);
		}

		public static BinaryFunction<Colorx, List<Colorx>>
			transform(BinaryFunction<Color, List<Color>> rgbFn, Bias bias) {
			return (cx0, cx1) -> applyRgb(cx0, cx1, rgbFn, bias);
		}

		public static BinaryFunction<Colorx, List<Colorx>>
			transform(BinaryFunction<Color, List<Color>> rgbFn, ChannelScaler xFn) {
			return (cx0, cx1) -> applyRgb(cx0, cx1, rgbFn, xFn);
		}

		public static BinaryOperator<Colorx> transform(BinaryOperator<Color> rgbFn) {
			return (cx0, cx1) -> applyRgb(cx0, cx1, rgbFn);
		}

		public static BinaryOperator<Colorx> transform(BinaryOperator<Color> rgbFn,
			IntBinaryOperator xFn) {
			return (cx0, cx1) -> applyRgb(cx0, cx1, rgbFn, xFn);
		}

		public static UnaryOperator<Colorx> transform(UnaryOperator<Color> rgbFn) {
			return cx -> applyRgb(cx, rgbFn);
		}

		public static UnaryOperator<Colorx> transform(UnaryOperator<Color> rgbFn,
			IntUnaryOperator xFn) {
			return cx -> applyRgb(cx, rgbFn, xFn);
		}

		public static List<Colorx> applyRgb(Colorx colorx, Function<Color, List<Color>> rgbFn) {
			return applyRgb(colorx, rgbFn, ChannelAdjuster.none());
		}

		public static List<Colorx> applyRgb(Colorx colorx, Function<Color, List<Color>> rgbFn,
			ChannelAdjuster xFn) {
			if (colorx == null) return null;
			if (rgbFn == null) return Collections.emptyList();
			List<Color> colors = rgbFn.apply(colorx.rgb);
			List<Colorx> colorxs = new ArrayList<>();
			for (int i = 0; i < colors.size(); i++) {
				double d = (double) i / (colors.size() - 1);
				int x = xFn == null ? 0 : xFn.applyAsInt(colorx.x(), d);
				colorxs.add(Colorx.of(colors.get(i), x));
			}
			return colorxs;
		}

		public static List<Colorx> applyRgb(Colorx cx0, Colorx cx1,
			BinaryFunction<Color, List<Color>> rgbFn) {
			return applyRgb(cx0, cx1, rgbFn, Biases.NONE);
		}

		public static List<Colorx> applyRgb(Colorx cx0, Colorx cx1,
			BinaryFunction<Color, List<Color>> rgbFn, Bias bias) {
			return applyRgb(cx0, cx1, rgbFn, ChannelScaler.of(bias));
		}

		public static List<Colorx> applyRgb(Colorx cx0, Colorx cx1,
			BinaryFunction<Color, List<Color>> rgbFn, ChannelScaler xFn) {
			if (cx0 == null || cx1 == null) return null;
			if (rgbFn == null) return Collections.emptyList();
			List<Color> colors = rgbFn.apply(cx0.rgb, cx1.rgb);
			List<Colorx> colorxs = new ArrayList<>();
			for (int i = 0; i < colors.size(); i++) {
				double d = (double) i / (colors.size() - 1);
				int x = xFn == null ? 0 : xFn.applyAsInt(cx0.x(), cx1.x(), d);
				colorxs.add(Colorx.of(colors.get(i), x));
			}
			return colorxs;
		}

		public static Colorx applyRgb(Colorx cx0, Colorx cx1, BinaryOperator<Color> rgbFn) {
			return applyRgb(cx0, cx1, rgbFn, MathUtil::averageInt);
		}

		public static Colorx applyRgb(Colorx cx0, Colorx cx1, BinaryOperator<Color> rgbFn,
			IntBinaryOperator xFn) {
			if (cx0 == null) return cx1;
			if (cx1 == null || rgbFn == null) return cx0;
			return Colorx.of(rgbFn.apply(cx0.rgb, cx1.rgb), xFn.applyAsInt(cx0.x(), cx1.x()));
		}

		public static Colorx applyRgb(Colorx colorx, UnaryOperator<Color> rgbFn) {
			return applyRgb(colorx, rgbFn, x -> x);
		}

		public static Colorx applyRgb(Colorx colorx, UnaryOperator<Color> rgbFn,
			IntUnaryOperator xFn) {
			if (rgbFn == null || colorx == null) return colorx;
			return Colorx.of(rgbFn.apply(colorx.rgb), xFn.applyAsInt(colorx.x()));
		}

		public static List<Colorx> apply(Colorx colorx, Function<Colorx, List<Colorx>> rgbxFn) {
			if (colorx == null) return null;
			if (rgbxFn == null) return Collections.emptyList();
			return rgbxFn.apply(colorx);
		}

		public static List<Colorx> apply(Colorx cx0, Colorx cx1,
			BinaryFunction<Colorx, List<Colorx>> rgbxFn) {
			if (cx0 == null || cx1 == null) return null;
			if (rgbxFn == null) return Collections.emptyList();
			return rgbxFn.apply(cx0, cx1);
		}

		public static Colorx apply(Colorx cx0, Colorx cx1, BinaryOperator<Colorx> rgbxFn) {
			if (cx0 == null) return cx1;
			if (cx1 == null || rgbxFn == null) return cx0;
			return rgbxFn.apply(cx0, cx1);
		}

		public static Colorx apply(Colorx colorx, UnaryOperator<Colorx> rgbxFn) {
			if (rgbxFn == null || colorx == null) return colorx;
			return rgbxFn.apply(colorx);
		}
	}

	public static Colorx max(Colorx colorx) {
		return max(colorx.r(), colorx.g(), colorx.b(), colorx.x());
	}

	public static Colorx max(int r, int g, int b, int x) {
		double ratio = MathUtil.max(toRatio(r), toRatio(g), toRatio(b), toRatio(x));
		return Colorx.of(divide(r, ratio), divide(g, ratio), divide(b, ratio), divide(x, ratio));
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
		return Colorx.of(colorx.r(), colorx.g(), colorx.b(), colorx.x(), alpha);
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

	public static Colorx scale(int rgbxMin, int rgbxMax, double ratio) {
		return scale(Colorx.of(rgbxMin), Colorx.of(rgbxMax), ratio);
	}

	public static Colorx scaleHsbx(Colorx min, Colorx max, double ratio) {
		if (min == null) return max;
		if (max == null) return min;
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		Color rgb = ColorUtil.scaleHsb(min.rgb, max.rgb, ratio);
		int x = scaleChannel(min.x(), max.x(), ratio);
		return Colorx.of(rgb, x);
	}

	public static Colorx scale(Colorx min, Colorx max, double ratio) {
		if (ratio <= 0.0) return min;
		if (ratio >= 1.0) return max;
		int r = scaleChannel(min.r(), max.r(), ratio);
		int g = scaleChannel(min.g(), max.g(), ratio);
		int b = scaleChannel(min.b(), max.b(), ratio);
		int x = scaleChannel(min.x(), max.x(), ratio);
		int a = scaleChannel(min.a(), max.a(), ratio);
		return Colorx.of(r, g, b, x, a);
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
		List<Color> rgbs = ColorUtil.rotateHue(colorx.rgb, steps, bias);
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
		return (int) (ByteUtil.shiftByteLeft(r, 3) | ByteUtil.shiftByteLeft(g, 2) |
			ByteUtil.shiftByteLeft(b, 1) | x & CHANNEL_MAX);
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
		int max = CHANNEL_MAX + 1;
		return Colorx.of(rnd.nextInt(max), rnd.nextInt(max), rnd.nextInt(max), rnd.nextInt(max));
	}

	private static Map<Integer, String> colorMap() {
		Map<Integer, String> map = new HashMap<>();
		map.put(Colorx.black.rgbx(), "black");
		map.put(Colorx.full.rgbx(), "full");
		return Collections.unmodifiableMap(map);
	}
}
