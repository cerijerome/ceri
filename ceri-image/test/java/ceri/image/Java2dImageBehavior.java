package ceri.image;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import org.junit.Test;
import ceri.image.test.TestImage;

public class Java2dImageBehavior {

	@Test
	public void shouldBeAbleToReadAndWriteRgbJpegs() throws IOException {
		Java2dImage image;
		image = Java2dImage.createWithJai(TestImage.jpg_eps_450x600.read());
		image.write(Format.JPEG, Image.BEST_QUALITY, ImageUtilTest.DEV_NULL);
	}

	@Test
	public void shouldBeAbleToConvertExifAnd8BitPngToJpeg() throws IOException {
		Java2dImage image;
		image = Java2dImage.createWithJai(TestImage.jpg_eps_exif_800x800.read());
		image.write(Format.JPEG, Image.BEST_QUALITY, ImageUtilTest.DEV_NULL);
		image = Java2dImage.createWithJai(TestImage.png_rgb_8_300x300.read());
		image.write(Format.JPEG, Image.BEST_QUALITY, ImageUtilTest.DEV_NULL);
	}

	@Test
	public void shouldAllowResizingWithoutMaintainingAspectRatio() throws IOException {
		Image image = Java2dImage.createWithJai(TestImage.jpg_eps_800x456.read());
		image = image.resize(new Dimension(100, 200), Interpolation.NONE);
		byte[] data = ImageUtil.writeBytes(image, Format.JPEG);
		image = Java2dImage.createWithJai(data);
		assertThat(image.getWidth(), is(100));
		assertThat(image.getHeight(), is(200));
	}

	@Test
	public void shouldAllowResizingLarger() throws IOException {
		Image image = Java2dImage.createWithJai(TestImage.jpg_eps_450x600.read());
		image = image.resize(new Dimension(600, 800), Interpolation.BICUBIC);
		byte[] data = ImageUtil.writeBytes(image, Format.JPEG);
		image = Java2dImage.createWithJai(data);
		assertThat(image.getWidth(), is(600));
		assertThat(image.getHeight(), is(800));
	}

	@Test
	public void shouldCropToLimitOfImage() throws IOException {
		Image image = Java2dImage.createWithJai(TestImage.jpg_eps_450x600.read());
		image = image.crop(new Rectangle(100, 150, 500, 500));
		byte[] data = ImageUtil.writeBytes(image, Format.JPEG);
		image = Java2dImage.createWithJai(data);
		assertThat(image.getWidth(), is(350));
		assertThat(image.getHeight(), is(450));
	}

}
