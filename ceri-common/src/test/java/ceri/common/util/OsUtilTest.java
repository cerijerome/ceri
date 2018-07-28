package ceri.common.util;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class OsUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(OsUtil.class);
	}

	@Test
	public void testOsArch() {
		String arch = System.getProperty("os.arch");
		boolean isX86 = arch.startsWith("x86");
		boolean is64Bit = arch.endsWith("64");
		assertThat(OsUtil.IS_X86, is(isX86));
		assertThat(OsUtil.IS_64BIT, is(is64Bit));
	}

	@Test
	public void testOsName() {
		String name = System.getProperty("os.name");
		boolean isMac = name.startsWith("Mac");
		assertThat(OsUtil.IS_MAC, is(isMac));
	}

	@Test
	public void testAws() {
		String name = System.getProperty("AWS_PATH");
		boolean isAws = name != null && !name.isEmpty();
		assertThat(OsUtil.IS_AWS, is(isAws));
	}

	@Test
	public void testPropertyIsSet() {
		OsUtil.propertyIsSet("\0\0\0\0");
		OsUtil.propertyIsSet("os.name");
	}

}
