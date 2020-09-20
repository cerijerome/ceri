package ceri.common.color;

import java.util.Objects;
import ceri.common.math.MathUtil;
import ceri.common.text.ToString;

/**
 * Encapsulates color temperature, brightness, and alpha channel.
 */
public class CtColor {
	private static final double MEGA = 1_000_000;
	public static final double MAX_BRIGHTESS = 1.0;
	public static final double MAX_ALPHA = 1.0;
	public final int k;
	public final double b;
	public final double a;

	public static double kelvinToMired(int kelvin) {
		return MEGA / kelvin;
	}

	public static int miredToKelvin(double mired) {
		return MathUtil.intRoundExact(MEGA / mired);
	}

	public static CtColor of(int k) {
		return of(k, MAX_BRIGHTESS);
	}

	public static CtColor of(int k, double b) {
		return of(k, b, MAX_ALPHA);
	}

	public static CtColor of(int k, double b, double a) {
		return new CtColor(k, b, a);
	}

	private CtColor(int k, double b, double a) {
		this.k = k;
		this.b = b;
		this.a = a;
	}

	public double mired() {
		return kelvinToMired(k);
	}

	@Override
	public int hashCode() {
		return Objects.hash(k, b, a);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CtColor)) return false;
		CtColor other = (CtColor) obj;
		if (k != other.k) return false;
		if (!Objects.equals(b, other.b)) return false;
		if (!Objects.equals(a, other.a)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, k, b, a);
	}

}
