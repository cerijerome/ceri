package ceri.common.math;

import java.util.stream.Stream;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Vector {
	public final Matrix matrix;

	public static Vector of(double... values) {
		return new Vector(Matrix.columnVector(values));
	}

	public static Vector of(Matrix matrix) {
		if (matrix == null) return null;
		MatrixUtil.verifyVector(matrix);
		if (matrix.rows == 1) matrix = matrix.transpose();
		return new Vector(matrix);
	}

	private Vector(Matrix matrix) {
		this.matrix = matrix;
	}

	public int size() {
		return matrix.columns;
	}

	public double magnitude() {
		return Math.sqrt(scalarProduct(this));
	}

	public double scalarProduct(Vector vector) {
		verifySize(vector);
		return matrix.transpose().multiply(vector.matrix).valueAt(0, 0);
	}

	public double valueAt(int index) {
		return matrix.valueAt(index, 0);
	}

	public double[] values() {
		return Stream.of(matrix.values()).mapToDouble(r -> r[0]).toArray();
	}

	public Vector subVector(int offset, int size) {
		return Vector.of(matrix.subMatrix(offset, 0, size, 1));
	}

	public Vector wrappedSubVector(int offset, int size) {
		return Vector.of(matrix.wrappedSubMatrix(offset, 0, size, 1));
	}

	public Vector negate() {
		return Vector.of(matrix.negate());
	}

	public Vector add(Vector v) {
		verifySize(v);
		return Vector.of(matrix.add(v.matrix));
	}

	public Vector multiply(double scalar) {
		return Vector.of(matrix.multiply(scalar));
	}

	private void verifySize(Vector v) {
		VectorUtil.verifySize(v, size());
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(matrix);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Vector)) return false;
		Vector other = (Vector) obj;
		if (!EqualsUtil.equals(matrix, other.matrix)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, matrix).toString();
	}

}
