package ceri.common.math;

import static ceri.common.test.TestUtil.*;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;

public class GeometryUtilTest {
	private final Dimension2d d0 = new Dimension2d(400, 100);
	private final Dimension2d d1 = new Dimension2d(200, 200);
	private final Dimension2d d2 = new Dimension2d(100, 400);
	private final Dimension2d d3 = new Dimension2d(60, 40);
	private final Dimension2d d4 = new Dimension2d(0, 0);
	private final Dimension2d d5 = new Dimension2d(POSITIVE_INFINITY, 0);
	private final Dimension2d d6 = new Dimension2d(0, POSITIVE_INFINITY);
	private final Dimension2d d7 = new Dimension2d(POSITIVE_INFINITY, POSITIVE_INFINITY);

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(GeometryUtil.class);
	}

	@Test
	public void testResizeToMin() {
//		 assertDim(GeometryUtil.resizeToMin(d1, d0), 400, 400);
//		 assertDim(GeometryUtil.resizeToMin(d3, d1), 300, 200);
//		 assertDim(GeometryUtil.resizeToMin(d1, d1), 200, 200);
//		 assertDim(GeometryUtil.resizeToMin(d1, d4), 0, 0);
//		 assertDim(GeometryUtil.resizeToMin(d5, d3), 60, 0);
//		 assertDim(GeometryUtil.resizeToMin(d6, d3), 0, 40);
//		 assertDim(GeometryUtil.resizeToMin(d7, d3), 60, 40);
	}
	
	private void assertDim(Dimension2d dim, double w, double h) {
		System.out.println(dim);
		assertThat(dim.w, is(w));
		assertThat(dim.h, is(h));
	}
	
}
