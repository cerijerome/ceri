package ceri.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
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
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageUtil {
	private static final float BEST_QUALITY = 1.0f;

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
	 * Crop dimension to given dimensions. If dimension is smaller than
	 * the desired crop it will remain unchanged.
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
		int x = (int)(alignX.offsetMultiplier * (w - cropDimension.width));
		int y = (int)(alignY.offsetMultiplier * (h - cropDimension.height));
		return image.getSubimage(x, y, cropDimension.width, cropDimension.height);
	}

	public static BufferedImage read(byte[] imageBytes) {
		try (InputStream in = new ByteArrayInputStream(imageBytes)) {
			return ImageIO.read(in);
		} catch (IOException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

	public static BufferedImage readFromUrl(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		try (InputStream in = url.openStream()) {
			return ImageIO.read(in);
		}
	}
	
	public static BufferedImage read(File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			return ImageIO.read(in);
		}
	}

	public static BufferedImage read(InputStream in) throws IOException {
		return ImageIO.read(in);
		//		SeekableStream seekableStream =  new FileSeekableStream(new File("front.jpg"));
		//		ParameterBlock pb = new ParameterBlock();
		//		pb.add(seekableStream);
		//
		//		pb.add();
		//		BufferedImage image = JAI.create("jpeg", pb).getAsBufferedImage();
		//		
		//		
		//		ImageWriter writer = ImageIO.getImageWritersByFormatName(JPEG).next();
		//		writer = writer.getOriginatingProvider().createWriterInstance();
		//		ImageWriteParam param = writer.getDefaultWriteParam();
		//		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		//		param.setCompressionQuality(quality);
		//
		//		JAI.create(JPEG, image, writer, param);
		//		File file = new File(destPath);
		//		FileImageOutputStream output = new FileImageOutputStream(file);
		//		writer.setOutput(output);
		//
		//		FileInputStream inputStream = new FileInputStream(srcPath);
		//		BufferedImage originalImage = ImageIO.read(inputStream);
		//
		//		IIOImage image = new IIOImage(originalImage, null, null);
		//		writer.write(null, image, iwp);
		//		writer.dispose();
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
