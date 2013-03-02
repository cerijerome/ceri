package ceri.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
	public static enum AlignX {
		Left,
		Center,
		Right
	};

	public static enum AlignY {
		Top,
		Center,
		Bottom
	};

	private ImageUtil() {}

	public static BufferedImage resize(BufferedImage image, int width, int height) {
		int type =
			image.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		while (true) {
			int w = image.getWidth();
			int h = image.getHeight();
			if (w == width && h == height) return image;
			// resize in 50% chunks if very large image
			//if (w > width * 2) w /= 2;
			//else
				w = width;
			//if (h > height * 2) h /= 2;
			//else
				h = height;
			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.drawImage(image, 0, 0, w, h, null);
			g2.dispose();
			image = tmp;
		}
	}

	public static BufferedImage crop(BufferedImage image, int width, int height) {
		return crop(image, width, height, AlignX.Center, AlignY.Center);
	}

	public static BufferedImage crop(BufferedImage image, int width, int height, AlignX alignX,
		AlignY alignY) {
		int x = 0;
		int y = 0;
		int w = width;
		int h = height;
		int imgW = image.getWidth();
		int imgH = image.getHeight();
		if (w >= imgW) w = imgW;
		else if (AlignX.Right == alignX) x = imgW - w;
		else if (AlignX.Center == alignX) x = (imgW - w) / 2;
		if (h >= imgH) h = imgH;
		else if (AlignY.Bottom == alignY) y = imgH - h;
		else if (AlignY.Center == alignY) y = (imgH - h) / 2;
		if (x == 0 && y == 0 && w == imgW && h == imgH) return image;
		// no change
		return image.getSubimage(x, y, w, h);
	}

	public static BufferedImage loadUrl(String url) throws IOException {
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

	public static void write(BufferedImage image, Format format, float quality, File file)
		throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			write(image, format, quality, out);
		}
	}
	
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

	public static byte[] writeBytes(BufferedImage image, Format format, float quality)
		throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			write(image, format, quality, out);
			out.flush();
			return out.toByteArray();
		}
	}

}
