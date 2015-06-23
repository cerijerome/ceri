package ceri.image.geo;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.awt.Dimension;
import java.awt.Rectangle;
import org.junit.Test;
import ceri.image.geo.AlignX;
import ceri.image.geo.AlignY;
import ceri.image.geo.GeoUtil;

public class GeoUtilTest {
	private static final Dimension MAX_MAX = dim(Integer.MAX_VALUE, Integer.MAX_VALUE);
	private static final Dimension ZERO_ZERO = dim(0, 0);

	@Test
	public void testResizePercentForMaxAndMin() {
		assertThat(GeoUtil.resizePercent(MAX_MAX, 100, 100), is(MAX_MAX));
		assertThat(GeoUtil.resizePercent(MAX_MAX, 0, 0), is(ZERO_ZERO));
		assertThat(GeoUtil.resizePercent(ZERO_ZERO, Integer.MAX_VALUE, Integer.MAX_VALUE),
			is(ZERO_ZERO));
	}

	@Test
	public void testResizePercentForGeneralValues() {
		assertThat(GeoUtil.resizePercent(dim(200, 100), 50, 10), is(dim(100, 10)));
		assertThat(GeoUtil.resizePercent(dim(100, 200), 200, 300), is(dim(200, 600)));
		assertThat(GeoUtil.resizePercent(dim(10, 20), 0, 0), is(ZERO_ZERO));
	}

	@Test
	public void testResizeToMaxForMaxAndMin() {
		assertThat(GeoUtil.resizeToMax(MAX_MAX, Integer.MAX_VALUE, Integer.MAX_VALUE), is(MAX_MAX));
		assertThat(GeoUtil.resizeToMax(MAX_MAX, 0, 0), is(ZERO_ZERO));
		assertThat(GeoUtil.resizeToMax(ZERO_ZERO, Integer.MAX_VALUE, Integer.MAX_VALUE),
			is(ZERO_ZERO));
	}

	@Test
	public void testResizeToMaxForGeneralValues() {
		assertThat(GeoUtil.resizeToMax(dim(200, 100), 50, 10), is(dim(20, 10)));
		assertThat(GeoUtil.resizeToMax(dim(100, 200), 200, 500), is(dim(200, 400)));
		assertThat(GeoUtil.resizeToMax(dim(10, 20), 0, 0), is(ZERO_ZERO));
	}

	@Test
	public void testResizeToMinForMaxAndMin() {
		assertThat(GeoUtil.resizeToMin(MAX_MAX, Integer.MAX_VALUE, Integer.MAX_VALUE), is(MAX_MAX));
		assertThat(GeoUtil.resizeToMin(MAX_MAX, 0, 0), is(ZERO_ZERO));
		assertThat(GeoUtil.resizeToMin(ZERO_ZERO, Integer.MAX_VALUE, Integer.MAX_VALUE),
			is(ZERO_ZERO));
	}

	@Test
	public void testResizeToMinForGeneralValues() {
		assertThat(GeoUtil.resizeToMin(dim(200, 100), 50, 10), is(dim(50, 25)));
		assertThat(GeoUtil.resizeToMin(dim(100, 200), 200, 300), is(dim(200, 400)));
		assertThat(GeoUtil.resizeToMin(dim(10, 20), 0, 0), is(ZERO_ZERO));
	}

	@Test
	public void testCropForMaxAndMin() {
		assertThat(GeoUtil.crop(MAX_MAX, Integer.MAX_VALUE, Integer.MAX_VALUE), is(MAX_MAX));
		assertThat(GeoUtil.crop(MAX_MAX, 0, 0), is(ZERO_ZERO));
		assertThat(GeoUtil.crop(ZERO_ZERO, Integer.MAX_VALUE, Integer.MAX_VALUE), is(ZERO_ZERO));
	}

	@Test
	public void testCropForGeneralValues() {
		assertThat(GeoUtil.crop(dim(200, 100), 150, 150), is(dim(150, 100)));
		assertThat(GeoUtil.crop(dim(100, 200), 200, 100), is(dim(100, 100)));
		assertThat(GeoUtil.crop(dim(10, 20), 0, 0), is(ZERO_ZERO));
	}

	@Test
	public void testCropAlignedForMaxAndMin() {
		assertThat(GeoUtil.crop(MAX_MAX, Integer.MAX_VALUE, Integer.MAX_VALUE, AlignX.Center,
			AlignY.Center), is(rect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE)));
		assertThat(GeoUtil.crop(MAX_MAX, 0, 0, AlignX.Center, AlignY.Center), is(rect(
			Integer.MAX_VALUE / 2 + 1, Integer.MAX_VALUE / 2 + 1, 0, 0)));
		assertThat(GeoUtil.crop(ZERO_ZERO, Integer.MAX_VALUE, Integer.MAX_VALUE, AlignX.Center,
			AlignY.Center), is(rect(0, 0, 0, 0)));
	}

	@Test
	public void testCropAlignedForGeneralValues() {
		assertThat(GeoUtil.crop(dim(200, 100), 150, 150, AlignX.Center, AlignY.Center), is(rect(25,
			0, 150, 100)));
		assertThat(GeoUtil.crop(dim(100, 200), 200, 100, AlignX.Center, AlignY.Center), is(rect(0,
			50, 100, 100)));
		assertThat(GeoUtil.crop(dim(10, 20), 0, 0, AlignX.Center, AlignY.Center), is(rect(5, 10, 0,
			0)));
		assertThat(GeoUtil.crop(dim(20, 40), 10, 10, AlignX.Right, AlignY.Bottom3rd), is(rect(10,
			20, 10, 10)));
	}

	private static Dimension dim(int w, int h) {
		return new Dimension(w, h);
	}

	private static Rectangle rect(int x, int y, int w, int h) {
		return new Rectangle(x, y, w, h);
	}

}
