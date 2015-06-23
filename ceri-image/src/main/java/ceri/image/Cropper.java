package ceri.image;

import java.awt.Dimension;
import ceri.common.util.HashCoder;
import ceri.image.geo.AlignX;
import ceri.image.geo.AlignY;
import ceri.image.geo.GeoUtil;

/**
 * Cropper that will resize and crop images based on limit parameters. maxSizeIncrease specifies how
 * many times larger an image can be resized. interpolation specifies the algorithm used in resizing
 * up/down. format specifies the file type of the converted image. x2Quality specifies that an image
 * at least 2x larger than the desired crop will be resized to 2x, and with given quality.
 * Specifying a low quality such as 0.2 will not have a noticeable effect on quality but has a
 * superior file size. x1Quality specifies the quality of a cropped image.
 */
public class Cropper {
	static final float X1_QUALITY_DEF = 0.9f;
	static final float X2_QUALITY_DEF = 0.2f;
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
	private final int hashCode;

	public static class Builder {
		AlignX alignX = AlignX.Center;
		AlignY alignY = AlignY.Top3rd;
		float maxSizeIncrease = ONE;
		Interpolation interpolation = Interpolation.BICUBIC;
		Format format = Format.JPEG;
		float x2Quality = X2_QUALITY_DEF;
		float x1Quality = X1_QUALITY_DEF;
		final int width;
		final int height;

		Builder(int width, int height) {
			this.width = width;
			this.height = height;
		}

		/**
		 * Specifies horizontal alignment for cropping.
		 */
		public Builder alignX(AlignX alignX) {
			this.alignX = alignX;
			return this;
		}

		/**
		 * Specifies vertical alignment for cropping.
		 */
		public Builder alignY(AlignY alignY) {
			this.alignY = alignY;
			return this;
		}

		/**
		 * Specifies the maximum amount an image can be resized larger while trying to meet output
		 * width and height requirements.
		 */
		public Builder maxSizeIncrease(float maxSizeIncrease) {
			this.maxSizeIncrease = maxSizeIncrease;
			return this;
		}

		/**
		 * Resizing interpolation method.
		 */
		public Builder interpolation(Interpolation interpolation) {
			this.interpolation = interpolation;
			return this;
		}

		/**
		 * Output image format.
		 */
		public Builder format(Format format) {
			this.format = format;
			return this;
		}

		/**
		 * Quality used when creating a double-sized image. A double-sized lower quality image
		 * displayed at half-size is very close in visual quality to a full quality single-sized
		 * image, but with much lower file size.
		 */
		public Builder x2Quality(float x2Quality) {
			this.x2Quality = x2Quality;
			return this;
		}

		/**
		 * Quality used for single-sized image output.
		 */
		public Builder x1Quality(float x1Quality) {
			this.x1Quality = x1Quality;
			return this;
		}

		/**
		 * Builds the cropper.
		 */
		public Cropper build() {
			return new Cropper(this);
		}
	}

	/**
	 * Create a builder with given width and height settings.
	 */
	public static Builder builder(int width, int height) {
		return new Builder(width, height);
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
		hashCode =
			HashCoder.hash(width, height, alignX, alignY, maxSizeIncrease, interpolation, format,
				x2Quality, x1Quality);
	}

	/**
	 * Resize and crop, allowing 2x image with lower quality if the image is large enough (saves
	 * file size). If image resize exceeds the specified maximum increase, crop only.
	 */
	public byte[] crop(Image image) {
		int w = image.getWidth();
		int h = image.getHeight();
		if (x2Quality > ZERO && w >= width * 2 && h >= height * 2) {
			image = ImageUtil.resizeToMin(image, width * 2, height * 2, interpolation);
			image = ImageUtil.crop(image, width * 2, height * 2, alignX, alignY);
			return ImageUtil.writeBytes(image, format, x2Quality);
		}
		int maxW = (int) ((ONE + maxSizeIncrease) * w);
		int maxH = (int) ((ONE + maxSizeIncrease) * h);
		Dimension resizeDimension = GeoUtil.resizeToMin(new Dimension(maxW, maxH), width, height);
		if (resizeDimension.width <= maxW && resizeDimension.height <= maxH) {
			image = ImageUtil.resizeToMin(image, width, height, interpolation);
		} else {
			image = ImageUtil.resize(image, maxW, maxH, interpolation);
		}
		image = ImageUtil.crop(image, width, height, alignX, alignY);
		return ImageUtil.writeBytes(image, format, x1Quality);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Cropper)) return false;
		Cropper cropper = (Cropper) obj;
		if (hashCode != cropper.hashCode) return false;
		if (width != cropper.width) return false;
		if (height != cropper.height) return false;
		if (alignX != cropper.alignX) return false;
		if (alignY != cropper.alignY) return false;
		if (format != cropper.format) return false;
		if (interpolation != cropper.interpolation) return false;
		if (Float.floatToIntBits(maxSizeIncrease) != Float.floatToIntBits(cropper.maxSizeIncrease)) return false;
		if (Float.floatToIntBits(x1Quality) != Float.floatToIntBits(cropper.x1Quality)) return false;
		if (Float.floatToIntBits(x2Quality) != Float.floatToIntBits(cropper.x2Quality)) return false;
		return true;
	}

}
