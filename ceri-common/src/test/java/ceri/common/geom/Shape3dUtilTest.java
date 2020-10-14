package ceri.common.geom;

import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class Shape3dUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Shape3dUtil.class);
	}

	@Test
	public void testTruncatedConcaveSemiSpheroidFromNegativeGradient() {
		TestUtil
			.assertThrown(() -> Shape3dUtil.truncatedConcaveSemiSpheroidFromGradient(4, 4, 8, 1));
		TestUtil
			.assertThrown(() -> Shape3dUtil.truncatedConcaveSemiSpheroidFromGradient(2, 4, 8, 0));
		TruncatedRadial3d<ConcaveSpheroid3d> r =
			Shape3dUtil.truncatedConcaveSemiSpheroidFromGradient(2, 4, 8, 1);
		ConcaveSpheroid3d c = r.wrapped();
		assertApprox(c.r, 5);
		assertApprox(c.a, 3);
		assertApprox(c.c, 8.485);
		r = Shape3dUtil.truncatedConcaveSemiSpheroidFromGradient(2, 4, 8, -1);
		c = r.wrapped();
		assertApprox(c.r, 5);
		assertApprox(c.a, 3);
		assertApprox(c.c, 8.485);
	}

	@Test
	public void testTruncatedSpheroidFromPositiveGradient() {
		TruncatedRadial3d<Spheroid3d> r = Shape3dUtil.truncatedSpheroidFromGradient(2, 4, 2, 2);
		Spheroid3d s = r.wrapped();
		assertApprox(s.r, 4.472);
		assertApprox(s.c, 4.472);
		r = Shape3dUtil.truncatedSpheroidFromGradient(1, 2, 1, 2);
		s = r.wrapped();
		assertApprox(s.r, 2.236);
		assertApprox(s.c, 2.236);
		// Upper radius must be larger than lower radius
		TestUtil.assertThrown(() -> Shape3dUtil.truncatedSpheroidFromGradient(2, 2, 1, 1000));
		// Too much height for curvature
		TestUtil.assertThrown(() -> Shape3dUtil.truncatedSpheroidFromGradient(0, 2, 1, 1));
		Shape3dUtil.truncatedSpheroidFromGradient(0, 2, 0.7, 1);
	}

	@Test
	public void testTruncatedSpheroidFromNegativeGradient() {
		TruncatedRadial3d<Spheroid3d> r = Shape3dUtil.truncatedSpheroidFromGradient(1, 1, 2, -1);
		Spheroid3d s = r.wrapped();
		assertApprox(s.r, Math.sqrt(2));
		assertApprox(s.c, Math.sqrt(2));
		r = Shape3dUtil.truncatedSpheroidFromGradient(0, 1, 2, -1);
		s = r.wrapped();
		assertApprox(s.r, 1.342);
		assertApprox(s.c, 1.200);
		// Cannot fit into cone shape
		TestUtil.assertThrown(() -> Shape3dUtil.truncatedSpheroidFromGradient(2, 1, 1, -1));
	}

	@Test
	public void testConicalFrustum() {
		Radial3d r = Shape3dUtil.conicalFrustum(1, 1, 1);
		assertThat(r, is(Cylinder3d.create(1, 1)));
		r = Shape3dUtil.conicalFrustum(0, 1, 1);
		assertThat(r, is(Cone3d.create(1, 1)));
		r = Shape3dUtil.conicalFrustum(1, 0, 1);
		assertThat(r, is(InvertedRadial3d.create(Cone3d.create(1, 1))));
		r = Shape3dUtil.conicalFrustum(1, 2, 1);
		assertThat(r, is(TruncatedRadial3d.create(Cone3d.create(2, 2), 1, 1)));
		r = Shape3dUtil.conicalFrustum(2, 1, 1);
		assertThat(r,
			is(InvertedRadial3d.create(TruncatedRadial3d.create(Cone3d.create(2, 2), 1, 1))));
	}

}
