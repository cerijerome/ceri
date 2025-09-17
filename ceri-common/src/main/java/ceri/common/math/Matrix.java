package ceri.common.math;

import java.util.stream.Stream;
import ceri.common.exception.Exceptions;
import ceri.common.function.Functions;
import ceri.common.text.StringBuilders;
import ceri.common.text.ToString;
import ceri.common.util.Hasher;
import ceri.common.util.Validate;

/**
 * An immutable virtual matrix of (rows x columns) double values. Internally, the class uses a value
 * accessor function, which may or may not map to an actual array. Many operations on the matrix are
 * applied by adding a new accessor layer rather than generating a new value array. If the accessor
 * becomes too complex, copy() can be used to generate the value array, with a new direct accessor.
 */
public class Matrix {
	private static final int STR_MAX_N = 5;
	public static final Matrix EMPTY = new Matrix(null, 0, 0);
	public static final Matrix I1 = identity(1);
	public static final Matrix I2 = identity(2);
	public static final Matrix I3 = identity(3);
	private final Accessor accessor;
	public final int rows;
	public final int columns;

	/**
	 * Provides indirect access to matrix values.
	 */
	private static interface Accessor {
		double get(int r, int c);
	}

	/**
	 * Convenience method for creating a double[n] matrix data row.
	 */
	public static double[] r(double... row) {
		return row;
	}

	/**
	 * Convenience method for creating a double[n][1] matrix data column.
	 */
	public static double[][] c(double... column) {
		double[][] values = new double[column.length][1];
		for (int i = 0; i < column.length; i++)
			values[i] = r(column[i]);
		return values;
	}

	/**
	 * Creates a matrix from number of columns and wrapping value sequence. The number of rows is
	 * that which minimally fits the sequence. The values are accessed by column in rows, starting
	 * at 0 and wrapping to the next.
	 */
	public static Matrix from(int columns, double... seq) {
		if (columns == 0) return EMPTY;
		int rows = (seq.length + columns - 1) / columns;
		return of((r, c) -> {
			int i = r * columns + c;
			return i < seq.length ? seq[i] : 0;
		}, rows, columns);
	}

	/**
	 * Creates an constant matrix of given size.
	 */
	public static Matrix constant(int rows, int columns, double value) {
		return of((_, _) -> value, rows, columns);
	}

	/**
	 * Creates an identity matrix of given size.
	 */
	public static Matrix identity(int size) {
		return of((r, c) -> r == c ? 1 : 0, size, size);
	}

	/**
	 * Creates a column vector.
	 */
	public static Matrix vector(double... row) {
		return of((r, _) -> row[r], row.length, 1);
	}

	/**
	 * Creates a row vector.
	 */
	public static Matrix ofRow(double... row) {
		return of((_, c) -> row[c], 1, row.length);
	}

	/**
	 * Creates a matrix for an array, using the given row and column sizes. 0 is returned for any
	 * value outside the array.
	 */
	public static Matrix of(int rows, int columns, double[]... values) {
		return of((r, c) -> get(values, r, c), rows, columns);
	}

	/**
	 * Creates a matrix for an array, using the maximum column size. 0 is returned for any value
	 * outside the array.
	 */
	public static Matrix of(double[]... values) {
		return of(values.length, maxColumns(values), values);
	}

	/**
	 * Creates a matrix of given size with virtual value accessor.
	 */
	public static Matrix of(Accessor accessor, int rows, int columns) {
		Validate.validateMin(rows, 0, "Rows");
		Validate.validateMin(columns, 0, "Columns");
		if (rows == 0 || columns == 0) return EMPTY;
		Validate.validateNotNull(accessor);
		return new Matrix(accessor, rows, columns);
	}

	Matrix(Accessor accessor, int rows, int columns) {
		this.accessor = accessor;
		this.rows = rows;
		this.columns = columns;
	}

	/**
	 * Transpose the matrix.
	 */
	public Matrix transpose() {
		return new Matrix((r, c) -> accessor.get(c, r), columns, rows);
	}

	/**
	 * Returns true if the matrix is square.
	 */
	public boolean isSquare() {
		return rows == columns;
	}

	/**
	 * Returns true if this is a row vector.
	 */
	public boolean isRow() {
		return isEmpty() || rows == 1;
	}

	/**
	 * Returns true if this is a column vector.
	 */
	public boolean isColumn() {
		return isEmpty() || columns == 1;
	}

	/**
	 * Returns this matrix as a column vector, transposing if this is a row vector. Throws an
	 * exception if this matrix is not a row or column vector.
	 */
	public Matrix vector() {
		return validateColumnVector(this);
	}

	/**
	 * Returns this matrix as a column vector, transposing if this is a row vector. Throws an
	 * exception if this matrix is not a row or column vector, or the vector is not the required
	 * size.
	 */
	public Matrix vector(int size) {
		Matrix m = vector();
		Validate.validateEqual(m.rows, size, "Size");
		return m;
	}

	/**
	 * Returns true if this matrix has no elements.
	 */
	public boolean isEmpty() {
		return rows == 0;
	}

	/**
	 * Returns a row vector view.
	 */
	public Matrix row(int row) {
		validateRow(row);
		return new Matrix((r, c) -> get(row + r, c), 1, columns);
	}

	/**
	 * Returns a column vector view.
	 */
	public Matrix column(int column) {
		validateColumn(column);
		return new Matrix((r, c) -> get(r, column + c), rows, 1);
	}

	/**
	 * Copies the row values as a double array.
	 */
	public double[] rowValues(int row) {
		validateRow(row);
		double[] copy = new double[columns];
		for (int c = 0; c < columns; c++)
			copy[c] = get(row, c);
		return copy;
	}

	/**
	 * Copies the column values as a double array.
	 */
	public double[] columnValues(int column) {
		validateColumn(column);
		double[] copy = new double[rows];
		for (int r = 0; r < rows; r++)
			copy[r] = get(r, column);
		return copy;
	}

	/**
	 * Returns a sub-matrix view. Row and column offsets will wrap, and can be negative. The row and
	 * column counts cannot exceed current counts.
	 */
	public Matrix sub(int row, int column, int rows, int columns) {
		Validate.validateRange(rows, 0, this.rows, "Rows");
		Validate.validateRange(columns, 0, this.columns, "Columns");
		if (isEmpty() || rows == 0 || columns == 0) return EMPTY;
		int r0 = Math.floorMod(row, this.rows);
		int c0 = Math.floorMod(column, this.columns);
		if (r0 == 0 && c0 == 0 && rows == this.rows && columns == this.columns) return this;
		// Use accessor directly if no wrapping
		if (r0 + rows <= this.rows && c0 + columns <= this.columns)
			return new Matrix((r, c) -> accessor.get(r0 + r, c0 + c), rows, columns);
		return new Matrix((r, c) -> get(r0 + r, c0 + c), rows, columns);
	}

	/**
	 * Returns the value at given row and column. Throws an exception if outside bounds.
	 */
	public double at(int row, int column) {
		validateRow(row);
		validateColumn(column);
		return get(row, column);
	}

	/**
	 * Returns a copy of the values.
	 */
	public double[][] values() {
		double[][] values = new double[rows][columns];
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < columns; c++)
				values[r][c] = get(r, c);
		return values;
	}

	/**
	 * Returns a matrix with all values incremented by a scalar value.
	 */
	public Matrix add(double scalar) {
		if (scalar == 0.0) return this;
		return apply(x -> scalar + x);
	}

	/**
	 * Returns the sum of this and the given matrix.
	 */
	public Matrix add(Matrix m) {
		Validate.validateEqual(m.rows, rows, "Rows");
		Validate.validateEqual(m.columns, columns, "Columns");
		if (isEmpty()) return this;
		return new Matrix((r, c) -> get(r, c) + m.get(r, c), rows, columns);
	}

	/**
	 * Returns a matrix with all values negated.
	 */
	public Matrix negate() {
		return multiply(-1);
	}

	/**
	 * Returns a matrix with all values multiplied by a scalar value.
	 */
	public Matrix multiply(double scalar) {
		if (scalar == 1.0) return this;
		return apply(x -> scalar * x);
	}

	/**
	 * Returns a matrix with a scalar operator applied to all values. The operator is only applied
	 * when a value is accessed.
	 */
	public Matrix apply(Functions.DoubleOperator scalarFn) {
		if (isEmpty()) return this;
		// Add 0.0 to prevent -0.0
		return new Matrix((r, c) -> 0 + scalarFn.applyAsDouble(accessor.get(r, c)), rows, columns);
	}

	/**
	 * Returns the matrix product of this and the given matrix. Throws an exception if the matrix
	 * row count does not match this matrix column count.
	 */
	public Matrix multiply(Matrix m) {
		Validate.validateEqual(m.rows, columns, "Rows");
		if (isEmpty()) return this;
		double[][] values = new double[rows][m.columns];
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < m.columns; c++)
				for (int i = 0; i < columns; i++)
					values[r][c] += get(r, i) * m.get(i, c);
		return new Matrix(accessor(values), rows, m.columns);
	}

	/**
	 * Returns the dot product of this and the given matrix. Throws an exception if this is not a
	 * vector, the other matrix is not a vector, or the vector sizes do not match.
	 */
	public double dot(Matrix m) {
		return dot(this, m);
	}

	/**
	 * Returns the 2d cross product of this and the given matrix. Throws an exception if both are
	 * not vectors of 2.
	 */
	public double cross2d(Matrix m) {
		return cross2d(this, m);
	}

	/**
	 * Returns the cross product vector of this and the given matrix. Throws an exception if this is
	 * not a vector, the other matrix is not a vector, or the vector sizes do not match. Currently
	 * only vectors of size 3 are supported.
	 */
	public Matrix cross(Matrix vector) {
		return cross(this, vector);
	}

	/**
	 * Returns the magnitude of this vector. Throws an exception is this matrix is not a vector.
	 */
	public double magnitude() {
		return Math.sqrt(quadrance());
	}

	/**
	 * Returns the squared magnitude of this vector. Throws an exception is this matrix is not a
	 * vector.
	 */
	public double quadrance() {
		Validate.validatef(isColumn() || isRow(), "Matrix is not a vector: %dx%d", rows, columns);
		double sum = 0;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < columns; c++)
				sum += sqr(get(r, c));
		return sum;
	}

	/**
	 * Returns the determinant of this matrix. Throws an exception if not square.
	 */
	public double determinant() {
		validateSquare();
		if (rows == 0) return 0.0;
		if (rows == 1) return get(0, 0);
		if (rows == 2) return get(0, 0) * get(1, 1) - get(0, 1) * get(1, 0);
		double sum = 0;
		for (int c = 0; c < columns; c++) {
			sum += dsign(0, c) * get(0, c) * sub(1, c + 1, rows - 1, columns - 1).determinant();
		}
		return sum;
	}

	/**
	 * Inverts the matrix, or returns null if the determinant is 0. Throws an exception if not
	 * square.
	 */
	public Matrix invert() {
		validateSquare();
		if (rows == 0) return this;
		if (rows > 2) return invertNxN();
		double d = determinant();
		if (d == 0.0) return null;
		return rows == 1 ? ofRow(1 / d) : invert2x2(d);
	}

	/**
	 * Makes a copy of this matrix. Allocates a new array, with direct accessor. Useful to simplify
	 * an accessor that has multiple layers.
	 */
	public Matrix copy() {
		return new Matrix(accessor(values()), rows, columns);
	}

	@Override
	public int hashCode() {
		Hasher h = Hasher.of().hash(rows).hash(columns);
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < columns; c++)
				h.hash(get(r, c));
		return h.code();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Matrix other)) return false;
		if (rows != other.rows) return false;
		if (columns != other.columns) return false;
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < columns; c++)
				if (longBits(r, c) != other.longBits(r, c)) return false;
		return true;
	}

	@Override
	public String toString() {
		return toString(STR_MAX_N);
	}

	/* instance support methods */

	private String toString(int max) {
		ToString s = ToString.ofClass(this, rows + "x" + columns);
		int nr = rows > max ? max - 1 : rows;
		int nc = columns > max ? max - 1 : columns;
		StringBuilder b = new StringBuilder();
		for (int r = 0; r < nr; r++) {
			StringBuilders.clear(b);
			appendRow(b, r, nc);
			s.children(b.toString());
		}
		if (nr < rows) s.children("...");
		return s.toString();
	}

	private void appendRow(StringBuilder b, int r, int nc) {
		for (int c = 0; c < nc; c++) {
			if (c > 0) b.append(", ");
			b.append(get(r, c));
		}
		if (nc < columns) b.append(", ...");
	}

	private long longBits(int r, int c) {
		return Double.doubleToLongBits(get(r, c));
	}

	private Matrix invert2x2(double d) {
		double[][] values =
			{ { get(1, 1) / d, -get(0, 1) / d }, { -get(1, 0) / d, get(0, 0) / d } };
		return new Matrix(accessor(values), 2, 2);
	}

	private Matrix invertNxN() {
		double[][] values = new double[rows][columns];
		double d = invertRow0(values);
		if (d == 0.0) return null;
		invertRows1Plus(values, d);
		return new Matrix(accessor(values), rows, columns);
	}

	private double invertRow0(double[][] values) {
		double d = 0.0;
		for (int c = 0; c < columns; c++) {
			values[c][0] = dsign(0, c) * minor(0, c).determinant();
			d += get(0, c) * values[c][0];
		}
		if (d != 0.0) for (int c = 0; c < columns; c++)
			values[c][0] /= d;
		return d;
	}

	private void invertRows1Plus(double[][] values, double d) {
		for (int r = 1; r < rows; r++)
			for (int c = 0; c < columns; c++)
				values[c][r] = dsign(r, c) * minor(r, c).determinant() / d;
	}

	private Matrix minor(int row, int column) {
		return sub(row + 1, column + 1, rows - 1, columns - 1);
	}

	private int dsign(int r, int c) {
		// alternate determinant sign for even size matrix
		return (rows & 1) == 0 && ((r + c) & 1) == 1 ? -1 : 1;
	}

	private double get(int r, int c) {
		return accessor.get(r % rows, c % columns);
	}

	private void validateRow(int r) {
		Validate.validateRange(r, 0, rows - 1, "Row");
	}

	private void validateColumn(int c) {
		Validate.validateRange(c, 0, columns - 1, "Column");
	}

	private void validateSquare() {
		Validate.validatef(isSquare(), "Matrix is not square: %dx%d", rows, columns);
	}

	private static Matrix validateRowVector(Matrix m) {
		if (m.isRow()) return m;
		if (m.isColumn()) return m.transpose();
		throw Exceptions.illegalArg("Matrix is not a vector: %dx%d", m.rows, m.columns);
	}

	private static Matrix validateColumnVector(Matrix m) {
		if (m.isColumn()) return m;
		if (m.isRow()) return m.transpose();
		throw Exceptions.illegalArg("Matrix is not a vector: %dx%d", m.rows, m.columns);
	}

	private static double dot(Matrix rv, Matrix cv) {
		rv = validateRowVector(rv);
		cv = validateColumnVector(cv);
		Validate.validateEqual(cv.rows, rv.columns, "Rows");
		double sum = 0;
		for (int i = 0; i < rv.columns; i++)
			sum += rv.get(0, i) * cv.get(i, 0);
		return sum;
	}

	private static double cross2d(Matrix u, Matrix v) {
		u = validateColumnVector(u);
		v = validateColumnVector(v);
		Validate.validateEqual(u.rows, 2, "Size");
		Validate.validateEqual(v.rows, 2, "Size");
		return u.get(0, 0) * v.get(1, 0) - u.get(1, 0) * v.get(0, 0);
	}

	private static Matrix cross(Matrix u, Matrix v) {
		u = validateColumnVector(u);
		v = validateColumnVector(v);
		Validate.validateEqual(v.rows, u.rows, "Size");
		Validate.validate(u.rows != 7, "Cross product exists for size 7, but is unsupported");
		Validate.validatef(u.rows == 3, "Cross product only supported for size 3: %d", u.columns);
		return Matrix.vector( //
			u.get(1, 0) * v.get(2, 0) - u.get(2, 0) * v.get(1, 0),
			u.get(2, 0) * v.get(0, 0) - u.get(0, 0) * v.get(2, 0),
			u.get(0, 0) * v.get(1, 0) - u.get(1, 0) * v.get(0, 0));
	}

	private static Accessor accessor(double[][] values) {
		return (r, c) -> 0.0 + values[r][c]; // prevent -0.0
	}

	private static double get(double[][] values, int r, int c) {
		if (r >= values.length) return 0;
		double[] row = values[r];
		return c >= row.length ? 0 : row[c];
	}

	private static int maxColumns(double[][] values) {
		return Stream.of(values).mapToInt(row -> row.length).max().orElse(0);
	}

	private static double sqr(double x) {
		return x * x;
	}
}
