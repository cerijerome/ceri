package ceri.common.color;

/**
 * Outputs an animated ASCII-grayscale terminal display of frames.
 */
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class CharGrayscale {
	public static final String COURIER_GRAYSCALE =
		"@WMB#80Q&$%bdpOmqUXZkawho*CYJIunx1zfjtLv{}c[]?i()l<>|/\\r+;!~\"^:_-,'.` ";
	public static final String COURIER_GRAYSCALE_COMPACT =
		"@WMB#80Q&$%bOmqUXZkawho*CYJIunx1zfjtLv{c[?i(l<|/r+;!~\"^:_-,'.` ";
	public static final String COURIER_GRAYSCALE_COMPACT2 =
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

	public char charOf(double ratio) {
		if (ratio < 0.0) ratio = 0;
		int index = (int) Math.min(ratio * grayscale.length(), grayscale.length() - 1);
		return grayscale.charAt(index);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(grayscale);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CharGrayscale)) return false;
		CharGrayscale other = (CharGrayscale) obj;
		if (!EqualsUtil.equals(grayscale, other.grayscale)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, grayscale).toString();
	}

}
