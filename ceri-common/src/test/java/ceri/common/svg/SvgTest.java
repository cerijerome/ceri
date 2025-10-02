package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.exerciseEnum;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.text.Regex;

public class SvgTest {
	private static final Pattern FLOATING_POINT = Pattern.compile("([0-9]+\\.[0-9]{3})[0-9]+");

	public static void assertD(Path<?> path, String expected) {
		assertD(path.d(), expected);
	}

	public static void assertD(String d, String expected) {
		var simplePath = narrow(d);
		assertEquals(simplePath, expected);
	}

	public static String narrow(String d) {
		return Regex.appendAll(FLOATING_POINT, d, (b, m) -> b.append(m.group(1)));
	}

	@Test
	public void testConstructorIsPrivate() {
		exerciseEnum(Svg.SweepFlag.class);
		exerciseEnum(Svg.LargeArcFlag.class);
		assertPrivateConstructor(Svg.class);
	}

	@Test
	public void testSweepReverse() {
		assertEquals(Svg.SweepFlag.negative.reverse(), Svg.SweepFlag.positive);
		assertEquals(Svg.SweepFlag.positive.reverse(), Svg.SweepFlag.negative);
	}

	@Test
	public void shouldLargeArcReverse() {
		assertEquals(Svg.LargeArcFlag.large.reverse(), Svg.LargeArcFlag.small);
		assertEquals(Svg.LargeArcFlag.small.reverse(), Svg.LargeArcFlag.large);
	}

	@Test
	public void testDoubleString() {
		assertEquals(Svg.string(10.0), "10");
		assertEquals(Svg.string(0.1234567891), "0.12345679");
	}

	@Test
	public void testString() {
		assertEquals(Svg.string(null), "");
		assertEquals(Svg.string("test"), "test");
	}

	@Test
	public void testStringPc() {
		assertEquals(Svg.stringPc(null), "");
		assertEquals(Svg.stringPc(1), "1%");
		assertEquals(Svg.stringPc(0.00001), "0.00001%");
	}
}
