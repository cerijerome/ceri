package ceri.image.magick;

/**
 * ImageMagick interpolation values.
 */
public enum Interpolation {
	none,
	integer,
	nearest_neighbor,
	average,
	bilinear,
	mesh,
	bicubic,
	spline,
	filter;
	
	public final String value;
	
	private Interpolation() {
		value = name().replaceAll("_", "-");
	}
	
}
