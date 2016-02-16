package ceri.common.svg;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class SvgUtil {

	private SvgUtil() {}

	public static Position combinedEnd(Path<?>... paths) {
		return combinedEnd(Arrays.asList(paths));
	}

	public static Position combinedEnd(Collection<Path<?>> paths) {
		return paths.stream().map(path -> path.end()).reduce(Position.ZERO,
			(result, end) -> result.combine(end));
	}

	public static String combinedPath(Path<?>... paths) {
		return combinedPath(Arrays.asList(paths));
	}

	public static String combinedPath(Collection<Path<?>> paths) {
		return paths.stream().map(p -> p.path()).collect(Collectors.joining(" "));
	}

}
