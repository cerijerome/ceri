package ceri.common.color;

import static ceri.common.color.ColorUtil.CHANNEL_MAX;
import java.awt.Color;
import java.util.Objects;
import ceri.common.data.ByteUtil;
import ceri.common.math.MathUtil;

/**
 * Encapsulates an rgb color with additional x component. Commonly used with led strips, such as
 * rgbw, rgbww.
 */
public class Colorx {
	public static final Colorx black = of(0, 0, 0, 0);
	public static final Colorx full = of(CHANNEL_MAX, CHANNEL_MAX, CHANNEL_MAX, CHANNEL_MAX);
	public final Color rgb;
	private final int x;

	/**
	 * Creates Colorx by extracting given x-color component from rgb.
	 */
	public static Colorx from(Color rgb, Color xColor) {
		return from(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), rgb.getAlpha(), xColor);
	}

	/**
	 * Creates Colorx by extracting given x-color component from rgb.
	 */
	public static Colorx from(int r, int g, int b, Color xColor) {
		return from(r, g, b, CHANNEL_MAX, xColor);
	}

	/**
	 * Creates Colorx by extracting given x-color component from rgb.
	 */
	public static Colorx from(int r, int g, int b, int a, Color xColor) {
		double xRatio = xRatio(r, g, b, xColor);
		int r0 = r - (int) (xRatio * xColor.getRed());
		int g0 = g - (int) (xRatio * xColor.getGreen());
		int b0 = b - (int) (xRatio * xColor.getBlue());
		int x = (int) (xRatio * CHANNEL_MAX);
		return of(r0, g0, b0, x, a);
	}

	public static Colorx of(int rgbx) {
		return of(new Color(ByteUtil.shift(rgbx, 1)), rgbx);
	}

	public static Colorx of(int rgb, int x) {
		return of(new Color(rgb), x);
	}

	public static Colorx of(int r, int g, int b, int x) {
		return of(new Color(r, g, b), x);
	}

	public static Colorx of(int r, int g, int b, int x, int a) {
		return of(new Color(r, g, b, a), x);
	}

	public static Colorx of(Color rgb, int x) {
		return new Colorx(rgb, x);
	}

	private Colorx(Color rgb, int x) {
		this.rgb = rgb;
		this.x = x & CHANNEL_MAX;
	}

	public int r() {
		return rgb.getRed();
	}

	public int g() {
		return rgb.getGreen();
	}

	public int b() {
		return rgb.getBlue();
	}

	public int x() {
		return x;
	}

	public int a() {
		return rgb.getAlpha();
	}

	public int rgbx() {
		return ByteUtil.shift(rgb.getRGB(), -1) | x();
	}

	public Color normalizeFor(Color xColor) {
		if (xColor == null) return rgb;
		double xRatio = (double) x / CHANNEL_MAX;
		int r = r() + (int) (xRatio * xColor.getRed());
		int g = g() + (int) (xRatio * xColor.getGreen());
		int b = b() + (int) (xRatio * xColor.getBlue());
		return RgbColor.from(r, g, b).normalize().asColor();
	}

	@Override
	public int hashCode() {
		return Objects.hash(rgb, x);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Colorx)) return false;
		Colorx other = (Colorx) obj;
		if (!Objects.equals(rgb, other.rgb)) return false;
		if (x != other.x) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Colorx(r=%d, g=%d, b=%d, x=%d, a=%d)", r(), g(), b(), x(), a());
	}

	private static double xRatio(int r, int g, int b, Color xColor) {
		return MathUtil.min(1, xRatio(r, xColor.getRed()), xRatio(g, xColor.getGreen()),
			xRatio(b, xColor.getBlue()));
	}

	private static double xRatio(int value, int xValue) {
		if (xValue == 0) return 1.0;
		if (value == 0) return 0.0;
		return (double) value / xValue;
	}

}
