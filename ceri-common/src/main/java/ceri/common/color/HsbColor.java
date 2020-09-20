package ceri.common.color;

import static ceri.common.color.ColorUtil.b;
import static ceri.common.color.ColorUtil.g;
import static ceri.common.color.ColorUtil.r;
import static ceri.common.color.ColorUtil.toRatio;
import static ceri.common.math.Bound.Type.inclusive;
import static ceri.common.validation.ValidationUtil.validateRangeFp;
import java.awt.Color;
import java.util.Objects;
import ceri.common.data.ByteUtil;
import ceri.common.math.MathUtil;

/**
 * Encapsulates HSBA color with values 0-1.
 */
public class HsbColor implements ComponentColor<HsbColor> {
	public static final double MAX_VALUE = 1.0;
	public static final HsbColor BLACK = HsbColor.of(0, 0, 0);
	public static final HsbColor WHITE = HsbColor.of(0, 0, MAX_VALUE);
	private static final int MAX_COLOR_VALUE = 255;
	private static final int RGB_MASK = 0xffffff;
	private static final int RGB_BYTES = 3;
	public final double h; // hue
	public final double s; // saturation
	public final double b; // brightness
	public final double a; // alpha

	public static HsbColor from(Color color) {
		return from(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	public static HsbColor from(RgbColor color) {
		return from(color.asColor());
	}

	public static HsbColor from(int rgb) {
		return from(r(rgb), g(rgb), b(rgb));
	}

	public static HsbColor from(int red, int green, int blue) {
		return from(red, green, blue, MAX_COLOR_VALUE);
	}

	public static HsbColor from(int red, int green, int blue, int alpha) {
		float[] hsb = Color.RGBtoHSB(red & 0xff, green & 0xff, blue & 0xff, null);
		int i = 0;
		double hue = hsb[i++];
		double saturation = hsb[i++];
		double brightness = hsb[i];
		double a = (double) alpha / MAX_COLOR_VALUE;
		return new HsbColor(hue, saturation, brightness, a);
	}

	public static Color toColor(double hue, double saturation, double brightness) {
		return toColor(hue, saturation, brightness, MAX_VALUE);
	}

	public static Color toColor(double hue, double saturation, double brightness, double alpha) {
		return new HsbColor(hue, saturation, brightness, alpha).asColor();
	}

	public static HsbColor of(double hue, double saturation, double brightness) {
		return of(hue, saturation, brightness, MAX_VALUE);
	}

	public static HsbColor of(double hue, double saturation, double brightness, double alpha) {
		return new HsbColor(hue, saturation, brightness, alpha);
	}

	public static HsbColor max(double hue) {
		return of(hue, MAX_VALUE, MAX_VALUE);
	}

	private HsbColor(double hue, double saturation, double brightness, double alpha) {
		this.h = hue;
		this.s = saturation;
		this.b = brightness;
		this.a = alpha;
	}

	public Color asColor() {
		int rgb = Color.HSBtoRGB((float) h, (float) s, (float) b) & RGB_MASK;
		int a = (int) ByteUtil.shiftByteLeft((int) (this.a * MAX_COLOR_VALUE), RGB_BYTES);
		return new Color(a | rgb, true);
	}

	public RgbColor asRgb() {
		Color color = asColor();
		return RgbColor.of(toRatio(color.getRed()), toRatio(color.getGreen()),
			toRatio(color.getBlue()), a);
	}

	public boolean isBlack() {
		return b <= 0;
	}

	public boolean isWhite() {
		return s <= 0 && b >= MAX_VALUE;
	}

	public HsbColor dim(double ratio) {
		if (ratio == 1) return this;
		return of(h, s, b * ratio, a);
	}

	@Override
	public boolean hasAlpha() {
		return a < MAX_VALUE;
	}

	@Override
	public HsbColor normalize() {
		double h = MathUtil.periodicLimit(this.h, MAX_VALUE, inclusive);
		double s = limit(this.s);
		double b = limit(this.b);
		double a = limit(this.a);
		if (h == this.h && s == this.s && b == this.b && a == this.a) return this;
		return of(h, s, b, a);
	}

	@Override
	public HsbColor limit() {
		double h = limit(this.h);
		double s = limit(this.s);
		double b = limit(this.b);
		double a = limit(this.a);
		if (h == this.h && s == this.s && b == this.b && a == this.a) return this;
		return of(h, s, b, a);
	}

	@Override
	public void verify() {
		validate(h, "hue");
		validate(s, "saturation");
		validate(b, "brightness");
		validate(a, "alpha");
	}

	private void validate(double value, String name) {
		validateRangeFp(value, 0, MAX_VALUE, name);
	}

	private double limit(double value) {
		return MathUtil.limit(value, 0, MAX_VALUE);
	}

	@Override
	public int hashCode() {
		return Objects.hash(h, s, b, a);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof HsbColor)) return false;
		HsbColor other = (HsbColor) obj;
		if (!Objects.equals(h, other.h)) return false;
		if (!Objects.equals(s, other.s)) return false;
		if (!Objects.equals(b, other.b)) return false;
		if (!Objects.equals(a, other.a)) return false;
		return true;
	}

	@Override
	public String toString() {
		return hasAlpha() ? String.format("(h=%.5f,s=%.5f,b=%.5f,a=%.5f)", h, s, b, a) :
			String.format("(h=%.5f,s=%.5f,b=%.5f)", h, s, b);
	}

}
