package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class CStdlibTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CStdlib.class);
	}

}
