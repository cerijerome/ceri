package ceri.common.color;

import static ceri.common.comparator.Comparators.nonNull;
import static java.util.Comparator.comparing;
import java.awt.Color;
import java.util.Comparator;
import ceri.common.comparator.Comparators;

/**
 * Comparators for Color.
 */
public class ColorComparators {
	/** Compare by argb int value. */
	public static final Comparator<Color> ARGB =
		nonNull(comparing(Color::getRGB, Comparators.UINT));
	/** Compare by alpha component. */
	public static final Comparator<Color> ALPHA =
		nonNull(comparing(Color::getAlpha, Comparators.INT));
	/** Compare by red component. */
	public static final Comparator<Color> RED = //
		nonNull(comparing(Color::getRed, Comparators.INT));
	/** Compare by green component. */
	public static final Comparator<Color> GREEN =
		nonNull(comparing(Color::getGreen, Comparators.INT));
	/** Compare by blue component. */
	public static final Comparator<Color> BLUE =
		nonNull(comparing(Color::getBlue, Comparators.INT));
	/** Compare by HSB hue. */
	public static final Comparator<Color> HUE =
		nonNull(comparing(c -> toHsb(c)[0], Comparators.FLOAT));
	/** Compare by HSB saturation. */
	public static final Comparator<Color> SATURATION =
		nonNull(comparing(c -> toHsb(c)[1], Comparators.FLOAT));
	/** Compare by HSB brightness. */
	public static final Comparator<Color> BRIGHTNESS =
		nonNull(comparing(c -> toHsb(c)[2], Comparators.FLOAT));
	/** Compare by hue, saturation, then brightness. */
	public static final Comparator<Color> HSB =
		nonNull(comparing(ColorComparators::toHsb, ColorComparators::compareHsb));

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
