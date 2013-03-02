package ceri.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.Test;

public class ImageUtilTest {

	private void resize(TestImage testImage, int w, int h, Format format, String outFile) throws IOException {
		BufferedImage image = ImageIO.read(testImage.file("doc/img"));
		System.out.println("Image: " + testImage.filename);
		System.out.println("Size:  " + image.getWidth() + " x " + image.getHeight());
		image = ImageUtil.resize(image, w, h);
		ImageUtil.write(image, format, 1.0f, new File(outFile + "." + format.suffix));
	}

	@Test
	public void test() throws IOException {
		long t = System.currentTimeMillis();
		resize(TestImage.png_hd_rgba_1920x1080, 100, 100, Format.PNG, "doc/img/testimg");
		t = System.currentTimeMillis() - t;
		System.out.println("Time:  " + t + "ms");
	}
	
}
