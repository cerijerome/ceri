package ceri.jna.clib.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFileDescriptorBehavior {
	private TestFileDescriptor fd;

	@Before
	public void before() {
		fd = TestFileDescriptor.of(33);
	}

	@After
	public void after() {
		fd.close();
	}

	@Test
	public void shouldAcceptFdConsumer() {
		fd.accept(f -> assertEquals(f, 33));
	}

	@Test
	public void shouldApplyFdFunction() {
		assertEquals(fd.apply(f -> {
			assertEquals(f, 33);
			return 77;
		}), 77);
	}

	@Test
	public void shouldFailAllForBadFd() {
		fd.fd.error.setFrom(IOException::new);
		assertThrown(() -> fd.fd());
		assertThrown(() -> fd.accept(f -> {}));
		assertThrown(() -> fd.apply(f -> 0));
	}

}
