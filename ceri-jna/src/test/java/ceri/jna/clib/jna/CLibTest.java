package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.util.OsUtil;
import ceri.jna.util.JnaOs;

public class CLibTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CLib.class);
	}

	@Test
	public void testValidateOs() {
		JnaOs.forEach(_ -> CLib.validateOs());
		try (var _ = OsUtil.os("Other", null, null)) {
			assertThrown(CLib::validateOs);
		}
	}
}
