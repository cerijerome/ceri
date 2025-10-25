package ceri.common.test;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertRead;
import static ceri.common.test.Assert.assertTrue;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.threadCall;
import java.io.IOException;
import org.junit.Test;
import ceri.common.text.Utf8;

public class TestInputStreamBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldFeedBytes() throws IOException {
		try (var in = TestInputStream.of()) {
			in.to.writeBytes(1, 2, 3);
			assertEquals(in.available(), 3);
			assertRead(in, 1, 2, 3);
			assertEquals(in.available(), 0);
		}
	}

	@Test
	public void shouldWaitForFeedToBeEmpty() throws IOException {
		try (var in = TestInputStream.from(1, 2, 3)) {
			try (var exec = threadCall(() -> in.readNBytes(3))) {
				in.awaitFeed();
				assertArray(exec.get(), 1, 2, 3);
			}
		}
	}

	@Test
	public void shouldProvideForEof() throws IOException {
		try (var in = TestInputStream.from(1, 2, 3)) {
			assertEquals(in.read(), 1);
			in.eof(true);
			assertEquals(in.read(), -1); // returns -1 but still reads byte
			in.eof(false);
			assertEquals(in.read(), 3);
		}
	}

	@Test
	public void shouldMarkAndReset() throws IOException {
		try (var in = TestInputStream.from("testing")) {
			assertRead(in, Utf8.encode("test"));
			in.markSupported.autoResponses(true);
			assertTrue(in.markSupported());
			in.mark(10);
			in.mark.assertAuto(10);
			assertRead(in, Utf8.encode("ing"));
			in.reset();
			in.reset.awaitAuto();
		}
	}

	@Test
	public void shouldGenerateReadError() throws IOException {
		try (var in = TestInputStream.from(0)) {
			in.read.error.setFrom(IOX);
			assertEquals(in.available(), 1);
			Assert.thrown(() -> in.read());
			assertEquals(in.available(), 0);
		}
	}

	@Test
	public void shouldGenerateAvailableError() throws IOException {
		try (var in = TestInputStream.of()) {
			assertEquals(in.available(), 0);
			in.available.error.setFrom(IOX);
			Assert.thrown(() -> in.available());
		}
	}

}
