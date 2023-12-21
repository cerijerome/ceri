package ceri.common.color;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import ceri.common.math.MathUtil;

/**
 * Interface to set and get colorx.
 */
public interface Colorxable extends Colorable {
	/**
	 * A no-op, stateless instance.
	 */
	static Colorxable NULL = new Null() {};

	/**
	 * Get the colorx as an xargb long.
	 */
	long xargb();

	/**
	 * Set the colorx with an xargb long.
	 */
	void xargb(long xargb);

	/**
	 * Get the opaque xrgb color.
	 */
	default long xrgb() {
		return xargb() | Component.a.mask;
	}

	/**
	 * Set the opaque xrgb color.
	 */
	default void xrgb(long xargb) {
		xargb(xargb | Component.a.mask);
	}

	/**
	 * Get the colorx.
	 */
	default Colorx colorx() {
		return Colorx.of(xargb());
	}

	/**
	 * Set the colorx.
	 */
	default void colorx(Colorx colorx) {
		xargb(colorx.xargb);
	}

	/**
	 * Get the argb color, dropping any x-component.
	 */
	@Override
	default int argb() {
		return (int) xargb();
	}

	/**
	 * Set the argb color, clearing any x-component.
	 */
	@Override
	default void argb(int argb) {
		xargb(MathUtil.uint(argb));
	}

	/**
	 * A no-op, stateless implementation.
	 */
	interface Null extends Colorxable {
		@Override
		default void xargb(long xargb) {}

		@Override
		default long xargb() {
			return 0;
		}
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
		var first = colorxables.stream().findFirst().orElse(NULL);
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
}
