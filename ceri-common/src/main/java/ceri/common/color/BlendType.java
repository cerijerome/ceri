package ceri.common.color;

import static ceri.common.color.Colors.MAX_VALUE;
import static ceri.common.color.Component.b;
import static ceri.common.color.Component.g;
import static ceri.common.color.Component.r;
import static ceri.common.color.Component.x0;
import static ceri.common.color.Component.x1;
import static ceri.common.color.Component.x2;
import static ceri.common.color.Component.x3;
import static ceri.common.math.MathUtil.roundDiv;
import java.util.List;
import java.util.function.LongBinaryOperator;

/**
 * Algorithms to blend a foreground xargb onto a background opaque xrgb.
 */
public enum BlendType {
	alpha(BlendType::alphaBlend), // standard composite using alpha channel
	sum(BlendType::addBlend), // apply alpha, and sum component values up to max
	max(BlendType::maxBlend), // apply alpha, and use the max value of each component
	diff(BlendType::diffBlend); // apply alpha, and use the absolute difference of each component

	public static final BlendType DEFAULT = alpha;
	private static final List<Component> BLEND_COMPONENTS = List.of(b, g, r, x0, x1, x2, x3);
	private final LongBinaryOperator mergeFn;

	private static interface ComponentBlender {
		int blend(int a, int fg, int bg);
	}

	private BlendType(ComponentBlender blender) {
		this((fg, bg) -> blend(fg, bg, blender));
	}

	private BlendType(LongBinaryOperator mergeFn) {
		this.mergeFn = mergeFn;
	}

	/**
	 * Merges foreground xargb onto opaque background xrgb. Returns an opaque xargb value.
	 */
	public long blend(long xargb, long xrgb) {
		return xargb == 0L ? xrgb : mergeFn.applyAsLong(xargb, xrgb);
	}

	private static int alphaBlend(int a, int fg, int bg) {
		return roundDiv(a * (fg - bg), MAX_VALUE) + bg;
	}

	private static int addBlend(int a, int fg, int bg) {
		return Math.min(roundDiv(a * fg, MAX_VALUE) + bg, MAX_VALUE);
	}

	private static int maxBlend(int a, int fg, int bg) {
		return Math.max(roundDiv(a * fg, MAX_VALUE), bg);
	}

	private static int diffBlend(int a, int fg, int bg) {
		return Math.abs(roundDiv(a * fg, MAX_VALUE) - bg);
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
