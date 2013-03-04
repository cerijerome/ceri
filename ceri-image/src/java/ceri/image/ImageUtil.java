package ceri.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.jai.JAI;
import com.sun.media.jai.codec.SeekableStream;

public class ImageUtil {
	public static final float BEST_QUALITY = 1.0f;
	private static final String JAI_STREAM = "stream";

	private ImageUtil() {}

	/**
	 * Resize dimension to fit at least given dimensions while maintaining
	 * aspect ratio.
	 */
	public static Dimension resizeToMin(Dimension dimension, int width, int height) {
		int w = dimension.width;
		int h = dimension.height;
		if ((long) w * height <= (long) width * h) {
			h = width * h / w;
			w = width;
		} else {
			w = w * height / h;
			h = height;
		}
		return new Dimension(w, h);
	}

	/**
	 * Resize image to fit at least given dimensions while maintaining aspect
	 * ratio.
	 */
	public static BufferedImage resizeToMin(BufferedImage image, int width, int height,
		Interpolation interpolation) {
		Dimension resizeDimension =
			resizeToMin(new Dimension(image.getWidth(), image.getHeight()), width, height);
		return resize(image, resizeDimension.width, resizeDimension.height, interpolation);
	}

	/**
	 * Resize dimension to fit within given dimensions while maintaining aspect
	 * ratio.
	 */
	public static Dimension resizeToMax(Dimension dimension, int width, int height) {
		int w = dimension.width;
		int h = dimension.height;
		if ((long) w * height >= (long) width * h) {
			h = width * h / w;
			w = width;
		} else {
			w = w * height / h;
			h = height;
		}
		return new Dimension(w, h);
	}

	/**
	 * Resize image to fit within given dimensions while maintaining aspect
	 * ratio.
	 */
	public static BufferedImage resizeToMax(BufferedImage image, int width, int height,
		Interpolation interpolation) {
		Dimension resizeDimension =
			resizeToMax(new Dimension(image.getWidth(), image.getHeight()), width, height);
		return resize(image, resizeDimension.width, resizeDimension.height, interpolation);
	}

	/**
	 * Resize to given dimensions without maintaining aspect ratio. Downsizing
	 * is done in 50% chunks if the image is large enough. Upsizing is done in
	 * one step.
	 */
	public static BufferedImage resize(BufferedImage image, int width, int height,
		Interpolation interpolation) {
		if (interpolation == null) interpolation = Interpolation.BILINEAR;
		int type =
			image.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		while (true) {
			int w = image.getWidth();
			int h = image.getHeight();
			if (w == width && h == height) return image;
			// resize in 50% chunks if very large image
			if (w > width * 2) w /= 2;
			else w = width;
			if (h > height * 2) h /= 2;
			else h = height;
			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			interpolation.setRenderingHint(g2);
			g2.drawImage(image, 0, 0, w, h, null);
			g2.dispose();
			image = tmp;
		}
	}

	/**
	 * Crop image to given dimensions, with center crop window alignment. If the
	 * image is smaller than the desired crop in one dimension that dimension is
	 * not cropped.
	 */
	public static BufferedImage crop(BufferedImage image, int width, int height) {
		return crop(image, width, height, AlignX.Center, AlignY.Center);
	}

	/**
	 * Crop dimension to given dimensions. If dimension is smaller than the
	 * desired crop it will remain unchanged.
	 */
	public static Dimension crop(Dimension dimension, int width, int height) {
		int w = dimension.width;
		int h = dimension.height;
		if (width >= w) width = w;
		if (height >= h) height = h;
		return new Dimension(width, height);
	}

	/**
	 * Crop image to given dimensions, with specified crop window alignment. If
	 * the image is smaller than the desired crop in one dimension that
	 * dimension is not cropped.
	 */
	public static BufferedImage crop(BufferedImage image, int width, int height, AlignX alignX,
		AlignY alignY) {
		int w = image.getWidth();
		int h = image.getHeight();
		Dimension cropDimension = crop(new Dimension(w, h), width, height);
		// If no change required, just return the image
		if (cropDimension.width == w && cropDimension.height == h) return image;
		int x = (int) (alignX.offsetMultiplier * (w - cropDimension.width));
		int y = (int) (alignY.offsetMultiplier * (h - cropDimension.height));
		return image.getSubimage(x, y, cropDimension.width, cropDimension.height);
	}

	public static BufferedImage read(byte[] imageBytes) {
		try (InputStream in = new ByteArrayInputStream(imageBytes)) {
			return read(in);
		} catch (IOException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

	public static BufferedImage readFromUrl(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		try (InputStream in = url.openStream()) {
			return read(in);
		}
	}

	public static BufferedImage read(File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			return read(in);
		}
	}

	public static BufferedImage read(InputStream in) throws IOException {
		long t = System.currentTimeMillis();
		//BufferedImage image = new ImgReader().read(in);
		BufferedImage image = readJai(in);
		t = System.currentTimeMillis() - t;
		System.out.println("Time: " + t + "ms ");
		return image;
	}

	public static BufferedImage readJai(InputStream in) throws IOException {
		try {
			return JAI.create(JAI_STREAM, SeekableStream.wrapInputStream(in, true))
				.getAsBufferedImage();
		} catch (RuntimeException e) {
			throw new IOException("Failed to read image from stream", e);
		}
	}

	/**
	 * Much faster performance for reads, but doesn't handle Exif and CMYK
	 * jpegs. Throws CMMException for Exif, IIOException for CMYK.
	 */
	public static BufferedImage readFast(InputStream in) throws IOException {
		return ImageIO.read(ImageIO.createImageInputStream(in));
	}

//	public static BufferedImage readCustom(InputStream in) {
//		ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
//		reader.setInput(in);
//		reader.readRaster(0, null);
//	}
	
	public static BufferedImage convertCmykToRgb(BufferedImage image) {
		BufferedImage rgbImage =
			new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		ColorConvertOp op = new ColorConvertOp(null);
		op.filter(image, rgbImage);
		return rgbImage;
	}

	/**
	 * Writes image to a file in given format.
	 */
	public static void write(BufferedImage image, Format format, File file) throws IOException {
		write(image, format, BEST_QUALITY, file);
	}

	/**
	 * Writes image to a file in given format. Quality is only used for JPEG.
	 */
	public static void write(BufferedImage image, Format format, float quality, File file)
		throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			write(image, format, quality, out);
		}
	}

	/**
	 * Writes image to an output stream in given format.
	 */
	public static void write(BufferedImage image, Format format, OutputStream out)
		throws IOException {
		write(image, format, BEST_QUALITY, out);
	}

	/**
	 * Writes image to an output stream in given format. Quality is only used
	 * for JPEG.
	 */
	public static void write(BufferedImage image, Format format, float quality, OutputStream out)
		throws IOException {
		if (format == null) format = Format.JPEG;
		if (quality == 0.0f) quality = 1.0f;
		ImageWriter writer = ImageIO.getImageWritersByFormatName(format.name).next();
		ImageWriteParam param = writer.getDefaultWriteParam();
		if (format == Format.JPEG) {
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
		}
		param.setDestinationType(ImageTypeSpecifier
			.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB));
		IIOImage iioImage = new IIOImage(image, null, null);
		try (MemoryCacheImageOutputStream mOut = new MemoryCacheImageOutputStream(out)) {
			writer.setOutput(mOut);
			writer.write(null, iioImage, param);
			writer.dispose();
		}
	}

	/**
	 * Writes image to a byte array in given format.
	 */
	public static byte[] writeBytes(BufferedImage image, Format format) {
		return writeBytes(image, format, BEST_QUALITY);
	}

	/**
	 * Writes image to a byte array in given format. Quality is only used for
	 * JPEG.
	 */
	public static byte[] writeBytes(BufferedImage image, Format format, float quality) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			write(image, format, quality, out);
			out.flush();
			return out.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

}
