package ceri.serial.clib;

import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.serial.clib.jna.CException;

public class CFileDescriptorBehavior {

	@Test
	public void testIsBroken() {
		assertFalse(CFileDescriptor.isBroken(null));
		assertFalse(CFileDescriptor.isBroken(CException.of(1, "test")));
		assertTrue(CFileDescriptor.isBroken(CException.of(2, "test")));
		assertTrue(CFileDescriptor.isBroken(CException.of(121, "test")));
		assertTrue(CFileDescriptor.isBroken(CException.of(1, "remote i/o")));
	}

}
