package ceri.jna.clib.test;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;

public class TestFileDescriptorBehavior {
	private TestFileDescriptor fd;

	@BeforeEach
	public void before() {
		fd = TestFileDescriptor.of(33);
	}

	@AfterEach
	public void after() {
		fd.close();
	}

	@Test
	public void shouldAcceptFdConsumer() throws IOException {
		fd.accept(f -> Assert.equal(f, 33));
	}

	@Test
	public void shouldApplyFdFunction() throws IOException {
		Assert.equal(fd.apply(f -> {
			Assert.equal(f, 33);
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
