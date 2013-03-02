package ceri.image.ebay;

import java.awt.image.BufferedImage;
import java.io.IOException;
import ceri.image.Format;
import ceri.image.ImageUtil;
import ceri.image.Interpolation;

public class EpsUtil {

	private EpsUtil() {}

	public static String url(String path, EpsImageType type) {
		return "http://" + path + "_" + type.id + "." + Format.JPEG.suffix;
	}

	public static BufferedImage load(String path) throws IOException {
		return ImageUtil.loadFromUrl(url(path, EpsImageType._3)); // Loads largest image
	}

	/**
	 * 1. crop w, h is inside image 2. crop w only is inside image 3. crop h
	 * only is inside image 4. crop outside image
	 */
	public static byte[] crop(String path, int width, int height, float sdQuality)
		throws IOException {
		BufferedImage image = load(path);
		int w = image.getWidth();
		int h = image.getHeight();
		if (w >= width * 2 && h >= height * 2) {
			image = ImageUtil.resizeToMin(image, width * 2, height * 2, Interpolation.BICUBIC);
			image = ImageUtil.crop(image, width * 2, height * 2);
			return ImageUtil.writeBytes(image, Format.JPEG, sdQuality);
		} else if (w >= width && h >= height) {
			image = ImageUtil.resizeToMin(image, width, height, Interpolation.BICUBIC);
			image = ImageUtil.crop(image, width, height);
			return ImageUtil.writeBytes(image, Format.JPEG);
		} else {
			image = ImageUtil.crop(image, width, height);
			return ImageUtil.writeBytes(image, Format.JPEG);
		}
	}

	public static void print() {
		for (EpsSetId setId : EpsSetId.values()) {
			System.out.print(setId.id + "(" + setId.authorizedApps + ")");
			for (EpsImageType type : setId.types)
				System.out.print(" " + type.id);
			System.out.println();
		}
	}

	public static void main(String[] args) {
		print();
	}

}
