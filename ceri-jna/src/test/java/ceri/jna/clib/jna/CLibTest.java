package ceri.jna.clib.jna;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.util.OsUtil;
import ceri.jna.util.JnaOs;

public class CLibTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CLib.class);
	}

	@Test
	public void testValidateOs() {
		JnaOs.forEach(_ -> CLib.validateOs());
		try (var _ = OsUtil.os("Other", null, null)) {
			Assert.thrown(CLib::validateOs);
		}
	}
}
