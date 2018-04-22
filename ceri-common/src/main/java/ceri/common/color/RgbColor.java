package ceri.common.color;

import static ceri.common.color.ColorUtil.fromRatio;
import static ceri.common.color.ColorUtil.toRatio;
import static ceri.common.validation.ValidationUtil.validateRange;
import java.awt.Color;
import ceri.common.data.ByteUtil;
import ceri.common.math.MathUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Encapsulates RGBA color with values 0-1.
 */
public class RgbColor {
	private static final int MAX_COLOR_VALUE = 255;
	private static final int RGB_DECIMALS = 5;
	public static final double MIN_VALUE = 0.0;
	public static final double MAX_VALUE = 1.0;
	public final double r; // red
	public final double g; // green
	public final double b; // blue
	public final double a; // alpha

	public static RgbColor from(Color color) {
		return from(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	public static RgbColor from(int rgb) {
		return from(ByteUtil.byteAt(rgb, 2), ByteUtil.byteAt(rgb, 1), rgb);
	}

	public static RgbColor from(int red, int green, int blue) {
		return from(red, green, blue, MAX_COLOR_VALUE);
	}

	public static RgbColor from(int red, int green, int blue, int alpha) {
		double r = MathUtil.simpleRound(toRatio(red), RGB_DECIMALS);
		double g = MathUtil.simpleRound(toRatio(green), RGB_DECIMALS);
		double b = MathUtil.simpleRound(toRatio(blue), RGB_DECIMALS);
		double a = MathUtil.simpleRound(toRatio(alpha), RGB_DECIMALS);
		return of(r, g, b, a);
	}

	public static Color toColor(double red, double green, double blue) {
		return toColor(red, green, blue, MAX_VALUE);
	}

	public static Color toColor(double red, double green, double blue, double alpha) {
		return of(red, green, blue, alpha).asColor();
	}

	public static RgbColor normalize(double red, double green, double blue) {
		return normalize(red, green, blue, MAX_VALUE);
	}
	
	public static RgbColor normalize(double red, double green, double blue, double alpha) {
		double max = MathUtil.max(red, green, blue);
		if (max <= 1.0) return limit(red, green, blue, alpha);
		return limit(red / max, green / max, blue / max, alpha);
	}

	public static RgbColor limit(double red, double green, double blue) {
		return limit(red, green, blue, MAX_VALUE);
	}

	public static RgbColor limit(double red, double green, double blue, double alpha) {
		red = MathUtil.limit(red, MIN_VALUE, MAX_VALUE);
		green = MathUtil.limit(green, MIN_VALUE, MAX_VALUE);
		blue = MathUtil.limit(blue, MIN_VALUE, MAX_VALUE);
		alpha = MathUtil.limit(alpha, MIN_VALUE, MAX_VALUE);
		return of(red, green, blue, alpha);
	}
	
	public static RgbColor of(double red, double green, double blue) {
		return of(red, green, blue, MAX_VALUE);
	}

	public static RgbColor of(double red, double green, double blue, double alpha) {
		validateRange(red, MIN_VALUE, MAX_VALUE);
		validateRange(green, MIN_VALUE, MAX_VALUE);
		validateRange(blue, MIN_VALUE, MAX_VALUE);
		validateRange(alpha, MIN_VALUE, MAX_VALUE);
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

	public boolean hasAlpha() {
		return a < MAX_VALUE;
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
		String name = getClass().getSimpleName();
		return hasAlpha() ? String.format("%s[r=%.3f,g=%.3f,b=%.3f,a=%.3f]", name, r, g, b, a) :
			String.format("%s[r=%.3f,g=%.3f,b=%.3f]", name, r, g, b);
	}

}
