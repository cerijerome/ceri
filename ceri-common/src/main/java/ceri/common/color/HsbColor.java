package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_RATIO;
import static ceri.common.color.ColorUtil.a;
import static ceri.common.color.ColorUtil.ratio;
import static ceri.common.validation.ValidationUtil.validateRangeFp;
import java.awt.Color;
import java.util.Objects;

/**
 * Encapsulates an HSB color with alpha, all values 0-1 inclusive.
 */
public class HsbColor {
	public static final HsbColor clear = HsbColor.of(0, 0, 0, 0);
	public static final HsbColor black = HsbColor.of(0, 0, 0);
	public static final HsbColor white = HsbColor.of(0, 0, MAX_RATIO);
	public final double a; // alpha
	public final double h; // hue
	public final double s; // saturation
	public final double b; // brightness

	/**
	 * Construct from color.
	 */
	public static HsbColor from(Color color) {
		return from(color.getRGB());
	}

	/**
	 * Construct an opaque instance from rgb int value.
	 */
	public static HsbColor fromRgb(int rgb) {
		double[] hsb = ColorSpaces.rgbToHsb(rgb);
		return of(hsb[0], hsb[1], hsb[2]);
	}

	/**
	 * Construct from argb int value.
	 */
	public static HsbColor from(int argb) {
		double[] hsb = ColorSpaces.rgbToHsb(argb);
		return of(ratio(a(argb)), hsb[0], hsb[1], hsb[2]);
	}

	/**
	 * Construct an opaque instance from HSB 0-1 values.
	 */
	public static HsbColor of(double h, double s, double b) {
		return of(MAX_RATIO, h, s, b);
	}

	/**
	 * Construct alpha+HSB 0-1 values.
	 */
	public static HsbColor of(double a, double h, double s, double b) {
		return new HsbColor(a, h, s, b);
	}

	/**
	 * Construct maximized instance for given hue 0-1 value.
	 */
	public static HsbColor max(double h) {
		return of(h, MAX_RATIO, MAX_RATIO);
	}

	private HsbColor(double a, double h, double s, double b) {
		this.a = a;
		this.h = h;
		this.s = s;
		this.b = b;
	}

	/**
	 * Provide hsb 0-1 values. Alpha is dropped.
	 */
	public double[] hsbValues() {
		return new double[] { h, s, b };
	}

	/**
	 * Convert to argb int. Alpha is maintained.
	 */
	public int argb() {
		return ColorSpaces.hsbToRgb(h, s, b);
	}

	/**
	 * Convert to color. Alpha is maintained.
	 */
	public Color color() {
		return ColorUtil.color(argb());
	}

	/**
	 * Convert to RGB. Alpha is maintained.
	 */
	public RgbColor rgb() {
		double[] rgb = rgbValues();
		return RgbColor.of(a, rgb[0], rgb[1], rgb[2]);
	}

	/**
	 * Convert to RGB 0-1 values. Alpha is dropped.
	 */
	public double[] rgbValues() {
		return ColorSpaces.hsbToSrgb(h, s, b);
	}

	/**
	 * Return true if this color represents black.
	 */
	public boolean isBlack() {
		return b <= 0;
	}

	/**
	 * Return true if this color represents white.
	 */
	public boolean isWhite() {
		return s <= 0 && b >= MAX_RATIO;
	}

	/**
	 * Apply alpha to convert to an opaque color.
	 */
	public HsbColor applyAlpha() {
		double b = ColorUtil.limit(this.b * a);
		return b == this.b ? this : of(h, s, b);
	}

	/**
	 * Returns true if not opaque.
	 */
	public boolean hasAlpha() {
		return a < MAX_RATIO;
	}

	/**
	 * Shift hue by given amount, normalized to 0-1.
	 */
	public HsbColor shiftHue(double delta) {
		double h = ColorUtil.limitHue(this.h + delta);
		return h == this.h ? this : of(a, h, s, b);
	}

	/**
	 * Dims by given ratio.
	 */
	public HsbColor dim(double ratio) {
		double b = ColorUtil.limit(this.b * ratio);
		return b == this.b ? this : of(a, h, s, b);
	}

	/**
	 * Hue wraps to 0-1, other components truncated to 0-1.
	 */
	public HsbColor normalize() {
		double a = ColorUtil.limit(this.a);
		double h = ColorUtil.limitHue(this.h);
		double s = ColorUtil.limit(this.s);
		double b = ColorUtil.limit(this.b);
		if (a == this.a && h == this.h && s == this.s && b == this.b) return this;
		return of(a, h, s, b);
	}

	/**
	 * Validates all components are 0-1. Throws an exception if not.
	 */
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
		return String.format("(a=%.5f,h=%.5f,s=%.5f,b=%.5f)", a, h, s, b);
	}

	private static void validate(double value, String name) {
		validateRangeFp(value, 0, MAX_RATIO, name);
	}
}
