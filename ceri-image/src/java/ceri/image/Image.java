package ceri.image;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Encapsulates an image that can have operations applied to it.
 * Underlying implementations exist for Java2d BufferedImage and ImageMagick processing.
 * Image is immutable, so every operation generates a new instance.
 */
public interface Image {
	static final float BEST_QUALITY = 1.0f;
	
	static interface Factory {
		Image create(byte[] data) throws IOException;
	};
	
	/**
	 * Image width.
	 */
	int getWidth();
	
	/**
	 * Image height.
	 */
	int getHeight();
	
	/**
	 * Resize to given dimensions without necessarily maintaining aspect ratio.
	 */
	Image resize(Dimension dimension, Interpolation interpolation);

	/**
	 * Crop image to given dimensions and offset. If the crop window extends
	 * past the image it will be adjusted to fit.
	 */
	Image crop(Rectangle rectangle);

	/**
	 * Writes image to an output stream in given format.
	 * Quality value is 0.0 (lowest) to 1.0 (highest).
	 */
	void write(Format format, float quality, OutputStream out) throws IOException;

}
