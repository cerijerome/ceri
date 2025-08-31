package ceri.common.color;

import java.awt.Color;
import ceri.common.text.Strings;

/**
 * Provides a monochrome character scale, useful for generating gray images in text-only displays.
 */
public record GrayChar(String grayscale) {
	public static final GrayChar COURIER =
		new GrayChar("@WMB#80Q&$%bdpOmqUXZkawho*CYJIunx1zfjtLv{}c[]?i()l<>|/\\r+;!~\"^:_-,'.` ");
	public static final GrayChar COURIER_COMPACT =
		new GrayChar("@WMB#80Q&$%bOmqUXZkawho*CYJIunx1zfjtLv{c[?i(l</r+;!~\"^:,'.` ");
	public static final GrayChar UNICODE_SHADE = new GrayChar("█▓▒░ ");
	public static final GrayChar UNICODE_WEDGE = new GrayChar("█▇▆▅▄▃▂▁ ");

	/**
	 * Look up grayscale char by lightness of color without alpha.
	 */
	public char charOf(Color c) {
		return charOf(c.getRGB());
	}

	/**
	 * Look up grayscale char by lightness of rgb int.
	 */
	public char charOf(int rgb) {
		return charOf(LuvColor.Ref.CIE_D65.l(rgb));
	}

	/**
	 * Look up grayscale char by 0-1 ratio.
	 */
	public char charOf(double ratio) {
		if (ratio < 0.0) ratio = 0;
		int index = (int) Math.min(ratio * grayscale.length(), grayscale.length() - 1);
		return grayscale.charAt(index);
	}

	/**
	 * Create an instance with scale reversed.
	 */
	public GrayChar reverse() {
		return new GrayChar(Strings.reverse(grayscale));
	}
}
