package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class SvgUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(SvgUtil.class);
	}

	@Test
	public void testDoubleString() {
		assertEquals(SvgUtil.string(10.0), "10");
		assertEquals(SvgUtil.string(0.1234567891), "0.12345679");
	}

	@Test
	public void testString() {
		assertEquals(SvgUtil.string(null), "");
		assertEquals(SvgUtil.string("test"), "test");
	}

	@Test
	public void testStringPc() {
		assertEquals(SvgUtil.stringPc(null), "");
		assertEquals(SvgUtil.stringPc(1), "1%");
		assertEquals(SvgUtil.stringPc(0.00001), "0.00001%");
	}

}
