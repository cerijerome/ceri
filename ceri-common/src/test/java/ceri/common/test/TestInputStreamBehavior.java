package ceri.common.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.threadCall;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.text.Utf8Util;

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
			assertRead(in, Utf8Util.encode("test"));
			in.markSupported.autoResponses(true);
			assertTrue(in.markSupported());
			in.mark(10);
			in.mark.assertAuto(10);
			assertRead(in, Utf8Util.encode("ing"));
			in.reset();
			in.reset.awaitAuto();
		}
	}

	@Test
	public void shouldGenerateReadError() throws IOException {
		try (var in = TestInputStream.from(0)) {
			in.read.error.setFrom(IOX);
			assertEquals(in.available(), 1);
			assertThrown(() -> in.read());
			assertEquals(in.available(), 0);
		}
	}

	@Test
	public void shouldGenerateAvailableError() throws IOException {
		try (var in = TestInputStream.of()) {
			assertEquals(in.available(), 0);
			in.available.error.setFrom(IOX);
			assertThrown(() -> in.available());
		}
	}

}
