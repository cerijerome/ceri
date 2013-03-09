package ceri.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.jai.JAI;
import ceri.geo.GeoUtil;
import com.sun.media.jai.codec.SeekableStream;

/**
 * Image holder for Java2d BufferedImage.
 * Java2d/JAI is limited in the image types it can process, so MagickImage is preferred.
 */
public class Java2dImage implements Image {
	public static final Image.Factory JAI_FACTORY = new Image.Factory() {
		@Override
		public Image create(byte[] data) throws IOException {
			return createWithJai(data);
		};
	};
	public static final Image.Factory IMAGE_IO_FACTORY = new Image.Factory() {
		@Override
		public Image create(byte[] data) throws IOException {
			return createWithImageIo(data);
		};
	};
	private static final String JAI_STREAM = "stream";
	private final BufferedImage image;

	public Java2dImage(BufferedImage image) {
		this.image = image;
	}

	@Override
	public int getWidth() {
		return image.getWidth();
	}

	@Override
	public int getHeight() {
		return image.getHeight();
	}

	/**
	 * Resize to given dimensions without maintaining aspect ratio. Downsizing
	 * is done in 50% chunks if the image is large enough. Upsizing is done in
	 * one step.
	 */
	@Override
	public Image resize(Dimension dimension, Interpolation interpolation) {
		BufferedImage image = this.image;
		if (interpolation == null) interpolation = Interpolation.NONE;
		int type =
			image.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		while (true) {
			int w = image.getWidth();
			int h = image.getHeight();
			if (w == dimension.width && h == dimension.height) {
				if (image == this.image) return this;
				return new Java2dImage(image);
			}
			// resize in 50% chunks if very large image
			if (w > dimension.width * 2) w /= 2;
			else w = dimension.width;
			if (h > dimension.height * 2) h /= 2;
			else h = dimension.height;
			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			interpolation.setRenderingHint(g2);
			g2.drawImage(image, 0, 0, w, h, null);
			g2.dispose();
			image = tmp;
		}
	}

	/**
	 * Crop image to given dimensions and offset. If the crop window extends past the image
	 * it will be adjusted to fit. 
	 */
	@Override
	public Image crop(Rectangle rectangle) {
		if (rectangle.x == 0 && rectangle.y == 0 && rectangle.width == image.getWidth() &&
			rectangle.height == image.getHeight()) return this;
		rectangle = GeoUtil.overlap(rectangle, new Dimension(image.getWidth(), image.getHeight()));
		return new Java2dImage(image.getSubimage(rectangle.x, rectangle.y, rectangle.width,
			rectangle.height));
	}

	/**
	 * Writes image to an output stream in given format. Quality is only used
	 * for JPEG.
	 */
	@Override
	public void write(Format format, float quality, OutputStream out) throws IOException {
		if (format == null) format = Format.JPEG;
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
	 * Much faster performance for reads, but doesn't handle Exif and CMYK
	 * jpegs. Throws CMMException for Exif, IIOException for CMYK.
	 */
	public static Java2dImage createWithImageIo(byte[] data) throws IOException {
		try (InputStream in = new ByteArrayInputStream(data)) {
			BufferedImage image = ImageIO.read(ImageIO.createImageInputStream(in));
			return new Java2dImage(image);
		}
	}

	/**
	 * Reads many image types but doesn't handle cmyk well.
	 */
	public static Java2dImage createWithJai(byte[] data) throws IOException {
		try (InputStream in = new ByteArrayInputStream(data)) {
			BufferedImage image =
				JAI.create(JAI_STREAM, SeekableStream.wrapInputStream(in, true))
					.getAsBufferedImage();
			return new Java2dImage(image);
		} catch (RuntimeException e) {
			throw new IOException("Failed to read image from stream", e);
		}
	}

}
