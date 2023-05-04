package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.jna.test.JnaTestUtil;

public class CLibTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CLib.class);
	}

	@Test
	public void testValidateOs() {
		JnaTestUtil.testForEachOs(CLib::validateOs);
		JnaTestUtil.testAsOs("Other", () -> assertThrown(CLib::validateOs));
	}
}
