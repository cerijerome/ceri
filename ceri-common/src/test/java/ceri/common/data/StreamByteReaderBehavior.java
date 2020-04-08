package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertByte;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray.Mutable;

public class StreamByteReaderBehavior {

	@Test
	public void shouldReadByte() {
		var reader = reader(0, -1, 2);
		assertByte(reader.readByte(), 0);
		assertByte(reader.readByte(), -1);
		assertByte(reader.readByte(), 2);
		assertThrown(() -> reader.readByte());
	}

	@Test
	public void shouldSkipBytes() {
		assertRemaining(reader(1, 2, 3).skip(0), 1, 2, 3);
		assertRemaining(reader(1, 2, 3, 4, 5).skip(3), 4, 5);
		assertThrown(() -> reader(1, 2, 3).skip(4));
	}

	@Test
	public void shouldReadBytes() {
		assertArray(reader(1, 2, 3).readBytes(0));
		assertArray(reader(1, 2, 3).readBytes(3), 1, 2, 3);
		assertThrown(() -> reader(1, 2, 3).readBytes(4));
	}

	@Test
	public void shouldReadIntoByteArray() {
		byte[] bytes = new byte[3];
		assertThat(reader(0, -1, 2, -3, 4).readInto(bytes), is(3));
		assertArray(bytes, 0, -1, 2);
		assertThrown(() -> reader(0, -1, 2, -3, 4).readInto(bytes, 1, 3));
		assertThrown(() -> reader(0, -1).readInto(bytes));
	}

	@Test
	public void shouldReadIntoByteReceiver() {
		byte[] bytes = new byte[3];
		assertThat(reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(bytes)), is(3));
		assertArray(bytes, 0, -1, 2);
		assertThrown(() -> reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(bytes), 1, 3));
		assertThrown(() -> reader(0, -1).readInto(Mutable.wrap(bytes)));
	}

	@Test
	public void shouldTransferToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertThat(reader(0, -1, 2, -3, 4).transferTo(out, 3), is(3));
		assertArray(out.toByteArray(), 0, -1, 2);
		out.reset();
		assertThat(ByteReader.transferBufferTo(reader(0, -1, 2), out, 3), is(3));
		assertArray(out.toByteArray(), 0, -1, 2);
	}

	@Test
	public void shouldProvideAccessToInputStream() {
		ByteArrayInputStream in = new ByteArrayInputStream(ArrayUtil.bytes(0, -1, 2));
		var reader = StreamByteReader.of(in);
		assertRemaining(reader, 0, -1, 2);
		reader.in().reset();
		assertRemaining(reader, 0, -1, 2);
	}

	@Test
	public void shouldCountAvailableStreamBytes() {
		var reader = reader(0, -1, 2, -3, 4);
		assertThat(reader.available(), is(5));
		reader.skip(2);
		assertThat(reader.available(), is(3));
	}

	@Test
	public void shouldSupportMarkAndReset() {
		var reader = reader(0, -1, 2, -3, 4);
		assertThat(reader.markSupported(), is(true));
		assertThat(reader.skip(2).mark().skip(2).available(), is(1));
		assertRemaining(reader.reset(), 2, -3, 4);
	}

	private static void assertRemaining(StreamByteReader<?> reader, int... bytes) {
		for (int b : bytes)
			assertByte(reader.readByte(), b);
		assertThrown(() -> reader.readByte());
	}

	private static StreamByteReader<?> reader(int... bytes) {
		return reader(ArrayUtil.bytes(bytes));
	}

	private static StreamByteReader<?> reader(byte[] bytes) {
		return reader(bytes, 0, bytes.length);
	}

	private static StreamByteReader<?> reader(byte[] bytes, int offset, int length) {
		return StreamByteReader.of(new ByteArrayInputStream(bytes, offset, length));
	}

}
