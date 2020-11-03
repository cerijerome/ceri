package ceri.common.process;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class NullProcessBehavior {

	@SuppressWarnings("resource")
	@Test
	public void testNullProcess() throws InterruptedException, IOException {
		Process p = NullProcess.of();
		p.destroy();
		assertEquals(p.exitValue(), 0);
		assertEquals(p.waitFor(), 0);
		assertTrue(p.waitFor(1, TimeUnit.MILLISECONDS));
		assertArray(p.getInputStream().readAllBytes());
		assertArray(p.getErrorStream().readAllBytes());
		p.getOutputStream().write(new byte[16]);
	}

}
