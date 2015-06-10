package ceri.geo;

/**
 * Vertical alignment.
 */
public enum AlignY {
	Top(0),
	Top3rd(0.333d),
	Center(0.5d),
	Bottom3rd(0.667d),
	Bottom(1.0d);
	
	public final double offsetMultiplier;
	
	private AlignY(double offsetMultiplier) {
		this.offsetMultiplier = offsetMultiplier;
	}
	
}