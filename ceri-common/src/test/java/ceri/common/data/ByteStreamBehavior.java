package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.inputStream;
import static ceri.common.test.TestUtil.outputStream;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.data.ByteStream.Reader;
import ceri.common.data.ByteStream.Writer;

@SuppressWarnings("resource")
public class ByteStreamBehavior {

	/* Reader tests */

	@Test
	public void shouldReadByte() {
		Reader r = ByteStream.reader(inputStream(1, -3, 2, -2, 3));
		assertThat(r.readByte(), is((byte) 1));
		assertThrown(() -> r.readByte());
		assertThat(r.readByte(), is((byte) 2));
		assertThrown(() -> r.readByte());
		assertThat(r.readByte(), is((byte) 3));
		assertThrown(() -> r.readByte());
	}

	@Test
	public void shouldSkipReaderBytes() {
		Reader r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		assertThat(r.skip(3).readByte(), is((byte) 4));
		assertThrown(() -> r.skip(2));
		assertThrown(() -> r.skip(1));
	}

	@Test
	public void shouldReadBytes() {
		Reader r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		assertArray(r.readBytes(3), 1, 2, 3);
		assertThrown(() -> r.readBytes(3));
	}

	@Test
	public void shouldReadIntoByteArray() {
		Reader r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		byte[] bytes = new byte[5];
		assertThat(r.readInto(bytes, 1, 3), is(4));
		assertArray(bytes, 0, 1, 2, 3, 0);
		assertThrown(() -> r.readInto(bytes, 0, 3));
	}

	@Test
	public void shouldReadIntoByteReceiver() {
		Reader r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		Mutable m = Mutable.of(5);
		assertThat(r.readInto(m, 1, 3), is(4));
		assertArray(m.copy(0), 0, 1, 2, 3, 0);
		assertThrown(() -> r.readInto(m, 0, 3));
	}

	@Test
	public void shouldTransferToOutputStream() throws IOException {
		Reader r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		var out = outputStream(new int[6]);
		assertThat(r.transferTo(out, 3), is(3));
		assertThrown(() -> r.transferTo(out, 3));
		assertArray(out.written(), 1, 2, 3);
	}

	/* Writer tests */

	@Test
	public void shouldWriteByte() {
		var out = outputStream(0, -3, -2, -1); // 1 byte, then exceptions
		Writer w = ByteStream.writer(out);
		w.writeByte(1);
		assertThrown(() -> w.writeByte(2));
		assertThrown(() -> w.writeByte(3));
		assertThrown(() -> w.writeByte(4));
		assertArray(out.written(), 1);
	}

	@Test
	public void shouldFillBytes() {
		var out = outputStream(0, 0, 0); // EOF after 3 writes
		Writer w = ByteStream.writer(out);
		w.fill(2, 1);
		assertThrown(() -> w.fill(2, 2));
		assertArray(out.written(), 1, 1, 2);
	}

	@Test
	public void shouldWriteFromByteArray() {
		var out = outputStream(0, 0, 0, 0); // EOF after 4 writes
		byte[] bytes = ArrayUtil.bytes(1, 2, 3, 4, 5);
		Writer w = ByteStream.writer(out);
		w.writeFrom(bytes, 1, 3);
		assertThrown(() -> w.writeFrom(bytes, 0, 2));
		assertArray(out.written(), 2, 3, 4, 1);
	}

	@Test
	public void shouldWriteFromByteProvider() {
		var out = outputStream(0, 0, 0, 0); // EOF after 4 writes
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		Writer w = ByteStream.writer(out);
		w.writeFrom(m, 1, 3);
		assertThrown(() -> w.writeFrom(m, 0, 2));
		assertArray(out.written(), 2, 3, 4, 1);
	}

	@Test
	public void shouldTransferFromInputStream() throws IOException {
		var in = inputStream(1, 2, 3, 4, 5);
		var out = outputStream(new int[6]);
		Writer w = ByteStream.writer(out);
		assertThat(w.transferFrom(in, 3), is(3));
		assertThat(w.transferFrom(in, 3), is(2));
		assertThat(w.transferFrom(in, 3), is(0));
		assertArray(out.written(), 1, 2, 3, 4, 5);
	}

}