package ceri.common.math;

import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class VectorUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(VectorUtil.class);
	}

	@Test
	public void testCrossProduct2d() {
		Vector v0 = Vector.of(1, 2);
		Vector v1 = Vector.of(-1, 3);
		Vector v2 = Vector.of(1);
		assertApprox(VectorUtil.crossProduct2d(v0, v1), 5);
		assertException(() -> VectorUtil.crossProduct2d(v0, v2));
		assertException(() -> VectorUtil.crossProduct2d(v2, v0));
	}

	@Test
	public void testCrossProduct() {
		Vector v0 = Vector.of(1, 0, -1);
		Vector v1 = Vector.of(-1, 2, 4);
		Vector v2 = Vector.of(0, 1);
		Vector v3 = Vector.of(0, 1, 2, 3, 4, 5, 6);
		assertThat(VectorUtil.crossProduct(v0, v1), is(Vector.of(2, -3, 2)));
		assertException(() -> VectorUtil.crossProduct(v0, v2));
		assertException(() -> VectorUtil.crossProduct(v2, v2));
		assertException(() -> VectorUtil.crossProduct(v3, v3));
	}

	@Test
	public void testSimpleRound() {
		Vector v0 = Vector.of(1.11, 2.22, 9.99);
		assertThat(VectorUtil.simpleRound(v0, 1), is(Vector.of(1.1, 2.2, 10)));
	}

}
