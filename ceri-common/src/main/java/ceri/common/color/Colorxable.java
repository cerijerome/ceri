package ceri.common.color;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

public interface Colorxable {
	static Colorxable NULL = ofNull();

	void argbx(long argbx);

	long argbx();

	default void rgbx(int rgbx) {
		argbx(ColorxUtil.argbx(rgbx));
	}

	default int rgbx() {
		return ColorxUtil.rgbx(argbx());
	}

	default void colorx(Colorx colorx) {
		argbx(colorx.argbx());
	}

	default Colorx colorx() {
		return Colorx.of(argbx());
	}

	static Colorxable from(Colorable colorable, Color x) {
		return from(colorable, x.getRGB());
	}

	static Colorxable from(Colorable colorable, int xRgb) {
		return new Colorxable() {
			@Override
			public void argbx(long argbx) {
				colorable.argb(ColorxUtil.normalizeArgb(argbx, xRgb));
			}

			@Override
			public long argbx() {
				return ColorxUtil.denormalizeArgbx(colorable.argb(), xRgb);
			}
		};
	}

	static Colorxable multi(Colorxable... colorxables) {
		return multi(Arrays.asList(colorxables));
	}

	static Colorxable multi(Collection<Colorxable> colorxables) {
		Colorxable first = colorxables.isEmpty() ? NULL : colorxables.iterator().next();
		return new Colorxable() {
			@Override
			public void argbx(long argbx) {
				colorxables.forEach(c -> c.argbx(argbx));
			}

			@Override
			public long argbx() {
				return first.argbx();
			}
		};
	}

	private static Colorxable ofNull() {
		return new Colorxable() {
			@Override
			public void argbx(long argbx) {}

			@Override
			public long argbx() {
				return Colorx.clear.argbx();
			}
		};
	}
}
