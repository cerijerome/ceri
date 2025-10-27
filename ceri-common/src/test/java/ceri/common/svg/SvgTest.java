package ceri.common.svg;

import static ceri.common.test.TestUtil.exerciseEnum;
import org.junit.Test;
import ceri.common.test.Assert;

public class SvgTest {

	@Test
	public void testConstructorIsPrivate() {
		exerciseEnum(Svg.SweepFlag.class);
		exerciseEnum(Svg.LargeArcFlag.class);
		Assert.privateConstructor(Svg.class);
	}

	@Test
	public void testSweepReverse() {
		Assert.equal(Svg.SweepFlag.negative.reverse(), Svg.SweepFlag.positive);
		Assert.equal(Svg.SweepFlag.positive.reverse(), Svg.SweepFlag.negative);
	}

	@Test
	public void shouldLargeArcReverse() {
		Assert.equal(Svg.LargeArcFlag.large.reverse(), Svg.LargeArcFlag.small);
		Assert.equal(Svg.LargeArcFlag.small.reverse(), Svg.LargeArcFlag.large);
	}

	@Test
	public void testDoubleString() {
		Assert.string(Svg.string(10.0), "10");
		Assert.string(Svg.string(0.1234567891), "0.12345679");
	}

	@Test
	public void testString() {
		Assert.string(Svg.string(null), "");
		Assert.string(Svg.string("test"), "test");
	}

	@Test
	public void testStringPc() {
		Assert.string(Svg.stringPc(null), "");
		Assert.string(Svg.stringPc(1), "1%");
		Assert.string(Svg.stringPc(0.00001), "0.00001%");
	}
}
