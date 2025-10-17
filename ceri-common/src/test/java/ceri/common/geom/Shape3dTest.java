package ceri.common.geom;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class Shape3dTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Shape3d.class);
	}

	@Test
	public void testTruncatedConcaveSemiSpheroidFromNegativeGradient() {
		assertThrown(() -> Shape3d.truncatedConcaveSemiSpheroidFromGradient(4, 4, 8, 1));
		assertThrown(() -> Shape3d.truncatedConcaveSemiSpheroidFromGradient(2, 4, 8, 0));
		TruncatedRadial3d<ConcaveSpheroid> r =
			Shape3d.truncatedConcaveSemiSpheroidFromGradient(2, 4, 8, 1);
		ConcaveSpheroid c = r.wrapped();
		assertApprox(c.r, 5);
		assertApprox(c.a, 3);
		assertApprox(c.c, 8.485);
		r = Shape3d.truncatedConcaveSemiSpheroidFromGradient(2, 4, 8, -1);
		c = r.wrapped();
		assertApprox(c.r, 5);
		assertApprox(c.a, 3);
		assertApprox(c.c, 8.485);
	}

	@Test
	public void testTruncatedSpheroidFromPositiveGradient() {
		TruncatedRadial3d<Spheroid3d> r = Shape3d.truncatedSpheroidFromGradient(2, 4, 2, 2);
		Spheroid3d s = r.wrapped();
		assertApprox(s.r, 4.472);
		assertApprox(s.c, 4.472);
		r = Shape3d.truncatedSpheroidFromGradient(1, 2, 1, 2);
		s = r.wrapped();
		assertApprox(s.r, 2.236);
		assertApprox(s.c, 2.236);
		// Upper radius must be larger than lower radius
		assertThrown(() -> Shape3d.truncatedSpheroidFromGradient(2, 2, 1, 1000));
		// Too much height for curvature
		assertThrown(() -> Shape3d.truncatedSpheroidFromGradient(0, 2, 1, 1));
		Shape3d.truncatedSpheroidFromGradient(0, 2, 0.7, 1);
	}

	@Test
	public void testTruncatedSpheroidFromNegativeGradient() {
		TruncatedRadial3d<Spheroid3d> r = Shape3d.truncatedSpheroidFromGradient(1, 1, 2, -1);
		Spheroid3d s = r.wrapped();
		assertApprox(s.r, Math.sqrt(2));
		assertApprox(s.c, Math.sqrt(2));
		r = Shape3d.truncatedSpheroidFromGradient(0, 1, 2, -1);
		s = r.wrapped();
		assertApprox(s.r, 1.342);
		assertApprox(s.c, 1.200);
		// Cannot fit into cone shape
		assertThrown(() -> Shape3d.truncatedSpheroidFromGradient(2, 1, 1, -1));
	}

	@Test
	public void testConicalFrustum() {
		Radial3d r = Shape3d.conicalFrustum(1, 1, 1);
		assertEquals(r, Cylinder.of(1, 1));
		r = Shape3d.conicalFrustum(0, 1, 1);
		assertEquals(r, Cone.of(1, 1));
		r = Shape3d.conicalFrustum(1, 0, 1);
		assertEquals(r, InvertedRadial3d.of(Cone.of(1, 1)));
		r = Shape3d.conicalFrustum(1, 2, 1);
		assertEquals(r, TruncatedRadial3d.of(Cone.of(2, 2), 1, 1));
		r = Shape3d.conicalFrustum(2, 1, 1);
		assertEquals(r, InvertedRadial3d.of(TruncatedRadial3d.of(Cone.of(2, 2), 1, 1)));
	}
}
