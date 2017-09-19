package ceri.common.color;

import java.awt.Color;

public class ColorPresets {
	// Warm whites at 100%, 75%, 50%
	public static final Color WARM_WHITE_100 = new Color(0xff8d0b);
	public static final Color WARM_WHITE_075 = ColorUtil.dim(WARM_WHITE_100, 0.75); // 0xbf6a08
	public static final Color WARM_WHITE_050 = ColorUtil.dim(WARM_WHITE_100, 0.5); // 0x804706

}
