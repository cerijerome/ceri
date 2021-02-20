package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_RATIO;
import static ceri.common.color.ColorUtil.MAX_VALUE;
import static ceri.common.math.MathUtil.ubyte;
import java.awt.Color;
import java.util.Objects;
import ceri.common.math.MathUtil;

/**
 * Encapsulates an argb color with additional x component. Commonly used with led strips, such as
 * rgbw (white), rgbww (warm white), rgba (amber).
 */
public class Colorx {
	private static final long MASK = 0xffffffffffL;
	private static final long A_MASK = 0xff00000000L;
	private static final long A_SHIFT = 32L;
	private static final long R_SHIFT = 24L;
	private static final long G_SHIFT = 16L;
	private static final long B_SHIFT = 8L;
	public static final Colorx clear = of(0L);
	public static final Colorx black = of(A_MASK);
	public static final Colorx full = of(MASK);
	private final long argbx;

	/**
	 * Creates Colorx by extracting given x-color component from argb.
	 */
	public static Colorx from(Color argb, Color x) {
		return from(argb.getRGB(), x.getRGB());
	}

	/**
	 * Creates Colorx by extracting given x-color component from rgb.
	 */
	public static Colorx from(int argb, int xRgb) {
		return from(ColorUtil.a(argb), ColorUtil.r(argb), ColorUtil.g(argb), ColorUtil.b(argb),
			xRgb);
	}

	/**
	 * Creates Colorx by extracting given x-color component from rgb color.
	 */
	public static Colorx from(int r, int g, int b, Color x) {
		return from(MAX_VALUE, r, g, b, x);
	}

	/**
	 * Creates Colorx by extracting given x-color component from rgb color.
	 */
	public static Colorx from(int a, int r, int g, int b, Color x) {
		return from(a, r, g, b, x.getRGB());
	}

	/**
	 * Creates Colorx by extracting given x-rgb components from rgb color.
	 */
	public static Colorx from(int a, int r, int g, int b, int xRgb) {
		double xRatio = xRatio(r, g, b, xRgb);
		int r0 = r - (int) (xRatio * ColorUtil.r(xRgb));
		int g0 = g - (int) (xRatio * ColorUtil.g(xRgb));
		int b0 = b - (int) (xRatio * ColorUtil.b(xRgb));
		int x0 = (int) (xRatio * MAX_VALUE);
		return of(a, r0, g0, b0, x0);
	}

	/**
	 * Constructs a Colorx from components.
	 */
	public static Colorx of(int a, int r, int g, int b, int x) {
		return of(ubyte(a) << A_SHIFT | ubyte(r) << R_SHIFT | ubyte(g) << G_SHIFT |
			ubyte(b) << B_SHIFT | ubyte(x));
	}

	/**
	 * Constructs an opaque Colorx from components.
	 */
	public static Colorx of(int r, int g, int b, int x) {
		return of(MAX_VALUE, r, g, b, x);
	}

	/**
	 * Constructs a Colorx from color and x component.
	 */
	public static Colorx of(Color color, int x) {
		return of(color.getRGB(), x);
	}

	/**
	 * Constructs a Colorx from argb int and x component.
	 */
	public static Colorx of(int argb, int x) {
		return of(argb << B_SHIFT | ubyte(x));
	}

	/**
	 * Constructs an opaque Colorx from rgbx int.
	 */
	public static Colorx of(int rgbx) {
		return of(A_MASK | rgbx);
	}

	/**
	 * Constructs an Colorx from argbx long.
	 */
	public static Colorx of(long argbx) {
		return new Colorx(argbx & MASK);
	}

	private Colorx(long argbx) {
		this.argbx = argbx;
	}

	/**
	 * Extract component.
	 */
	public int a() {
		return ubyte(argbx >>> A_SHIFT);
	}

	/**
	 * Extract component.
	 */
	public int r() {
		return ubyte(argbx >>> R_SHIFT);
	}

	/**
	 * Extract component.
	 */
	public int g() {
		return ubyte(argbx >>> G_SHIFT);
	}

	/**
	 * Extract component.
	 */
	public int b() {
		return ubyte(argbx >>> B_SHIFT);
	}

	/**
	 * Extract component.
	 */
	public int x() {
		return ubyte(argbx);
	}

	/**
	 * Extract rgb int.
	 */
	public int rgb() {
		return ColorUtil.rgb(argb());
	}

	/**
	 * Extract argb int.
	 */
	public int argb() {
		return (int) argbx >>> B_SHIFT;
	}

	/**
	 * Extract rgbx int.
	 */
	public int rgbx() {
		return (int) argbx;
	}

	/**
	 * Provide full argbx long.
	 */
	public long argbx() {
		return argbx;
	}

	/**
	 * Extract argb color.
	 */
	public Color color() {
		return ColorUtil.color(argb());
	}

	/**
	 * Return an argb color normalized again the given x color.
	 */
	public Color normalizeFor(Color x) {
		if (x == null) return color();
		return ColorUtil.color(ColorxUtil.normalizeArgb(argbx, x.getRGB()));
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
		return String.format("%s(a=%d,r=%d,g=%d,b=%d,x=%d)", getClass().getSimpleName(), a(), r(),
			g(), b(), x());
	}

	private static double xRatio(int r, int g, int b, int xRgb) {
		return MathUtil.min(MAX_RATIO, xRatio(r, ColorUtil.r(xRgb)), xRatio(g, ColorUtil.g(xRgb)),
			xRatio(b, ColorUtil.b(xRgb)));
	}

	private static double xRatio(int value, int xValue) {
		if (xValue == 0) return MAX_RATIO;
		return (double) value / xValue;
	}

}
