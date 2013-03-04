package ceri.image;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.io.IoUtil;

public class ImageTestHelper implements Closeable {
	private static final String IMAGE_DIR_DEF = "doc/img";
	private final File imageDir;
	private final File outDir;

	public ImageTestHelper() {
		this(new File(IMAGE_DIR_DEF), null);
	}
	
	public ImageTestHelper(File imageDir, File outParent) {
		this.imageDir = imageDir;
		outDir = IoUtil.createTempDir(outParent);
	}

	public BufferedImage read(TestImage testImage) throws IOException {
		return ImageUtil.read(new File(imageDir, testImage.filename));
	}

	public BufferedImage readFast(TestImage testImage) throws IOException {
		try (InputStream in = new FileInputStream(new File(imageDir, testImage.filename))) {
			return ImageUtil.readFast(in);
		}
	}

	public File outFile(String filename) {
		return new File(outDir, filename);
	}
	
	public File write(byte[] imageBytes, String filename) throws IOException {
		File file = new File(outDir, filename);
		try (OutputStream out =
			new BufferedOutputStream(new FileOutputStream(file))) {
			out.write(imageBytes);
			out.flush();
		}
		return file;
	}

	@Override
	public void close() {
		IoUtil.deleteAll(outDir);
	}

}
