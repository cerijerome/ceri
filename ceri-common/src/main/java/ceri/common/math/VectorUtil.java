package ceri.common.math;

import static ceri.common.validation.ValidationUtil.validatef;
import ceri.common.exception.ExceptionUtil;
import ceri.common.geom.Point2d;

public class VectorUtil {
	private static final int SIZE_2 = 2;
	private static final int SIZE_3 = 3;
	private static final int SIZE_7 = 7;

	private VectorUtil() {}

	public static Point2d toPoint(Vector v) {
		verifySize(v, SIZE_2);
		return Point2d.of(v.valueAt(0), v.valueAt(1));
	}

	public static Vector fromPoint(Point2d point) {
		return Vector.of(point.x, point.y);
	}

	public static double crossProduct2d(Vector v1, Vector v2) {
		verifySize(v1, SIZE_2);
		verifySize(v2, SIZE_2);
		return (v1.valueAt(0) * v2.valueAt(1)) - (v1.valueAt(1) * v2.valueAt(0));
	}

	public static Vector simpleRound(Vector v, int decimals) {
		return Vector.of(MatrixUtil.simpleRound(v.matrix, decimals));
	}

	public static Vector crossProduct(Vector u, Vector v) {
		verifySameSize(u, v);
		if (u.size() == SIZE_7) throw ExceptionUtil.exceptionf(UnsupportedOperationException::new,
			"Cross product exists for size %d, but is not supported here", u.size());
		validatef(u.size() == SIZE_3, "Cross product only supported for size %d: %d", SIZE_3,
			u.size());
		return Vector.of((u.valueAt(1) * v.valueAt(2)) - (u.valueAt(2) * v.valueAt(1)),
			(u.valueAt(2) * v.valueAt(0)) - (u.valueAt(0) * v.valueAt(2)),
			(u.valueAt(0) * v.valueAt(1)) - (u.valueAt(1) * v.valueAt(0)));
	}

	public static void verifySameSize(Vector v1, Vector v2) {
		validatef(v1.size() == v2.size(), "Vectors must be the same size: %d, %d", v1.size(),
			v2.size());
	}

	public static void verifySize(Vector vector, int size) {
		validatef(vector.size() == size, "Vector size must be %d: %d", size, vector.size());
	}

}
