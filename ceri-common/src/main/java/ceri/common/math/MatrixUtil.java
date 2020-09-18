package ceri.common.math;

import static ceri.common.validation.ValidationUtil.validate;
import static ceri.common.validation.ValidationUtil.validatef;
import java.util.stream.IntStream;
import ceri.common.exception.ExceptionUtil;

public class MatrixUtil {
	private static final int SIZE_2 = 2;

	private MatrixUtil() {}

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

	public static Matrix simpleRound(Matrix m, int decimals) {
		Matrix.Builder b = Matrix.builder(m.rows, m.columns);
		for (int row = 0; row < m.rows; row++)
			for (int column = 0; column < m.columns; column++)
				b.set(row, column, MathUtil.simpleRound(decimals, m.valueAt(row, column)));
		return b.build();
	}

	public static void verifySquare(Matrix m) {
		validatef(m.isSquare(), "Matrix must be square: %dx%d", m.rows, m.columns);
	}

	public static void verifySquare(Matrix m, int size) {
		verifySize(m, size, size);
	}

	public static void verifyVector(Matrix m) {
		validatef(m.isVector(), "Matrix must be 1xN or Nx1: %dx%d", m.rows, m.columns);
	}

	public static void verifyVector(Matrix m, int size) {
		if (m.isEmpty() && size == 0) return;
		if (m.rows == 1 && m.columns == size) return;
		if (m.rows == size && m.columns == 1) return;
		throw ExceptionUtil.exceptionf("Matrix must be 1x%1$d or %1$dx1: %2$dx%3$d", size, m.rows,
			m.columns);
	}

	public static void verifySize(Matrix m, int rows, int columns) {
		validatef(m.rows == rows && m.columns == columns, "Matrix must be %dx%d: %dx%d", rows,
			columns, m.rows, m.columns);
	}

}
