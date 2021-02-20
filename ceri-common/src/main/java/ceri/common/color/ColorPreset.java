package ceri.common.color;

import java.awt.Color;

public enum ColorPreset {
	//white
	// Warm whites at 100%, 75%, 50%
	warmWhite(0xff8d0b),
	warmWhite75(warmWhite, 0.75), // 0xbf6a08
	warmWhite50(warmWhite, 0.5), // 0x804706
	amber(0xffbf00);

	public final int argb;

	ColorPreset(int rgb) {
		argb = ColorUtil.argb(rgb);
	}

	ColorPreset(ColorPreset preset, double dim) {
		this(ColorUtil.dimArgb(preset.argb, dim));
	}

	public Color color() {
		return ColorUtil.color(argb);
	}
}
