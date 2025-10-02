package ceri.common.svg;

import java.util.Arrays;
import java.util.Collection;
import ceri.common.stream.Streams;
import ceri.common.text.Format;
import ceri.common.text.Joiner;

/**
 * SVG support and flag types.
 */
public class Svg {
	private static final Format.OfDouble FORMAT = new Format.OfDouble(0, 8);

	private Svg() {}

	/**
	 * Sweep flag.
	 */
	public enum SweepFlag {
		negative(0),
		positive(1);

		public final int value;

		private SweepFlag(int value) {
			this.value = value;
		}

		public SweepFlag reverse() {
			return this == negative ? positive : negative;
		}
	}

	/**
	 * Large arc flag.
	 */
	public enum LargeArcFlag {
		small(0),
		large(1);

		public final int value;

		private LargeArcFlag(int value) {
			this.value = value;
		}

		public LargeArcFlag reverse() {
			return this == small ? large : small;
		}
	}

	public static String string(double n) {
		return FORMAT.apply(n + 0.0);
	}

	public static String string(Object obj) {
		if (obj == null) return "";
		return String.valueOf(obj);
	}

	public static String stringPc(Number n) {
		if (n == null) return "";
		return string(n.doubleValue()) + "%";
	}

	public static Position combinedEnd(Path<?>... paths) {
		return combinedEnd(Arrays.asList(paths));
	}

	public static Position combinedEnd(Collection<Path<?>> paths) {
		return Streams.from(paths).map(Path::end).reduce(Position::combine, Position.RELATIVE_ZERO);
	}

	public static String combinedPath(Path<?>... paths) {
		return combinedPath(Arrays.asList(paths));
	}

	public static String combinedPath(Collection<Path<?>> paths) {
		return Streams.from(paths).map(Path::d).collect(Joiner.SPACE);
	}
}
