package ceri.ffm.clib.ffm;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.util.Os;
import ceri.ffm.util.FfmOs;

public class CLibTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(CLib.class);
	}

	@Test
	public void testValidateOs() {
		FfmOs.mac.accept(_ -> CLib.validateOs());
		FfmOs.linux.accept(_ -> CLib.validateOs());
		try (var _ = Os.info("test", null, null)) {
			Assert.unsupportedOp(CLib::validateOs);
		}
	}
}
