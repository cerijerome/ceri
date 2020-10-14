package ceri.common.math;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;

public class VectorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Vector v0 = Vector.of(1, 0, -1);
		Vector v1 = Vector.of(1, 0, -1);
		Vector v2 = Vector.of(1, 0, -1, 0);
		Vector v3 = Vector.of(1, 0, 1);
		exerciseEquals(v0, v1);
		assertAllNotEqual(v0, v2, v3);
	}

	@Test
	public void shouldCreateFromMatrix() {
		assertNull(Vector.of((Matrix) null));
		assertThat(Vector.of(Matrix.EMPTY), is(Vector.EMPTY));
		assertThat(Vector.of(Matrix.singleton(7)), is(Vector.of(7)));
		assertThat(Vector.of(Matrix.rowVector(1, 2, 3)), is(Vector.of(1, 2, 3)));
		assertThat(Vector.of(Matrix.columnVector(1, 2, 3)), is(Vector.of(1, 2, 3)));
	}

	@Test
	public void shouldCalculateMagnitue() {
		assertApprox(Vector.EMPTY.magnitude(), 0);
		assertApprox(Vector.of(0, 0, 0, 0).magnitude(), 0);
		assertApprox(Vector.of(1, 1).magnitude(), 1.414);
	}

	@Test
	public void shouldProvideAccessToValues() {
		assertArray(Vector.EMPTY.values());
		assertArray(Vector.of(-1, 0, 1).values(), -1, 0, 1);
	}

	@Test
	public void shouldProvideSubVectors() {
		assertThat(Vector.of(1, 2, 3).subVector(1, 2), is(Vector.of(2, 3)));
		assertThat(Vector.of(1, 2, 3).subVector(2, 2), is(Vector.of(3, 0)));
		assertThat(Vector.of(1, 2, 3).subVector(4, 2), is(Vector.of(0, 0)));
	}

	@Test
	public void shouldProvideWrappedSubVectors() {
		assertThat(Vector.of(1, 2, 3).wrappedSubVector(1, 2), is(Vector.of(2, 3)));
		assertThat(Vector.of(1, 2, 3).wrappedSubVector(2, 2), is(Vector.of(3, 1)));
		assertThat(Vector.of(1, 2, 3).wrappedSubVector(4, 4), is(Vector.of(2, 3, 1, 2)));
		assertThat(Vector.of(1, 2, 3).wrappedSubVector(-1, 2), is(Vector.of(3, 1)));
	}

	@Test
	public void shouldNegateValues() {
		assertThat(Vector.of(-1, 0, 1).negate(), is(Vector.of(1, 0, -1)));
	}

}
