package ceri.common.color;

import java.util.List;
import ceri.common.function.Functions;
import ceri.common.math.Maths;

/**
 * Algorithms to blend a foreground xargb onto a background opaque xrgb.
 */
public enum Blend {
	alpha(Blend::alphaBlend), // standard composite using alpha channel
	sum(Blend::addBlend), // apply alpha, and sum component values up to max
	max(Blend::maxBlend), // apply alpha, and use the max value of each component
	diff(Blend::diffBlend); // apply alpha, and use the absolute difference of each component

	public static final Blend DEFAULT = alpha;
	private static final List<Component> BLEND_COMPONENTS = List.of(Component.b, Component.g,
		Component.r, Component.x0, Component.x1, Component.x2, Component.x3);
	private final Functions.LongBiOperator mergeFn;

	private static interface ComponentBlender {
		int blend(int a, int fg, int bg);
	}

	private Blend(ComponentBlender blender) {
		this((fg, bg) -> blend(fg, bg, blender));
	}

	private Blend(Functions.LongBiOperator mergeFn) {
		this.mergeFn = mergeFn;
	}

	/**
	 * Merges foreground xargb onto opaque background xrgb. Returns an opaque xargb value.
	 */
	public long blend(long xargb, long xrgb) {
		return xargb == 0L ? xrgb : mergeFn.applyAsLong(xargb, xrgb);
	}

	private static int alphaBlend(int a, int fg, int bg) {
		return Maths.roundDiv(a * (fg - bg), Colors.MAX_VALUE) + bg;
	}

	private static int addBlend(int a, int fg, int bg) {
		return Math.min(Maths.roundDiv(a * fg, Colors.MAX_VALUE) + bg, Colors.MAX_VALUE);
	}

	private static int maxBlend(int a, int fg, int bg) {
		return Math.max(Maths.roundDiv(a * fg, Colors.MAX_VALUE), bg);
	}

	private static int diffBlend(int a, int fg, int bg) {
		return Math.abs(Maths.roundDiv(a * fg, Colors.MAX_VALUE) - bg);
	}

	private static long blend(long fg, long bg, ComponentBlender blender) {
		int a = Component.a.get(fg);
		if (a == 0) return bg;
		long blend = Component.a.mask;
		for (var component : BLEND_COMPONENTS)
			blend = component.set(blend, blender.blend(a, component.get(fg), component.get(bg)));
		return blend;
	}
}
