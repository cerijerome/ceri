package ceri.common.math;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertPrivateConstructor;
import static ceri.common.test.Assert.assertUnordered;
import org.junit.Test;
import ceri.common.test.Assert;

public class AlgebraTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Algebra.class);
	}

	@Test
	public void testCubicRealRoots() {
		assertUnordered(Algebra.cubicRealRoots(0, 0, 0), 0, 0, 0);
		assertUnordered(Algebra.cubicRealRoots(0, 0, 1), -1);
		assertUnordered(Algebra.cubicRealRoots(0, 0, -1000000000000000000000000000000000.0),
			100000000000.0);
		assertUnordered(Algebra.cubicRealRoots(-3, 3, -1), 1, 1, 1);
		assertUnordered(Algebra.cubicRealRoots(1, -1, -1), 1, -1, -1);
		assertUnordered(round(10, Algebra.cubicRealRoots(-14, 56, -64)), 2, 4, 8);
		assertUnordered(round(10, Algebra.cubicRealRoots(-0.875, 0.21875, -0.015625)), 0.5, 0.25,
			0.125);
	}

	private static double[] round(int precision, double[] roots) {
		for (int i = 0; i < roots.length; i++)
			roots[i] = Maths.round(precision, roots[i]);
		return roots;
	}

	@Test
	public void testQuadraticRealRoots() {
		assertUnordered(Algebra.quadraticRealRoots(0, 0, 0));
		assertUnordered(Algebra.quadraticRealRoots(1, 0, -1), 1, -1);
		assertUnordered(Algebra.quadraticRealRoots(1000, 0, -1000), 1, -1);
		assertUnordered(Algebra.quadraticRealRoots(0, 50000000000.0, 100000000000.0), -2);
		assertUnordered(Algebra.quadraticRealRoots(1, 0, 1));
		assertUnordered(Algebra.quadraticRealRoots(1, 3, 1.25), -0.5, -2.5);
		assertUnordered(Algebra.quadraticRealRoots(32, -12, 1), 0.25, 0.125);
	}

	@Test
	public void testFactorial() {
		assertEquals(Algebra.factorial(0), 1.0);
		assertEquals(9.33262e157, Algebra.factorial(100), 0.00001e157);
		Assert.thrown(() -> Algebra.factorial(-1));
		Assert.thrown(() -> Algebra.longFactorial(21));
		assertEquals(Algebra.longFactorial(20), 2432902008176640000L);
	}

	@Test
	public void testPascal() {
		assertEquals(Algebra.pascal(0, 0), 1L);
		assertEquals(Algebra.pascal(1, 0), 1L);
		assertEquals(Algebra.pascal(1, 1), 1L);
		assertEquals(Algebra.pascal(5, 0), 1L);
		assertEquals(Algebra.pascal(5, 1), 5L);
		assertEquals(Algebra.pascal(5, 2), 10L);
		assertEquals(Algebra.pascal(5, 3), 10L);
		assertEquals(Algebra.pascal(5, 4), 5L);
		assertEquals(Algebra.pascal(5, 5), 1L);
		assertEquals(Algebra.pascal(-1, 0), 0L);
		assertEquals(Algebra.pascal(0, -1), 0L);
		assertEquals(Algebra.pascal(0, 1), 0L);
		assertEquals(Algebra.pascal(1, 2), 0L);
	}

}