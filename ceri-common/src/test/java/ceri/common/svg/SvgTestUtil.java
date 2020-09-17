package ceri.common.svg;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.regex.Pattern;

public class SvgTestUtil {
	private static final Pattern FLOATING_POINT = Pattern.compile("([0-9]+\\.[0-9]{3})[0-9]+");

	private SvgTestUtil() {}

	public static void assertPath(Path<?> path, String expected) {
		assertPath(path.path(), expected);
	}

	public static void assertPath(String path, String expected) {
		String simplePath = simplePath(path);
		assertThat(simplePath, is(expected));
	}

	public static String simplePath(String path) {
		return FLOATING_POINT.matcher(path).replaceAll("$1");
	}

}
