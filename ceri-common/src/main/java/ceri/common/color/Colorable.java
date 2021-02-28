package ceri.common.color;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

public interface Colorable {
	static Colorable NULL = ofNull();

	void argb(int argb);

	int argb();

	default void rgb(int rgb) {
		argb(ColorUtil.argb(rgb));
	}

	default int rgb() {
		return ColorUtil.rgb(argb());
	}

	default void color(Color color) {
		argb(color.getRGB());
	}

	default Color color() {
		return ColorUtil.color(argb());
	}

	static Colorable from(Colorxable colorxable, Color... xs) {
		return from(colorxable, ColorUtil.argbArray(xs));
	}

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

	static Colorable multi(Colorable... colorables) {
		return multi(Arrays.asList(colorables));
	}

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
