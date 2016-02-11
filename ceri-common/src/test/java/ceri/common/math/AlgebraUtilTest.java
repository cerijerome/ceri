package ceri.common.math;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class AlgebraUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(AlgebraUtil.class);
	}

	@Test
	public void testCubicRealRoots() {
		assertCollection(AlgebraUtil.cubicRealRoots(0, 0, 0), 0, 0, 0);
		assertCollection(AlgebraUtil.cubicRealRoots(0, 0, 1), -1);
		assertCollection(AlgebraUtil.cubicRealRoots(0, 0, -1000000000000000000000000000000000.0),
			100000000000.0);
		assertCollection(AlgebraUtil.cubicRealRoots(-3, 3, -1), 1, 1, 1);
		assertCollection(AlgebraUtil.cubicRealRoots(1, -1, -1), 1, -1, -1);
		assertCollection(MathUtil.simpleRound(10, AlgebraUtil.cubicRealRoots(-14, 56, -64)), 2, 4,
			8);
		assertCollection(MathUtil.simpleRound(10, AlgebraUtil.cubicRealRoots(-0.875, 0.21875,
			-0.015625)), 0.5, 0.25, 0.125);
	}

	@Test
	public void testQuadraticRealRoots() {
		assertCollection(AlgebraUtil.quadraticRealRoots(0, 0, 0));
		assertCollection(AlgebraUtil.quadraticRealRoots(1, 0, -1), 1, -1);
		assertCollection(AlgebraUtil.quadraticRealRoots(1000, 0, -1000), 1, -1);
		assertCollection(AlgebraUtil.quadraticRealRoots(0, 50000000000.0, 100000000000.0), -2);
		assertCollection(AlgebraUtil.quadraticRealRoots(1, 0, 1));
		assertCollection(AlgebraUtil.quadraticRealRoots(1, 3, 1.25), -0.5, -2.5);
		assertCollection(AlgebraUtil.quadraticRealRoots(32, -12, 1), 0.25, 0.125);
	}

	@Test
	public void testFactorial() {
		assertThat(AlgebraUtil.factorial(0), is(1.0));
		assertEquals(9.33262e157, AlgebraUtil.factorial(100), 0.00001e157);
		assertException(() -> AlgebraUtil.factorial(-1));
		assertException(() -> AlgebraUtil.longFactorial(21));
		assertThat(AlgebraUtil.longFactorial(20), is(2432902008176640000L));
	}

	@Test
	public void testPascal() {
		assertThat(AlgebraUtil.pascal(0, 0), is(1L));
		assertThat(AlgebraUtil.pascal(1, 0), is(1L));
		assertThat(AlgebraUtil.pascal(1, 1), is(1L));
		assertThat(AlgebraUtil.pascal(5, 0), is(1L));
		assertThat(AlgebraUtil.pascal(5, 1), is(5L));
		assertThat(AlgebraUtil.pascal(5, 2), is(10L));
		assertThat(AlgebraUtil.pascal(5, 3), is(10L));
		assertThat(AlgebraUtil.pascal(5, 4), is(5L));
		assertThat(AlgebraUtil.pascal(5, 5), is(1L));
		assertThat(AlgebraUtil.pascal(-1, 0), is(0L));
		assertThat(AlgebraUtil.pascal(0, -1), is(0L));
		assertThat(AlgebraUtil.pascal(0, 1), is(0L));
		assertThat(AlgebraUtil.pascal(1, 2), is(0L));
	}

}