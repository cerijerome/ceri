package ceri.common.math;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.test.TestUtil;

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
		TestUtil.assertThrown(() -> VectorUtil.crossProduct2d(v0, v2));
		TestUtil.assertThrown(() -> VectorUtil.crossProduct2d(v2, v0));
	}

	@Test
	public void testCrossProduct() {
		Vector v0 = Vector.of(1, 0, -1);
		Vector v1 = Vector.of(-1, 2, 4);
		Vector v2 = Vector.of(0, 1);
		Vector v3 = Vector.of(0, 1, 2, 3, 4, 5, 6);
		assertThat(VectorUtil.crossProduct(v0, v1), is(Vector.of(2, -3, 2)));
		TestUtil.assertThrown(() -> VectorUtil.crossProduct(v0, v2));
		TestUtil.assertThrown(() -> VectorUtil.crossProduct(v2, v2));
		TestUtil.assertThrown(() -> VectorUtil.crossProduct(v3, v3));
	}

	@Test
	public void testSimpleRound() {
		Vector v0 = Vector.of(1.11, 2.22, 9.99);
		assertThat(VectorUtil.simpleRound(v0, 1), is(Vector.of(1.1, 2.2, 10)));
	}

}
