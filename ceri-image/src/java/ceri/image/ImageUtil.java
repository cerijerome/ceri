package ceri.image;

import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.jai.JAI;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

public class ImageUtil {
	private static final String[] IMG_URLS = {
		// 604 x 453
		//"http://i.ebayimg.com/00/s/NDUzWDYwNA==/$T2eC16dHJGIE9nnWrdEFBRLvQU0Jsw~~48_20.JPG",
		"/Users/cjerome/_LM-SJC-00712379/git/ceri/ceri-image/img0.jpg",
		// 800 x 456
		//"http://i.ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_20.JPG",
		"/Users/cjerome/_LM-SJC-00712379/git/ceri/ceri-image/img1.jpg",
		// 450 x 600
		//"http://i.ebayimg.com/00/s/NjQwWDQ4MA==/$T2eC16NHJG!E9nm3o)QWBRL)(5qCkg~~48_20.JPG",
		"/Users/cjerome/_LM-SJC-00712379/git/ceri/ceri-image/img2.jpg",
	};

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

	private static final String JPEG = "jpeg";

	private ImageUtil() {}

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

		if (x == 0 && y == 0 && w == imgW && h == imgH) return image; // no change
		return image.getSubimage(x, y, w, h);
	}

	public static void load() {
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
	
	public static void write(BufferedImage image, String format, float quality, File file)
		throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			write(image, format, quality, out);
		}
	}
	
	public static void write(BufferedImage image, String format, float quality,
		OutputStream out) throws IOException {
		ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(quality);
		IIOImage iioImage = new IIOImage(image, null, null);
		try (MemoryCacheImageOutputStream mOut = new MemoryCacheImageOutputStream(out)) {
			writer.setOutput(mOut);
			writer.write(null, iioImage, param);
			writer.dispose();
		}
	}

	public static byte[] writeBytes(BufferedImage image, String format, float quality)
		throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			write(image, format, quality, out);
			out.flush();
			return out.toByteArray();
		}
	}
	
	public static void main(String[] args) throws Exception {
		BufferedImage image;
		String filename;
		filename = "doc/front.jpg";
		System.out.println("Reading " + filename);
		image = ImageIO.read(new File(filename));
		
		if (false == false) return;
		int i = 0;
		for (String url : IMG_URLS) {
			System.out.println("Reading " + url);
			image = ImageIO.read(new File(url));
			BufferedImage image0 = crop(image, 500, 500);
			
			filename = "img" + i + "_0.jpg";
			System.out.println("Writing " + filename);
			write(image0, JPEG, 1.0f, new File(filename));
			
			filename = "img" + i + "_1.jpg";
			System.out.println("Writing " + filename);
			write(image0, JPEG, 0.2f, new File(filename));
			i++;
		}
	}

}
