package ceri.image;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageTestHelper {
	private static final String ROOT_DEF = "doc/img";
	private final String inDir;
	private final String outDir;

	public ImageTestHelper() {
		this(ROOT_DEF, ROOT_DEF);
	}
	
	public ImageTestHelper(String inDir, String outDir) {
		this.inDir = inDir;
		this.outDir = outDir;
	}
	
	public File inFile(String filename) {
		return new File(inDir, filename);
	}
	
	public File inFile(TestImage testImage) {
		return testImage.file(inDir);
	}
	
	public File outFile(String filename) {
		return new File(outDir, filename);
	}
	
	public BufferedImage read(TestImage testImage) throws IOException {
		return ImageUtil.read(inFile(testImage));
	}
	
	public void write(byte[] imageBytes, String filename) throws IOException {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile(filename)))) {
			out.write(imageBytes);
			out.flush();
		}
	}
	
}
