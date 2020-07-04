package ceri.common.test;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.TestInputStream.BRK;
import static ceri.common.test.TestInputStream.EOF;
import static ceri.common.test.TestInputStream.EOFX;
import static ceri.common.test.TestInputStream.IOX;
import static ceri.common.test.TestInputStream.RTX;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertAssertion;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;

public class TestInputStreamBehavior {

	@Test
	public void shouldProvideBytesByDefault() throws IOException {
		try (var in = TestInputStream.builder().build()) {
			assertThat(in.available(), is(16));
			assertArray(in.readNBytes(3), 0, 0, 0);
			assertThat(in.available(), is(16));
		}
	}

	@Test
	public void shouldProvideBytes() throws IOException {
		try (var in = TestInputStream.of(bytes(1, 2, 3, 4, 5))) {
			assertArray(in.readNBytes(3), 1, 2, 3);
			assertArray(in.readNBytes(3), 4, 5);
		}
	}

	@Test
	public void shouldProvideStringBytes() throws IOException {
		try (var in = TestInputStream.of("abcde")) {
			assertArray(in.readNBytes(3), 'a', 'b', 'c');
			assertArray(in.readNBytes(3), 'd', 'e');
		}
	}

	@Test
	public void shouldProvideCloseCallback() throws IOException {
		var c = Capturer.ofInt();
		try (var in = TestInputStream.builder().close(() -> c.accept(1)).build()) {
			in.close();
		}
		c.verifyInt(1, 1);
	}

	@Test
	public void shouldReset() throws IOException {
		try (var in = TestInputStream.of(1, 2, IOX, 3)) {
			assertArray(in.readNBytes(3), 1, 2, 3);
			in.reset();
			assertArray(in.readNBytes(3), 1, 2, 3);
		}
	}

	@Test
	public void shouldBreakUpAvailableBytes() throws IOException {
		try (var in = TestInputStream.of(1, 2, BRK, 3, BRK, BRK, 4, 5, -2)) {
			assertThat(in.available(), is(2));
			assertArray(in.readNBytes(2), 1, 2);
			assertThat(in.available(), is(1));
			assertThat(in.read(), is(3));
			assertThat(in.available(), is(0));
			assertThat(in.read(), is(4));
			assertThat(in.available(), is(1));
			assertThat(in.read(), is(5));
			assertThat(in.available(), is(0));
			assertThat(in.read(), is(-1));
			assertThat(in.available(), is(0));
		}
	}

	@Test
	public void shouldCountBytes() throws IOException {
		try (var in = TestInputStream.of(1, 2, BRK, 3, BRK, BRK, 4, 5, BRK)) {
			assertArray(in.readAllBytes(), 1, 2, 3, 4, 5);
			assertThat(in.count(), is(9));
		}
	}

	@Test
	public void shouldFailForInvalidActions() throws IOException {
		assertAssertion(() -> TestInputStream.of(0, EOF, BRK, -3));
		try (var in = TestInputStream.builder().read(i -> -3).build()) {
			assertAssertion(() -> in.read());
		}
		try (var in = TestInputStream.builder().available(i -> -3).build()) {
			assertAssertion(() -> in.available());
		}
	}

	@Test
	public void shouldStopOnEof() throws IOException {
		try (var in = TestInputStream.of(1, 2, 3, EOF, 4, 5)) {
			assertArray(in.readAllBytes(), 1, 2, 3);
			assertArray(in.readAllBytes());
			assertThat(in.read(), is(-1));
		}
	}

	@Test
	public void shouldThrowEofException() throws IOException {
		try (var in = TestInputStream.of(1, 2, EOFX, 3, 4, EOFX, 5)) {
			assertThat(in.read(), is(1));
			assertThat(in.read(), is(2));
			assertThrown(() -> in.read());
			assertThat(in.read(), is(3));
			assertArray(in.readAllBytes(), 4, 5);
			assertThat(in.read(), is(-1));
		}
	}

	@Test
	public void shouldThrowIoException() throws IOException {
		try (var in = TestInputStream.of(1, 2, IOX, 3, 4, IOX, 5)) {
			assertThat(in.read(), is(1));
			assertThat(in.read(), is(2));
			assertThrown(() -> in.read());
			assertThat(in.read(), is(3));
			assertArray(in.readAllBytes(), 4, 5);
			assertThat(in.read(), is(-1));
		}
	}

	@Test
	public void shouldThrowRuntimeException() throws IOException {
		try (var in = TestInputStream.of(1, 2, RTX, 3, 4, RTX, 5)) {
			assertThat(in.read(), is(1));
			assertThat(in.read(), is(2));
			assertThrown(() -> in.read());
			assertThat(in.read(), is(3));
			assertThrown(() -> in.readAllBytes()); // different to IOException
			assertThat(in.read(), is(5)); // different to IOException
		}
	}

}
