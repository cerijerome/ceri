package ceri.common.color;

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

/**
 * Encapsulates A+RGB color with values 0-1 inclusive.
 */
public class RgbColor implements ComponentColor<RgbColor> {
	public static final double MAX_VALUE = 1.0;
	public static final RgbColor clear = RgbColor.of(0, 0, 0, 0);
	public static final RgbColor black = RgbColor.of(0, 0, 0);
	public static final RgbColor white = RgbColor.of(MAX_VALUE, MAX_VALUE, MAX_VALUE);
	public final double a; // alpha
	public final double r; // red
	public final double g; // green
	public final double b; // blue

	public static RgbColor from(Color color) {
		return from(color.getRGB());
	}

	public static RgbColor fromRgb(int rgb) {
		return from(r(rgb), g(rgb), b(rgb));
	}

	public static RgbColor from(int argb) {
		return from(a(argb), r(argb), g(argb), b(argb));
	}

	public static RgbColor from(int r, int g, int b) {
		return of(ratio(r), ratio(g), ratio(b));
	}

	public static RgbColor from(int a, int r, int g, int b) {
		return of(ratio(a), ratio(r), ratio(g), ratio(b));
	}

	public static RgbColor of(double r, double g, double b) {
		return of(MAX_VALUE, r, g, b);
	}

	public static RgbColor of(double alpha, double red, double green, double blue) {
		return new RgbColor(alpha, red, green, blue);
	}

	private RgbColor(double alpha, double red, double green, double blue) {
		this.r = red;
		this.g = green;
		this.b = blue;
		this.a = alpha;
	}

	public int argb() {
		return ColorUtil.argb(value(a), value(r), value(g), value(b));
	}

	public Color color() {
		return ColorUtil.color(argb());
	}

	public RgbColor dim(double ratio) {
		if (ratio == 1) return this;
		return of(a, r * ratio, g * ratio, b * ratio);
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
		double a = limit(this.a);
		double r = (this.r - min) / divisor;
		double g = (this.g - min) / divisor;
		double b = (this.b - min) / divisor;
		if (a == this.a && r == this.r && g == this.g && b == this.b) return this;
		return of(a, r, g, b);
	}

	@Override
	public RgbColor limit() {
		double a = limit(this.a);
		double r = limit(this.r);
		double g = limit(this.g);
		double b = limit(this.b);
		if (a == this.a && r == this.r && g == this.g && b == this.b) return this;
		return of(a, r, g, b);
	}

	@Override
	public void verify() {
		validate(a, "alpha");
		validate(r, "red");
		validate(g, "green");
		validate(b, "blue");
	}

	private void validate(double value, String name) {
		validateRangeFp(value, 0, MAX_VALUE, name);
	}

	private double limit(double value) {
		return MathUtil.limit(value, 0, MAX_VALUE);
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

}
