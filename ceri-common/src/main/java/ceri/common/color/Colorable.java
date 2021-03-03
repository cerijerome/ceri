package ceri.common.color;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

/**
 * Interface to set and get color.
 */
public interface Colorable {
	/**
	 * No-op implementation.
	 */
	static Colorable NULL = ofNull();

	/**
	 * Set the color with an argb int.
	 */
	void argb(int argb);

	/**
	 * Get the color as an argb int.
	 */
	int argb();

	/**
	 * Set the color as an opaque rgb int.
	 */
	default void rgb(int rgb) {
		argb(ColorUtil.argb(rgb));
	}

	/**
	 * Get the color as an rgb int with alpha removed.
	 */
	default int rgb() {
		return ColorUtil.rgb(argb());
	}

	/**
	 * Set the color.
	 */
	default void color(Color color) {
		argb(color.getRGB());
	}

	/**
	 * Get the color.
	 */
	default Color color() {
		return ColorUtil.color(argb());
	}

	/**
	 * Adapt a type that gets/sets colorx, by denormalizing color with given x colors.
	 */
	static Colorable from(Colorxable colorxable, Color... xs) {
		return from(colorxable, ColorUtil.argbs(xs));
	}

	/**
	 * Adapt a type that gets/sets colorx, by denormalizing color with given x rgb int colors.
	 */
	static Colorable from(Colorxable colorxable, int... xrgbs) {
		return new Colorable() {
			@Override
			public void argb(int argb) {
				colorxable.xargb(ColorxUtil.denormalizeXargb(argb, xrgbs));
			}

			@Override
			public int argb() {
				return ColorxUtil.normalizeArgb(colorxable.xargb(), xrgbs);
			}
		};
	}

	/**
	 * Provide a wrapper for multiple Colorable types.
	 */
	static Colorable multi(Colorable... colorables) {
		return multi(Arrays.asList(colorables));
	}

	/**
	 * Provide a wrapper for multiple Colorable types.
	 */
	static Colorable multi(Collection<Colorable> colorables) {
		return new Colorable() {
			@Override
			public void argb(int argb) {
				colorables.forEach(c -> c.argb(argb));
			}

			@Override
			public int argb() {
				return colorables.stream().mapToInt(Colorable::argb).findFirst().orElse(0);
			}
		};
	}

	private static Colorable ofNull() {
		return new Colorable() {
			@Override
			public void argb(int argb) {}

			@Override
			public int argb() {
				return ColorUtil.clear.getRGB();
			}
		};
	}
}
