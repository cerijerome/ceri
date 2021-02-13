package ceri.common.color;

import static ceri.common.color.ColorUtil.ratio;
import static ceri.common.color.ColorUtil.value;
import static ceri.common.validation.ValidationUtil.validateRangeFp;
import java.awt.Color;
import java.util.Objects;
import ceri.common.math.MathUtil;

/**
 * Encapsulates ARGB(x) color with values 0-1.
 */
public class RgbxColor implements ComponentColor<RgbxColor> {
	public static final double MAX_VALUE = 1.0;
	public static final RgbxColor clear = RgbxColor.of(0, 0, 0, 0, 0);
	public static final RgbxColor black = RgbxColor.of(0, 0, 0, 0);
	public static final RgbxColor full = RgbxColor.of(MAX_VALUE, MAX_VALUE, MAX_VALUE, MAX_VALUE);
	private static final long A_MASK = 0xff00000000L;
	public final double a; // alpha
	public final double r; // red
	public final double g; // green
	public final double b; // blue
	public final double x; // x

	public static RgbxColor from(Colorx colorx) {
		return from(colorx.a(), colorx.r(), colorx.g(), colorx.b(), colorx.x());
	}

	public static RgbxColor from(Color color, int x) {
		return from(Colorx.of(color, x));
	}

	public static RgbxColor from(int rgbx) {
		return from(A_MASK | rgbx);
	}

	public static RgbxColor from(long argbx) {
		return from(Colorx.of(argbx));
	}

	private static RgbxColor from(int a, int r, int g, int b, int x) {
		return of(ratio(a), ratio(r), ratio(g), ratio(b), ratio(x));
	}

	public static RgbxColor of(double r, double g, double b, double x) {
		return of(MAX_VALUE, r, g, b, x);
	}

	public static RgbxColor of(double a, double r, double g, double b, double x) {
		return new RgbxColor(a, r, g, b, x);
	}

	private RgbxColor(double a, double r, double g, double b, double x) {
		this.a = a;
		this.r = r;
		this.g = g;
		this.b = b;
		this.x = x;
	}

	public Colorx colorx() {
		return Colorx.of(value(a), value(r), value(g), value(b), value(x));
	}

	public RgbxColor dim(double ratio) {
		if (ratio == MAX_VALUE) return this;
		return of(a, r * ratio, g * ratio, b * ratio, x * ratio);
	}

	@Override
	public boolean hasAlpha() {
		return a < MAX_VALUE;
	}

	@Override
	public RgbxColor normalize() {
		double min = MathUtil.min(r, g, b, x, 0);
		double max = MathUtil.max(r, g, b, x, 0);
		double divisor = Math.max(max - min, MAX_VALUE);
		double a = limit(this.a);
		double r = (this.r - min) / divisor;
		double g = (this.g - min) / divisor;
		double b = (this.b - min) / divisor;
		double x = (this.x - min) / divisor;
		if (a == this.a && r == this.r && g == this.g && b == this.b && x == this.x) return this;
		return of(a, r, g, b, x);
	}

	@Override
	public RgbxColor limit() {
		double a = limit(this.a);
		double r = limit(this.r);
		double g = limit(this.g);
		double b = limit(this.b);
		double x = limit(this.x);
		if (a == this.a && r == this.r && g == this.g && b == this.b && x == this.x) return this;
		return of(a, r, g, b, x);
	}

	@Override
	public void verify() {
		validate(a, "alpha");
		validate(r, "red");
		validate(g, "green");
		validate(b, "blue");
		validate(x, "x");
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, r, g, b, x);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof RgbxColor)) return false;
		RgbxColor other = (RgbxColor) obj;
		if (!Objects.equals(a, other.a)) return false;
		if (!Objects.equals(r, other.r)) return false;
		if (!Objects.equals(g, other.g)) return false;
		if (!Objects.equals(b, other.b)) return false;
		if (!Objects.equals(x, other.x)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,r=%.5f,g=%.5f,b=%.5f,x=%.5f)", a, r, g, b, x);
	}

	private void validate(double value, String name) {
		validateRangeFp(value, 0, MAX_VALUE, name);
	}

	private double limit(double value) {
		return MathUtil.limit(value, 0, MAX_VALUE);
	}

}
