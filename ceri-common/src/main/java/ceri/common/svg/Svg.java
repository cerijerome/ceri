package ceri.common.svg;

import java.util.Arrays;
import java.util.Collection;
import ceri.common.stream.Streams;
import ceri.common.text.Format;
import ceri.common.text.Joiner;
import ceri.common.text.Strings;

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

		/**
		 * Reverse of the flag.
		 */
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

		/**
		 * Reverse of the flag.
		 */
		public LargeArcFlag reverse() {
			return this == small ? large : small;
		}
	}

	/**
	 * Formats a double to its simplest string.
	 */
	public static String string(double n) {
		return FORMAT.apply(n + 0.0);
	}

	/**
	 * Formats an object to string; empty if null.
	 */
	public static String string(Object obj) {
		return Strings.safe(obj);
	}

	/**
	 * Formats a number to a percent.
	 */
	public static String stringPc(Number n) {
		return n == null ? "" : string(n.doubleValue()) + "%";
	}

	/**
	 * Combines paths to get an end position.
	 */
	public static Position combinedEnd(Path<?>... paths) {
		return combinedEnd(Arrays.asList(paths));
	}

	/**
	 * Combines paths to get an end position.
	 */
	public static Position combinedEnd(Collection<Path<?>> paths) {
		return Streams.from(paths).map(Path::end).reduce(Position::combine, Position.RELATIVE_ZERO);
	}

	/**
	 * Combines paths to a string.
	 */
	public static String combinedPath(Path<?>... paths) {
		return combinedPath(Arrays.asList(paths));
	}

	/**
	 * Combines paths to a string.
	 */
	public static String combinedPath(Collection<Path<?>> paths) {
		return Streams.from(paths).map(Path::d).collect(Joiner.SPACE);
	}
}
