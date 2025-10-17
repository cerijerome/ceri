package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.TestUtil.exerciseEnum;
import org.junit.Test;

public class SvgTest {

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
		assertString(Svg.string(10.0), "10");
		assertString(Svg.string(0.1234567891), "0.12345679");
	}

	@Test
	public void testString() {
		assertString(Svg.string(null), "");
		assertString(Svg.string("test"), "test");
	}

	@Test
	public void testStringPc() {
		assertString(Svg.stringPc(null), "");
		assertString(Svg.stringPc(1), "1%");
		assertString(Svg.stringPc(0.00001), "0.00001%");
	}
}
