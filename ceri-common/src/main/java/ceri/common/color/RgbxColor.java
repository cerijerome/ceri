package ceri.common.color;

import static ceri.common.color.ColorUtil.fromRatio;
import static ceri.common.color.ColorUtil.toRatio;
import static ceri.common.validation.ValidationUtil.validateRangeD;
import java.awt.Color;
import ceri.common.math.MathUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Encapsulates RGB(x)A color with values 0-1.
 */
public class RgbxColor implements ComponentColor<RgbxColor> {
	public static final double MAX_VALUE = RgbColor.MAX_VALUE;
	public static final RgbxColor BLACK = RgbxColor.of(0, 0, 0, 0);
	public static final RgbxColor FULL = RgbxColor.of(MAX_VALUE, MAX_VALUE, MAX_VALUE, MAX_VALUE);
	private static final int MAX_COLOR_VALUE = 255;
	public final double r; // red
	public final double g; // green
	public final double b; // blue
	public final double x; // x
	public final double a; // alpha

	public static RgbxColor from(Colorx colorx) {
		return from(colorx.rgb, colorx.x());
	}

	public static RgbxColor from(Color color, int x) {
		return from(color.getRed(), color.getGreen(), color.getBlue(), x, color.getAlpha());
	}

	public static RgbxColor from(int red, int green, int blue, int x) {
		return from(red, green, blue, x, MAX_COLOR_VALUE);
	}

	public static RgbxColor from(int red, int green, int blue, int x, int alpha) {
		return of(toRatio(red), toRatio(green), toRatio(blue), toRatio(x), toRatio(alpha));
	}

	public static Colorx toColorx(double red, double green, double blue, double x) {
		return toColorx(red, green, blue, x, MAX_VALUE);
	}

	public static Colorx toColorx(double red, double green, double blue, double x, double alpha) {
		return of(red, green, blue, x, alpha).asColorx();
	}

	public static RgbxColor of(double red, double green, double blue, double x) {
		return of(red, green, blue, x, MAX_VALUE);
	}

	public static RgbxColor of(double red, double green, double blue, double x, double alpha) {
		return new RgbxColor(red, green, blue, x, alpha);
	}

	private RgbxColor(double red, double green, double blue, double x, double alpha) {
		this.r = red;
		this.g = green;
		this.b = blue;
		this.x = x;
		this.a = alpha;
	}

	public Colorx asColorx() {
		return Colorx.of(fromRatio(r), fromRatio(g), fromRatio(b), fromRatio(x), fromRatio(a));
	}

	public RgbxColor dim(double ratio) {
		if (ratio == 1) return this;
		return of(r * ratio, g * ratio, b * ratio, x * ratio, a);
	}

	@Override
	public boolean hasAlpha() {
		return a < MAX_VALUE;
	}

	@Override
	public RgbxColor normalize() {
		double min = MathUtil.min(r, g, b, x, 0);
		double max = MathUtil.max(r, g, b, x, 0);
		double divisor = Math.max(max - min, 1);
		double r = (this.r - min) / divisor;
		double g = (this.g - min) / divisor;
		double b = (this.b - min) / divisor;
		double x = (this.x - min) / divisor;
		double a = limit(this.a);
		if (r == this.r && g == this.g && b == this.b && x == this.x && a == this.a) return this;
		return of(r, g, b, x, a);
	}

	@Override
	public RgbxColor limit() {
		double r = limit(this.r);
		double g = limit(this.g);
		double b = limit(this.b);
		double x = limit(this.x);
		double a = limit(this.a);
		if (r == this.r && g == this.g && b == this.b && x == this.x && a == this.a) return this;
		return of(r, g, b, x, a);
	}

	@Override
	public void verify() {
		validate(r, "red");
		validate(g, "green");
		validate(b, "blue");
		validate(x, "x");
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
		return HashCoder.hash(r, g, b, x, a);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof RgbxColor)) return false;
		RgbxColor other = (RgbxColor) obj;
		if (!EqualsUtil.equals(r, other.r)) return false;
		if (!EqualsUtil.equals(g, other.g)) return false;
		if (!EqualsUtil.equals(b, other.b)) return false;
		if (!EqualsUtil.equals(x, other.x)) return false;
		if (!EqualsUtil.equals(a, other.a)) return false;
		return true;
	}

	@Override
	public String toString() {
		return hasAlpha() ? String.format("(r=%.5f,g=%.5f,b=%.5f,x=%.5f,a=%.5f)", r, g, b, x, a) :
			String.format("(r=%.5f,g=%.5f,b=%.5f,x=%.5f)", r, g, b, x);
	}

}
