package ceri.common.test;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;

public class TestProcessBehavior {

	@Test
	public void shouldInitialize() throws IOException {
		try (var p = TestProcess.of(null, null, 0, true)) {
			assertEquals(p.exitValue(), 0);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReset() throws IOException {
		try (var p = TestProcess.of("in", "err", 0, true)) {
			p.getOutputStream().write(ArrayUtil.bytes(1, 2, 3));
			assertEquals(p.getInputStream().available(), 2);
			assertEquals(p.getErrorStream().available(), 3);
			p.out.assertAvailable(3);
			p.resetState();
			assertEquals(p.getInputStream().available(), 0);
			assertEquals(p.getErrorStream().available(), 0);
			p.out.assertAvailable(0);
		}
	}

}
