package ceri.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 
 * SD: 1. SD, >= 2x bigger: resize to 2x and reduce quality 2. SD, < 2x bigger:
 * resize to 1x, full quality
 */
public class Cropper {
	private static final float X2_QUALITY_DEF = 0.2f;
	private static final float MAX_SIZE_INCREASE_DEF = 2.0f;
	private static final Interpolation INTERPOLATION_DEF = Interpolation.BICUBIC;
	private static final Format FORMAT_DEF = Format.JPEG;
	private final float x2Quality;
	private final float maxSizeIncrease;
	private final Interpolation interpolation;
	private final Format format;

	private Cropper(float maxSizeIncrease, Interpolation interpolation, Format format,
		float x2Quality) {
		this.maxSizeIncrease = maxSizeIncrease == 0.0f ? MAX_SIZE_INCREASE_DEF : maxSizeIncrease;
		this.interpolation = interpolation == null ? INTERPOLATION_DEF : interpolation;
		this.format = format == null ? FORMAT_DEF : format;
		this.x2Quality = x2Quality == 0.0f ? X2_QUALITY_DEF : x2Quality;
	}

	public static Cropper createJpeg(float maxSizeIncrease, Interpolation interpolation,
		float x2Quality) {
		return new Cropper(maxSizeIncrease, interpolation, Format.JPEG, x2Quality);
	}

	public static Cropper createPng(float maxSizeIncrease, Interpolation interpolation) {
		return new Cropper(maxSizeIncrease, interpolation, Format.PNG, 0.0f);
	}

	public byte[] crop(BufferedImage image, int width, int height) throws IOException {
		int w = image.getWidth();
		int h = image.getHeight();
		if (format == Format.JPEG && w >= width * 2 && h >= height * 2) {
			image = ImageUtil.resizeToMin(image, width * 2, height * 2, interpolation);
			image = ImageUtil.crop(image, width * 2, height * 2);
			return ImageUtil.writeBytes(image, format, x2Quality);
		}
		if (w >= width && h >= height) {
			image = ImageUtil.resizeToMin(image, width, height, interpolation);
			image = ImageUtil.crop(image, width, height);
			return ImageUtil.writeBytes(image, format);
		}
		image = ImageUtil.crop(image, width, height);
		return ImageUtil.writeBytes(image, format);
	}

}
