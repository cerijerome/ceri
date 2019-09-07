package ceri.common.svg;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class SvgUtil {
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.########");

	private SvgUtil() {}

	public static String string(double n) {
		return NUMBER_FORMAT.format(n + 0.0);
	}

	public static String string(Object obj) {
		if (obj == null) return "";
		return String.valueOf(obj);
	}

	public static String stringPc(Number n) {
		if (n == null) return "";
		return NUMBER_FORMAT.format(n) + "%";
	}

	public static Position combinedEnd(Path<?>... paths) {
		return combinedEnd(Arrays.asList(paths));
	}

	public static Position combinedEnd(Collection<Path<?>> paths) {
		return paths.stream().map(Path::end).reduce(Position.RELATIVE_ZERO, Position::combine);
	}

	public static String combinedPath(Path<?>... paths) {
		return combinedPath(Arrays.asList(paths));
	}

	public static String combinedPath(Collection<Path<?>> paths) {
		return paths.stream().map(Path::path).collect(Collectors.joining(" "));
	}

}
