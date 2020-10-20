package ceri.common.math;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.TestUtil.exerciseEquals;
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
		assertEquals(Vector.of(Matrix.EMPTY), Vector.EMPTY);
		assertEquals(Vector.of(Matrix.singleton(7)), Vector.of(7));
		assertEquals(Vector.of(Matrix.rowVector(1, 2, 3)), Vector.of(1, 2, 3));
		assertEquals(Vector.of(Matrix.columnVector(1, 2, 3)), Vector.of(1, 2, 3));
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
		assertEquals(Vector.of(1, 2, 3).subVector(1, 2), Vector.of(2, 3));
		assertEquals(Vector.of(1, 2, 3).subVector(2, 2), Vector.of(3, 0));
		assertEquals(Vector.of(1, 2, 3).subVector(4, 2), Vector.of(0, 0));
	}

	@Test
	public void shouldProvideWrappedSubVectors() {
		assertEquals(Vector.of(1, 2, 3).wrappedSubVector(1, 2), Vector.of(2, 3));
		assertEquals(Vector.of(1, 2, 3).wrappedSubVector(2, 2), Vector.of(3, 1));
		assertEquals(Vector.of(1, 2, 3).wrappedSubVector(4, 4), Vector.of(2, 3, 1, 2));
		assertEquals(Vector.of(1, 2, 3).wrappedSubVector(-1, 2), Vector.of(3, 1));
	}

	@Test
	public void shouldNegateValues() {
		assertEquals(Vector.of(-1, 0, 1).negate(), Vector.of(1, 0, -1));
	}

}
