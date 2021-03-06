package ceri.common.color;

import static ceri.common.color.ColorUtil.argbs;
import java.awt.Color;
import java.util.Arrays;
import java.util.Objects;

/**
 * Encapsulates an argb color with additional x (0-3) components. Commonly used with led strips.
 * Single-x includes rgbw (white), rgbww (warm white) and rgba (amber). Multi-x includes rgbcct
 * (white + warm white) and rgbacct (amber + white + warm white).
 */
public class Colorx {
	public static final Colorx clear = of(0L);
	public static final Colorx black = of(0xff000000L);
	public static final Colorx fullX0 = of(0xffffffffffL); // ma rgb, x0
	public static final Colorx fullX01 = of(0xffffffffffffL); // max rgb, x0, x1
	public static final Colorx fullX012 = of(0xffffffffffffffL); // max rgb, x0, x1, x2
	public static final Colorx full = of(-1L); // max rgb, all x
	public final long xargb;

	/**
	 * Construct by extracting x-color components from argb.
	 */
	public static Colorx from(Color argb, Color... xcolors) {
		return from(argb.getRGB(), argbs(xcolors));
	}

	/**
	 * Construct by extracting x-color components from argb.
	 */
	public static Colorx from(int argb, int... xrgbs) {
		return of(ColorxUtil.denormalizeXargb(argb, xrgbs));
	}

	/**
	 * Construct from color and x components.
	 */
	public static Colorx of(Color color, int... xs) {
		return of(color.getRGB(), xs);
	}

	/**
	 * Construct from argb int and x components.
	 */
	public static Colorx of(int argb, int... xs) {
		return of(ColorxUtil.xargb(argb, xs));
	}

	/**
	 * Construct from xargb long.
	 */
	public static Colorx of(long xargb) {
		return new Colorx(xargb);
	}

	private Colorx(long xargb) {
		this.xargb = xargb;
	}

	/**
	 * Extract component.
	 */
	public int a() {
		return ColorxUtil.a(xargb);
	}

	/**
	 * Extract component.
	 */
	public int r() {
		return ColorxUtil.r(xargb);
	}

	/**
	 * Extract component.
	 */
	public int g() {
		return ColorxUtil.g(xargb);
	}

	/**
	 * Extract component.
	 */
	public int b() {
		return ColorxUtil.b(xargb);
	}

	/**
	 * Extract x[i] component.
	 */
	public int x(int i) {
		return ColorxUtil.x(xargb, i);
	}

	/**
	 * Extract all x components.
	 */
	public int[] xs() {
		return ColorxUtil.xs(xargb);
	}

	/**
	 * Extract rgb int. X and alpha values are dropped.
	 */
	public int rgb() {
		return ColorUtil.rgb(argb());
	}

	/**
	 * Extract argb int. X values are dropped.
	 */
	public int argb() {
		return (int) xargb;
	}

	/**
	 * Extract argb color. X values are dropped.
	 */
	public Color color() {
		return ColorUtil.color(argb());
	}

	/**
	 * Normalize x values using given colors.
	 */
	public Color normalize(Color... xrgbs) {
		if (xrgbs.length == 0) return color();
		return ColorUtil.color(normalizeArgb(argbs(xrgbs)));
	}

	/**
	 * Normalize x values using given colors.
	 */
	public int normalizeArgb(int... xrgbs) {
		if (xrgbs.length == 0) return argb();
		return ColorxUtil.normalizeArgb(xargb, xrgbs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(xargb);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Colorx)) return false;
		Colorx other = (Colorx) obj;
		if (xargb != other.xargb) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s(a=%d,r=%d,g=%d,b=%d,x=%s)", getClass().getSimpleName(), a(), r(),
			g(), b(), Arrays.toString(xs()));
	}
}
