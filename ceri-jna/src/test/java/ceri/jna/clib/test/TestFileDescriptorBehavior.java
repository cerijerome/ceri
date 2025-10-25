package ceri.jna.clib.test;

import static ceri.common.test.Assert.assertEquals;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.test.Assert;

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
	public void shouldAcceptFdConsumer() throws IOException {
		fd.accept(f -> assertEquals(f, 33));
	}

	@Test
	public void shouldApplyFdFunction() throws IOException {
		assertEquals(fd.apply(f -> {
			assertEquals(f, 33);
			return 77;
		}), 77);
	}

	@Test
	public void shouldFailAllForBadFd() {
		fd.fd.error.setFrom(IOException::new);
		Assert.thrown(() -> fd.fd());
		Assert.thrown(() -> fd.accept(_ -> {}));
		Assert.thrown(() -> fd.apply(_ -> 0));
	}
}
