package ceri.image.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import ceri.image.Cropper;
import ceri.image.magick.MagickImage;

/**
 * Class used to test the results of Cropper configuration.
 * Writes the cropped images to disk for visual inspection.
 */
public class ImageTester {

	public static void main(String[] args) throws IOException {
		write(TestImage.jpg_eps_cmyk_500x333, "test.jpg", Cropper.builder(100, 100));
	}

	private static void write(TestImage testImage, String filename, Cropper.Builder builder)
		throws IOException {
		byte[] data = testImage.read();
		MagickImage image = MagickImage.create(data);
		data = builder.build().crop(image);
		try (OutputStream out = new FileOutputStream(filename)) {
			out.write(data);
		}
	}

}
