package ceri.image;

import java.awt.Dimension;
import ceri.geo.GeoUtil;

/**
 * Cropper that will resize and crop images based on limit parameters.
 */
public class Cropper {
	static final float ZERO = 0.0f;
	static final float ONE = 1.0f;
	private final CropperParams params;

	public Cropper(CropperParams params) {
		this.params = params;
	}

	/**
	 * Resize and crop, allowing 2x image with lower quality if the image is
	 * large enough (saves file size). If image resize exceeds the specified
	 * maximum increase, crop only.
	 */
	public byte[] crop(Image image) {
		int w = image.getWidth();
		int h = image.getHeight();
		if (params.x2Quality > ZERO && w >= params.width * 2 && h >= params.height * 2) {
			image =
				ImageUtil.resizeToMin(image, params.width * 2, params.height * 2,
					params.interpolation);
			image =
				ImageUtil.crop(image, params.width * 2, params.height * 2, params.alignX,
					params.alignY);
			return ImageUtil.writeBytes(image, params.format, params.x2Quality);
		}
		Dimension resizeDimension =
			GeoUtil.resizeToMin(new Dimension(w, h), params.width, params.height);
		if (w * (ONE + params.maxSizeIncrease) >= resizeDimension.width &&
			h * (ONE + params.maxSizeIncrease) >= resizeDimension.height) {
			image = ImageUtil.resizeToMin(image, params.width, params.height, params.interpolation);
		}
		image = ImageUtil.crop(image, params.width, params.height, params.alignX, params.alignY);
		return ImageUtil.writeBytes(image, params.format, params.x1Quality);
	}

}
