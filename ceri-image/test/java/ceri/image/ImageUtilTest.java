package ceri.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.HexPrinter;

public class ImageUtilTest {
	private static ImageTestHelper helper = null;

	@BeforeClass
	public static void createTempDir() {
		helper = new ImageTestHelper();
	}

	@AfterClass
	public static void deleteTempDir() {
		helper.close();
	}

	private void resize(TestImage testImage, int w, int h, Interpolation interpolation,
		Format format, String outFile) throws IOException {
		BufferedImage image = helper.read(testImage);
		System.out.println("Image: " + testImage.filename);
		System.out.println("Size:  " + image.getWidth() + " x " + image.getHeight());
		System.out.println("Fit:   " + w + " x " + h);
		image = ImageUtil.resizeToMax(image, w, h, interpolation);
		System.out.println("New:   " + image.getWidth() + " x " + image.getHeight());
		ImageUtil.write(image, format, 1.0f, new File(outFile + "." + format.suffix));
	}

	//@Test
	public void test() throws IOException {
		long t = System.currentTimeMillis();
		resize(TestImage.jpg_eps_450x600, 400, 300, Interpolation.BICUBIC, Format.JPEG,
			"doc/img/testimg");
		t = System.currentTimeMillis() - t;
		System.out.println("Time:  " + t + "ms");
	}

	//@Test
	public void test0() throws Exception {
		TestImage testImage = TestImage.jpg_cmyk_500x333;
		FileInputStream in = new FileInputStream("doc/img/" + testImage.filename);
		HexPrinter printer = new HexPrinter();
		printer.print(in, 256);
	}

	@Test
	public void testReadExifImage() throws IOException {
		BufferedImage image = helper.read(TestImage.jpg_exif_2850x2850);
		//ImageUtil.write(image, null, 0, new File("exif.jpg"));
	}

	@Test
	public void testReadCmykImage() throws IOException {
		BufferedImage image = helper.read(TestImage.jpg_cmyk_500x333);
		//ImageUtil.write(image, null, 0, new File("cmyk.jpg"));
	}

	@Test
	public void testReadEpsStandardJpeg() throws IOException {
		helper.read(TestImage.jpg_eps_450x600);
		helper.read(TestImage.jpg_eps_604x453);
		helper.read(TestImage.jpg_eps_800x456);
	}

	@Test
	public void testReadEpsCmykJpeg() throws IOException {
		//helper.read(TestImage.jpg_eps_cmyk_500x333);
	}

	@Test
	public void testReadEpsExifJpeg() throws IOException {
		helper.read(TestImage.jpg_eps_exif_800x800);
	}

}
