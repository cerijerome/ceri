package ceri.common.color;

import static ceri.common.color.ColorUtil.CHANNEL_MAX;
import java.awt.Color;
import java.util.Objects;
import ceri.common.text.StringUtil;
import ceri.common.text.ToString;

public class CharGrayscale {
	public static final String COURIER_GRAYSCALE =
		"@WMB#80Q&$%bdpOmqUXZkawho*CYJIunx1zfjtLv{}c[]?i()l<>|/\\r+;!~\"^:_-,'.` ";
	public static final String COURIER_GRAYSCALE_COMPACT =
		"@WMB#80Q&$%bOmqUXZkawho*CYJIunx1zfjtLv{c[?i(l</r+;!~\"^:,'.` ";
	private final String grayscale;

	/**
	 * String containing gray-scale chars, starting with darkest.
	 */
	public static CharGrayscale of(String grayscale) {
		return new CharGrayscale(grayscale);
	}

	private CharGrayscale(String ascii) {
		this.grayscale = ascii;
	}

	public char charOf(Color c, double r, double g, double b) {
		double max = (r + g + b) * CHANNEL_MAX;
		if (max == 0.0) return charOf(0);
		return charOf((c.getRed() * r + c.getGreen() * g + c.getBlue() * b) / max);
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
