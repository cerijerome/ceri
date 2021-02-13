package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_RATIO;
import static ceri.common.color.ColorUtil.a;
import static ceri.common.color.ColorUtil.b;
import static ceri.common.color.ColorUtil.g;
import static ceri.common.color.ColorUtil.r;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.validation.ValidationUtil.validateRangeFp;
import java.awt.Color;
import java.util.Objects;

/**
 * Encapsulates A+HSB color with values 0-1 inclusive.
 */
public class HsbColor implements ComponentColor<HsbColor> {
	public static final HsbColor BLACK = HsbColor.of(0, 0, 0);
	public static final HsbColor WHITE = HsbColor.of(0, 0, MAX_RATIO);
	public final double a; // alpha
	public final double h; // hue
	public final double s; // saturation
	public final double b; // brightness

	public static HsbColor from(Color color) {
		return from(color.getRGB());
	}

	public static HsbColor from(RgbColor color) {
		return from(color.argb());
	}

	public static HsbColor fromRgb(int rgb) {
		return from(r(rgb), g(rgb), b(rgb));
	}

	public static HsbColor from(int argb) {
		return from(a(argb), r(argb), g(argb), b(argb));
	}

	public static HsbColor from(int r, int g, int b) {
		return from(ColorUtil.MAX_VALUE, r, g, b);
	}

	public static HsbColor from(int a, int r, int g, int b) {
		double alpha = ColorUtil.ratio(a);
		float[] hsb = Color.RGBtoHSB(ubyte(r), ubyte(g), ubyte(b), null);
		int i = 0;
		double hue = hsb[i++];
		double saturation = hsb[i++];
		double brightness = hsb[i];
		return new HsbColor(alpha, hue, saturation, brightness);
	}

	public static HsbColor of(double h, double s, double b) {
		return of(MAX_RATIO, h, s, b);
	}

	public static HsbColor of(double a, double h, double s, double b) {
		return new HsbColor(a, h, s, b);
	}

	public static HsbColor max(double h) {
		return of(h, MAX_RATIO, MAX_RATIO);
	}

	private HsbColor(double a, double h, double s, double b) {
		this.a = a;
		this.h = h;
		this.s = s;
		this.b = b;
	}

	public int argb() {
		int rgb = Color.HSBtoRGB((float) h, (float) s, (float) b);
		return ColorUtil.argb(ColorUtil.value(a), rgb);
	}

	public Color color() {
		return ColorUtil.color(argb());
	}

	public RgbColor rgbColor() {
		return RgbColor.from(argb());
	}

	public boolean isBlack() {
		return b <= 0;
	}

	public boolean isWhite() {
		return s <= 0 && b >= MAX_RATIO;
	}

	public HsbColor shiftHue(double delta) {
		double h = ColorUtil.limitHue(this.h + delta);
		return h == this.h ? this : of(a, h, s, b);
	}

	public HsbColor dim(double ratio) {
		if (ratio == 1 || b == 0) return this;
		return of(a, h, s, b * ratio);
	}

	@Override
	public boolean hasAlpha() {
		return a < MAX_RATIO;
	}

	@Override
	public HsbColor normalize() {
		double a = ColorUtil.limit(this.a);
		double h = ColorUtil.limitHue(this.h);
		double s = ColorUtil.limit(this.s);
		double b = ColorUtil.limit(this.b);
		if (a == this.a && h == this.h && s == this.s && b == this.b) return this;
		return of(a, h, s, b);
	}

	@Override
	public HsbColor limit() {
		double a = ColorUtil.limit(this.a);
		double h = ColorUtil.limit(this.h);
		double s = ColorUtil.limit(this.s);
		double b = ColorUtil.limit(this.b);
		if (a == this.a && h == this.h && s == this.s && b == this.b) return this;
		return of(a, h, s, b);
	}

	@Override
	public void verify() {
		validate(a, "alpha");
		validate(h, "hue");
		validate(s, "saturation");
		validate(b, "brightness");
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, h, s, b);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof HsbColor)) return false;
		HsbColor other = (HsbColor) obj;
		if (!Objects.equals(a, other.a)) return false;
		if (!Objects.equals(h, other.h)) return false;
		if (!Objects.equals(s, other.s)) return false;
		if (!Objects.equals(b, other.b)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,h=%.5f,s=%.5f,b=%.5f,a=%.5f)", a, h, s, b);
	}

	private static void validate(double value, String name) {
		validateRangeFp(value, 0, MAX_RATIO, name);
	}

}
