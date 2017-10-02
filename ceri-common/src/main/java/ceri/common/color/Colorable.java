package ceri.common.color;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

public interface Colorable {

	void color(int r, int g, int b);

	Color color();

	default void color(X11Color color) {
		color(color.color);
	}

	default void color(Color color) {
		color(color.getRed(), color.getGreen(), color.getBlue());
	}

	default void color(int rgb) {
		color(new Color(rgb));
	}

	static Colorable multi(Colorable...colorables) {
		return multi(Arrays.asList(colorables));
	}

	static Colorable multi(Collection<Colorable> colorables) {
		return new Colorable() {

			@Override
			public Color color() {
				return colorables.stream().findFirst().map(c -> c.color()).orElse(null);
			}

			@Override
			public void color(int r, int g, int b) {
				colorables.forEach(c -> c.color(r, g, b));
			}

		};
	}

}
