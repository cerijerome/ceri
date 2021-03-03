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
	/** Compare by xargb long. */
	public static final Comparator<Colorx> XARGB =
		nonNull(comparing(cx -> cx.xargb, Comparators.ULONG));
	/** Compare by xrgb long value without alpha. */
	public static final Comparator<Colorx> XRGB =
		nonNull(comparing(cx -> ColorxUtil.xrgb(cx.xargb), Comparators.ULONG));
	/** Compare by argb int value. */
	public static final Comparator<Colorx> ARGB =
		nonNull(comparing(Colorx::argb, Comparators.UINT));
	/** Compare by alpha component. */
	public static final Comparator<Colorx> ALPHA =
		nonNull(comparing(cx -> ColorxUtil.a(cx.xargb), Comparators.INT));
	/** Compare by red component. */
	public static final Comparator<Colorx> RED =
		nonNull(comparing(cx -> ColorxUtil.r(cx.xargb), Comparators.INT));
	/** Compare by green component. */
	public static final Comparator<Colorx> GREEN =
		nonNull(comparing(cx -> ColorxUtil.g(cx.xargb), Comparators.INT));
	/** Compare by blue component. */
	public static final Comparator<Colorx> BLUE =
		nonNull(comparing(cx -> ColorxUtil.b(cx.xargb), Comparators.INT));

	private ColorxComparators() {}

	/**
	 * Compare using color comparator, ignoring x.
	 */
	public static Comparator<Colorx> color(Comparator<Color> comparator) {
		return nonNull(comparing(t -> t.color(), comparator));
	}

	/**
	 * Compare by x[i].
	 */
	public static Comparator<Colorx> x(int i) {
		return nonNull(comparing(cx -> ColorxUtil.x(cx.xargb, i), Comparators.INT));
	}

}
