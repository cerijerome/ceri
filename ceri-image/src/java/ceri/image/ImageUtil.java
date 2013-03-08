package ceri.image;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import ceri.geo.AlignX;
import ceri.geo.AlignY;
import ceri.geo.GeoUtil;

/**
 * Utility methods for Image instances.
 */
public class ImageUtil {
	private static final int BUFFER_SIZE = 64 * 1024;

	private ImageUtil() {}

	/**
	 * Resize image by multiplying width and height by percentages, without
	 * maintaining aspect ratio.
	 */
	public static Image resizePercent(Image image, int widthPercent, int heightPercent,
		Interpolation interpolation) {
		Dimension resizeDimension =
			GeoUtil.resizePercent(new Dimension(image.getWidth(), image.getHeight()), widthPercent,
				heightPercent);
		return resize(image, resizeDimension.width, resizeDimension.height, interpolation);
	}

	/**
	 * Resize image to fit at least given dimensions while maintaining aspect
	 * ratio.
	 */
	public static Image
		resizeToMin(Image image, int width, int height, Interpolation interpolation) {
		Dimension resizeDimension =
			GeoUtil.resizeToMin(new Dimension(image.getWidth(), image.getHeight()), width, height);
		return resize(image, resizeDimension.width, resizeDimension.height, interpolation);
	}

	/**
	 * Resize image to fit within given dimensions while maintaining aspect
	 * ratio.
	 */
	public static Image
		resizeToMax(Image image, int width, int height, Interpolation interpolation) {
		Dimension resizeDimension =
			GeoUtil.resizeToMax(new Dimension(image.getWidth(), image.getHeight()), width, height);
		return resize(image, resizeDimension.width, resizeDimension.height, interpolation);
	}

	/**
	 * Resize to given dimensions without necessarily maintaining aspect ratio.
	 * Interpolation specifies the algorithm used for resizing.
	 */
	public static Image resize(Image image, int width, int height, Interpolation interpolation) {
		return image.resize(new Dimension(width, height), interpolation);
	}

	/**
	 * Crop image to given dimensions, with center crop window alignment. If the
	 * image is smaller than the desired crop in one dimension that dimension is
	 * not cropped.
	 */
	public static Image crop(Image image, int width, int height) {
		return crop(image, width, height, AlignX.Center, AlignY.Center);
	}

	/**
	 * Crop image to given dimensions, with specified crop window alignment. If
	 * the image is smaller than the desired crop in one dimension that
	 * dimension is not cropped.
	 */
	public static Image crop(Image image, int width, int height, AlignX alignX, AlignY alignY) {
		Rectangle rectangle =
			GeoUtil.crop(new Dimension(image.getWidth(), image.getHeight()), width, height, alignX,
				alignY);
		return image.crop(rectangle);
	}

	/**
	 * Creates an image from byte array. The factory determines the
	 * implementation.
	 */
	public static Image read(Image.Factory factory, byte[] data) throws IOException {
		return factory.create(data);
	}

	/**
	 * Creates an image from an input stream, with maximum data size. The factory
	 * determines the implementation.
	 */
	public static Image read(Image.Factory factory, InputStream in, int max)
		throws IOException {
		try {
			byte[] data = readMax(in, max);
			return factory.create(data);
		} finally {
			in.close();
		}
	}

	/**
	 * Creates an image from a url, with maximum data size. The factory
	 * determines the implementation.
	 */
	public static Image readFromUrl(Image.Factory factory, String urlStr, int max)
		throws IOException {
		URL url = new URL(urlStr);
		return read(factory, url.openStream(), max);
	}

	/**
	 * Creates an image from a file. The factory determines the implementation.
	 */
	public static Image readFromFile(Image.Factory factory, File file) throws IOException {
		return read(factory, new FileInputStream(file), 0);
	}

	/**
	 * Writes image to a file in given format.
	 */
	public static void write(Image image, Format format, File file) throws IOException {
		write(image, format, Image.BEST_QUALITY, file);
	}

	/**
	 * Writes image to a file in given format. Quality is only used for JPEG.
	 */
	public static void write(Image image, Format format, float quality, File file)
		throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			image.write(format, quality, out);
		}
	}

	/**
	 * Writes image to a byte array in given format.
	 */
	public static byte[] writeBytes(Image image, Format format) {
		return writeBytes(image, format, Image.BEST_QUALITY);
	}

	/**
	 * Writes image to a byte array in given format. Quality is only used for
	 * JPEG.
	 */
	public static byte[] writeBytes(Image image, Format format, float quality) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			image.write(format, quality, out);
			out.flush();
			return out.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

	/**
	 * Read byte array from input stream up to given maximum size. Throws
	 * IOException if max size is exceeded. Use 0 to specify unlimited size.
	 */
	private static byte[] readMax(InputStream in, int max) throws IOException {
		try (InputStream bIn = new BufferedInputStream(in, BUFFER_SIZE)) {
			ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
			byte[] buffer = new byte[BUFFER_SIZE];
			int count;
			int n = 0;
			while ((count = in.read(buffer)) != -1) {
				n += count;
				if (max != 0 && n > max) throw new IOException("Data has exceeded max size of " +
					max + " bytes");
				out.write(buffer, 0, count);
			}
			return out.toByteArray();
		}
	}

}
