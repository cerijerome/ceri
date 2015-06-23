package ceri.image.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

public enum TestImage {
	tif_cymk_512x343,
	jpg_small_10x10,
	jpg_eps_450x600(
		"http://i.ebayimg.com/00/s/NjQwWDQ4MA==/$T2eC16NHJG!E9nm3o)QWBRL)(5qCkg~~48_20.JPG"),
		jpg_eps_604x453(
			"http://i.ebayimg.com/00/s/NDUzWDYwNA==/$T2eC16dHJGIE9nnWrdEFBRLvQU0Jsw~~48_20.JPG"),
			jpg_eps_800x456(
				"http://i.ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_20.JPG"),
				jpg_eps_cmyk_500x333(
					"http://i.ebayimg.com/00/s/MzMzWDUwMA==/$(KGrHqN,!lUFEF4J6rB9BRM-dcBtp!~~48_3.JPG"),
					jpg_eps_exif_800x800(
						"http://i.ebayimg.com/00/s/MTAwMFgxMDAw/$(KGrHqZ,!pQFD8e95s+hBRM-d4,CSg~~48_3.JPG"),
						gif_multi_100x100,
						png_rgb_8_300x300,
						png_rgb_16_600x600;

	public static final EnumSet<TestImage> epsImages = EnumSet.of(jpg_eps_450x600, jpg_eps_604x453,
		jpg_eps_800x456, jpg_eps_cmyk_500x333, jpg_eps_exif_800x800);
	private static final int BUFFER_SIZE = 32 * 1024;
	public final String filename;
	public final String source;

	private TestImage() {
		this(null);
	}

	private TestImage(String source) {
		this.filename = name().substring(4) + "." + name().substring(0, 3);
		this.source = source;
	}

	public InputStream getInputStream() {
		return getClass().getResourceAsStream(filename);
	}

	public byte[] read() throws IOException {
		try (InputStream in = getInputStream()) {
			ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
			byte[] buffer = new byte[BUFFER_SIZE];
			int count;
			while ((count = in.read(buffer)) != -1) {
				out.write(buffer, 0, count);
			}
			return out.toByteArray();
		}
	}

}
