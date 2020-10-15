package ceri.common.svg;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class SvgUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(SvgUtil.class);
	}

	@Test
	public void testDoubleString() {
		assertThat(SvgUtil.string(10.0), is("10"));
		assertThat(SvgUtil.string(0.1234567891), is("0.12345679"));
	}

	@Test
	public void testString() {
		assertThat(SvgUtil.string(null), is(""));
		assertThat(SvgUtil.string("test"), is("test"));
	}

	@Test
	public void testStringPc() {
		assertThat(SvgUtil.stringPc(null), is(""));
		assertThat(SvgUtil.stringPc(1), is("1%"));
		assertThat(SvgUtil.stringPc(0.00001), is("0.00001%"));
	}

}
