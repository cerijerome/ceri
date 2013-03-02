package ceri.image;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
	 * Resize to fit at least given dimensions while maintaining aspect ratio.
	 */
	public static BufferedImage resizeToMin(BufferedImage image, int width, int height,
		Interpolation interpolation) {
		int w = image.getWidth();
		int h = image.getHeight();
		if ((long)w * height <= (long)width * h) {
			h = width * h / w;
			w = width;
		} else {
			w = w * height / h;
			h = height;
		}
		return resize(image, w, h, interpolation);
	}
	
	/**
	 * Resize to fit within given dimensions while maintaining aspect ratio.
	 */
	public static BufferedImage resizeToMax(BufferedImage image, int width, int height,
		Interpolation interpolation) {
		int w = image.getWidth();
		int h = image.getHeight();
		if ((long)w * height >= (long)width * h) {
			h = width * h / w;
			w = width;
		} else {
			w = w * height / h;
			h = height;
		}
		return resize(image, w, h, interpolation);
	}
	
	/**
	 * Resize to given dimensions without maintaining aspect ratio.
	 * Downsizing is done in 50% chunks if the image is large enough.
	 * Upsizing is done in one step.
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
	 * Crop image to given dimensions, with center crop window alignment.
	 * If the image is smaller than the desired crop in one dimension that dimension is
	 * not cropped.
	 */
	public static BufferedImage crop(BufferedImage image, int width, int height) {
		return crop(image, width, height, AlignX.Center, AlignY.Center);
	}

	/**
	 * Crop image to given dimensions, with specified crop window alignment.
	 * If the image is smaller than the desired crop in one dimension that dimension is
	 * not cropped.
	 */
	public static BufferedImage crop(BufferedImage image, int width, int height, AlignX alignX,
		AlignY alignY) {
		int w = image.getWidth();
		int h = image.getHeight();
		int x = 0;
		int y = 0;
		if (width >= w) width = w;
		else if (AlignX.Right == alignX) x = w - width;
		else if (AlignX.Center == alignX) x = (w - width) / 2;
		if (height >= h) height = h;
		else if (AlignY.Bottom == alignY) y = h - height;
		else if (AlignY.Center == alignY) y = (h - height) / 2;
		// no change required, just return the image
		if (x == 0 && y == 0 && width == w && height == h) return image;
		return image.getSubimage(x, y, width, height);
	}

	public static BufferedImage loadFromFile(File file) throws IOException {
		return ImageIO.read(file);
	}
	
	public static BufferedImage loadFromUrl(String url) throws IOException {
		return ImageIO.read(new URL(url));
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
	public static byte[] writeBytes(BufferedImage image, Format format)
		throws IOException {
		return writeBytes(image, format, BEST_QUALITY);
	}

	/**
	 * Writes image to a byte array in given format. Quality is only used for
	 * JPEG.
	 */
	public static byte[] writeBytes(BufferedImage image, Format format, float quality)
		throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			write(image, format, quality, out);
			out.flush();
			return out.toByteArray();
		}
	}

}
