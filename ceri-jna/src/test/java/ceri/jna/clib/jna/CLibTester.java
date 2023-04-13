package ceri.jna.clib.jna;

import ceri.jna.test.JnaTestUtil;

public class CLibTester {

	public static void main(String[] args) {
		JnaTestUtil.testAsLinux();
		System.out.printf("TIOCEXCL = 0x%x\n", CIoctl.TIOCEXCL);
		System.out.printf("TIOCMGET = 0x%x\n", CIoctl.TIOCMGET);
		System.out.printf("TIOCMSET = 0x%x\n", CIoctl.TIOCMSET);
	}

}
