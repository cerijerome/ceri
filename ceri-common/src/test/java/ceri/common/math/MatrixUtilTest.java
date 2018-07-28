package ceri.common.math;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class MatrixUtilTest {
	private static final Matrix m0 = Matrix.of(new double[][] { { 1, -1 }, { -2, 0 } });
	private static final Matrix m1 =
		Matrix.of(new double[][] { { 1, -1, 0 }, { -2, 0, 1 }, { 0, 1, -1 } });
	private static final Matrix m2 = Matrix.of(new double[][] { { 1, -1 }, { -1, 1 } });
	private static final Matrix m3 =
		Matrix.of(new double[][] { { 1, 0, 0 }, { 1, 0, 0 }, { 1, 1, 0 } });

	@Test
	public void testDeterminant() {
		assertThat(MatrixUtil.determinant(Matrix.EMPTY), is(0.0));
		assertThat(MatrixUtil.determinant(Matrix.singleton(1)), is(1.0));
		assertThat(MatrixUtil.determinant(m0), is(-2.0));
		assertThat(MatrixUtil.determinant(m1), is(1.0));
		assertThat(MatrixUtil.determinant(m2), is(0.0));
		assertThat(MatrixUtil.determinant(m3), is(0.0));
	}

	@Test
	public void testInvert() {
		assertThat(MatrixUtil.invert(Matrix.EMPTY), is(Matrix.EMPTY));
		assertThat(MatrixUtil.invert(Matrix.singleton(2)), is(Matrix.singleton(0.5)));
		assertThat(MatrixUtil.invert(m0),
			is(Matrix.of(new double[][] { { 0, -0.5 }, { -1, -0.5 } })));
		assertThat(MatrixUtil.invert(m1),
			is(Matrix.of(new double[][] { { -1, -1, -1 }, { -2, -1, -1 }, { -2, -1, -2 } })));
		assertException(() -> MatrixUtil.invert(m2));
		assertException(() -> MatrixUtil.invert(m3));
	}

	@Test
	public void testVerifySquare() {
		MatrixUtil.verifySquare(m0, 2);
		assertException(() -> MatrixUtil.verifySquare(m0, 1));
	}

	@Test
	public void testVerifyVector() {
		MatrixUtil.verifyVector(Matrix.EMPTY, 0);
		MatrixUtil.verifyVector(Matrix.singleton(3), 1);
		MatrixUtil.verifyVector(Matrix.rowVector(1, 2), 2);
		MatrixUtil.verifyVector(Matrix.columnVector(1, 2), 2);
		assertException(() -> MatrixUtil.verifyVector(Matrix.EMPTY, 1));
		assertException(() -> MatrixUtil.verifyVector(Matrix.singleton(3), 0));
		assertException(() -> MatrixUtil.verifyVector(Matrix.rowVector(1, 2), 3));
		assertException(() -> MatrixUtil.verifyVector(Matrix.columnVector(1, 2), 3));
		assertException(() -> MatrixUtil.verifyVector(m0, 2));
	}

	@Test
	public void testVerifySize() {
		MatrixUtil.verifySize(m0, 2, 2);
		assertException(() -> MatrixUtil.verifySize(m0, 1, 2));
		assertException(() -> MatrixUtil.verifySize(m0, 2, 1));
	}

}
