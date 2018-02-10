package ceri.common.math;

import static ceri.common.util.BasicUtil.formatted;
import static ceri.common.validation.ValidationUtil.validate;
import java.util.stream.IntStream;

public class MatrixUtil {
	private static final int SIZE_2 = 2;
	private static final int SIZE_3 = 3;
	private static final int SIZE_7 = 7;

	private MatrixUtil() {}

	public static void main(String[] args) {
		double[][] d = { { -5, 0, -1 }, { 1, 2, -1 }, { -3, 4, 1 } };
		Matrix m = Matrix.of(d);
		System.out.println(m);
		System.out.println(determinant(m));
		System.out.println(simpleRound(invert(m), 5));
		System.out.println(simpleRound(m.multiply(invert(m)), 5));
		System.out.println(simpleRound(invert(m).multiply(m), 5));
	}

	public static double scalarProduct(Matrix vector1, Matrix vector2) {
		verifyVector(vector1);
		verifyVector(vector2);
		if (vector1.rows != 1) vector1 = vector1.transpose();
		if (vector2.columns != 1) vector2 = vector2.transpose();
		return vector1.multiply(vector2).valueAt(0, 0);
	}

	public static double determinant(Matrix m) {
		verifySquare(m);
		if (m.rows == 0) return 0.0;
		if (m.rows == 1) return m.valueAt(0, 0);
		if (m.rows == SIZE_2)
			return (m.valueAt(0, 0) * m.valueAt(1, 1)) - (m.valueAt(0, 1) * m.valueAt(1, 0));
		return IntStream.range(0, m.columns).mapToDouble(column -> m.valueAt(0, column) *
			determinant(m.wrappedSubMatrix(1, column + 1, m.rows - 1, m.columns - 1))).sum();
	}

	public static Matrix invert(Matrix m) {
		verifySquare(m);
		if (m.rows == 0) return Matrix.EMPTY;
		if (m.rows > SIZE_2) return invertSize3Plus(m);
		double d = determinant(m);
		validate(d != 0.0, "Unable to invert a matrix with a determinant of 0");
		if (m.rows == 1) return Matrix.singleton(1 / d);
		return Matrix.builder(m.rows, m.rows) //
			.set(0, 0, m.valueAt(1, 1) / d).set(0, 1, -m.valueAt(0, 1) / d)
			.set(1, 0, -m.valueAt(1, 0) / d).set(1, 1, m.valueAt(0, 0) / d).build();
	}

	private static Matrix invertSize3Plus(Matrix m) {
		Matrix.Builder b = Matrix.builder(m.rows, m.rows);
		double d = 0.0;
		for (int row = 0; row < m.rows; row++) {
			for (int column = 0; column < m.columns; column++) {
				double determinant = determinant(minor(m, row, column));
				b.set(column, row, determinant);
				if (row == 0) d += m.valueAt(row, column) * determinant;
			}
		}
		validate(d != 0.0, "Unable to invert a matrix with a determinant of 0");
		return b.build().multiply(1 / d);
	}

	private static Matrix minor(Matrix m, int row, int column) {
		return m.wrappedSubMatrix(row + 1, column + 1, m.rows - 1, m.columns - 1);
	}

	public static double crossProduct2d(Matrix v1, Matrix v2) {
		verifyVector(v1, SIZE_2);
		verifyVector(v2, SIZE_2);
		if (v1.columns != 1) v1 = v1.transpose();
		if (v2.columns != 1) v2 = v2.transpose();
		return (v1.valueAt(0, 0) * v2.valueAt(0, 1)) - (v1.valueAt(0, 1) * v2.valueAt(0, 0));
	}

	public static Matrix simpleRound(Matrix m, int decimals) {
		Matrix.Builder b = Matrix.builder(m.rows, m.columns);
		for (int row = 0; row < m.rows; row++)
			for (int column = 0; column < m.columns; column++)
				b.set(row, column, MathUtil.simpleRound(m.valueAt(row, column), decimals));
		return b.build();
	}

	public static Matrix crossProduct(Matrix v1, Matrix v2) {
		verifyVector(v1);
		verifyVector(v2);
		if (v1.columns != 1) v1 = v1.transpose();
		if (v2.columns != 1) v2 = v2.transpose();
		validate(v1.rows == v2.rows, "Vectors must be the same size: %dx%d, %d%d", v1.rows,
			v1.columns, v2.rows, v2.columns);
		if (v1.rows == SIZE_7) throw formatted(UnsupportedOperationException::new,
			"Crossproduct exists for size %d, but is not supported here", v1.rows);
		validate(v1.rows == SIZE_3, "Crossproduct only supported for size %d: %dx1", SIZE_3,
			v1.rows);
		return Matrix.columnVector();
	}

	public static void verifySquare(Matrix m) {
		validate(m.isSquare(), "Matrix must be square: %dx%d", m.rows, m.columns);
	}

	public static void verifySquare(Matrix m, int size) {
		verifySize(m, size, size);
	}

	public static void verifyVector(Matrix m) {
		validate(m.isVector(), "Matrix must be 1xN or Nx1: %dx%d", m.rows, m.columns);
	}

	public static void verifyVector(Matrix m, int size) {
		validate((m.rows == 1 && m.columns == size) || (m.rows == size && m.columns == 1),
			"Matrix must be 1x%1$d or %1$dx1: %2$dx%3$d", size, m.rows, m.columns);
	}

	public static void verifySize(Matrix m, int rows, int columns) {
		validate(m.rows == rows && m.columns == columns, "Matrix must be %dx%d: %dx%d", rows,
			columns, m.rows, m.columns);
	}

}
