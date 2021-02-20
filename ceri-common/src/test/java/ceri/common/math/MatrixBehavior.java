package ceri.common.math;

import static ceri.common.math.Matrix.r;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.common.test.CallSync;

public class MatrixBehavior {

	@Test
	public void shouldProvideStringRepresentation() {
		assertEquals(Matrix.constant(6, 6, 1).toString(), """
			Matrix(6x6) {
			  1.0, 1.0, 1.0, 1.0, ...
			  1.0, 1.0, 1.0, 1.0, ...
			  1.0, 1.0, 1.0, 1.0, ...
			  1.0, 1.0, 1.0, 1.0, ...
			  ...
			}""");
		assertEquals(Matrix.from(3, 1, 2, 3, 4, 5, 6, 7, 8, 9).toString(), """
			Matrix(3x3) {
			  1.0, 2.0, 3.0
			  4.0, 5.0, 6.0
			  7.0, 8.0, 9.0
			}""");
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		Matrix t = Matrix.of(new double[][] { { -1, 1, 0 }, { 1, 0, -1 } });
		Matrix eq0 = Matrix.of(new double[][] { { -1, 1, 0 }, { 1, 0, -1 } });
		Matrix eq1 = Matrix.of(new double[][] { { -1, 1 }, { 1, 0, -1 } });
		Matrix eq2 = Matrix.from(3, -1, 1, 0, 1, 0, -1);
		Matrix ne0 = Matrix.of(new double[][] { { -1, 1, 0 }, { 1, 0, -1 }, { 1, 0, -1 } });
		Matrix ne1 = Matrix.of(new double[][] { { -1, 1, 0, 0 }, { 1, 0, -1 } });
		Matrix ne2 = Matrix.of(new double[][] { { -1, 1, 0 }, { 1, 0, 1 } });
		Matrix ne3 = Matrix.from(2, -1, 1, 0, 1, 0, -1);
		Matrix ne4 = Matrix.from(4, -1, 1, 0, 1, 0, -1);
		Matrix ne5 = Matrix.from(3, -1, 1, 0, 1, 0);
		Matrix ne6 = Matrix.from(3, -1, 1, 0, 1, 0, -1, 0);
		Matrix ne7 = Matrix.from(0, 1, -1);
		Matrix ne8 = Matrix.of(-1, 1, 0, 1, 0, -1);
		Matrix ne9 = Matrix.EMPTY;
		Matrix ne10 = Matrix.I3;
		exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7, ne8, ne9, ne10);
	}

	@Test
	public void shouldCreateIdentityMatrices() {
		assertMatrix(Matrix.identity(0));
		assertMatrix(Matrix.identity(1), r(1));
		assertMatrix(Matrix.identity(2), r(1, 0), r(0, 1));
	}

	@Test
	public void shouldCreateRowVectors() {
		assertTrue(Matrix.of(new double[0]).isRow());
		assertTrue(Matrix.of(1, 2, 3).isRow());
		assertMatrix(Matrix.of(1, 2, 3), r(1, 2, 3));
	}

	@Test
	public void shouldTransposeMatrix() {
		assertMatrix(Matrix.vector(1, 2, 3).transpose(), r(1, 2, 3));
		assertMatrix(Matrix.from(2, 1, 2, 3, 4).transpose(), r(1, 3), r(2, 4));
		assertMatrix(Matrix.from(2, 1, 2, 3, 4, 5, 6).transpose(), r(1, 3, 5), r(2, 4, 6));
	}

	@Test
	public void shouldProvideVector() {
		assertMatrix(Matrix.of(1, 2, 3).vector(), r(1), r(2), r(3));
		assertMatrix(Matrix.vector(1, 2, 3).vector(), r(1), r(2), r(3));
		assertThrown(() -> Matrix.of(r(1, 2), r(3, 4)).vector());
	}
	
	@Test
	public void shouldProvideVectorOfGivenSize() {
		assertMatrix(Matrix.of(1, 2, 3).vector(3), r(1), r(2), r(3));
		assertMatrix(Matrix.vector(1, 2).vector(2), r(1), r(2));
		assertThrown(() -> Matrix.of(1, 2, 3).vector(2));
		assertThrown(() -> Matrix.of(1, 2).vector(3));
	}
	
	@Test
	public void shouldDetermineIfEmpty() {
		assertTrue(Matrix.EMPTY.isEmpty());
		assertTrue(Matrix.identity(0).isEmpty());
		assertTrue(Matrix.of(new double[][] {}).isEmpty());
		assertTrue(Matrix.of(new double[][] { {} }).isEmpty());
		assertFalse(Matrix.of(new double[][] { {}, { 0 } }).isEmpty());
	}

	@Test
	public void shouldDetermineIfSquare() {
		assertTrue(Matrix.EMPTY.isSquare());
		assertTrue(Matrix.identity(1).isSquare());
		assertFalse(Matrix.of(1, 2).isSquare());
	}

	@Test
	public void shouldDetermineIfRowVector() {
		assertTrue(Matrix.EMPTY.isRow());
		assertTrue(Matrix.of(1, 2).isRow());
		assertFalse(Matrix.vector(1, 2).isRow());
		assertTrue(Matrix.identity(1).isRow());
		assertFalse(Matrix.identity(2).isRow());
	}

	@Test
	public void shouldDetermineIfColumnVector() {
		assertTrue(Matrix.EMPTY.isColumn());
		assertFalse(Matrix.of(1, 2).isColumn());
		assertTrue(Matrix.vector(1, 2).isColumn());
		assertTrue(Matrix.identity(1).isColumn());
		assertFalse(Matrix.identity(2).isColumn());
	}

	@Test
	public void shouldExtractRow() {
		Matrix m = Matrix.of(r(1, 2), r(3, -1));
		assertMatrix(m.row(1), r(3, -1));
	}

	@Test
	public void shouldExtractRowValues() {
		Matrix m = Matrix.of(r(1, 2), r(3, -1));
		assertArray(m.rowValues(1), 3, -1);
	}

	@Test
	public void shouldExtractColumn() {
		Matrix m = Matrix.of(r(1, 2), r(3, -1));
		assertMatrix(m.column(1), r(2), r(-1));
	}

	@Test
	public void shouldExtractColumnValues() {
		Matrix m = Matrix.of(r(1, 2), r(3, -1));
		assertArray(m.columnValues(1), 2, -1);
	}

	@Test
	public void shouldExtractSubMatrix() {
		Matrix m = Matrix.of(r(1, -2, -1), r(-1, 3, 0));
		assertMatrix(m.sub(0, 0, 2, 3), r(1, -2, -1), r(-1, 3, 0));
		assertMatrix(m.sub(0, 0, 2, 2), r(1, -2), r(-1, 3));
		assertMatrix(m.sub(0, 0, 1, 3), r(1, -2, -1));
		assertMatrix(m.sub(0, 1, 2, 3), r(-2, -1, 1), r(3, 0, -1));
		assertMatrix(m.sub(1, 0, 2, 3), r(-1, 3, 0), r(1, -2, -1));
		assertMatrix(m.sub(1, 2, 2, 3), r(0, -1, 3), r(-1, 1, -2));
		assertMatrix(m.sub(1, 2, 0, 3));
		assertMatrix(m.sub(1, 2, 1, 0));
		assertThrown(() -> m.sub(2, 0, 1, 1));
		assertThrown(() -> m.sub(-1, 0, 1, 1));
		assertThrown(() -> m.sub(0, 3, 1, 1));
		assertThrown(() -> m.sub(0, -1, 1, 1));
	}

	@Test
	public void shouldAccessValue() {
		Matrix m = Matrix.of(r(1, -2, -1), r(-1, 3, 0));
		assertEquals(m.value(0, 0), 1.0);
		assertEquals(m.value(0, 1), -2.0);
		assertEquals(m.value(1, 2), 0.0);
		assertThrown(() -> m.value(-1, 0));
		assertThrown(() -> m.value(2, 0));
		assertThrown(() -> m.value(0, -1));
		assertThrown(() -> m.value(0, 3));
	}
	
	@Test
	public void shouldAddMatrices() {
		Matrix m = Matrix.identity(2);
		assertThrown(() -> m.add(Matrix.of(1, 2)));
		assertThrown(() -> m.add(Matrix.vector(1, 2)));
		assertMatrix(m.add(m), r(2, 0), r(0, 2));
	}

	@Test
	public void shouldMultiplyMatrices() {
		Matrix m = Matrix.of(r(1, 2, 3), r(-3, -1, -2));
		assertThrown(() -> m.multiply(Matrix.of(1, 2, 3)));
		assertThrown(() -> m.multiply(Matrix.vector(1, 2)));
		assertMatrix(m.multiply(Matrix.vector(1, -1, 0)), r(-1), r(-2));
		assertMatrix(m.multiply(Matrix.of(r(1, 0), r(0, 1), r(-1, -1))), r(-2, -1), r(-1, 1));
	}

	private static void assertMatrix(Matrix m, double[]... rows) {
		//assertArray(m.values(), values);
		assertEquals(m, Matrix.of(rows));
	}
}
