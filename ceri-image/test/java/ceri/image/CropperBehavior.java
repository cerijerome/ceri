package ceri.image;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.geo.AlignX;
import ceri.geo.AlignY;

public class CropperBehavior {

	@Test
	public void shouldNotResizeLargerThanSpecified() {
		ImageMock image = new ImageMock(50, 100);
		Cropper.builder(100, 200).maxSizeIncrease(0.5f).build().crop(image);
		assertThat(image.w, is(75));
		assertThat(image.h, is(150));
	}

	@Test
	public void shouldResizeUpToFit() {
		ImageMock image = new ImageMock(50, 100);
		Cropper.builder(100, 400).maxSizeIncrease(2.0f).build().crop(image);
		assertThat(image.w, is(100));
		assertThat(image.h, is(300));
	}

	@Test
	public void shouldResizeToDoubleOnlyIfLargerThanDoubleWhenX2QualityIsSet() {
		ImageMock image = new ImageMock(400, 500);
		Cropper.builder(100, 200).x2Quality(0.5f).x1Quality(0.6f).build().crop(image);
		assertThat(image.w, is(200));
		assertThat(image.h, is(400));
		assertThat(image.quality, is(0.5f));
		image = new ImageMock(199, 401);
		Cropper.builder(100, 200).x2Quality(0.5f).x1Quality(0.6f).build().crop(image);
		assertThat(image.w, is(100));
		assertThat(image.h, is(200));
		assertThat(image.quality, is(0.6f));
	}

	@Test
	public void shouldAlignX() {
		ImageMock image = new ImageMock(300, 200);
		Cropper.builder(100, 200).alignX(AlignX.Right).build().crop(image);
		assertThat(image.x, is(200));
		image = new ImageMock(300, 200);
		Cropper.builder(100, 200).alignX(AlignX.Center).build().crop(image);
		assertThat(image.x, is(100));
	}

	@Test
	public void shouldAlignY() {
		ImageMock image = new ImageMock(200, 400);
		Cropper.builder(200, 100).alignY(AlignY.Bottom).build().crop(image);
		assertThat(image.y, is(300));
		image = new ImageMock(200, 400);
		Cropper.builder(200, 100).alignY(AlignY.Top3rd).build().crop(image);
		assertThat(image.y, is(100));
	}
	
	@Test
	public void shouldConvertToGivenFormat() {
		ImageMock image = new ImageMock(200, 100);
		Cropper.builder(200, 100).format(Format.PNG).build().crop(image);
		assertThat(image.format, is(Format.PNG));
	}

	@Test
	public void shouldResizeUseGivenInterpolation() {
		ImageMock image = new ImageMock(200, 100);
		Cropper.builder(200, 100).interpolation(Interpolation.NEAREST_NEIGHBOR).build().crop(image);
		assertThat(image.interpolation, is(Interpolation.NEAREST_NEIGHBOR));
	}

}
