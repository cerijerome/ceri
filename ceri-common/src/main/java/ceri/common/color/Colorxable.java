package ceri.common.color;

import java.util.Arrays;
import java.util.Collection;

public interface Colorxable {

	void colorx(int r, int g, int b, int x);

	Colorx colorx();

	default void colorx(Colorx colorx) {
		colorx(colorx.r(), colorx.g(), colorx.b(), colorx.x());
	}

	default void colorx(int rgbx) {
		colorx(Colorx.of(rgbx));
	}

	static Colorxable multi(Colorxable... colorxables) {
		return multi(Arrays.asList(colorxables));
	}

	static Colorxable multi(Collection<Colorxable> colorxables) {
		return new Colorxable() {

			@Override
			public Colorx colorx() {
				return colorxables.stream().findFirst().map(c -> c.colorx()).orElse(null);
			}

			@Override
			public void colorx(int r, int g, int b, int x) {
				colorxables.forEach(c -> c.colorx(r, g, b, x));
			}

		};
	}

}
