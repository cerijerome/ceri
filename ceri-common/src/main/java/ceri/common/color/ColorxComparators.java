package ceri.common.color;

import java.awt.Color;
import java.util.Comparator;
import ceri.common.comparator.Comparators;

/**
 * Comparators for Colorx.
 */
public class ColorxComparators {
	public static final Comparator<Colorx> BY_RGBX =
		Comparators.nonNull(Comparator.comparing(Colorx::rgbx, Comparators.UINT));
	public static final Comparator<Colorx> BY_ALPHA = byColor(ColorComparators.BY_ALPHA);
	public static final Comparator<Colorx> BY_RED = byColor(ColorComparators.BY_RED);
	public static final Comparator<Colorx> BY_GREEN = byColor(ColorComparators.BY_GREEN);
	public static final Comparator<Colorx> BY_BLUE = byColor(ColorComparators.BY_BLUE);
	public static final Comparator<Colorx> BY_X =
		Comparators.nonNull(Comparator.comparing(Colorx::x, Comparators.INT));

	private ColorxComparators() {}

	public static Comparator<Colorx> byColor(Comparator<Color> comparator) {
		return Comparators.nonNull(Comparator.comparing(t -> t.rgb, comparator));
	}

}
