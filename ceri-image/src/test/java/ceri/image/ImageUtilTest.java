package ceri.image;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.Test;
import ceri.image.geo.AlignX;
import ceri.image.geo.AlignY;
import ceri.image.magick.MagickImage;
import ceri.image.test.TestImage;

public class ImageUtilTest {
	public static OutputStream DEV_NULL = new OutputStream() {
		@Override
		public void write(int b) {}

		@Override
		public void write(byte[] b, int off, int len) {}
	};

	@Test(expected = IOException.class)
	public void testReadMaxFromInputStream() throws IOException {
		byte[] data = new byte[100];
		ImageUtil.read(null, new ByteArrayInputStream(data), 99);
	}

	@Test
	public void testReadFromByteArray() throws IOException {
		Image image = ImageUtil.read(MagickImage.FACTORY, TestImage.gif_multi_100x100.read());
		image.write(Format.JPEG, 1.0f, DEV_NULL);
	}

	@Test
	public void testWrite() throws IOException {
		Image image = ImageUtil.read(MagickImage.FACTORY, TestImage.gif_multi_100x100.read());
		ImageUtil.writeBytes(image, Format.JPEG, 0.1f);
		File tmp = File.createTempFile("testimage", "jpg");
		ImageUtil.write(image, Format.JPEG, 0.2f, tmp);
		tmp.delete();
	}

	@Test
	public void testResizePercent() throws IOException {
		Image image = ImageUtil.read(MagickImage.FACTORY, TestImage.jpg_eps_450x600.read());
		image = ImageUtil.resizePercent(image, 50, 200, Interpolation.NONE);
		assertThat(image.getWidth(), is(225));
		assertThat(image.getHeight(), is(1200));
	}

	@Test
	public void testResizeToMax() throws IOException {
		Image image = ImageUtil.read(MagickImage.FACTORY, TestImage.jpg_eps_604x453.read());
		image = ImageUtil.resizeToMax(image, 500, 800, Interpolation.NONE);
		assertThat(image.getWidth(), is(500));
		assertThat(image.getHeight(), is(375));
	}

	@Test
	public void testResizeToMin() throws IOException {
		Image image = ImageUtil.read(MagickImage.FACTORY, TestImage.jpg_eps_800x456.read());
		image = ImageUtil.resizeToMin(image, 600, 600, Interpolation.NONE);
		assertThat(image.getWidth(), is(1052));
		assertThat(image.getHeight(), is(600));
	}

	@Test
	public void testCrop() throws IOException {
		Image image = ImageUtil.read(MagickImage.FACTORY, TestImage.jpg_eps_450x600.read());
		image = ImageUtil.crop(image, 400, 900, AlignX.Right, AlignY.Bottom3rd);
		assertThat(image.getWidth(), is(400));
		assertThat(image.getHeight(), is(600));
	}

}
