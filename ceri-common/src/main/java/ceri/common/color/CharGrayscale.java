package ceri.common.color;

import java.awt.Color;
import java.util.Objects;
import ceri.common.text.StringUtil;
import ceri.common.text.ToString;

/**
 * Provides a monochrome character scale, useful for generating gray images in text-only displays. 
 */
public class CharGrayscale {
	public static final CharGrayscale COURIER =
		of("@WMB#80Q&$%bdpOmqUXZkawho*CYJIunx1zfjtLv{}c[]?i()l<>|/\\r+;!~\"^:_-,'.` ");
	public static final CharGrayscale COURIER_COMPACT =
		of("@WMB#80Q&$%bOmqUXZkawho*CYJIunx1zfjtLv{c[?i(l</r+;!~\"^:,'.` ");
	public static final CharGrayscale UNICODE_SHADE = of("█▓▒░ ");
	public static final CharGrayscale UNICODE_WEDGE = of("█▇▆▅▄▃▂▁ ");
	public final String grayscale;

	/**
	 * String containing gray-scale chars, starting with darkest.
	 */
	public static CharGrayscale of(String grayscale) {
		return new CharGrayscale(grayscale);
	}

	private CharGrayscale(String ascii) {
		this.grayscale = ascii;
	}

	public char charOf(Color c) {
		return charOf(c.getRGB());
	}

	public char charOf(int rgb) {
		return charOf(ColorSpaces.rgbToL(rgb));
	}

	public char charOf(double ratio) {
		if (ratio < 0.0) ratio = 0;
		int index = (int) Math.min(ratio * grayscale.length(), grayscale.length() - 1);
		return grayscale.charAt(index);
	}

	public CharGrayscale reverse() {
		return of(StringUtil.reverse(grayscale));
	}

	@Override
	public int hashCode() {
		return Objects.hash(grayscale);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CharGrayscale)) return false;
		CharGrayscale other = (CharGrayscale) obj;
		if (!Objects.equals(grayscale, other.grayscale)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, grayscale);
	}

}
