package ceri.common.color;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

public interface Colorxable {
	static Colorxable NULL = ofNull();

	void xargb(long xargb);

	long xargb();

	default void xrgb(long xargb) {
		xargb(ColorxUtil.xrgb(xargb));
	}

	default long xrgb() {
		return ColorxUtil.xrgb(xargb());
	}

	default void colorx(Colorx colorx) {
		xargb(colorx.xargb);
	}

	default Colorx colorx() {
		return Colorx.of(xargb());
	}

	static Colorxable from(Colorable colorable, Color... xs) {
		return from(colorable, ColorUtil.argbArray(xs));
	}

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

	static Colorxable multi(Colorxable... colorxables) {
		return multi(Arrays.asList(colorxables));
	}

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
