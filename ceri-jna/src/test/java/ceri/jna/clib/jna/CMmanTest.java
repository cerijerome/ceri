package ceri.jna.clib.jna;

import static ceri.jna.test.JnaTestUtil.LINUX_OS;
import static ceri.jna.test.JnaTestUtil.MAC_OS;
import org.junit.Test;
import ceri.jna.test.JnaTestUtil;

public class CMmanTest {

	@Test
	public void testFields() throws Exception {
		JnaTestUtil.testAsOs(MAC_OS, CMman.class);
		JnaTestUtil.testAsOs(LINUX_OS, CMman.class);
	}

}
