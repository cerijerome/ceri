package ceri.common.math;

import static ceri.common.validation.ValidationUtil.validate;
import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

public class Matrix {
	public static final Matrix EMPTY = builder().build();
	private final double[][] values;
	public final int rows;
	public final int columns;

	public static class Builder {
		double[][] values = {};
		int rows = 0;
		int columns = 0;

		Builder() {}

		public Builder rows(int rows) {
			return size(rows, columns);
		}

		public Builder columns(int columns) {
			return size(rows, columns);
		}

		public Builder size(int rows, int columns) {
			if (this.rows == rows && this.columns == columns) return this;
			validateMin(rows, 0);
			validateMin(columns, 0);
			this.rows = rows;
			this.columns = columns;
			values = resize(rows, columns, values);
			return this;
		}

		public Builder setAll(double[]... values) {
			validateNotNull(values);
			this.rows = rows(values);
			this.columns = columns(values);
			this.values = resize(rows, columns, values);
			return this;
		}

		public Builder set(int row, int column, double value) {
			validateMin(row, 0);
			validateMin(column, 0);
			size(Math.max(row + 1, this.rows), Math.max(column + 1, this.columns));
			set(row, column, values, value);
			return this;
		}

		public Builder setRow(int row, double... line) {
			validateMin(row, 0);
			validateNotNull(line);
			size(Math.max(row + 1, rows), Math.max(line.length, columns));
			for (int column = 0; column < line.length; column++)
				set(row, column, values, line[column]);
			return this;
		}

		public Builder addRow(double... line) {
			return setRow(rows, line);
		}

		public Builder setColumn(int column, double... line) {
			validateMin(column, 0);
			validateNotNull(line);
			size(Math.max(line.length, rows), Math.max(column + 1, columns));
			for (int row = 0; row < line.length; row++)
				set(row, column, values, line[row]);
			return this;
		}

		public Builder addColumn(double... line) {
			return setColumn(columns, line);
		}

		public Matrix build() {
			return new Matrix(this);
		}

		private static double[][] copy(double[][] from, double[][] to) {
			for (int row = 0; row < to.length; row++)
				for (int column = 0; column < to[row].length; column++)
					set(row, column, to, get(row, column, from));
			return to;
		}

		private static double[][] resize(int rows, int columns, double[][] values) {
			return copy(values, create(rows, columns));
		}

		private static int rows(double[][] values) {
			return values.length;
		}

		private static int columns(double[][] values) {
			return Stream.of(values).mapToInt(line -> line.length).max().orElse(0);
		}

		static double get(int row, int column, double[][] values) {
			if (row < 0 || row >= values.length) return 0.0;
			double[] line = values[row];
			if (column < 0 || column >= line.length) return 0.0;
			return line[column];
		}

		private static void set(int row, int column, double[][] values, double value) {
			// parameters already validated
			double[] line = values[row];
			line[column] = value + 0.0;
		}

		private static double[][] create(int rows, int columns) {
			return new double[rows][columns];
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(int rows, int columns) {
		return builder().size(rows, columns);
	}

	public static Matrix of(double[]... values) {
		return builder().setAll(values).build();
	}

	public static Matrix rowVector(double... values) {
		return builder().addRow(values).build();
	}

	public static Matrix columnVector(double... values) {
		return builder().addColumn(values).build();
	}

	public static Matrix singleton(double value) {
		return builder(1, 1).set(0, 0, value).build();
	}

	public static Matrix identity(int size) {
		Builder b = builder(size, size);
		for (int i = 0; i < size; i++)
			b.set(i, i, 1);
		return b.build();
	}

	Matrix(Builder builder) {
		values = builder.values;
		rows = builder.rows;
		columns = builder.columns;
	}

	public boolean isSquare() {
		return rows == columns;
	}

	public boolean isVector() {
		return isEmpty() || rows == 1 || columns == 1;
	}

	public boolean isEmpty() {
		return rows == 0 && columns == 0;
	}

	public double valueAt(int row, int column) {
		return Builder.get(row, column, values);
	}

	public Matrix transpose() {
		Builder b = builder(columns, rows);
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < columns; column++)
				b.set(column, row, valueAt(row, column));
		return b.build();
	}

	public Matrix row(int row) {
		return subMatrix(row, 0, 1, columns);
	}

	public Matrix column(int column) {
		return subMatrix(0, column, rows, 1);
	}

	/**
	 * Sub matrix with 0.0 values outside the rows and columns.
	 */
	public Matrix subMatrix(int offsetRow, int offsetColumn, int rows, int columns) {
		Builder b = builder(rows, columns);
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < columns; column++)
				b.set(row, column, Builder.get(row + offsetRow, column + offsetColumn, values));
		return b.build();
	}

	/**
	 * Sub matrix that allows row and columns indexes to wrap.
	 */
	public Matrix wrappedSubMatrix(int offsetRow, int offsetColumn, int rows, int columns) {
		Builder b = builder(rows, columns);
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < columns; column++)
				b.set(row, column, getWrapped(row + offsetRow, column + offsetColumn, values));
		return b.build();
	}

	private double getWrapped(int row, int column, double[][] values) {
		return Builder.get(wrappedIndex(row, rows), wrappedIndex(column, columns), values);
	}

	private static int wrappedIndex(int value, int max) {
		while (value >= max)
			value -= max;
		while (value < 0)
			value += max;
		return value;
	}

	public Matrix negate() {
		Builder b = builder(rows, columns);
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < columns; column++)
				b.set(row, column, -valueAt(row, column) + 0.0);
		return b.build();
	}

	public Matrix add(Matrix m) {
		validate(rows == m.rows && columns == m.columns, "Matrix must be %dx%d: %dx%d", rows,
			columns, m.rows, m.columns);
		Builder b = builder(rows, m.columns);
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < columns; column++)
				b.set(row, column, valueAt(row, column) + m.valueAt(row, column));
		return b.build();
	}

	public Matrix multiply(double scalar) {
		Builder b = builder(rows, columns);
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < columns; column++)
				b.set(row, column, valueAt(row, column) * scalar);
		return b.build();
	}

	public Matrix multiply(Matrix m) {
		validate(columns == m.rows, "Matrix must have %d rows: %d", columns, m.rows);
		Builder b = builder(rows, m.columns);
		for (int row = 0; row < rows; row++)
			for (int column = 0; column < m.columns; column++)
				b.set(row, column, multiplyLine(row, column, m));
		return b.build();
	}

	private double multiplyLine(int row, int column, Matrix m) {
		return IntStream.range(0, columns).mapToDouble( //
			i -> valueAt(row, i) * m.valueAt(i, column)).sum();
	}

	public double[][] values() {
		return values.clone();
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(rows, columns, values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Matrix)) return false;
		Matrix other = (Matrix) obj;
		if (rows != other.rows) return false;
		if (columns != other.columns) return false;
		if (!Arrays.deepEquals(values, other.values)) return false;
		return true;
	}

	@SuppressWarnings("RedundantCast")
	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, String.format("%dx%d", rows, columns))
			.children((Object[]) values).toString();
	}

}
