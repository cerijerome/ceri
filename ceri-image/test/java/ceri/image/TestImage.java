package ceri.image;

import java.io.File;

public enum TestImage {
	jpg_cmyk_500x333,
	// http://i.ebayimg.com/00/s/NjQwWDQ4MA==/$T2eC16NHJG!E9nm3o)QWBRL)(5qCkg~~48_20.JPG
	jpg_eps_450x600,
	// http://i.ebayimg.com/00/s/NDUzWDYwNA==/$T2eC16dHJGIE9nnWrdEFBRLvQU0Jsw~~48_20.JPG
	jpg_eps_604x453,
	// http://i.ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_20.JPG
	jpg_eps_800x456,
	jpg_exif_2850x2850,
	png_hd_rgba_1920x1080,
	png_rgb_8_300x300,
	png_rgb_16_600x600,
	png_trans_175x65;
	
	public String filename;
	
	private TestImage() {
		this.filename = name().substring(4) + "." + name().substring(0, 3);
	}
	
	public File file(String root) {
		return new File(root, filename);
	}
	
	
}
