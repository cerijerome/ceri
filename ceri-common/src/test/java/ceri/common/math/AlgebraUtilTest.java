package ceri.common.math;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
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
		assertCollection(round(10, AlgebraUtil.cubicRealRoots(-14, 56, -64)), 2, 4, 8);
		assertCollection(round(10, AlgebraUtil.cubicRealRoots(-0.875, 0.21875, -0.015625)), 0.5,
			0.25, 0.125);
	}

	private static double[] round(int precision, double[] roots) {
		for (int i = 0; i < roots.length; i++)
			roots[i] = MathUtil.round(precision, roots[i]);
		return roots;
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
		assertEquals(AlgebraUtil.factorial(0), 1.0);
		assertEquals(9.33262e157, AlgebraUtil.factorial(100), 0.00001e157);
		assertThrown(() -> AlgebraUtil.factorial(-1));
		assertThrown(() -> AlgebraUtil.longFactorial(21));
		assertEquals(AlgebraUtil.longFactorial(20), 2432902008176640000L);
	}

	@Test
	public void testPascal() {
		assertEquals(AlgebraUtil.pascal(0, 0), 1L);
		assertEquals(AlgebraUtil.pascal(1, 0), 1L);
		assertEquals(AlgebraUtil.pascal(1, 1), 1L);
		assertEquals(AlgebraUtil.pascal(5, 0), 1L);
		assertEquals(AlgebraUtil.pascal(5, 1), 5L);
		assertEquals(AlgebraUtil.pascal(5, 2), 10L);
		assertEquals(AlgebraUtil.pascal(5, 3), 10L);
		assertEquals(AlgebraUtil.pascal(5, 4), 5L);
		assertEquals(AlgebraUtil.pascal(5, 5), 1L);
		assertEquals(AlgebraUtil.pascal(-1, 0), 0L);
		assertEquals(AlgebraUtil.pascal(0, -1), 0L);
		assertEquals(AlgebraUtil.pascal(0, 1), 0L);
		assertEquals(AlgebraUtil.pascal(1, 2), 0L);
	}

}