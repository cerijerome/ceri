package ceri.jna.clib.jna;

import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;

public class CUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(CUtil.class);
	}

	@Test
	public void testRequireContiguous() throws IOException {
		CUtil.requireContiguous(new CPoll.pollfd[0]);
		CUtil.requireContiguous(CPoll.pollfd.array(0));
		CUtil.requireContiguous(CPoll.pollfd.array(2));
		assertThrown(CException.class, () -> CUtil
			.requireContiguous(new CPoll.pollfd[] { new CPoll.pollfd(), new CPoll.pollfd() }));
	}

}
