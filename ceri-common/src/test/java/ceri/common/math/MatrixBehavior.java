package ceri.common.math;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class MatrixBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Matrix m0 = Matrix.of(new double[][] { { -1, 1, 0 }, { 1, 0, -1 } });
		Matrix m1 = Matrix.of(new double[][] { { -1, 1, 0 }, { 1, 0, -1 } });
		Matrix m2 = Matrix.of(new double[][] { { -1, 1, 0 }, { 1, 0, 1 } });
		Matrix m3 = Matrix.of(new double[][] { { -1, 1, 0 }, { 1, 0, -1 }, {} });
		Matrix m4 = Matrix.of(new double[][] { { -1, 1, 0, 0 }, { 1, 0, -1 } });
		exerciseEquals(m0, m1);
		assertAllNotEqual(m0, m2, m3, m4);
	}

	@Test
	public void shouldCreateFrom2dArray() {
		double[][] array = { { -1, 1 }, { 0, 1, 2 }, { -2 } };
		Matrix m = Matrix.of(array);
		assertArray(m.values(), new double[][] { { -1, 1, 0 }, { 0, 1, 2 }, { -2, 0, 0 } });
		m = Matrix.builder().setAll(array).set(3, 3, 7).build();
		assertArray(m.values(),
			new double[][] { { -1, 1, 0, 0 }, { 0, 1, 2, 0 }, { -2, 0, 0, 0 }, { 0, 0, 0, 7 } });
	}

	@Test
	public void shouldCreateEmptyMatricesOfGivenSize() {
		Matrix m = Matrix.builder().rows(2).columns(3).build();
		assertArray(m.values(), new double[][] { { 0, 0, 0 }, { 0, 0, 0 } });
		m = Matrix.builder().rows(1).size(1, 1).build();
		assertArray(m.values(), new double[][] { { 0 } });
	}

	@Test
	public void shouldCreateIdentityMatrices() {
		assertThat(Matrix.identity(0), is(Matrix.EMPTY));
		assertThat(Matrix.identity(1), is(Matrix.of(new double[][] { { 1 } })));
		assertThat(Matrix.identity(2), is(Matrix.of(new double[][] { { 1, 0 }, { 0, 1 } })));
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertTrue(Matrix.EMPTY.isEmpty());
		assertTrue(Matrix.identity(0).isEmpty());
		assertTrue(Matrix.of(new double[][] {}).isEmpty());
		assertFalse(Matrix.of(new double[][] { {} }).isEmpty());
		assertFalse(Matrix.builder().rows(1).build().isEmpty());
		assertFalse(Matrix.builder().columns(1).build().isEmpty());
	}

	@Test
	public void shouldDetermineIfSquare() {
		assertTrue(Matrix.EMPTY.isSquare());
		assertTrue(Matrix.identity(1).isSquare());
		assertFalse(Matrix.rowVector(1, 2).isSquare());
	}

	@Test
	public void shouldDetermineIfVector() {
		assertTrue(Matrix.EMPTY.isVector());
		assertTrue(Matrix.rowVector(1, 2).isVector());
		assertTrue(Matrix.columnVector(1, 2).isVector());
		assertTrue(Matrix.identity(1).isVector());
		assertFalse(Matrix.identity(2).isVector());
	}

	@Test
	public void shouldExtractRow() {
		Matrix m = Matrix.of(new double[][] { { 1, 2 }, { 3, -1 } });
		assertThat(m.row(1), is(Matrix.of(new double[][] { { 3, -1 } })));
	}

	@Test
	public void shouldExtractColumn() {
		Matrix m = Matrix.of(new double[][] { { 1, 2 }, { 3, -1 } });
		assertThat(m.column(1), is(Matrix.of(new double[][] { { 2 }, { -1 } })));
	}

	@Test
	public void shouldAddMatrices() {
		Matrix m = Matrix.identity(2);
		TestUtil.assertThrown(() -> m.add(Matrix.rowVector(1, 2)));
		TestUtil.assertThrown(() -> m.add(Matrix.columnVector(1, 2)));
		assertThat(m.add(m), is(Matrix.of(new double[][] { { 2, 0 }, { 0, 2 } })));
	}

	@Test
	public void shouldMultiplyMatrices() {
		Matrix m = Matrix.of(new double[][] { { 1, 2, 3 }, { -3, -1, -2 } });
		TestUtil.assertThrown(() -> m.multiply(Matrix.rowVector(1, 2, 3)));
		TestUtil.assertThrown(() -> m.multiply(Matrix.columnVector(1, 2)));
		assertThat(m.multiply(Matrix.columnVector(1, -1, 0)),
			is(Matrix.of(new double[][] { { -1 }, { -2 } })));
	}

	@Test
	public void shouldExtractSubMatrix() {
		Matrix m = Matrix.of(new double[][] { { 1 }, { -3 } });
		assertThat(m.subMatrix(-1, -1, 4, 3), is(Matrix.of( //
			new double[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 0, -3, 0 }, { 0, 0, 0 } })));
	}

}
