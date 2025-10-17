package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import java.util.regex.Pattern;
import ceri.common.svg.Position.Type;
import ceri.common.text.Regex;

public class SvgAssert {
	private static final Pattern FLOATING_POINT = Pattern.compile("([0-9]+\\.[0-9]{3})[0-9]+");
	
	private SvgAssert() {}

	public static void absolute(Position p, double x, double y) {
		approx(p, Type.absolute, x, y);
	}
	
	public static void relative(Position p, double x, double y) {
		approx(p, Type.relative, x, y);
	}
	
	public static void approx(Position p, Position.Type type, double x, double y) {
		assertEquals(p.type(), type, "type");
		assertApprox(p.x(), x, "x");
		assertApprox(p.y(), y, "y");
	}
	
	public static void d(Path<?> path, String expected) {
		d(path.d(), expected);
	}

	public static void d(String d, String expected) {
		assertString(narrow(d), expected);
	}

	private static String narrow(String d) {
		return Regex.appendAll(FLOATING_POINT, d, (b, m) -> b.append(m.group(1)));
	}
}
