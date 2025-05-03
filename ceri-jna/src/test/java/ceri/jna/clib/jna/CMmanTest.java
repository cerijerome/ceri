package ceri.jna.clib.jna;

import org.junit.Test;
import ceri.jna.test.JnaTestUtil;

public class CMmanTest {

	@Test
	public void testFields() throws Exception {
		JnaTestUtil.testForEachOs(CMman.class);
	}
}
