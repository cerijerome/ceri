package ceri.common.geom;

import ceri.common.util.Validate;

public record Ratio2d(double x, double y) {
	public static final Ratio2d ZERO = new Ratio2d(0, 0);
	public static final Ratio2d UNIT = new Ratio2d(1, 1);

	public static Ratio2d uniform(double scale) {
		return of(scale, scale);
	}

	public static Ratio2d of(double x, double y) {
		if (x == 0.0 && y == 0.0) return ZERO;
		if (x == 1.0 && y == 1.0) return UNIT;
		Validate.finiteMin(x, 0.0);
		Validate.finiteMin(y, 0.0);
		return new Ratio2d(x + 0.0, y + 0.0);
	}

	public boolean isZero() {
		return equals(ZERO);
	}
	
	public boolean isUnit() {
		return equals(UNIT);
	}

	public Ratio2d multiply(double x, double y) {
		return create(x() * x, y() * y);
	}
	
	public boolean equals(double x, double y) {
		return x() == x && y() == y;
	}
	
	@Override
	public String toString() {
		return "(" + x() + " x " + y() + ")";
	}
	
	private Ratio2d create(double x, double y) {
		if (equals(x, y)) return this;
		return of(x, y);
	}
}
