package ceri.image.magick;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IMOps;
import org.im4java.core.Info;
import org.im4java.core.InfoException;
import org.im4java.process.Pipe;
import org.im4java.process.ProcessStarter;
import ceri.geo.GeoUtil;
import ceri.image.Format;
import ceri.image.Image;
import ceri.image.Interpolation;

/**
 * This class keeps track of images to be processed by ImageMagick. It queues up
 * operations and modifies image dimensions to match the operation. The
 * processing is executed only when write is called. Image can be written
 * multiple times. Class is immutable, any operation generates a new image.
 */
public class MagickImage implements Image {
	public static final String STDIO = "-";
	public static final String SEPARATOR = ":";

	static {
		ProcessStarter.setGlobalSearchPath("/opt/ImageMagick/bin");
	}

	public static final Image.Factory FACTORY = new Image.Factory() {
		@Override
		public Image create(byte[] data) throws IOException {
			return MagickImage.create(data);
		};
	};
	private final byte[] data;
	private final Dimension dimension;
	private final IMOps op;

	/**
	 * Create a new image holder. Adds operation to read image data from stdin.
	 */
	private MagickImage(Dimension dimension, byte[] data) {
		this.dimension = new Dimension(dimension);
		this.data = data;
		op = new IMOperation();
		op.addImage(STDIO);
	}

	/**
	 * Create an image holder based off the state of another holder.
	 */
	private MagickImage(Dimension dimension, byte[] data, IMOps op) {
		this.dimension = new Dimension(dimension);
		this.data = data;
		this.op = copy(op);
	}

	@Override
	public int getWidth() {
		return dimension.width;
	}

	@Override
	public int getHeight() {
		return dimension.height;
	}

	/**
	 * Resize to given dimensions without necessarily maintaining aspect ratio.
	 */
	@Override
	public Image resize(Dimension dimension, Interpolation interpolation) {
		if (interpolation == null) interpolation = Interpolation.BILINEAR;
		ceri.image.magick.Interpolation magickInterpolation = getMagickInterpolation(interpolation);
		IMOps op = copy(this.op);
		if (magickInterpolation != ceri.image.magick.Interpolation.none) op
			.interpolate(magickInterpolation.value);
		op.resize(dimension.width, dimension.height, GeoSpecial.stretch.value);
		return new MagickImage(dimension, data, op);
	}

	/**
	 * Crop image to given dimensions and offset. If the crop window extends
	 * past the image it will be adjusted to fit.
	 */
	@Override
	public Image crop(Rectangle rectangle) {
		if (rectangle.x == 0 && rectangle.y == 0 && rectangle.width == getWidth() &&
			rectangle.height == getHeight()) return this;
		rectangle = GeoUtil.overlap(rectangle, dimension);
		IMOps op = copy(this.op);
		op.crop(rectangle.width, rectangle.height, rectangle.x, rectangle.y);
		return new MagickImage(rectangle.getSize(), data, op);
	}

	/**
	 * Writes image to an output stream in given format. Quality is only used
	 * for JPEG.
	 */
	@Override
	public void write(Format format, float quality, OutputStream out) throws IOException {
		if (format == null) format = Format.JPEG;
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		Pipe inPipe = new Pipe(in, null);
		Pipe outPipe = new Pipe(null, out);
		IMOps op = copy(this.op);
		op.quality((double) quality * 100);
		op.addImage(format.suffix + SEPARATOR + STDIO);
		ConvertCmd cmd = new ConvertCmd();
		cmd.setInputProvider(inPipe);
		cmd.setOutputConsumer(outPipe);
		try {
			cmd.run(op);
		} catch (IM4JavaException | InterruptedException e) {
			throw new IOException("Failed to write image", e);
		}
	}

	/**
	 * Creates an image magick image holder from image data.
	 */
	public static Image create(byte[] data) throws IOException {
		Dimension dimension = readDimension(data);
		return new MagickImage(dimension, data);
	}

	private static Dimension readDimension(byte[] data) throws IOException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
			Info info = new Info(STDIO, in, true);
			return new Dimension(info.getImageWidth(), info.getImageHeight());
		} catch (InfoException e) {
			throw new IOException("Failed to get image info", e);
		}
	}

	private ceri.image.magick.Interpolation getMagickInterpolation(Interpolation interpolation) {
		switch (interpolation) {
		case BICUBIC:
			return ceri.image.magick.Interpolation.bicubic;
		case BILINEAR:
			return ceri.image.magick.Interpolation.bilinear;
		case NEAREST_NEIGHBOR:
			return ceri.image.magick.Interpolation.nearest_neighbor;
		default:
			return ceri.image.magick.Interpolation.none;
		}
	}

	private IMOps copy(IMOps op) {
		IMOperation copy = new IMOperation();
		copy.addRawArgs(op.getCmdArgs());
		return copy;
	}

}
