package ceri.common.test;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestInputStream.BRK;
import static ceri.common.test.TestInputStream.EOF;
import static ceri.common.test.TestInputStream.EOFX;
import static ceri.common.test.TestInputStream.IOX;
import static ceri.common.test.TestInputStream.IOXS;
import static ceri.common.test.TestInputStream.RTX;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Test;

public class TestInputStreamBehavior {

	@Test
	public void shouldProvideBytesByDefault() throws IOException {
		try (var in = new TestInputStream()) {
			assertEquals(in.available(), 16);
			assertArray(in.readNBytes(3), 0, 0, 0);
			assertEquals(in.available(), 16);
		}
	}

	@Test
	public void shouldProvideBytes() throws IOException {
		try (var in = new TestInputStream()) {
			in.data(bytes(1, 2, 3, 4, 5));
			assertArray(in.readNBytes(3), 1, 2, 3);
			assertArray(in.readNBytes(3), 4, 5);
		}
	}

	@Test
	public void shouldProvideStringBytes() throws IOException {
		try (var in = new TestInputStream()) {
			in.actions("abcde");
			assertArray(in.readNBytes(3), 'a', 'b', 'c');
			assertArray(in.readNBytes(3), 'd', 'e');
		}
	}

	@Test
	public void shouldDelegateToInputStream() throws IOException {
		try (var in = new TestInputStream()) {
			in.in(new ByteArrayInputStream(bytes(1, 2, 3)));
			assertEquals(in.available(), 3);
			assertArray(in.readAllBytes(), 1, 2, 3);
		}
	}

	@Test
	public void shouldProvideCloseCallback() throws IOException {
		var c = Captor.ofInt();
		try (var in = new TestInputStream()) {
			in.close(() -> c.accept(1));
			in.close();
		}
		c.verifyInt(1, 1);
	}

	@Test
	public void shouldMarkAndReset() throws IOException {
		try (var in = new TestInputStream()) {
			in.actions(1, 2, IOX, 3);
			in.mark(0);
			assertArray(in.readNBytes(3), 1, 2, 3);
			in.reset();
			assertArray(in.readNBytes(3), 1, 2, 3);
		}
	}

	@Test
	public void shouldBreakUpAvailableBytes() throws IOException {
		try (var in = new TestInputStream()) {
			in.actions(1, 2, BRK, 3, BRK, BRK, 4, 5, -2);
			assertEquals(in.available(), 2);
			assertArray(in.readNBytes(2), 1, 2);
			assertEquals(in.available(), 1);
			assertEquals(in.read(), 3);
			assertEquals(in.available(), 0);
			assertEquals(in.read(), 4);
			assertEquals(in.available(), 1);
			assertEquals(in.read(), 5);
			assertEquals(in.available(), 0);
			assertEquals(in.read(), -1);
			assertEquals(in.available(), 0);
		}
	}

	@Test
	public void shouldCountBytes() throws IOException {
		try (var in = new TestInputStream()) {
			in.actions(1, 2, BRK, 3, BRK, BRK, 4, 5, BRK);
			assertArray(in.readAllBytes(), 1, 2, 3, 4, 5);
			assertEquals(in.count(), 9);
		}
	}

	@Test
	public void shouldFailForInvalidActions() throws IOException {
		try (var in = new TestInputStream()) {
			assertAssertion(() -> in.actions(0, EOF, BRK, -3));
		}
		try (var in = new TestInputStream()) {
			in.read(i -> -3);
			assertAssertion(() -> in.read());
		}
		try (var in = new TestInputStream()) {
			in.available(i -> -3);
			assertAssertion(() -> in.available());
		}
	}

	@Test
	public void shouldStopOnEof() throws IOException {
		try (var in = new TestInputStream()) {
			in.actions(1, 2, 3, EOF, 4, 5);
			assertArray(in.readAllBytes(), 1, 2, 3);
			assertArray(in.readAllBytes());
			assertEquals(in.read(), -1);
		}
	}

	@Test
	public void shouldThrowEofException() throws IOException {
		try (var in = new TestInputStream()) {
			in.actions(1, 2, EOFX, 3, 4, EOFX, 5);
			assertEquals(in.read(), 1);
			assertEquals(in.read(), 2);
			assertThrown(() -> in.read());
			assertEquals(in.read(), 3);
			assertArray(in.readAllBytes(), 4, 5);
			assertEquals(in.read(), -1);
		}
	}

	@Test
	public void shouldThrowIoException() throws IOException {
		try (var in = new TestInputStream()) {
			in.actions(1, 2, IOX, 3, 4, IOX, 5);
			assertEquals(in.read(), 1);
			assertEquals(in.read(), 2);
			assertThrown(() -> in.read());
			assertEquals(in.read(), 3);
			assertArray(in.readAllBytes(), 4, 5);
			assertEquals(in.read(), -1);
		}
	}

	@Test
	public void shouldThrowRuntimeException() throws IOException {
		try (var in = new TestInputStream()) {
			in.actions(1, 2, RTX, 3, 4, RTX, 5);
			assertEquals(in.read(), 1);
			assertEquals(in.read(), 2);
			assertThrown(() -> in.read());
			assertEquals(in.read(), 3);
			assertThrown(() -> in.readAllBytes()); // different to IOException
			assertEquals(in.read(), 5); // different to IOException
		}
	}

	@Test
	public void shouldSupportActionsInText() throws IOException {
		try (var in = new TestInputStream()) {
			in.actions("\ud83c\udc39" + IOXS + "e");
			assertArray(in.readNBytes(4), 0xf0, 0x9f, 0x80, 0xb9);
			assertThrown(() -> in.read());
			assertArray(in.readAllBytes(), 'e');
			assertEquals(in.read(), -1);
		}
	}

}
