package ceri.common.color;

import static ceri.common.validation.ValidationUtil.validateRange;
import java.awt.Color;
import ceri.common.data.ByteUtil;
import ceri.common.math.MathUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class HsbColor {
	private static final int MAX_COLOR_VALUE = 255;
	private static final int HSB_DECIMALS = 5;
	private static final int RGB_MASK = 0xffffff;
	private static final int RGB_BYTES = 3;
	public static final double MIN_VALUE = 0.0;
	public static final double MAX_VALUE = 1.0;
	public final double h; // hue
	public final double s; // saturation
	public final double b; // brightness
	public final double a; // alpha

	public static HsbColor from(Color color) {
		return from(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	public static HsbColor from(int rgb) {
		return from(ByteUtil.shiftRight(rgb, 2), ByteUtil.shiftRight(rgb, 1), rgb & 0xff);
	}

	public static HsbColor from(int red, int green, int blue) {
		return from(red, green, blue, MAX_COLOR_VALUE);
	}

	public static HsbColor from(int red, int green, int blue, int alpha) {
		float[] hsb = Color.RGBtoHSB(red & 0xff, green & 0xff, blue & 0xff, null);
		int i = 0;
		double hue = MathUtil.simpleRound(hsb[i++], HSB_DECIMALS);
		double saturation = MathUtil.simpleRound(hsb[i++], HSB_DECIMALS);
		double brightness = MathUtil.simpleRound(hsb[i++], HSB_DECIMALS);
		double a = MathUtil.simpleRound(alpha / MAX_COLOR_VALUE, HSB_DECIMALS);
		return new HsbColor(hue, saturation, brightness, a);
	}

	public static Color toColor(double hue, double saturation, double brightness) {
		return toColor(hue, saturation, brightness, MAX_VALUE);
	}

	public static Color toColor(double hue, double saturation, double brightness, double alpha) {
		return new HsbColor(hue, saturation, brightness, alpha).asColor();
	}

	public HsbColor(double hue, double saturation, double brightness) {
		this(hue, saturation, brightness, MAX_VALUE);
	}

	public HsbColor(double hue, double saturation, double brightness, double alpha) {
		validateRange(hue, MIN_VALUE, MAX_VALUE);
		validateRange(saturation, MIN_VALUE, MAX_VALUE);
		validateRange(brightness, MIN_VALUE, MAX_VALUE);
		validateRange(alpha, MIN_VALUE, MAX_VALUE);
		this.h = hue;
		this.s = saturation;
		this.b = brightness;
		this.a = alpha;
	}

	public Color asColor() {
		int rgb = Color.HSBtoRGB((float) h, (float) s, (float) b) & RGB_MASK;
		int a = ByteUtil.shiftLeft((int) (this.a * MAX_COLOR_VALUE), RGB_BYTES);
		return new Color(a | rgb, true);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(h, s, b, a);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof HsbColor)) return false;
		HsbColor other = (HsbColor) obj;
		if (!EqualsUtil.equals(h, other.h)) return false;
		if (!EqualsUtil.equals(s, other.s)) return false;
		if (!EqualsUtil.equals(b, other.b)) return false;
		if (!EqualsUtil.equals(a, other.a)) return false;
		return true;
	}

	@Override
	public String toString() {
		String name = getClass().getSimpleName();
		return a < 1.0 ? String.format("%s[h=%.3f,s=%.3f,b=%.3f,a=%.3f]", name, h, s, b, a)
			: String.format("%s[h=%.3f,s=%.3f,b=%.3f]", name, h, s, b);
	}

}
