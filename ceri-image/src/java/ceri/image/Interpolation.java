package ceri.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

public enum Interpolation {
	NONE(null),
	BILINEAR(RenderingHints.VALUE_INTERPOLATION_BILINEAR),
	BICUBIC(RenderingHints.VALUE_INTERPOLATION_BICUBIC),
	NEAREST_NEIGHBOR(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

	public final Object value;
	
	private Interpolation(Object value) {
		this.value = value;
	}

	public Graphics2D setRenderingHint(Graphics2D g) {
		if (this == NONE) return g;
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, value);
		return g;
	}

}
