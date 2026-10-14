package ceri.jna.clib.jna;

import org.junit.jupiter.api.Test;
import ceri.jna.test.JnaTesting;

public class CMmanTest {

	@Test
	public void testFields() throws Exception {
		JnaTesting.testForEachOs(CMman.class);
	}
}
