package ceri.common.color;

import java.awt.Color;
import java.util.Objects;
import ceri.common.text.StringUtil;
import ceri.common.text.ToString;

/**
 * Provides a monochrome character scale, useful for generating gray images in text-only displays. 
 */
public class GrayChar {
	public static final GrayChar COURIER =
		of("@WMB#80Q&$%bdpOmqUXZkawho*CYJIunx1zfjtLv{}c[]?i()l<>|/\\r+;!~\"^:_-,'.` ");
	public static final GrayChar COURIER_COMPACT =
		of("@WMB#80Q&$%bOmqUXZkawho*CYJIunx1zfjtLv{c[?i(l</r+;!~\"^:,'.` ");
	public static final GrayChar UNICODE_SHADE = of("█▓▒░ ");
	public static final GrayChar UNICODE_WEDGE = of("█▇▆▅▄▃▂▁ ");
	public final String grayscale;

	/**
	 * String containing gray-scale chars, starting with darkest.
	 */
	public static GrayChar of(String grayscale) {
		return new GrayChar(grayscale);
	}

	private GrayChar(String ascii) {
		this.grayscale = ascii;
	}

	public char charOf(Color c) {
		return charOf(c.getRGB());
	}

	public char charOf(int rgb) {
		return charOf(LuvColor.Ref.CIE_D65.l(rgb));
	}

	public char charOf(double ratio) {
		if (ratio < 0.0) ratio = 0;
		int index = (int) Math.min(ratio * grayscale.length(), grayscale.length() - 1);
		return grayscale.charAt(index);
	}

	public GrayChar reverse() {
		return of(StringUtil.reverse(grayscale));
	}

	@Override
	public int hashCode() {
		return Objects.hash(grayscale);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof GrayChar)) return false;
		GrayChar other = (GrayChar) obj;
		if (!Objects.equals(grayscale, other.grayscale)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, grayscale);
	}

}
