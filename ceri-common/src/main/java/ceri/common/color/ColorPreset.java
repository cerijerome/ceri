package ceri.common.color;

import java.awt.Color;

public enum ColorPreset {
	// Warm whites at 100%, 75%, 50%
	warmWhite100(0xff8d0b),
	warmWhite075(warmWhite100, 0.75), // 0xbf6a08
	warmWhite050(warmWhite100, 0.5); // 0x804706

	public final Color color;
	
	ColorPreset(int color) {
		this(new Color(color));
	}

	ColorPreset(ColorPreset preset, double dim) {
		this(ColorUtil.dim(preset.color, dim));
	}
	
	ColorPreset(Color color) {
		this.color = color;
	}
	
}
