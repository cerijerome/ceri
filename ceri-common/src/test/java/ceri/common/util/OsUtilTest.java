package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class OsUtilTest {

	@Test
	public void testOsArch() {
		String arch = System.getProperty("os.arch");
		boolean isX86 = arch.startsWith("x86");
		boolean is64Bit = arch.endsWith("_64");
		assertThat(OsUtil.IS_X86, is(isX86));
		assertThat(OsUtil.IS_64BIT, is(is64Bit));
	}

	@Test
	public void testOsName() {
		String name = System.getProperty("os.name");
		boolean isMac = name.startsWith("Mac");
		assertThat(OsUtil.IS_MAC, is(isMac));
	}

}
