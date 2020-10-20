package ceri.common.math;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class MatrixUtilTest {
	private static final Matrix m0 = Matrix.of(new double[][] { { 1, -1 }, { -2, 0 } });
	private static final Matrix m1 =
		Matrix.of(new double[][] { { 1, -1, 0 }, { -2, 0, 1 }, { 0, 1, -1 } });
	private static final Matrix m2 = Matrix.of(new double[][] { { 1, -1 }, { -1, 1 } });
	private static final Matrix m3 =
		Matrix.of(new double[][] { { 1, 0, 0 }, { 1, 0, 0 }, { 1, 1, 0 } });

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(MatrixUtil.class);
	}

	@Test
	public void testDeterminant() {
		assertEquals(MatrixUtil.determinant(Matrix.EMPTY), 0.0);
		assertEquals(MatrixUtil.determinant(Matrix.singleton(1)), 1.0);
		assertEquals(MatrixUtil.determinant(m0), -2.0);
		assertEquals(MatrixUtil.determinant(m1), 1.0);
		assertEquals(MatrixUtil.determinant(m2), 0.0);
		assertEquals(MatrixUtil.determinant(m3), 0.0);
	}

	@Test
	public void testInvert() {
		assertEquals(MatrixUtil.invert(Matrix.EMPTY), Matrix.EMPTY);
		assertEquals(MatrixUtil.invert(Matrix.singleton(2)), Matrix.singleton(0.5));
		assertEquals(MatrixUtil.invert(m0),
			Matrix.of(new double[][] { { 0, -0.5 }, { -1, -0.5 } }));
		assertEquals(MatrixUtil.invert(m1),
			Matrix.of(new double[][] { { -1, -1, -1 }, { -2, -1, -1 }, { -2, -1, -2 } }));
		assertThrown(() -> MatrixUtil.invert(m2));
		assertThrown(() -> MatrixUtil.invert(m3));
	}

	@Test
	public void testVerifySquare() {
		MatrixUtil.verifySquare(m0, 2);
		assertThrown(() -> MatrixUtil.verifySquare(m0, 1));
	}

	@Test
	public void testVerifyVector() {
		MatrixUtil.verifyVector(Matrix.EMPTY, 0);
		MatrixUtil.verifyVector(Matrix.singleton(3), 1);
		MatrixUtil.verifyVector(Matrix.rowVector(1, 2), 2);
		MatrixUtil.verifyVector(Matrix.columnVector(1, 2), 2);
		assertThrown(() -> MatrixUtil.verifyVector(Matrix.EMPTY, 1));
		assertThrown(() -> MatrixUtil.verifyVector(Matrix.singleton(3), 0));
		assertThrown(() -> MatrixUtil.verifyVector(Matrix.rowVector(1, 2), 3));
		assertThrown(() -> MatrixUtil.verifyVector(Matrix.columnVector(1, 2), 3));
		assertThrown(() -> MatrixUtil.verifyVector(m0, 2));
	}

	@Test
	public void testVerifySize() {
		MatrixUtil.verifySize(m0, 2, 2);
		assertThrown(() -> MatrixUtil.verifySize(m0, 1, 2));
		assertThrown(() -> MatrixUtil.verifySize(m0, 2, 1));
	}

}
