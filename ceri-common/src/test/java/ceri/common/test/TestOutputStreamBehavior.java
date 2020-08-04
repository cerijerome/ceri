package ceri.common.test;

import static ceri.common.test.TestOutputStream.EOFX;
import static ceri.common.test.TestOutputStream.IOX;
import static ceri.common.test.TestOutputStream.RTX;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertAssertion;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteUtil;

public class TestOutputStreamBehavior {

	@Test
	public void shouldCollectWrittenBytes() throws IOException {
		try (var out = new TestOutputStream()) {
			out.limit(5);
			out.write(ArrayUtil.bytes(1, 2, 3));
			assertThrown(() -> out.write(ArrayUtil.bytes(4, 5, 6)));
			assertThrown(() -> out.write(ArrayUtil.bytes(7)));
			assertArray(out.written(), 1, 2, 3, 4, 5);
		}
	}

	@Test
	public void shouldDisableCollector() throws IOException {
		try (var out = new TestOutputStream()) {
			out.collect(false);
			out.write(ArrayUtil.bytes(1, 2, 3));
			assertArray(out.written());
			out.resetState();
			assertArray(out.written());
		}
	}

	@Test
	public void shouldRegisterByteConsumer() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (var out = new TestOutputStream()) {
			out.write((i, b) -> bout.write(b));
			out.write(ArrayUtil.bytes(1, 2, 3));
		}
		assertArray(bout.toByteArray(), 1, 2, 3);
	}

	@Test
	public void shouldDelegateToOutputStream() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (var out = new TestOutputStream()) {
			out.out(bout);
			out.write(ByteUtil.toAscii("abc").copy(0));
			out.flush();
			assertThat(out.writtenString(), is("abc"));
		}
		assertArray(bout.toByteArray(), 'a', 'b', 'c');
	}

	@Test
	public void shouldSetFlushBehavior() throws IOException {
		Capturer.Int captor = Capturer.ofInt();
		try (var out = new TestOutputStream()) {
			out.flush();
			out.flush(i -> captor.accept(i));
			out.flush();
			out.write(ArrayUtil.bytes(1, 2, 3));
			out.flush();
			captor.verifyInt(0, 3);
			out.flushAction(i -> IOX);
			assertThrown(out::flush);
		}
	}

	@Test
	public void shouldSetCloseBehavior() throws IOException {
		Capturer.Int captor = Capturer.ofInt();
		try (var out = new TestOutputStream()) {
			out.close(() -> captor.accept(-1));
			out.close();
		}
		captor.verifyInt(-1, -1);
	}

	@Test
	public void shouldFailForInvalidActions() throws IOException {
		try (var out = new TestOutputStream()) {
			assertAssertion(() -> out.actions(0, EOFX, IOX, RTX, -3));
		}
		try (var out = new TestOutputStream()) {
			out.writeAction((i, b) -> -3);
			assertAssertion(() -> out.write(0));
		}
	}

	@Test
	public void shouldGenerateExceptions() throws IOException {
		try (var out = new TestOutputStream()) {
			out.actions(0, RTX, 0, IOX, 0, EOFX, 0);
			out.write(1);
			assertThrown(() -> out.write(2));
			out.write(3);
			assertThrown(() -> out.write(4));
			out.write(5);
			assertThrown(() -> out.write(6));
			out.write(7);
			assertThrown(() -> out.write(8));
			assertArray(out.written(), 1, 3, 5, 7);
		}
	}

	@Test
	public void shouldReset() throws IOException {
		try (var out = new TestOutputStream()) {
			out.write(ArrayUtil.bytes(1, 2, 3));
			assertArray(out.written(), 1, 2, 3);
			out.resetState();
			assertArray(out.written());
			out.write(ArrayUtil.bytes(4, 5));
			assertArray(out.written(), 4, 5);
		}
	}

}
