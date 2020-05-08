package ceri.common.color;

import static ceri.common.color.ColorUtil.b;
import static ceri.common.color.ColorUtil.fromRatio;
import static ceri.common.color.ColorUtil.g;
import static ceri.common.color.ColorUtil.r;
import static ceri.common.color.ColorUtil.toRatio;
import static ceri.common.validation.ValidationUtil.validateRangeD;
import java.awt.Color;
import ceri.common.math.MathUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Encapsulates RGBA color with values 0-1.
 */
public class RgbColor implements ComponentColor<RgbColor> {
	public static final double MAX_VALUE = 1.0;
	public static final RgbColor BLACK = RgbColor.of(0, 0, 0);
	public static final RgbColor WHITE = RgbColor.of(MAX_VALUE, MAX_VALUE, MAX_VALUE);
	private static final int MAX_COLOR_VALUE = 255;
	public final double r; // red
	public final double g; // green
	public final double b; // blue
	public final double a; // alpha

	public static RgbColor from(Color color) {
		return from(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	public static RgbColor from(int rgb) {
		return from(r(rgb), g(rgb), b(rgb));
	}

	public static RgbColor from(int red, int green, int blue) {
		return from(red, green, blue, MAX_COLOR_VALUE);
	}

	public static RgbColor from(int red, int green, int blue, int alpha) {
		return of(toRatio(red), toRatio(green), toRatio(blue), toRatio(alpha));
	}

	public static Color toColor(double red, double green, double blue) {
		return toColor(red, green, blue, MAX_VALUE);
	}

	public static Color toColor(double red, double green, double blue, double alpha) {
		return of(red, green, blue, alpha).asColor();
	}

	public static RgbColor of(double red, double green, double blue) {
		return of(red, green, blue, MAX_VALUE);
	}

	public static RgbColor of(double red, double green, double blue, double alpha) {
		return new RgbColor(red, green, blue, alpha);
	}

	private RgbColor(double red, double green, double blue, double alpha) {
		this.r = red;
		this.g = green;
		this.b = blue;
		this.a = alpha;
	}

	public Color asColor() {
		return new Color(fromRatio(r), fromRatio(g), fromRatio(b), fromRatio(a));
	}

	public RgbColor dim(double ratio) {
		if (ratio == 1) return this;
		return of(r * ratio, g * ratio, b * ratio, a);
	}

	@Override
	public boolean hasAlpha() {
		return a < MAX_VALUE;
	}

	@Override
	public RgbColor normalize() {
		double min = MathUtil.min(r, g, b, 0);
		double max = MathUtil.max(r, g, b, 0);
		double divisor = Math.max(max - min, 1);
		double r = (this.r - min) / divisor;
		double g = (this.g - min) / divisor;
		double b = (this.b - min) / divisor;
		double a = limit(this.a);
		if (r == this.r && g == this.g && b == this.b && a == this.a) return this;
		return of(r, g, b, a);
	}

	@Override
	public RgbColor limit() {
		double r = limit(this.r);
		double g = limit(this.g);
		double b = limit(this.b);
		double a = limit(this.a);
		if (r == this.r && g == this.g && b == this.b && a == this.a) return this;
		return of(r, g, b, a);
	}

	@Override
	public void verify() {
		validate(r, "red");
		validate(g, "green");
		validate(b, "blue");
		validate(a, "alpha");
	}

	private void validate(double value, String name) {
		validateRangeD(value, 0, MAX_VALUE, name);
	}

	private double limit(double value) {
		return MathUtil.limit(value, 0, MAX_VALUE);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(r, g, b, a);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof RgbColor)) return false;
		RgbColor other = (RgbColor) obj;
		if (!EqualsUtil.equals(r, other.r)) return false;
		if (!EqualsUtil.equals(g, other.g)) return false;
		if (!EqualsUtil.equals(b, other.b)) return false;
		if (!EqualsUtil.equals(a, other.a)) return false;
		return true;
	}

	@Override
	public String toString() {
		return hasAlpha() ? String.format("(r=%.5f,g=%.5f,b=%.5f,a=%.5f)", r, g, b, a) :
			String.format("(r=%.5f,g=%.5f,b=%.5f)", r, g, b);
	}

}
