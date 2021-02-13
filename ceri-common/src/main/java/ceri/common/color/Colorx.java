package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_VALUE;
import static ceri.common.math.MathUtil.ubyte;
import java.awt.Color;
import java.util.Objects;
import ceri.common.math.MathUtil;

/**
 * Encapsulates an argb color with additional x component. Commonly used with led strips, such as
 * rgbw, rgbww.
 */
public class Colorx {
	private static final long MASK = 0xffffffffffL;
	private static final long A_MASK = 0xff00000000L;
	private static final int RGB_MASK = 0xffffff;
	private static final long A_SHIFT = 32L;
	private static final long R_SHIFT = 24L;
	private static final long G_SHIFT = 16L;
	private static final long B_SHIFT = 8L;
	public static final Colorx clear = of(0L);
	public static final Colorx black = of(A_MASK);
	public static final Colorx full = of(MASK);
	private final long argbx;

	/**
	 * Creates Colorx by extracting given x-color component from rgb.
	 */
	public static Colorx from(Color argb, Color x) {
		return from(argb.getRed(), argb.getGreen(), argb.getBlue(), argb.getAlpha(), x);
	}

	/**
	 * Creates Colorx by extracting given x-color component from rgb color.
	 */
	public static Colorx from(int r, int g, int b, Color xColor) {
		return from(MAX_VALUE, r, g, b, xColor);
	}

	/**
	 * Creates Colorx by extracting given x-color component from rgb color.
	 */
	public static Colorx from(int a, int r, int g, int b, Color x) {
		double xRatio = xRatio(r, g, b, x);
		int r0 = r - (int) (xRatio * x.getRed());
		int g0 = g - (int) (xRatio * x.getGreen());
		int b0 = b - (int) (xRatio * x.getBlue());
		int x0 = (int) (xRatio * MAX_VALUE);
		return of(a, r0, g0, b0, x0);
	}

	public static Colorx of(int a, int r, int g, int b, int x) {
		return of(ubyte(a) << A_SHIFT | ubyte(r) << R_SHIFT | ubyte(g) << G_SHIFT |
			ubyte(b) << B_SHIFT | ubyte(x));
	}

	public static Colorx of(int r, int g, int b, int x) {
		return of(MAX_VALUE, r, g, b, x);
	}

	public static Colorx of(Color argb, int x) {
		return of(argb.getRGB(), x);
	}

	public static Colorx of(int argb, int x) {
		return of(argb << B_SHIFT | ubyte(x));
	}

	public static Colorx of(int rgbx) {
		return of(A_MASK | rgbx);
	}

	public static Colorx of(long argbx) {
		return new Colorx(argbx & MASK);
	}

	private Colorx(long argbx) {
		this.argbx = argbx;
	}

	public int a() {
		return ubyte(argbx >>> A_SHIFT);
	}

	public int r() {
		return ubyte(argbx >>> R_SHIFT);
	}

	public int g() {
		return ubyte(argbx >>> G_SHIFT);
	}

	public int b() {
		return ubyte(argbx >>> B_SHIFT);
	}

	public int x() {
		return ubyte(argbx);
	}

	public int rgb() {
		return argb() & RGB_MASK;
	}

	public int argb() {
		return (int) argbx >>> B_SHIFT;
	}

	public int rgbx() {
		return (int) argbx;
	}

	public long argbx() {
		return argbx;
	}

	public Color color() {
		return ColorUtil.color(argb());
	}

	public Color normalizeFor(Color xColor) {
		if (xColor == null) return color();
		double xRatio = xRatio(x(), MAX_VALUE);
		int r = r() + (int) (xRatio * xColor.getRed());
		int g = g() + (int) (xRatio * xColor.getGreen());
		int b = b() + (int) (xRatio * xColor.getBlue());
		return RgbColor.from(r, g, b).normalize().color();
	}

	@Override
	public int hashCode() {
		return Objects.hash(argbx);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Colorx)) return false;
		Colorx other = (Colorx) obj;
		if (argbx != other.argbx) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Colorx(a=%d,r=%d,g=%d,b=%d,x=%d)", a(), r(), g(), b(), x());
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
