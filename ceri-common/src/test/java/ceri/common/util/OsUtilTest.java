package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.test.CallSync;

public class OsUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(OsUtil.class);
	}

	@Test
	public void testMac() {
		assertEquals(OsUtil.mac("mac", "other"), OsUtil.IS_MAC ? "mac" : "other");
	}

	@Test
	public void testLinux() {
		assertEquals(OsUtil.linux("linux", "other"), OsUtil.IS_LINUX ? "linux" : "other");
	}

	@Test
	public void testMacInt() {
		assertEquals(OsUtil.macInt(0, 1), OsUtil.IS_MAC ? 0 : 1);
	}

	@Test
	public void testLinuxInt() {
		assertEquals(OsUtil.linuxInt(0, 1), OsUtil.IS_LINUX ? 0 : 1);
	}

	@Test
	public void testOsArch() {
		String arch = SystemVars.sys("os.arch");
		boolean isX86 = arch.startsWith("x86");
		boolean is64Bit = arch.endsWith("64");
		assertEquals(OsUtil.IS_X86, isX86);
		assertEquals(OsUtil.IS_64BIT, is64Bit);
	}

	@Test
	public void testOsName() {
		String name = SystemVars.sys("os.name");
		boolean isMac = name.startsWith("Mac");
		assertEquals(OsUtil.IS_MAC, isMac);
	}

	@Test
	public void testAws() {
		String name = SystemVars.sys("AWS_PATH");
		boolean isAws = name != null && !name.isEmpty();
		assertEquals(OsUtil.IS_AWS, isAws);
	}

	@Test
	public void testPropertyIsSet() {
		OsUtil.propertyIsSet("\0\0\0\0");
		OsUtil.propertyIsSet("os.name");
	}

	@Test
	public void testUnsupportedOs() {
		assertThrown(() -> { 
			throw OsUtil.unsupportedOs();
		});
	}
	
}
