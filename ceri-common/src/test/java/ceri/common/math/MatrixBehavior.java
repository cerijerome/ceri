package ceri.common.math;

import static ceri.common.math.Matrix.c;
import static ceri.common.math.Matrix.r;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.util.function.DoubleUnaryOperator;
import org.junit.Test;

/**
 * Use https://matrixcalc.org/en/ to test results.
 */
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
		Matrix ne8 = Matrix.ofRow(-1, 1, 0, 1, 0, -1);
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
		assertTrue(Matrix.ofRow(1, 2, 3).isRow());
		assertMatrix(Matrix.ofRow(1, 2, 3), r(1, 2, 3));
	}

	@Test
	public void shouldTransposeMatrix() {
		assertMatrix(Matrix.vector(1, 2, 3).transpose(), r(1, 2, 3));
		assertMatrix(Matrix.from(2, 1, 2, 3, 4).transpose(), r(1, 3), r(2, 4));
		assertMatrix(Matrix.from(2, 1, 2, 3, 4, 5, 6).transpose(), r(1, 3, 5), r(2, 4, 6));
	}

	@Test
	public void shouldProvideVector() {
		assertMatrix(Matrix.ofRow(1, 2, 3).vector(), c(1, 2, 3));
		assertMatrix(Matrix.vector(1, 2, 3).vector(), c(1, 2, 3));
		assertThrown(() -> Matrix.of(r(1, 2), r(3, 4)).vector());
	}

	@Test
	public void shouldProvideVectorOfGivenSize() {
		assertMatrix(Matrix.ofRow(1, 2, 3).vector(3), c(1, 2, 3));
		assertMatrix(Matrix.vector(1, 2).vector(2), c(1, 2));
		assertThrown(() -> Matrix.ofRow(1, 2, 3).vector(2));
		assertThrown(() -> Matrix.ofRow(1, 2).vector(3));
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
		assertFalse(Matrix.ofRow(1, 2).isSquare());
	}

	@Test
	public void shouldDetermineIfRowVector() {
		assertTrue(Matrix.EMPTY.isRow());
		assertTrue(Matrix.ofRow(1, 2).isRow());
		assertFalse(Matrix.vector(1, 2).isRow());
		assertTrue(Matrix.identity(1).isRow());
		assertFalse(Matrix.identity(2).isRow());
	}

	@Test
	public void shouldDetermineIfColumnVector() {
		assertTrue(Matrix.EMPTY.isColumn());
		assertFalse(Matrix.ofRow(1, 2).isColumn());
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
		assertMatrix(m.sub(1, 3, 1, 0));
		assertMatrix(m.sub(2, 0, 1, 1), r(1));
		assertMatrix(m.sub(-1, -2, 1, 1), r(3));
		assertMatrix(Matrix.EMPTY.sub(1, 1, 0, 0));
		assertThrown(() -> m.sub(0, 0, 3, 1));
		assertThrown(() -> m.sub(1, 1, 2, 4));
		assertThrown(() -> Matrix.EMPTY.sub(0, 0, 1, 0));
	}

	@Test
	public void shouldAccessValue() {
		Matrix m = Matrix.of(r(1, -2, -1), r(-1, 3, 0));
		assertEquals(m.at(0, 0), 1.0);
		assertEquals(m.at(0, 1), -2.0);
		assertEquals(m.at(1, 2), 0.0);
		assertThrown(() -> m.at(-1, 0));
		assertThrown(() -> m.at(2, 0));
		assertThrown(() -> m.at(0, -1));
		assertThrown(() -> m.at(0, 3));
	}

	@Test
	public void shouldAccessMissingValues() {
		Matrix m = Matrix.of(3, 3, r(1, 2), r(3, 2, 1));
		assertEquals(m.at(0, 0), 1.0);
		assertEquals(m.at(0, 2), 0.0);
		assertEquals(m.at(2, 2), 0.0);
	}

	@Test
	public void shouldCopyValues() {
		assertArray(Matrix.of(r(1, -2, -1), r(-1, 3, 0)).values(), r(1, -2, -1), r(-1, 3, 0));
		assertArray(Matrix.EMPTY.values());
	}

	@Test
	public void shouldCopyMatrix() {
		assertMatrix(Matrix.from(4, 0, 1, 2, 3, 1, 2, 3, 0).negate().multiply(2).transpose().copy(),
			2, 0, -2, -2, -4, -4, -6, -6, 0);
	}

	@Test
	public void shouldAddScalar() {
		assertMatrix(Matrix.EMPTY.add(1));
		assertMatrix(Matrix.I3.add(0), Matrix.I3);
		assertMatrix(Matrix.I2.add(1), 2, 2, 1, 1, 2);
	}
	
	@Test
	public void shouldAddMatrices() {
		Matrix m = Matrix.identity(2);
		assertMatrix(m.add(m), r(2, 0), r(0, 2));
		assertMatrix(Matrix.EMPTY.add(Matrix.EMPTY));
		assertThrown(() -> m.add(Matrix.EMPTY));
		assertThrown(() -> m.add(Matrix.ofRow(1, 2)));
		assertThrown(() -> m.add(Matrix.vector(1, 2)));
	}

	@Test
	public void shouldNegateMatrix() {
		assertMatrix(Matrix.EMPTY.negate());
		assertMatrix(Matrix.I2.negate(), r(-1, 0.0), r(0.0, -1));
	}

	@Test
	public void shouldMultiplyByScalar() {
		assertMatrix(Matrix.EMPTY.multiply(1));
		assertMatrix(Matrix.I2.multiply(0.5), r(0.5, 0.0), r(0.0, 0.5));
	}

	@Test
	public void shouldApplyScalarFunction() {
		DoubleUnaryOperator fn = d -> MathUtil.simpleRound(1, d);
		assertMatrix(Matrix.EMPTY.apply(fn));
		assertMatrix(Matrix.of(r(0.333, 0.651), r(0, 1)).apply(fn), r(0.3, 0.7), r(0, 1));
	}

	@Test
	public void shouldMultiplyMatrices() {
		Matrix m = Matrix.of(r(1, 2, 3), r(-3, -1, -2));
		assertMatrix(Matrix.EMPTY.multiply(Matrix.EMPTY));
		assertMatrix(m.multiply(Matrix.vector(1, -1, 0)), r(-1), r(-2));
		assertMatrix(m.multiply(Matrix.of(r(1, 0), r(0, 1), r(-1, -1))), r(-2, -1), r(-1, 1));
		assertThrown(() -> m.multiply(Matrix.EMPTY));
		assertThrown(() -> m.multiply(Matrix.ofRow(1, 2, 3)));
		assertThrown(() -> m.multiply(Matrix.vector(1, 2)));
	}

	@Test
	public void shouldApplyDotProduct() {
		assertEquals(Matrix.EMPTY.dot(Matrix.EMPTY), 0.0);
		assertEquals(Matrix.ofRow(1).dot(Matrix.ofRow(-1)), -1.0);
		assertEquals(Matrix.ofRow(1, -1, 0).dot(Matrix.vector(3, 2, 1)), 1.0);
		assertEquals(Matrix.vector(1, -1, 0).dot(Matrix.ofRow(3, 2, 1)), 1.0);
		assertEquals(Matrix.ofRow(1, -1, 0).dot(Matrix.ofRow(3, 2, 1)), 1.0);
		assertThrown(() -> Matrix.EMPTY.dot(Matrix.ofRow(1)));
		assertThrown(() -> Matrix.ofRow(1).dot(Matrix.EMPTY));
		assertThrown(() -> Matrix.I2.dot(Matrix.I1));
	}

	@Test
	public void shouldApply2dCrossProduct() {
		assertEquals(Matrix.vector(1, 2).cross2d(Matrix.vector(-1, 3)), 5.0);
		assertEquals(Matrix.ofRow(1, 2).cross2d(Matrix.ofRow(-1, 3)), 5.0);
		assertThrown(() -> Matrix.ofRow(1, 2, 0).cross2d(Matrix.ofRow(-1, 3, 0)));
		assertThrown(() -> Matrix.EMPTY.cross2d(Matrix.EMPTY));
		assertThrown(() -> Matrix.I1.cross2d(Matrix.I1));
		assertThrown(() -> Matrix.I2.cross2d(Matrix.I2));
	}

	@Test
	public void shouldApplyCrossProduct() {
		assertMatrix(Matrix.vector(1, 2, -1).cross(Matrix.vector(-1, -1, 2)), c(3, -1, 1));
		assertMatrix(Matrix.ofRow(1, 2, -1).cross(Matrix.ofRow(-1, -1, 2)), c(3, -1, 1));
		assertThrown(() -> Matrix.vector(1, 2, 3, 4).cross(Matrix.vector(1, 2, 3, 4)));
		assertThrown(() -> Matrix.vector(1, 2, 3, 4, 5, 6, 7).cross(Matrix.constant(7, 1, 0)));
	}

	@Test
	public void shouldCalculateVectorMagnitude() {
		assertEquals(Matrix.vector(2, 1, 2).magnitude(), 3.0);
		assertEquals(Matrix.ofRow(0, 3, 4).magnitude(), 5.0);
		assertEquals(Matrix.I1.magnitude(), 1.0);
		assertEquals(Matrix.EMPTY.magnitude(), 0.0);
		assertThrown(() -> Matrix.I2.magnitude());
	}

	@Test
	public void shouldCalculateVectorQuarance() {
		assertEquals(Matrix.vector(1, 2, 3).quadrance(), 14.0);
	}

	@Test
	public void shouldCalculateDeterminant() {
		assertEquals(Matrix.of(r(3)).determinant(), 3.0);
		assertEquals(Matrix.EMPTY.determinant(), 0.0);
		assertEquals(Matrix.I3.determinant(), 1.0);
		assertEquals(Matrix.from(2, 1, 2, 0, -1).determinant(), -1.0);
		assertEquals(Matrix.from(3, 1, 0, 2, -1, 1, -1, 1, 2, -1).determinant(), -5.0);
		assertEquals(Matrix.from(4, //
			1, 2, -1, -1, //
			1, 1, 1, -1, //
			-1, 2, 1, 0, //
			0, 1, 1, -1).determinant(), 5.0);
		assertEquals(Matrix.from(5, //
			2, -1, 1, -1, 1, //
			-1, 1, 1, -1, -2, //
			1, 1, 1, -1, -1, //
			1, -1, 2, -2, 1, //
			1, 1, 1, 1, 1).determinant(), 10.0);
		assertEquals(Matrix.from(6, //
			1, -1, -1, 1, -1, 1, //
			1, -1, 1, 1, 0, -1, //
			1, -2, 2, 1, 1, -1, //
			1, -1, 1, -1, 1, 1, //
			1, -1, 0, -1, 1, 1, //
			-1, 1, 1, -1, -1, 1).determinant(), 4.0);
		assertEquals(Matrix.from(7, //
			1, -1, -1, 1, -1, 1, 1, //
			1, -1, 1, 1, 0, -1, 1, //
			1, -2, 2, 1, 1, -1, 1, //
			1, -1, 1, -1, 1, 1, 1, //
			1, -1, 0, -1, 1, 1, 1, //
			-1, 1, 1, -1, -1, 1, 0, //
			1, 0, -1, 1, 1, 1, -1).determinant(), 4.0);
		assertEquals(Matrix.constant(8, 8, 2).determinant(), 0.0);
	}

	@Test
	public void shouldFailIfNotInvertible() {
		assertNull(Matrix.from(2, 1, 1, 1, 1).invert());
		assertNull(Matrix.constant(8, 8, 2).invert());
		assertNull(Matrix.from(2, 3, 4, 6, 8).invert());
		assertThrown(() -> Matrix.of(r(1, 2, 3), r(1, 0, 1)).invert());
		assertThrown(() -> Matrix.of(c(1, 2)).invert());
	}

	@Test
	public void shouldInvert() {
		assertMatrix(Matrix.EMPTY.invert());
		assertMatrix(Matrix.of(r(5)).invert(), r(.2));
		assertMatrix(Matrix.I2.invert(), Matrix.I2);
		assertMatrix(Matrix.I3.invert(), Matrix.I3);
		assertMatrix(Matrix.from(2, 1, 2, 0, -1), 2, 1, 2, 0, -1);
		assertMatrix(Matrix.from(3, 1, 0, 2, -1, 1, -1, 1, 2, -1).invert(), 3, //
			-.2, -.8, .4, .4, .6, .2, .6, .4, -.2);
		assertMatrix(Matrix.from(4, //
			1, 2, -1, -1, //
			1, 1, 1, -1, //
			-1, 2, 1, 0, //
			0, 1, 1, -1).invert(), 4, //
			0, 1, 0, -1, //
			.2, .2, .4, -.4, //
			-.4, .6, .2, -.2, //
			-.2, .8, .6, -1.6);
		assertMatrix(Matrix.from(5, //
			2, -1, 1, -1, 1, //
			-1, 1, 1, -1, -2, //
			1, 1, 1, -1, -1, //
			1, -1, 2, -2, 1, //
			1, 1, 1, 1, 1).invert(), 5, //
			.6, 0, .2, -.4, 0, //
			-1.4, -1, 1.2, .6, 0, //
			.8, 1, -.9, -.2, .5, //
			1.2, 1, -1.1, -.8, .5, //
			-1.2, -1, .6, .8, 0);
		assertMatrix(Matrix.from(6, //
			1, -1, -1, 1, -1, 1, //
			1, -1, 1, 1, 0, -1, //
			1, -2, 2, 1, 1, -1, //
			1, -1, 1, -1, 1, 1, //
			1, -1, 0, -1, 1, 1, //
			-1, 1, 1, -1, -1, 1).invert(), 6, //
			0, 1, -1, 1.5, -1, -.5, //
			0, 0, -1, 3, -3, -1, //
			0, 0, 0, 1, -1, 0, //
			.5, -1, 0, 2.5, -3, -1, //
			0, -1, 0, 2, -2, -1, //
			.5, -1, 0, 2, -2, -.5);
		assertMatrix(Matrix.from(7, //
			1, -1, -1, 1, -1, 1, 1, //
			1, -1, 1, 1, 0, -1, 1, //
			1, -2, 2, 1, 1, -1, 1, //
			1, -1, 1, -1, 1, 1, 1, //
			1, -1, 0, -1, 1, 1, 1, //
			-1, 1, 1, -1, -1, 1, 0, //
			1, 0, -1, 1, 1, 1, -1).invert(), 7, //
			.5, 0, -1.5, 5, -4.5, -2, -.5, //
			-1, 2, 0, -4, 4, 2, 1, //
			0, 0, 0, 1, -1, 0, 0, //
			-.5, 1, 1, -4.5, 4, 2, 1, //
			-1, 1, 1, -5, 5, 2, 1, //
			0, 0, .5, -1.5, 1.5, 1, .5, //
			-1, 2, 1, -7, 7, 3, 1);
	}

	private static void assertMatrix(Matrix m, int columns, double... seq) {
		assertEquals(m, Matrix.from(columns, seq));
	}

	private static void assertMatrix(Matrix m, double[]... rows) {
		assertEquals(m, Matrix.of(rows));
	}

	private static void assertMatrix(Matrix m, Matrix expected) {
		assertEquals(m, expected);
	}
}
