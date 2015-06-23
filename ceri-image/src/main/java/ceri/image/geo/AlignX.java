package ceri.image.geo;

/**
 * Horizontal alignment.
 */
public enum AlignX {
	Left(0.0d),
	Center(0.5d),
	Right(1.0d);

	public final double offsetMultiplier;

	private AlignX(double offsetMultiplier) {
		this.offsetMultiplier = offsetMultiplier;
	}

}