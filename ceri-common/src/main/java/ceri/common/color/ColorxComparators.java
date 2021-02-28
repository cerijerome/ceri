package ceri.common.color;

import static ceri.common.comparator.Comparators.nonNull;
import static java.util.Comparator.comparing;
import java.awt.Color;
import java.util.Comparator;
import ceri.common.comparator.Comparators;

/**
 * Comparators for Colorx.
 */
public class ColorxComparators {
	public static final Comparator<Colorx> XARGB =
		nonNull(comparing(cx -> cx.xargb, Comparators.ULONG));
	public static final Comparator<Colorx> XRGB =
		nonNull(comparing(cx -> ColorxUtil.xrgb(cx.xargb), Comparators.ULONG));
	public static final Comparator<Colorx> ARGB =
		nonNull(comparing(Colorx::argb, Comparators.UINT));
	public static final Comparator<Colorx> ALPHA =
		nonNull(comparing(cx -> ColorxUtil.a(cx.xargb), Comparators.INT));
	public static final Comparator<Colorx> RED =
		nonNull(comparing(cx -> ColorxUtil.r(cx.xargb), Comparators.INT));
	public static final Comparator<Colorx> GRREN =
		nonNull(comparing(cx -> ColorxUtil.g(cx.xargb), Comparators.INT));
	public static final Comparator<Colorx> BLUE =
		nonNull(comparing(cx -> ColorxUtil.b(cx.xargb), Comparators.INT));

	private ColorxComparators() {}

	public static Comparator<Colorx> color(Comparator<Color> comparator) {
		return nonNull(comparing(t -> t.color(), comparator));
	}

	public static Comparator<Colorx> x(int i) {
		return nonNull(comparing(cx -> ColorxUtil.x(cx.xargb, i), Comparators.INT));
	}

}
