package ceri.image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.Test;
import ceri.image.magick.MagickImage;
import ceri.image.test.TestImage;

public class ImageUtilTest {
	private OutputStream devNull = new OutputStream() {
		@Override
		public void write(int b) {}
	};

	@Test(expected = IOException.class)
	public void testReadMax() throws IOException {
		byte[] data = new byte[100];
		ImageUtil.read(null, new ByteArrayInputStream(data), 99);
	}

	@Test
	public void test() throws IOException {
		Image image = ImageUtil.read(MagickImage.FACTORY, TestImage.gif_multi_100x100.read());
		image.write(Format.JPEG, 1.0f, devNull);
	}

	@Test
	public void testReadOfMultipleImageTypes() throws IOException {
		Image image;
		image = ImageUtil.read(MagickImage.FACTORY, TestImage.gif_multi_100x100.read());
		ImageUtil.writeBytes(image, Format.JPEG);
		image = ImageUtil.read(MagickImage.FACTORY, TestImage.jpg_eps_450x600.read());
		ImageUtil.writeBytes(image, Format.JPEG);
		image = ImageUtil.read(MagickImage.FACTORY, TestImage.png_rgb_8_300x300.read());
		ImageUtil.writeBytes(image, Format.JPEG);
		image = ImageUtil.read(MagickImage.FACTORY, TestImage.tif_cymk_512x343.read());
		ImageUtil.writeBytes(image, Format.JPEG);
	}

}
