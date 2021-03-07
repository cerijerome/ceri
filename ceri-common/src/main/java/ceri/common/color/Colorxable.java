package ceri.common.color;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

/**
 * Interface to set and get colorx.
 */
public interface Colorxable {
	/**
	 * No-op implementation.
	 */
	static Colorxable NULL = ofNull();

	/**
	 * Set the colorx with an xargb long.
	 */
	void xargb(long xargb);

	/**
	 * Get the colorx as an xargb long.
	 */
	long xargb();

	/**
	 * Set the colorx.
	 */
	default void colorx(Colorx colorx) {
		xargb(colorx.xargb);
	}

	/**
	 * Get the colorx.
	 */
	default Colorx colorx() {
		return Colorx.of(xargb());
	}

	/**
	 * Adapt a type that gets/sets color, by normalizing colorx with given x colors.
	 */
	static Colorxable from(Colorable colorable, Color... xs) {
		return from(colorable, ColorUtil.argbs(xs));
	}

	/**
	 * Adapt a type that gets/sets color, by normalizing colorx with given x rgb int colors.
	 */
	static Colorxable from(Colorable colorable, int... xrgbs) {
		return new Colorxable() {
			@Override
			public void xargb(long xargb) {
				colorable.argb(ColorxUtil.normalizeArgb(xargb, xrgbs));
			}

			@Override
			public long xargb() {
				return ColorxUtil.denormalizeXargb(colorable.argb(), xrgbs);
			}
		};
	}

	/**
	 * Provide a wrapper for multiple Colorxable types.
	 */
	static Colorxable multi(Colorxable... colorxables) {
		return multi(Arrays.asList(colorxables));
	}

	/**
	 * Provide a wrapper for multiple Colorxable types.
	 */
	static Colorxable multi(Collection<Colorxable> colorxables) {
		Colorxable first = colorxables.isEmpty() ? NULL : colorxables.iterator().next();
		return new Colorxable() {
			@Override
			public void xargb(long xargb) {
				colorxables.forEach(c -> c.xargb(xargb));
			}

			@Override
			public long xargb() {
				return first.xargb();
			}
		};
	}

	private static Colorxable ofNull() {
		return new Colorxable() {
			@Override
			public void xargb(long xargb) {}

			@Override
			public long xargb() {
				return Colorx.clear.xargb;
			}
		};
	}
}
