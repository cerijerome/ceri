package ceri.common.test;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import ceri.common.text.Utf8Util;

public class TestInputStreamBehavior {

	@Test
	public void shouldThrowExceptions() throws IOException {
		try (InputStream in = TestInputStream.of(1, 2, 3, -2, -2, -3, 0, -1)) {
			assertArray(in.readNBytes(2), 1, 2);
			assertThat(in.read(new byte[2]), is(1));
			assertThrown(IOException.class, () -> in.read());
			assertThrown(RuntimeException.class, () -> in.read());
			assertArray(in.readNBytes(5), 0);
		}
	}

	@Test
	public void shouldStopOnEof() throws IOException {
		try (InputStream in = TestInputStream.of(1, 2, 3)) {
			assertArray(in.readNBytes(5), 1, 2, 3);
			assertArray(in.readNBytes(5));
			assertArray(in.readNBytes(1));
		}
		try (InputStream in = TestInputStream.of(1, 2, 3, -1, 0, 0)) {
			assertArray(in.readNBytes(5), 1, 2, 3);
			assertArray(in.readNBytes(5));
			assertArray(in.readNBytes(1));
		}
	}

	@Test
	public void shouldCreateFromByteArray() throws IOException {
		byte[] b = Utf8Util.encode("abc");
		try (InputStream in = TestInputStream.of(b)) {
			assertThat(Utf8Util.decode(in.readAllBytes()), is("abc"));
		}
	}
	
	@Test
	public void shouldProvideAvailableByteCount() throws IOException {
		try (InputStream in = TestInputStream.of(1, 2, 3, -1, 0, 0)) {
			assertThat(in.available(), is(6));
			assertArray(in.readNBytes(6), 1, 2, 3);
			assertThat(in.available(), is(0));
		}
	}

	@Test
	public void shouldProvideDefaultBehavior() throws IOException {
		try (InputStream in = TestInputStream.builder().build()) {
			assertThat(in.available(), is(16));
			assertArray(in.readNBytes(5), 0, 0, 0, 0, 0);
			assertThat(in.available(), is(16));
			assertArray(in.readNBytes(5), 0, 0, 0, 0, 0);
		}
	}

	@Test
	public void shouldBuildBehaviorWithFunctions() throws IOException {
		AtomicInteger i = new AtomicInteger();
		Capturer<Boolean> closer = Capturer.of();
		try (InputStream in = TestInputStream.builder() //
			.read(i::getAndIncrement) //
			.available(() -> Math.max(0, 16 - i.get())) //
			.close(() -> closer.accept(true)) //
			.build()) {
			assertThat(in.available(), is(16));
			assertArray(in.readNBytes(5), 0, 1, 2, 3, 4);
			assertThat(in.available(), is(11));
			assertArray(in.readNBytes(5), 5, 6, 7, 8, 9);
		}
		closer.verify(true);
	}

}
