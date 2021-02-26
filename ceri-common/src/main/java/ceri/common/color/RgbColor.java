package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_RATIO;
import static ceri.common.color.ColorUtil.a;
import static ceri.common.color.ColorUtil.b;
import static ceri.common.color.ColorUtil.g;
import static ceri.common.color.ColorUtil.r;
import static ceri.common.color.ColorUtil.ratio;
import static ceri.common.color.ColorUtil.value;
import static ceri.common.validation.ValidationUtil.validateRangeFp;
import java.awt.Color;
import java.util.Objects;
import ceri.common.math.MathUtil;
import ceri.common.math.Matrix;

/**
 * Encapsulates an unscaled (0-1 inclusive) RGB color with alpha. Provides higher precision for
 * color calculations than (a)rgb int value. May encapsulate any conceptual rgb values, including
 * sRGB compressed and sRGB linear values.
 */
public class RgbColor {
	public static final RgbColor clear = RgbColor.of(0, 0, 0, 0);
	public static final RgbColor black = RgbColor.of(0, 0, 0);
	public static final RgbColor white = RgbColor.of(MAX_RATIO, MAX_RATIO, MAX_RATIO);
	public final double a; // alpha
	public final double r; // red
	public final double g; // green
	public final double b; // blue

	/**
	 * Construct from color int components. 
	 */
	public static RgbColor from(Color color) {
		return from(color.getRGB());
	}

	/**
	 * Construct an opaque instance from rgb int value. 
	 */
	public static RgbColor fromRgb(int rgb) {
		return from(r(rgb), g(rgb), b(rgb));
	}

	/**
	 * Construct from argb int value. 
	 */
	public static RgbColor from(int argb) {
		return from(a(argb), r(argb), g(argb), b(argb));
	}

	/**
	 * Construct an opaque instance from rgb 0-255 component values. 
	 */
	public static RgbColor from(int r, int g, int b) {
		return of(ratio(r), ratio(g), ratio(b));
	}

	/**
	 * Construct from argb 0-255 component values. 
	 */
	public static RgbColor from(int a, int r, int g, int b) {
		return of(ratio(a), ratio(r), ratio(g), ratio(b));
	}

	/**
	 * Construct an opaque instance from rgb 0-1 component values. 
	 */
	public static RgbColor of(double r, double g, double b) {
		return of(MAX_RATIO, r, g, b);
	}

	/**
	 * Construct from argb 0-1 component values. 
	 */
	public static RgbColor of(double a, double r, double g, double b) {
		return new RgbColor(a, r, g, b);
	}

	private RgbColor(double a, double r, double g, double b) {
		this.a = a;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	/**
	 * Provides r, g, b components as an array.
	 */
	public double[] rgbValues() {
		return new double[] { r, g, b };
	}

	/**
	 * Provides r, g, b components as a vector.
	 */
	public Matrix rgbVector() {
		return Matrix.vector(rgbValues());
	}

	/**
	 * Provides an argb int from all components.
	 */
	public int argb() {
		return ColorUtil.argb(value(a), value(r), value(g), value(b));
	}

	/**
	 * Provides a Color from all components.
	 */
	public Color color() {
		return ColorUtil.color(argb());
	}

	/**
	 * Dims rgb components by given ratio.
	 */
	public RgbColor dim(double ratio) {
		if (ratio == 1) return this;
		return of(a, r * ratio, g * ratio, b * ratio);
	}

	/**
	 * Returns true if not opaque.
	 */
	public boolean hasAlpha() {
		return a < MAX_RATIO;
	}

	/**
	 * Normalize components, limiting a, and scaling rgb so that max and min are 0-1.
	 */
	public RgbColor normalize() {
		double a = limit(this.a);
		double min = MathUtil.min(r, g, b, 0);
		double d = MathUtil.max(r, g, b, 1) - min;
		if (min == 0 && d == 1 && a == this.a) return this;
		return of(a, (this.r - min) / d, (this.g - min) / d, (this.b - min) / d);
	}

	/**
	 * Truncates components to range 0-1.
	 */
	public RgbColor limit() {
		double a = limit(this.a);
		double r = limit(this.r);
		double g = limit(this.g);
		double b = limit(this.b);
		if (a == this.a && r == this.r && g == this.g && b == this.b) return this;
		return of(a, r, g, b);
	}

	/**
	 * Validates all components are 0-1. Throws an exception if not.
	 */
	public void verify() {
		validate(a, "alpha");
		validate(r, "red");
		validate(g, "green");
		validate(b, "blue");
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, r, g, b);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof RgbColor)) return false;
		RgbColor other = (RgbColor) obj;
		if (!Objects.equals(a, other.a)) return false;
		if (!Objects.equals(r, other.r)) return false;
		if (!Objects.equals(g, other.g)) return false;
		if (!Objects.equals(b, other.b)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,r=%.5f,g=%.5f,b=%.5f)", a, r, g, b);
	}

	private void validate(double value, String name) {
		validateRangeFp(value, 0, MAX_RATIO, name);
	}

	private double limit(double value) {
		return MathUtil.limit(value, 0, MAX_RATIO);
	}
}
