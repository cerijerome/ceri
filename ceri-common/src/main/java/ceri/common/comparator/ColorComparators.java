package ceri.common.comparator;

import java.awt.Color;
import java.util.Comparator;

/**
 * Comparators for Color.
 */
public class ColorComparators {
	public static final Comparator<Color> BY_RGB = Comparators
		.nonNull((lhs, rhs) -> Comparators.INTEGER.compare(lhs.getRGB(), rhs.getRGB()));
	public static final Comparator<Color> BY_ALPHA = Comparators
		.nonNull((lhs, rhs) -> Comparators.INTEGER.compare(lhs.getAlpha(), rhs.getAlpha()));
	public static final Comparator<Color> BY_RED = Comparators
		.nonNull((lhs, rhs) -> Comparators.INTEGER.compare(lhs.getRed(), rhs.getRed()));
	public static final Comparator<Color> BY_GREEN = Comparators
		.nonNull((lhs, rhs) -> Comparators.INTEGER.compare(lhs.getGreen(), rhs.getGreen()));
	public static final Comparator<Color> BY_BLUE = Comparators
		.nonNull((lhs, rhs) -> Comparators.INTEGER.compare(lhs.getBlue(), rhs.getBlue()));
	public static final Comparator<Color> BY_HUE = Comparators
		.nonNull((lhs, rhs) -> Comparators.FLOAT.compare(toHsb(lhs)[0], toHsb(rhs)[0]));
	public static final Comparator<Color> BY_SATURATION = Comparators
		.nonNull((lhs, rhs) -> Comparators.FLOAT.compare(toHsb(lhs)[1], toHsb(rhs)[1]));
	public static final Comparator<Color> BY_BRIGHTNESS = Comparators
		.nonNull((lhs, rhs) -> Comparators.FLOAT.compare(toHsb(lhs)[2], toHsb(rhs)[2]));
	public static final Comparator<Color> BY_HSB = Comparators.nonNull((lhs, rhs) -> compareHsb(
		toHsb(lhs), toHsb(rhs)));

	private ColorComparators() {}

	private static float[] toHsb(Color color) {
		float[] hsb = new float[3];
		return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
	}

	private static int compareHsb(float[] lhs, float[] rhs) {
		int result = Comparators.FLOAT.compare(lhs[0], rhs[0]);
		if (result == 0) result = Comparators.FLOAT.compare(lhs[1], rhs[1]);
		if (result == 0) result = Comparators.FLOAT.compare(lhs[2], rhs[2]);
		return result;
	}

}
