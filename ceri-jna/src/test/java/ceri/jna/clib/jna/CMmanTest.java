package ceri.jna.clib.jna;

import org.junit.Test;
import ceri.jna.test.JnaTesting;

public class CMmanTest {

	@Test
	public void testFields() throws Exception {
		JnaTesting.testForEachOs(CMman.class);
	}
}
