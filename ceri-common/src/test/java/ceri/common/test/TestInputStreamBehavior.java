package ceri.common.test;

import java.io.IOException;
import org.junit.Test;
import ceri.common.text.Utf8;

public class TestInputStreamBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldFeedBytes() throws IOException {
		try (var in = TestInputStream.of()) {
			in.to.writeBytes(1, 2, 3);
			Assert.equal(in.available(), 3);
			Assert.read(in, 1, 2, 3);
			Assert.equal(in.available(), 0);
		}
	}

	@Test
	public void shouldWaitForFeedToBeEmpty() throws IOException {
		try (var in = TestInputStream.from(1, 2, 3)) {
			try (var exec = TestUtil.threadCall(() -> in.readNBytes(3))) {
				in.awaitFeed();
				Assert.array(exec.get(), 1, 2, 3);
			}
		}
	}

	@Test
	public void shouldProvideForEof() throws IOException {
		try (var in = TestInputStream.from(1, 2, 3)) {
			Assert.equal(in.read(), 1);
			in.eof(true);
			Assert.equal(in.read(), -1); // returns -1 but still reads byte
			in.eof(false);
			Assert.equal(in.read(), 3);
		}
	}

	@Test
	public void shouldMarkAndReset() throws IOException {
		try (var in = TestInputStream.from("testing")) {
			Assert.read(in, Utf8.encode("test"));
			in.markSupported.autoResponses(true);
			Assert.yes(in.markSupported());
			in.mark(10);
			in.mark.assertAuto(10);
			Assert.read(in, Utf8.encode("ing"));
			in.reset();
			in.reset.awaitAuto();
		}
	}

	@Test
	public void shouldGenerateReadError() throws IOException {
		try (var in = TestInputStream.from(0)) {
			in.read.error.setFrom(ErrorGen.IOX);
			Assert.equal(in.available(), 1);
			Assert.thrown(() -> in.read());
			Assert.equal(in.available(), 0);
		}
	}

	@Test
	public void shouldGenerateAvailableError() throws IOException {
		try (var in = TestInputStream.of()) {
			Assert.equal(in.available(), 0);
			in.available.error.setFrom(ErrorGen.IOX);
			Assert.thrown(() -> in.available());
		}
	}
}
