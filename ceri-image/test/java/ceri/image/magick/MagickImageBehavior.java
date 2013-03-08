package ceri.image.magick;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Test;
import ceri.geo.AlignX;
import ceri.geo.AlignY;
import ceri.image.Format;
import ceri.image.Image;
import ceri.image.ImageUtil;
import ceri.image.Interpolation;
import ceri.image.test.TestImage;

public class MagickImageBehavior {

	@Test
	public void should() throws IOException {
		Image image =
			ImageUtil.readFromFile(MagickImage.FACTORY, new File("doc/img/" +
				TestImage.jpg_eps_800x456.filename));
		Image image1 = ImageUtil.resizeToMin(image, 200,  200, Interpolation.BICUBIC);
		image1 = ImageUtil.crop(image1, 200, 200, AlignX.Center, AlignY.Top3rd);
		image1.write(Format.JPEG, 1.0f, new FileOutputStream("doc/img/test1.jpg"));
		Image image2 = ImageUtil.resizeToMin(image, 400,  400, Interpolation.BICUBIC);
		image2 = ImageUtil.crop(image2, 400, 400, AlignX.Center, AlignY.Top3rd);
		image2.write(Format.JPEG, 0.4f, new FileOutputStream("doc/img/test2.jpg"));
	}

}
