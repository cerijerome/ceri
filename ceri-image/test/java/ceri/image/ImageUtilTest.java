package ceri.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.Test;

public class ImageUtilTest {

	private void resize(TestImage testImage, int w, int h, Interpolation interpolation,
		Format format, String outFile) throws IOException {
		BufferedImage image = ImageIO.read(testImage.file("doc/img"));
		System.out.println("Image: " + testImage.filename);
		System.out.println("Size:  " + image.getWidth() + " x " + image.getHeight());
		System.out.println("Fit:   " + w + " x " + h);
		image = ImageUtil.resizeToMax(image, w, h, interpolation);
		System.out.println("New:   " + image.getWidth() + " x " + image.getHeight());
		ImageUtil.write(image, format, 1.0f, new File(outFile + "." + format.suffix));
	}

	@Test
	public void test() throws IOException {
		long t = System.currentTimeMillis();
		resize(TestImage.jpg_eps_450x600, 400, 300, Interpolation.BICUBIC, Format.JPEG, "doc/img/testimg");
		t = System.currentTimeMillis() - t;
		System.out.println("Time:  " + t + "ms");
	}

}
