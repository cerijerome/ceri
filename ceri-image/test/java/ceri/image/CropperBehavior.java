package ceri.image;

import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Test;
import ceri.image.magick.MagickImage;
import ceri.image.test.TestImage;

public class CropperBehavior {

	//@Test
	public void shouldOnlyAllowMatchingImagePaths() throws IOException {
		Cropper cropper1 = Cropper.builder(w, h).x2Quality(0.3f).build();
		Cropper cropper2 = Cropper.builder(w, h).build();
		long t = System.currentTimeMillis();
		Image image = MagickImage.FACTORY.create(TestImage.jpg_cmyk_500x333.read());
		System.out.println("Size:  " + image.getWidth() + " x " + image.getHeight());
		System.out.println("Fit:   " + w + " x " + h);
		byte[] imageBytes1 = cropper1.crop(image);
		byte[] imageBytes2 = cropper2.crop(image);
		t = System.currentTimeMillis() - t;
		System.out.println("Time:  " + t + "ms");
		//image = ImageUtil.read(imageBytes1);
		//System.out.println("New1:  " + image.getWidth() + " x " + image.getHeight());
		new FileOutputStream("testimg1.jpg").write(imageBytes1);
		//image = ImageUtil.read(imageBytes2);
		//System.out.println("New2:  " + image.getWidth() + " x " + image.getHeight());
		new FileOutputStream("testimg2.jpg").write(imageBytes2);
	}

	
	@Test
	public void should() {
		Cropper cropper = Cropper.builder(100, 100).build();
		Image image = 
		cropper.crop(image);
	}


}
