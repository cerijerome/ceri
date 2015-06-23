package ceri.image.magick;

/**
 * Special specifier for ImageMagick resizing.
 */
public enum GeoSpecial {
	none(""),
	percent("%"),
	min("^"),
	stretch("!");

	public final String value;

	private GeoSpecial(String value) {
		this.value = value;
	}

}
