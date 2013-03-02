package ceri.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.Test;

public class CropperBehavior {
	private ImageTestHelper helper = new ImageTestHelper();
	
	@Test
	public void should() throws IOException {
		int w = 150;
		int h = 100;
		Cropper cropper1 = Cropper.builder(w, h).x2Quality(0.3f).build();
		Cropper cropper2 = Cropper.builder(w, h).build();
		long t = System.currentTimeMillis();
		BufferedImage image = helper.read(TestImage.png_rgb_16_600x600);
		System.out.println("Size:  " + image.getWidth() + " x " + image.getHeight());
		System.out.println("Fit:   " + w + " x " + h);
		byte[] imageBytes1 = cropper1.crop(image);
		byte[] imageBytes2 = cropper2.crop(image);
		t = System.currentTimeMillis() - t;
		System.out.println("Time:  " + t + "ms");
		image = ImageUtil.read(imageBytes1);
		System.out.println("New1:  " + image.getWidth() + " x " + image.getHeight());
		helper.write(imageBytes1, "testimg1.jpg");
		image = ImageUtil.read(imageBytes2);
		System.out.println("New2:  " + image.getWidth() + " x " + image.getHeight());
		helper.write(imageBytes2, "testimg2.jpg");
	}

}
