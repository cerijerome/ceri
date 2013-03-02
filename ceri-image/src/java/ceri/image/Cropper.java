package ceri.image;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * Cropper that will resize and crop images based on limit parameters.
 * maxSizeIncrease specifies how many times larger an image can be resized.
 * interpolation specifies the algorithm used in resizing up/down.
 * format specifies the file type of the converted image.
 * x2Quality specifies that an image at least 2x larger than the desired crop will be 
 * resized to 2x, and with given quality. Specifying a low quality such as 0.2 will
 * not have a noticeable effect on quality but has a superior file size.
 * x1Quality specifies the quality of a cropped image.
 * Quality adjustments only apply to jpeg images.
 */
public class Cropper {
	static final float ZERO = 0.0f;
	static final float ONE = 1.0f;
	private final AlignX alignX;
	private final AlignY alignY;
	private final float maxSizeIncrease;
	private final Interpolation interpolation;
	private final Format format;
	private final float x2Quality;
	private final float x1Quality;
	private final int width;
	private final int height;

	public static class Builder {
		AlignX alignX = AlignX.Center;
		AlignY alignY = AlignY.Top3rd;
		float maxSizeIncrease = ONE;
		Interpolation interpolation = Interpolation.BICUBIC;
		Format format = Format.JPEG;
		float x2Quality = ZERO;
		float x1Quality = ONE;
		final int width;
		final int height;

		Builder(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		public Builder alignX(AlignX alignX) {
			this.alignX = alignX;
			return this;
		}
		
		public Builder alignY(AlignY alignY) {
			this.alignY = alignY;
			return this;
		}
		
		public Builder maxSizeIncrease(float maxSizeIncrease) {
			this.maxSizeIncrease = maxSizeIncrease;
			return this;
		}
		
		public Builder interpolation(Interpolation interpolation) {
			this.interpolation = interpolation ;
			return this;
		}
		
		public Builder format(Format format) {
			this.format = format;
			return this;
		}
		
		/**
		 * Only relevant for jpeg format.
		 */
		public Builder x2Quality(float x2Quality) {
			this.x2Quality = x2Quality;
			return this;
		}
		
		/**
		 * Only relevant for jpeg format.
		 */
		public Builder x1Quality(float x1Quality) {
			this.x1Quality = x1Quality;
			return this;
		}
		
		public Cropper build() {
			return new Cropper(this);
		}
	}
	
	Cropper(Builder builder) {
		alignX = builder.alignX;
		alignY = builder.alignY;
		maxSizeIncrease = builder.maxSizeIncrease;
		interpolation = builder.interpolation;
		format = builder.format;
		x2Quality = builder.x2Quality;
		x1Quality = builder.x1Quality;
		width = builder.width;
		height = builder.height;
	}

	public static Builder builder(int width, int height) {
		return new Builder(width, height);
	}
	
	/**
	 * Resize and crop, allowing 2x image with lower quality if the image is large enough
	 * (saves file size). If image resize exceeds maximum increase, crop only.
	 */
	public byte[] crop(BufferedImage image) {
		int w = image.getWidth();
		int h = image.getHeight();
		if (format == Format.JPEG && x2Quality > ZERO && w >= width * 2 && h >= height * 2) {
			image = ImageUtil.resizeToMin(image, width * 2, height * 2, interpolation);
			image = ImageUtil.crop(image, width * 2, height * 2, alignX, alignY);
			return ImageUtil.writeBytes(image, format, x2Quality);
		}
		Dimension resizeDimension = ImageUtil.resizeToMin(new Dimension(w, h), width, height);
		if (w * (ONE + maxSizeIncrease) >= resizeDimension.width &&
			h * (ONE + maxSizeIncrease) >= resizeDimension.height) {
			image = ImageUtil.resizeToMin(image, width, height, interpolation);
		}
		image = ImageUtil.crop(image, width, height, alignX, alignY);
		return ImageUtil.writeBytes(image, format, x1Quality);
	}

}
