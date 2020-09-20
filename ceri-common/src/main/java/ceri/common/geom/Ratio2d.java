package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMinFp;
import java.util.Objects;

public class Ratio2d {
	public static final Ratio2d ZERO = new Ratio2d(0, 0);
	public static final Ratio2d ONE = new Ratio2d(1, 1);
	public final double x;
	public final double y;

	public static Ratio2d uniform(double scale) {
		return of(scale, scale);
	}

	public static Ratio2d of(double x, double y) {
		if (x == 0 && y == 0) return ZERO;
		if (x == 1 && y == 1) return ONE;
		validateMinFp(x, 0, "X ratio");
		validateMinFp(y, 0, "Y ratio");
		return new Ratio2d(x + .0, y + .0);
	}

	private Ratio2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Ratio2d)) return false;
		Ratio2d other = (Ratio2d) obj;
		if (!Objects.equals(x, other.x)) return false;
		if (!Objects.equals(y, other.y)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + x + " x " + y + ")";
	}

}
