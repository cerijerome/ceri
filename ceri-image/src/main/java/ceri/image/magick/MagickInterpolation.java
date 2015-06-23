package ceri.image.magick;

/**
 * ImageMagick interpolation values.
 */
public enum MagickInterpolation {
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

	private MagickInterpolation() {
		value = name().replaceAll("_", "-");
	}

}
