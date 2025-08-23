package ceri.common.color;

import static ceri.common.color.Colors.argbs;
import static ceri.common.color.Component.x0;
import static ceri.common.color.Component.x1;
import static ceri.common.color.Component.x2;
import static ceri.common.color.Component.x3;
import java.awt.Color;
import java.util.Arrays;

/**
 * Encapsulates an argb color with additional x (0-3) components. Commonly used with led strips.
 * Single-x includes rgbw (white), rgbww (warm white) and rgba (amber). Multi-x includes rgbcct
 * (white + warm white) and rgbacct (amber + white + warm white).
 */
public record Colorx(long xargb) {
	public static final Colorx clear = of(0L);
	public static final Colorx black = of(0xff000000L);
	public static final Colorx white = of(0xffffffffL);
	public static final Colorx fullX0 = of(0xffffffffffL); // max rgb, x0
	public static final Colorx fullX01 = of(0xffffffffffffL); // max rgb, x0, x1
	public static final Colorx fullX012 = of(0xffffffffffffffL); // max rgb, x0, x1, x2
	public static final Colorx full = of(-1L); // max rgb, all x

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
		return of(Colorxs.denormalizeXargb(argb, xrgbs));
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
		return of(Colorxs.xargb(argb, xs));
	}

	/**
	 * Construct from xargb long.
	 */
	public static Colorx of(long xargb) {
		return new Colorx(xargb);
	}

	/**
	 * Determine if any x component is set.
	 */
	public boolean hasX() {
		return Colorxs.hasX(xargb);
	}

	/**
	 * Extract component.
	 */
	public int a() {
		return Colorxs.a(xargb);
	}

	/**
	 * Return colorx with component value set.
	 */
	public Colorx a(int a) {
		return a() == a ? this : of(Colorxs.a(xargb, a));
	}

	/**
	 * Extract component.
	 */
	public int r() {
		return Colorxs.r(xargb);
	}

	/**
	 * Return colorx with component value set.
	 */
	public Colorx r(int r) {
		return r() == r ? this : of(Colorxs.r(xargb, r));
	}

	/**
	 * Extract component.
	 */
	public int g() {
		return Colorxs.g(xargb);
	}

	/**
	 * Return colorx with component value set.
	 */
	public Colorx g(int g) {
		return g() == g ? this : of(Colorxs.g(xargb, g));
	}

	/**
	 * Extract component.
	 */
	public int b() {
		return Colorxs.b(xargb);
	}

	/**
	 * Return colorx with component value set.
	 */
	public Colorx b(int b) {
		return b() == b ? this : of(Colorxs.b(xargb, b));
	}

	/**
	 * Extract x[i] component.
	 */
	public int x(int i) {
		return Colorxs.x(xargb, i);
	}

	/**
	 * Return colorx with component value set.
	 */
	public Colorx x(int i, int x) {
		return x(i) == x ? this : of(Colorxs.x(xargb, i, x));
	}

	/**
	 * Extract all x components.
	 */
	public int[] xs() {
		return Component.getAll(xargb, x0, x1, x2, x3);
	}

	/**
	 * Extract rgb int. X and alpha values are dropped.
	 */
	public int rgb() {
		return Colors.rgb(argb());
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
		return Colors.color(argb());
	}

	/**
	 * Normalize x values using given colors.
	 */
	public Color normalize(Color... xrgbs) {
		if (xrgbs.length == 0) return color();
		return Colors.color(normalizeArgb(argbs(xrgbs)));
	}

	/**
	 * Normalize x values using given colors.
	 */
	public int normalizeArgb(int... xrgbs) {
		if (xrgbs.length == 0) return argb();
		return Colorxs.normalizeArgb(xargb, xrgbs);
	}

	/**
	 * Flattens the color by applying alpha channel on opaque black.
	 */
	public Colorx flatten() {
		long xargb = Colorxs.flattenXargb(this.xargb);
		return this.xargb == xargb ? this : of(xargb);
	}

	@Override
	public String toString() {
		return String.format("%s(#%x: a=%d,r=%d,g=%d,b=%d,x=%s)", getClass().getSimpleName(), xargb,
			a(), r(), g(), b(), Arrays.toString(xs()));
	}
}
